import db.UserDatabaseManager;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestSerwer {

    public static final int PORT = 4999;

    public static void main(String[] args) {
        // 1. Inicjalizujemy bazę danych PRZED startem serwera
        System.out.println("[SERWER] Inicjalizacja menedżera bazy danych...");
        UserDatabaseManager dbManager = new UserDatabaseManager();
        System.out.println("[SERWER] Menedżer bazy danych gotowy.");

        // Tworzymy pulę wątków, aby serwer mógł obsługiwać wielu klientów naraz
        ExecutorService pool = Executors.newCachedThreadPool();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[SERWER] Serwer szachowy uruchomiony na porcie " + PORT);
            System.out.println("[SERWER] Oczekuję na połączenia od klientów...");

            while (true) {
                // Czekaj na połączenie od klienta
                Socket clientSocket = serverSocket.accept();
                System.out.println("[SERWER] Nowe połączenie od: " + clientSocket.getInetAddress().getHostAddress());

                // Dla każdego klienta utwórz nowy wątek obsługi (ClientHandler)
                // i przekaż mu dostęp do managera bazy danych
                pool.submit(new ClientHandler(clientSocket, dbManager));
            }

        } catch (IOException e) {
            System.err.println("[SERWER] Krytyczny błąd serwera: " + e.getMessage());
            e.printStackTrace();
        }
    }
}