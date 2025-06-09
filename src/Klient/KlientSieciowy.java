package Klient;

import javafx.application.Platform;
import model.Uzytkownik;
import utils.Pozycja;

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
    private CompletableFuture<String> registrationFuture;
    private String pendingLoginUsername;

    private Consumer<List<String>> gameListUpdateCallback;
    private Consumer<Object[]> gameStartCallback;
    private Consumer<String> boardUpdateCallback;
    private Consumer<String> gameOverCallback; // Nowy callback

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
    public void logout() {
        sendMessage("LOGOUT");
        this.currentUser = null; // Wyczyść lokalne dane o użytkowniku
        System.out.println("[KlientSieciowy] Wysłano żądanie wylogowania.");
    }
    public CompletableFuture<String> register(String username, String password) {
        registrationFuture = new CompletableFuture<>();
        sendMessage("REGISTER:" + username + ":" + password);
        return registrationFuture;
    }


    public void refreshGamesList() {
        sendMessage("GET_GAMES_LIST");
    }

    public void sendMove(Pozycja start, Pozycja koniec) {
        String message = String.format("MOVE:%d:%d:%d:%d", start.getRzad(), start.getKolumna(), koniec.getRzad(), koniec.getKolumna());
        sendMessage(message);
    }

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
        // --- ZMIANA TUTAJ ---
        // Dzielimy na 2 części: komendę i resztę wiadomości.
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

            // --- NAJWAŻNIEJSZA ZMIANA JEST TUTAJ ---
            case "REGISTER_RESULT":
                if (registrationFuture != null) {
                    // parts[1] będzie zawierać "SUCCESS" lub komunikat błędu od serwera
                    registrationFuture.complete(parts[1]);
                }
                break;

            case "GAMES_LIST":
                if (gameListUpdateCallback != null) {
                    String payload = (parts.length > 1) ? parts[1] : "";
                    gameListUpdateCallback.accept(List.of(payload.split(";")));
                }
                break;
            case "GAME_START":
                if (gameStartCallback != null && parts.length > 1) {
                    String[] gameData = parts[1].split(":");
                    String opponentLogin = gameData[0];
                    model.enums.KolorFigur myColor = "WHITE".equals(gameData[1]) ? model.enums.KolorFigur.WHITE : model.enums.KolorFigur.BLACK;
                    gameStartCallback.accept(new Object[]{opponentLogin, myColor});
                }
                break;
            case "UPDATE_BOARD":
                if (boardUpdateCallback != null && parts.length > 1) {
                    boardUpdateCallback.accept(parts[1]);
                }
                break;
            case "GAME_OVER":
                if (gameOverCallback != null && parts.length > 1) {
                    gameOverCallback.accept(parts[1]);
                }
                break;
            default:
                // Ta linia nie powinna się już pojawiać dla komendy rejestracji
                System.out.println("UI Thread: Otrzymano nieznaną komendę: " + message);
                break;
        }
    }

    public Uzytkownik getCurrentUser() { return currentUser; }
    public void setOnGameListUpdate(Consumer<List<String>> callback) { this.gameListUpdateCallback = callback; }
    public void setOnGameStart(Consumer<Object[]> callback) { this.gameStartCallback = callback; }
    public void setOnBoardUpdate(Consumer<String> callback) { this.boardUpdateCallback = callback; }
    public void setOnGameOver(Consumer<String> callback) { this.gameOverCallback = callback; }
    public void createGame() { sendMessage("CREATE_GAME"); }
    public void joinGame(String gameId) { sendMessage("JOIN_GAME:" + gameId); }
    private void sendMessage(String message) { if (out != null && !socket.isClosed()) out.println(message); }


    public void disconnect() {
        System.out.println("[KlientSieciowy] Rozłączanie...");
        try {
            if (listenerThread != null && listenerThread.isAlive()) {
                listenerThread.interrupt();
            }
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("[KlientSieciowy] Błąd podczas rozłączania: " + e.getMessage());
        } finally {
            socket = null;
            in = null;
            out = null;
            currentUser = null;
            System.out.println("[KlientSieciowy] Rozłączono.");
        }
    }
}