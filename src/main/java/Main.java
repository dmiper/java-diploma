import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.action.PdfAction;
import com.itextpdf.kernel.pdf.annot.PdfLinkAnnotation;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Link;
import com.itextpdf.layout.element.Paragraph;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;

public class Main {

    public static void main(String[] args) throws Exception {

        // создаём конфиг
        LinksSuggester linksSuggester = new LinksSuggester(new File("data/config"));
        var dir = new File("data/pdfs/");
        List<Suggest> suggests = linksSuggester.getSuggestList();

        // перебираем пдфки в data/pdfs
        for (var fileIn : requireNonNull(dir.listFiles())) {

            // для каждой пдфки создаём новую в data/converted
            var fileOut = new File("data/converted/" + fileIn.getName());
            var doc = new PdfDocument(new PdfReader(fileIn), new PdfWriter(fileOut));

            // перебираем страницы pdf
            ArrayList<String> pages = IntStream
                    .rangeClosed(1, doc.getNumberOfPages())
                    .mapToObj(i -> PdfTextExtractor.getTextFromPage(doc.getPage(i)))
                    .collect(Collectors.toCollection(ArrayList::new));

            List<String> keyWord = new ArrayList<>();
            var i = 1;
            for (String page : pages) {

                List<String> url = new ArrayList<>();
                List<String> title = new ArrayList<>();

                suggests.stream()
                        .filter(suggest -> page.toLowerCase().contains(suggest.getKeyWord().toLowerCase()) & !keyWord.contains(suggest.getKeyWord()))
                        .forEach(suggest -> {
                            url.add(suggest.getUrl());
                            title.add(suggest.getTitle());
                            keyWord.add(suggest.getKeyWord());
                        });

                // если в странице есть неиспользованные ключевые слова, создаём новую страницу за ней
                if (url.size() > 0) {
                    var newPage = doc.addNewPage(pages.indexOf(page) + i + 1);
                    i++;
                    var rect = new Rectangle(newPage.getPageSize()).moveRight(10).moveDown(10);
                    Canvas canvas = new Canvas(newPage, rect);
                    Paragraph paragraph = new Paragraph("Suggestions:\n");
                    paragraph.setFontSize(25);

                    // вставляем туда рекомендуемые ссылки из конфига
                    IntStream.range(0, url.size()).forEach(j -> {
                        PdfLinkAnnotation annotation = new PdfLinkAnnotation(rect);
                        PdfAction action = PdfAction.createURI(url.get(j));
                        annotation.setAction(action);
                        Link link = new Link(title.get(j), annotation);
                        paragraph.add(link.setUnderline());
                        paragraph.add("\n");
                    });

                    canvas.add(paragraph);
                    canvas.close();
                }
            }

            doc.close();

        }

        System.out.println("Файлы с конвертировались");

    }

}
