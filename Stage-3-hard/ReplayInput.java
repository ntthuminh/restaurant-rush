import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ReplayInput implements InputSource {
    private final List<String> lines;
    private final String settingsLine;
    private int next;

    public ReplayInput(Path file) throws IOException {
        lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        if (!lines.isEmpty() && lines.get(0).startsWith("#mode")) {
            settingsLine = lines.get(0);
            next = 1;
        } else {
            settingsLine = null;
        }
    }

    public String getSettingsLine() {
        return settingsLine;
    }

    @Override
    public String readLine() {
        if (next >= lines.size()) {
            return null;
        }
        String line = lines.get(next++);
        System.out.println(line);
        return line;
    }
}
