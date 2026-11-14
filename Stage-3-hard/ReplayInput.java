import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ReplayInput implements InputSource {
    private final List<String> lines;
    private int next;

    public ReplayInput(Path file) throws IOException {
        lines = Files.readAllLines(file, StandardCharsets.UTF_8);
    }

    @Override
    public String readLine() {
        if (next >= lines.size()) {
            return null;
        }
        return lines.get(next++);
    }

    @Override
    public boolean echoesInput() {
        return true;
    }
}