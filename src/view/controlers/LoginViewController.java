package view.controlers;

import Klient.KlientSieciowy;
import gui.KontrolerKlienta;
import gui.KontrolerNawigator;
import gui.Nawigator;
import gui.ViewManager;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class LoginViewController implements Initializable, KontrolerNawigator, KontrolerKlienta {
    @FXML private TextField username;
    @FXML private PasswordField password;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private Hyperlink registerLink;

    private Nawigator nawigator;
    private KlientSieciowy klientSieciowy;

    @Override
    public void setNawigator(Nawigator nawigator) { this.nawigator = nawigator; }
    @Override
    public void setKlientSieciowy(KlientSieciowy klient) { this.klientSieciowy = klient; }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loginButton.setOnAction(event -> handleLogin());
        registerLink.setOnAction(event -> handleGoToRegister());
    }
    private void handleGoToRegister() {
        nawigator.nawigujDo(ViewManager.REJESTRACJA);
    }

    private void handleLogin() {
        String user = username.getText();
        String pass = password.getText();
        if (user.isEmpty() || pass.isEmpty()) {
            errorLabel.setText("Pola nie mogą być puste.");
            return;
        }

        setUIState(true); // Zablokuj UI

        Task<Boolean> loginTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                try {
                    // Nawiąż połączenie
                    klientSieciowy.connect();

                    // Wyślij żądanie logowania i czekaj na wynik z wątku nasłuchującego
                    CompletableFuture<Boolean> future = klientSieciowy.login(user, pass);

                    // Blokuje ten wątek w tle, aż wątek nasłuchujący wywoła future.complete()
                    return future.get();
                } catch (Exception e) {
                    e.printStackTrace();
                    updateMessage("Błąd: " + e.getMessage());
                    return false;
                }
            }
        };

        loginTask.setOnSucceeded(e -> {
            boolean isLoggedIn = loginTask.getValue();
            if (isLoggedIn) {
                nawigator.nawigujDo(ViewManager.STRONA_GLOWNA);
            } else {
                errorLabel.setText(klientSieciowy.getLastLoginError());
                setUIState(false); // Odblokuj UI
                //klientSieciowy.disconnect(); // Rozłącz, aby następna próba była czysta
            }
        });

        new Thread(loginTask).start();
    }

    private void setUIState(boolean loggingIn) {
        loginButton.setDisable(loggingIn);
        username.setDisable(loggingIn);
        password.setDisable(loggingIn);
        registerLink.setDisable(loggingIn); // Dodatkowo blokujemy link do rejestracji

        if (loggingIn) {
            errorLabel.setText("Logowanie...");
        } else {
            // Jeśli przestajemy się logować, a na etykiecie wciąż jest
            // tekst "Logowanie...", czyścimy go, aby zrobić miejsce
            // na właściwy komunikat o błędzie.
            if (errorLabel.getText().equals("Logowanie...")) {
                errorLabel.setText("");
            }
        }
    }

}