import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

// Ta klasa służy TYLKO do testowania.
public class TestKlient {

    public static void main(String[] args) {
        String serverAddress = "127.0.0.1"; // localhost
        int port = 4999;
        System.out.println("[TestKlient] Próbuję połączyć się z serwerem " + serverAddress + ":" + port);

        try (
                Socket socket = new Socket(serverAddress, port);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                Scanner consoleScanner = new Scanner(System.in)
        ) {
            System.out.println("[TestKlient] Połączono! Wpisz wiadomość i naciśnij Enter. Wpisz 'exit' aby zakończyć.");

            String userInput;
            do {
                System.out.print("> ");
                userInput = consoleScanner.nextLine();

                // Wyślij wpisaną linię do serwera
                out.println(userInput);

                // Odbierz odpowiedź od serwera
                String serverResponse = in.readLine();
                System.out.println("Odpowiedź serwera: " + serverResponse);

            } while (!"exit".equalsIgnoreCase(userInput));

        } catch (IOException e) {
            System.err.println("[TestKlient] Nie można połączyć się z serwerem. Czy na pewno jest uruchomiony?");
        }
        System.out.println("[TestKlient] Zakończono działanie.");
    }
}