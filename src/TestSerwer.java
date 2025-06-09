import db.UserDatabaseManager;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class TestSerwer {

    public static final int PORT = 4999;
    // NOWOŚĆ: Mapa przechowująca uchwyty do klientów (login -> ClientHandler)
    private final Map<String, ClientHandler> connectedClients = new ConcurrentHashMap<>();
    private final GameLobbyManager lobbyManager;

    public TestSerwer() {
        this.lobbyManager = new GameLobbyManager(this);
    }

    // NOWOŚĆ: Metoda do dodawania klienta po pomyślnym zalogowaniu
    public void addClient(String login, ClientHandler handler) {
        connectedClients.put(login, handler);
        System.out.println("[SERWER] Zalogowany klient dodany do listy: " + login);
        // Po dodaniu, od razu wyślij mu aktualną listę gier
        handler.sendMessage("GAMES_LIST:" + lobbyManager.getGamesListPayload());
    }

    // NOWOŚĆ: Metoda do usuwania klienta
    public void removeClient(String login) {
        if (login != null) {
            connectedClients.remove(login);
            System.out.println("[SERWER] Klient wylogowany/rozłączony: " + login);
        }
    }

    // NOWOŚĆ: Wysyła wiadomość do wszystkich zalogowanych klientów
    public void broadcastMessage(String message) {
        System.out.println("[SERWER BROADCAST] " + message);
        for (ClientHandler handler : connectedClients.values()) {
            handler.sendMessage(message);
        }
    }

    // NOWOŚĆ: Informuje konkretnych graczy o starcie gry
    public void notifyGameStart(String playerToSendTo, String opponentLogin) {
        ClientHandler handler = connectedClients.get(playerToSendTo);
        if (handler != null) {
            // Format: GAME_START:przeciwnik
            handler.sendMessage("GAME_START:" + opponentLogin);
        }
    }

    public void startServer() {
        System.out.println("[SERWER] Inicjalizacja menedżera bazy danych...");
        UserDatabaseManager dbManager = new UserDatabaseManager();
        System.out.println("[SERWER] Menedżer bazy danych gotowy.");

        ExecutorService pool = Executors.newCachedThreadPool();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[SERWER] Serwer szachowy uruchomiony na porcie " + PORT);
            System.out.println("[SERWER] Oczekuję na połączenia od klientów...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[SERWER] Nowe połączenie od: " + clientSocket.getInetAddress().getHostAddress());
                // Przekazujemy referencję do serwera i managera lobby
                pool.submit(new ClientHandler(clientSocket, dbManager, this, lobbyManager));
            }
        } catch (IOException e) {
            System.err.println("[SERWER] Krytyczny błąd serwera: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        TestSerwer server = new TestSerwer();
        server.startServer();
    }
}