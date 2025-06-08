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

    public ClientHandler(Socket socket, UserDatabaseManager dbManager) {
        this.clientSocket = socket;
        this.dbManager = dbManager;
    }

    @Override
    public void run() {
        try (
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
        ) {
            String request;
            while ((request = in.readLine()) != null) {
                System.out.println("[ClientHandler dla " + clientSocket.getPort() + "] Odebrano: " + request);
                handleRequest(request, out);
            }
        } catch (SocketException e) {
            System.out.println("[ClientHandler dla " + clientSocket.getPort() + "] Klient się rozłączył (SocketException).");
        } catch (IOException e) {
            System.err.println("[ClientHandler dla " + clientSocket.getPort() + "] Błąd komunikacji: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) { /* ignoruj */ }
        }
    }

    private void handleRequest(String request, PrintWriter out) {
        String[] parts = request.split(":", 3);
        String command = parts[0];
        String response;

        if (parts.length < 3) {
            response = "ERROR:INVALID_FORMAT";
            out.println(response);
            return;
        }

        String login = parts[1];
        String password = parts[2];

        switch (command.toUpperCase()) {
            case "LOGIN":
                boolean isLoggedIn = dbManager.loginUser(login, password);
                response = isLoggedIn ? "LOGIN_SUCCESS" : "LOGIN_FAILURE";
                break;
            case "REGISTER":
                boolean isRegistered = dbManager.registerUser(login, password);
                response = isRegistered ? "REGISTER_SUCCESS" : "REGISTER_FAILURE";
                break;
            default:
                response = "ERROR:UNKNOWN_COMMAND";
                break;
        }

        System.out.println("[ClientHandler dla " + clientSocket.getPort() + "] Wysyłam odpowiedź: " + response);
        out.println(response);
    }
}