import model.OpenGame;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GameLobbyManager {
    private final Map<String, OpenGame> openGames = new ConcurrentHashMap<>();
    private final TestSerwer server;

    public GameLobbyManager(TestSerwer server) {
        this.server = server;
    }

    public synchronized void createGame(String hostLogin) {
        if (openGames.values().stream().anyMatch(g -> g.getHostPlayerLogin().equals(hostLogin))) {
            return;
        }
        OpenGame newGame = new OpenGame(hostLogin);
        openGames.put(newGame.getGameId(), newGame);
        System.out.println("[LOBBY] Utworzono nową grę dla " + hostLogin + " (ID: " + newGame.getGameId() + ")");
        broadcastOpenGames();
    }

    public synchronized void joinGame(String gameId, String joiningPlayerLogin) {
        OpenGame game = openGames.get(gameId);
        if (game != null) {
            if (game.getHostPlayerLogin().equals(joiningPlayerLogin)) {
                return;
            }

            openGames.remove(gameId);
            System.out.println("[LOBBY] Gracz " + joiningPlayerLogin + " dołączył do gry gracza " + game.getHostPlayerLogin());

            // serwer rozpocznie gre.
            // Serwer zajmie się losowaniem kolorów i stworzeniem obiektu gry.
            server.rozpocznijGre(game.getHostPlayerLogin(), joiningPlayerLogin);

            broadcastOpenGames();
        }
    }

    public String getGamesListPayload() {
        if (openGames.isEmpty()) return "";
        return openGames.values().stream()
                .map(g -> g.getGameId() + "," + g.getHostPlayerLogin())
                .collect(Collectors.joining(";"));
    }

    public void broadcastOpenGames() {
        String payload = getGamesListPayload();
        server.broadcastMessage("GAMES_LIST:" + payload);
    }
}