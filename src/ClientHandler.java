import db.UserDatabaseManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final UserDatabaseManager dbManager;
    private final TestSerwer server;
    private final GameLobbyManager lobbyManager;
    private PrintWriter out;
    private String currentUserLogin = null;

    public ClientHandler(Socket socket, UserDatabaseManager dbManager, TestSerwer server, GameLobbyManager lobbyManager) {
        this.clientSocket = socket;
        this.dbManager = dbManager;
        this.server = server;
        this.lobbyManager = lobbyManager;
    }

    @Override
    public void run() {
        try {
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            sendMessage("HELLO");

            String request;
            while ((request = in.readLine()) != null) {
                String logPrefix = (currentUserLogin != null) ? currentUserLogin : clientSocket.getRemoteSocketAddress().toString();
                System.out.println("[ClientHandler dla " + logPrefix + "] Odebrano: " + request);
                handleRequest(request);
            }
        } catch (SocketException e) {
            String logPrefix = (currentUserLogin != null) ? currentUserLogin : clientSocket.getRemoteSocketAddress().toString();
            System.out.println("[ClientHandler dla " + logPrefix + "] Klient się rozłączył.");
        } catch (IOException e) {
            System.err.println("[ClientHandler] Błąd komunikacji: " + e.getMessage());
        } finally {
            if (currentUserLogin != null) {
                server.removeClient(currentUserLogin);
            }
            try {
                clientSocket.close();
            } catch (IOException e) { /* ignoruj */ }
        }
    }

    private void handleRequest(String request) {
        String[] parts = request.split(":", 3);
        String command = parts[0].toUpperCase();
        String response;

        switch (command) {
            case "LOGIN":
                if (parts.length < 3) return;
                String login = parts[1];
                String password = parts[2];
                boolean isLoggedIn = dbManager.loginUser(login, password);
                if (isLoggedIn) {
                    this.currentUserLogin = login;
                    server.addClient(login, this);
                    response = "LOGIN_SUCCESS";
                } else {
                    response = "LOGIN_FAILURE";
                }
                sendMessage(response);
                if (isLoggedIn) {
                    lobbyManager.broadcastOpenGames();
                }
                break;
            case "CREATE_GAME":
                if (currentUserLogin != null) {
                    lobbyManager.createGame(currentUserLogin);
                }
                break;
            case "JOIN_GAME":
                if (currentUserLogin != null && parts.length > 1) {
                    String gameId = parts[1];
                    lobbyManager.joinGame(gameId, currentUserLogin);
                }
                break;

            // ===== NOWA KOMENDA =====
            case "GET_GAMES_LIST":
                // Wyślij listę gier tylko do tego klienta, który o nią prosił
                String payload = lobbyManager.getGamesListPayload();
                sendMessage("GAMES_LIST:" + payload);
                break;
            // =========================

            default:
                response = "ERROR:UNKNOWN_COMMAND";
                sendMessage(response);
                break;
        }
    }

    public void sendMessage(String message) {
        if (out != null && !clientSocket.isClosed()) {
            String logPrefix = (currentUserLogin != null) ? currentUserLogin : "NOWY_KLIENT";
            System.out.println("[ClientHandler dla " + logPrefix + "] Wysyłam: " + message);
            out.println(message);
        }
    }
}