package Klient;

import javafx.application.Platform;
import model.Uzytkownik;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class KlientSieciowy {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final String serverAddress = "127.0.0.1";
    private final int port = 4999;
    private Thread listenerThread;
    private Uzytkownik currentUser;

    private CompletableFuture<Boolean> loginFuture;
    private String pendingLoginUsername;

    private Consumer<List<String>> gameListUpdateCallback;
    private Consumer<String> gameStartCallback;

    public void connect() throws IOException {
        if (socket != null && !socket.isClosed()) return;

        System.out.println("[KlientSieciowy] Próbuję połączyć się z serwerem " + serverAddress + ":" + port);
        socket = new Socket(serverAddress, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String welcomeMessage = in.readLine();
        if ("HELLO".equals(welcomeMessage)) {
            System.out.println("[KlientSieciowy] Połączenie gotowe.");
            startListening();
        } else {
            throw new IOException("Błędne powitanie od serwera: " + welcomeMessage);
        }
    }

    public CompletableFuture<Boolean> login(String username, String password) {
        if (socket == null || socket.isClosed()) {
            return CompletableFuture.failedFuture(new IOException("Brak połączenia z serwerem."));
        }
        loginFuture = new CompletableFuture<>();
        this.pendingLoginUsername = username;

        String loginMessage = "LOGIN:" + username + ":" + password;
        sendMessage(loginMessage);

        return loginFuture;
    }

    // ===== NOWA METODA =====
    /**
     * Wysyła do serwera prośbę o przesłanie aktualnej listy otwartych gier.
     */
    public void refreshGamesList() {
        sendMessage("GET_GAMES_LIST");
    }
    // ========================

    private void startListening() {
        if (listenerThread != null && listenerThread.isAlive()) return;
        listenerThread = new Thread(() -> {
            try {
                String fromServer;
                while (socket != null && !socket.isClosed() && (fromServer = in.readLine()) != null) {
                    final String messageToProcess = fromServer;
                    Platform.runLater(() -> processServerMessage(messageToProcess));
                }
            } catch (IOException e) {
                System.err.println("[KlientSieciowy-Listener] Utracono połączenie z serwerem: " + e.getMessage());
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
        System.out.println("[KlientSieciowy] Wątek nasłuchujący uruchomiony.");
    }

    private void processServerMessage(String message) {
        System.out.println("[KlientSieciowy-Listener] Przetwarzam: " + message);
        String[] parts = message.split(":", 2);
        String command = parts[0];

        switch (command) {
            case "LOGIN_SUCCESS":
                this.currentUser = new Uzytkownik(pendingLoginUsername);
                this.pendingLoginUsername = null;
                if (loginFuture != null) loginFuture.complete(true);
                break;
            case "LOGIN_FAILURE":
                this.currentUser = null;
                this.pendingLoginUsername = null;
                if (loginFuture != null) loginFuture.complete(false);
                break;
            case "GAMES_LIST":
                if (gameListUpdateCallback != null) {
                    String payload = (parts.length > 1) ? parts[1] : "";
                    gameListUpdateCallback.accept(List.of(payload.split(";")));
                }
                break;
            case "GAME_START":
                if (gameStartCallback != null && parts.length > 1) {
                    gameStartCallback.accept(parts[1]);
                }
                break;
            default:
                System.out.println("UI Thread: Otrzymano nieznaną komendę: " + message);
                break;
        }
    }

    public Uzytkownik getCurrentUser() { return currentUser; }
    public void setOnGameListUpdate(Consumer<List<String>> callback) { this.gameListUpdateCallback = callback; }
    public void setOnGameStart(Consumer<String> callback) { this.gameStartCallback = callback; }
    public void createGame() { sendMessage("CREATE_GAME"); }
    public void joinGame(String gameId) { sendMessage("JOIN_GAME:" + gameId); }
    private void sendMessage(String message) { if (out != null) out.println(message); }
    public void disconnect() { /* bez zmian */ }
}