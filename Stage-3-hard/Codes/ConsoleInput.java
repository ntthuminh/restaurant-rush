import java.util.Scanner;

public class ConsoleInput implements InputSource {
    private final Scanner scanner = new Scanner(System.in);

    @Override
    public String readLine() {
        return scanner.hasNextLine() ? scanner.nextLine() : null;
    }
}
