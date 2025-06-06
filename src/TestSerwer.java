import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

// Ta klasa służy TYLKO do testowania. Jest całkowicie niezależna od reszty projektu.
public class TestSerwer {

    public static void main(String[] args) {
        int port = 4999;
        System.out.println("[TestSerwer] Uruchamiam testowy serwer na porcie: " + port);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[TestSerwer] Nasłuchuję na połączenia...");

            // Czekaj na jedno połączenie od klienta
            Socket clientSocket = serverSocket.accept();
            System.out.println("[TestSerwer] Klient połączony: " + clientSocket.getInetAddress());

            // Automatycznie zamyka strumienie po zakończeniu bloku try
            try (
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
            ) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println("[TestSerwer] Otrzymałem: '" + inputLine + "'");

                    if ("exit".equalsIgnoreCase(inputLine)) {
                        out.println("Do widzenia!");
                        break;
                    }

                    // Odeślij wiadomość z powrotem (echo)
                    out.println("Serwer testowy odsyła: " + inputLine);
                }
            }
            System.out.println("[TestSerwer] Zamykanie połączenia z klientem.");

        } catch (IOException e) {
            System.err.println("[TestSerwer] Błąd: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("[TestSerwer] Serwer testowy zakończył działanie.");
    }
}