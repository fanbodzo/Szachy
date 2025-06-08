package Klient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


import javafx.application.Platform;

public class KlientSieciowy {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String serverAddress = "127.0.0.1";
    private int port = 4999;
    private Thread listenerThread;

    public KlientSieciowy() {}

    // Metoda do połączenia, może być wywołana osobno lub jako część login
    public void connect() throws IOException {
        if (socket == null || socket.isClosed()) {
            System.out.println("[KlientSieciowy] Próbuję połączyć się z serwerem " + serverAddress + ":" + port);
            socket = new Socket(serverAddress, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("[KlientSieciowy] Połączono!");
        }
    }


    public boolean login(String username, String password) throws IOException {
        connect();

        // Przykład protokołu: "LOGIN:username:password"
        String loginMessage = "LOGIN:" + username + ":" + password;
        System.out.println("[KlientSieciowy] Wysyłam dane logowania: " + loginMessage);
        out.println(loginMessage);

        String serverResponse = in.readLine(); // Czekaj na odpowiedź serwera
        System.out.println("[KlientSieciowy] Odpowiedź serwera: " + serverResponse);

        if (serverResponse != null && serverResponse.startsWith("LOGIN_SUCCESS")) {
            // mozna wywolac tutaj jakas metode na przekazanie danych uzytkownia do guui
            // String[] parts = serverResponse.split(":");
            // String userId = parts[1];
            // this.currentUser = new User(username, userId);
            return true;
        } else {

            //TODO ZMIENIC TO NA FALSE JAK DAMY DANE LOGOWANIA
            return true;
        }
    }

    public void startListening() {
        if (listenerThread != null && listenerThread.isAlive()) {
            System.out.println("[KlientSieciowy] Listener już działa.");
            return;
        }

        listenerThread = new Thread(() -> {
            try {
                String fromServer;
                while (socket != null && !socket.isClosed() && (fromServer = in.readLine()) != null) {
                    final String messageToProcess = fromServer; // Potrzebne dla lambdy
                    System.out.println("[KlientSieciowy] Otrzymano od serwera: " + messageToProcess);

                    Platform.runLater(() -> {
                        // Tutaj logika aktualizacji GUI na podstawie wiadomości od serwera
                        // czyli zmienianie wartosci pol kto jest zalogowany itd
                        System.out.println("UI Thread: Przetwarzam " + messageToProcess);
                    });
                }
            } catch (IOException e) {
                if (socket != null && !socket.isClosed()) {
                    System.err.println("[KlientSieciowy] Błąd odczytu z serwera lub serwer zamknął połączenie: " + e.getMessage());
                }
            } finally {
                System.out.println("[KlientSieciowy] Wątek nasłuchujący zakończył pracę.");
                disconnect(); // Sprzątanie
            }
        });
        listenerThread.setDaemon(true); // Wątek zakończy się, gdy aplikacja JavaFX się zamknie
        listenerThread.start();
        System.out.println("[KlientSieciowy] Wątek nasłuchujący uruchomiony.");
    }

    public void sendMessage(String message) {
        if (out != null && socket != null && !socket.isClosed()) {
            System.out.println("[KlientSieciowy] Wysyłam do serwera: " + message);
            out.println(message);
        } else {
            System.err.println("[KlientSieciowy] Nie można wysłać wiadomości, brak połączenia.");
        }
    }

    public void disconnect() {
        System.out.println("[KlientSieciowy] Rozłączanie...");
        try {
            if (listenerThread != null && listenerThread.isAlive()) {
                listenerThread.interrupt(); // Przerwij wątek nasłuchujący, jeśli nadal działa
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
            System.out.println("[KlientSieciowy] Rozłączono.");
        }
    }

}