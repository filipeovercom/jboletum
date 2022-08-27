import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Scanner;

public class JBoletumApplication {

    public static void main(String[] args) {
        var scanner = new Scanner(System.in);
        var exit = false;

        System.out.println("Application is running...");
        System.out.println("INFO: Type exit and press enter to close the application!");

        while (!exit) {
            String line = scanner.nextLine();

            if( Objects.isNull(line) || line.isBlank() || !line.equalsIgnoreCase("exit")) {
                System.out.println("WARN: incorrect input value!");
                System.out.println("INFO: Type 'exit' and press enter to close the application!");
            } else {
                System.out.println("INFO: Exiting the application!");
                System.out.println("INFO: Gooooooodbye!");
                exit = true;
            }
        }
    }
}
