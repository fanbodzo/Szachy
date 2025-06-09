import model.OpenGame;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GameLobbyManager {
    // Mapa przechowująca otwarte gry. Klucz to gameId, wartość to obiekt OpenGame.
    // Używamy ConcurrentHashMap dla bezpieczeństwa wątkowego.
    private final Map<String, OpenGame> openGames = new ConcurrentHashMap<>();
    private final TestSerwer server; // Referencja do głównego serwera, aby móc broadcastować

    public GameLobbyManager(TestSerwer server) {
        this.server = server;
    }

    // Tworzy nową grę i dodaje ją do listy otwartych
    public synchronized void createGame(String hostLogin) {
        // Upewniamy się, że jeden gracz nie może otworzyć wielu gier naraz
        if (openGames.values().stream().anyMatch(g -> g.getHostPlayerLogin().equals(hostLogin))) {
            System.out.println("[LOBBY] Gracz " + hostLogin + " już ma otwartą grę.");
            return;
        }
        OpenGame newGame = new OpenGame(hostLogin);
        openGames.put(newGame.getGameId(), newGame);
        System.out.println("[LOBBY] Utworzono nową grę dla " + hostLogin + " (ID: " + newGame.getGameId() + ")");
        broadcastOpenGames();
    }

    // Obsługuje dołączenie gracza do istniejącej gry
    public synchronized void joinGame(String gameId, String joiningPlayerLogin) {
        OpenGame game = openGames.get(gameId);
        if (game != null) {
            // Sprawdzenie, czy gracz nie dołącza do własnej gry
            if (game.getHostPlayerLogin().equals(joiningPlayerLogin)) {
                System.out.println("[LOBBY] Gracz " + joiningPlayerLogin + " nie może dołączyć do własnej gry.");
                return;
            }

            // Usuwamy grę z listy otwartych
            openGames.remove(gameId);
            System.out.println("[LOBBY] Gracz " + joiningPlayerLogin + " dołączył do gry " + game.getHostPlayerLogin());

            // Informujemy obu graczy o rozpoczęciu gry
            server.notifyGameStart(game.getHostPlayerLogin(), joiningPlayerLogin);
            server.notifyGameStart(joiningPlayerLogin, game.getHostPlayerLogin());

            // Odświeżamy listę gier u pozostałych klientów
            broadcastOpenGames();
        }
    }

    // Zwraca listę gier w formacie do wysłania klientowi
    public String getGamesListPayload() {
        if (openGames.isEmpty()) {
            return "";
        }
        // Format: gameId1,host1;gameId2,host2;...
        return openGames.values().stream()
                .map(g -> g.getGameId() + "," + g.getHostPlayerLogin())
                .collect(Collectors.joining(";"));
    }

    // Wysyła aktualną listę gier do wszystkich połączonych klientów
    public void broadcastOpenGames() {
        String payload = getGamesListPayload();
        server.broadcastMessage("GAMES_LIST:" + payload);
    }
}