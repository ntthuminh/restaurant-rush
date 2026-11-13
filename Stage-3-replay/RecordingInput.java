import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class RecordingInput implements InputSource {
    private final InputSource source;
    private final PrintWriter file;

    public RecordingInput(InputSource source, Path path) throws IOException {
        this.source = source;
        this.file = new PrintWriter(Files.newBufferedWriter(path, StandardCharsets.UTF_8));
    }

    @Override
    public String readLine() {
        String line = source.readLine();
        if (line != null) {
            file.println(line);
            file.flush();
        }
        return line;
    }
}
