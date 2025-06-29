import db.UserDatabaseManager;
import model.AktywnaGra;
import utils.Pozycja;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestSerwer {
    public static final int PORT = 4999;
    private final Map<String, ClientHandler> activeClients = new ConcurrentHashMap<>();
    private final GameLobbyManager lobbyManager = new GameLobbyManager(this);
    private final UserDatabaseManager dbManager;
    private final Map<String, AktywnaGra> aktywneGry = new ConcurrentHashMap<>();
    private final Map<String, String> graczDoGryId = new ConcurrentHashMap<>();

    public TestSerwer(UserDatabaseManager dbManager) {
        this.dbManager = dbManager;
    }
    public void addClient(String login, ClientHandler handler) {
        activeClients.put(login, handler);
    }

    public void removeClient(String login) {
        activeClients.remove(login);

    }

    public void broadcastMessage(String message) {
        activeClients.values().forEach(client -> client.sendMessage(message));
    }

    public void rozpocznijGre(String hostLogin, String joiningPlayerLogin) {
        String gameId = UUID.randomUUID().toString();
        AktywnaGra nowaGra = new AktywnaGra(hostLogin, joiningPlayerLogin);

        aktywneGry.put(gameId, nowaGra);
        graczDoGryId.put(nowaGra.getGraczBialyLogin(), gameId);
        graczDoGryId.put(nowaGra.getGraczCzarnyLogin(), gameId);

        ClientHandler bialyHandler = activeClients.get(nowaGra.getGraczBialyLogin());
        ClientHandler czarnyHandler = activeClients.get(nowaGra.getGraczCzarnyLogin());

        if (bialyHandler != null) {
            bialyHandler.sendMessage("GAME_START:" + nowaGra.getGraczCzarnyLogin() + ":WHITE");
        }
        if (czarnyHandler != null) {
            czarnyHandler.sendMessage("GAME_START:" + nowaGra.getGraczBialyLogin() + ":BLACK");
        }
    }
    public void obsluzPoddanieGry(String resigningPlayerLogin) {
        String gameId = graczDoGryId.get(resigningPlayerLogin);
        if (gameId == null) return;

        AktywnaGra gra = aktywneGry.get(gameId);

        if (gra == null || gra.isGraZakonczona()) return;


        String winnerLogin = gra.getGraczBialyLogin().equals(resigningPlayerLogin)
                ? gra.getGraczCzarnyLogin()
                : gra.getGraczBialyLogin();

        System.out.println("[SERWER] Gracz " + resigningPlayerLogin + " poddał grę. Wygrywa " + winnerLogin);


        String gameOverMessage = "GAME_OVER:RESIGNATION:" + winnerLogin;


        dbManager.zaktualizujEloIZapiszGre(gra, gameOverMessage);


        ClientHandler bialyHandler = activeClients.get(gra.getGraczBialyLogin());
        ClientHandler czarnyHandler = activeClients.get(gra.getGraczCzarnyLogin());

        if (bialyHandler != null) bialyHandler.sendMessage(gameOverMessage);
        if (czarnyHandler != null) czarnyHandler.sendMessage(gameOverMessage);


        aktywneGry.remove(gameId);
        graczDoGryId.remove(gra.getGraczBialyLogin());
        graczDoGryId.remove(gra.getGraczCzarnyLogin());
    }

    public void obsluzRuch(String loginGracza, Pozycja start, Pozycja koniec) {
        String gameId = graczDoGryId.get(loginGracza);
        if (gameId == null) return;

        AktywnaGra gra = aktywneGry.get(gameId);
        if (gra == null) return;

        if (gra.wykonajRuch(loginGracza, start, koniec)) {
            String stanPlanszy = gra.getPlansza().doZapisuString();
            String wiadomosc = "UPDATE_BOARD:" + stanPlanszy;

            ClientHandler bialyHandler = activeClients.get(gra.getGraczBialyLogin());
            ClientHandler czarnyHandler = activeClients.get(gra.getGraczCzarnyLogin());

            if (bialyHandler != null) bialyHandler.sendMessage(wiadomosc);
            if (czarnyHandler != null) czarnyHandler.sendMessage(wiadomosc);

            String gameOverMessage = gra.sprawdzStanGry();
            if (gameOverMessage != null) {
                if (bialyHandler != null) bialyHandler.sendMessage(gameOverMessage);
                if (czarnyHandler != null) czarnyHandler.sendMessage(gameOverMessage);

                dbManager.zaktualizujEloIZapiszGre(gra, gameOverMessage);

                aktywneGry.remove(gameId);
                graczDoGryId.remove(gra.getGraczBialyLogin());
                graczDoGryId.remove(gra.getGraczCzarnyLogin());
            }
        }
    }

    public static void main(String[] args) {
        UserDatabaseManager dbManager = new UserDatabaseManager();

        TestSerwer server = new TestSerwer(dbManager);
        ExecutorService pool = Executors.newCachedThreadPool();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[SERWER] Serwer uruchomiony na porcie " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                pool.submit(new ClientHandler(clientSocket, dbManager, server, server.lobbyManager));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}