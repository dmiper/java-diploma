import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LinksSuggester {

    private final List<Suggest> suggestList;

    public LinksSuggester(File file) throws IOException, WrongLinksFormatException {

        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = reader.readLine();
        StringBuilder stringBuilder = new StringBuilder();

        while (line != null) {
            stringBuilder.append(line).append("\n");
            line = reader.readLine();
        }

        suggestList = suggest(stringBuilder.toString());

    }

    public List<Suggest> getSuggestList() {
        return suggestList;
    }

    public List<Suggest> suggest(String text) {

        String[] lines = text.split("\n");
        List<Suggest> suggestList = new ArrayList<>();

        Arrays.stream(lines).map(line -> line.split("\t")).forEach(objects -> {
            if (objects.length != 3) {
                throw new WrongLinksFormatException("Неверное количество аргументов");
            }
            suggestList.add(new Suggest(objects[0], objects[1], objects[2]));
        });

        return suggestList;
    }

}
