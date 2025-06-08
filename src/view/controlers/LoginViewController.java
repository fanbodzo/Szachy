package view.controlers;

import Klient.KlientSieciowy;
import gui.KontrolerNawigator;
import gui.Nawigator;
import gui.ViewManager;
import gui.KontrolerKlienta;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;


import java.net.URL;
import java.util.ResourceBundle;

public class LoginViewController implements Initializable , KontrolerNawigator , KontrolerKlienta {
    @FXML
    private Pane bg;
    @FXML
    private TextField username;
    @FXML
    private PasswordField password;
    @FXML
    private ImageView ikona;
    @FXML
    private Button loginButton;
    @FXML
    private Label errorLabel;

    private Nawigator nawigator;

    private KlientSieciowy klientSieciowy;

    public LoginViewController() {
        this.klientSieciowy = new KlientSieciowy();
    }
    @Override
    public void setNawigator(Nawigator nawigator) {
        this.nawigator = nawigator;
    }

    @Override
    public void setKlientSieciowy(KlientSieciowy klient) {
        this.klientSieciowy = klient;
        if (this.klientSieciowy != null) {
            System.out.println("[LoginViewController] Otrzymałem instancję KlientSieciowy.");
        } else {
            System.err.println("[LoginViewController] Otrzymałem NULL jako KlientSieciowy! Sprawdź konfigurację w Main i Nawigator.");
        }
    }

    public void initialize(URL location, ResourceBundle resources) {
        loginButton();
    }

    private void loginButton() {

        loginButton.setOnMouseClicked(event -> {
            if (this.klientSieciowy == null) {
                System.err.println("[LoginViewController] KlientSieciowy nie został wstrzyknięty. Nie można się zalogować.");
                if (errorLabel != null) errorLabel.setText("Błąd aplikacji: Moduł sieciowy niedostępny.");
                return;
            }

            String user = username.getText();
            String pass = password.getText();

            if (user.isEmpty() || pass.isEmpty()) {
                if (errorLabel != null) errorLabel.setText("Nazwa użytkownika i hasło są wymagane.");
                return;
            }
            if (errorLabel != null) {
                errorLabel.setText("Logowanie..."); // Informacja dla użytkownika
                loginButton.setDisable(true); // Zablokuj przycisk podczas logowania
            }


            Task<Boolean> loginTask = new Task<>() {
                @Override
                protected Boolean call() throws Exception {
                    // Ta metoda wykonuje się w tle (nie w wątku JavaFX)
                    try {
                        // Zakładamy, że metoda login w KlientSieciowy zwraca boolean
                        // i obsługuje połączenie, jeśli to konieczne.
                        return klientSieciowy.login(user, pass);
                    } catch (Exception e) {
                        // Złap wyjątki, które mogą wystąpić podczas operacji sieciowej
                        System.err.println("[LoginViewController] Wyjątek podczas logowania w wątku tła: " + e.getMessage());
                        updateMessage("Błąd połączenia: " + e.getMessage()); // Można użyć do przekazania info do UI
                        // e.printStackTrace(); // Opcjonalnie dla debugowania
                        return false;
                    }
                }
            };

            loginTask.setOnSucceeded(e -> {
                // Ta metoda wykonuje się w wątku JavaFX po zakończeniu call()
                if (errorLabel != null) loginButton.setDisable(false); // Odblokuj przycisk
                boolean isLoggedIn = loginTask.getValue();
                if (isLoggedIn) {
                    System.out.println("[LoginViewController] Logowanie udane dla: " + user);
                    if (errorLabel != null) errorLabel.setText(""); // Wyczyść błędy
                    // Uruchom nasłuchiwanie na serwerze PO udanym logowaniu
                    klientSieciowy.startListening();
                    // Przejdź do strony głównej
                    this.nawigator.nawigujDo(ViewManager.STRONA_GLOWNA);
                } else {
                    String errorMessage = loginTask.getMessage(); // Spróbuj pobrać wiadomość z błędu
                    if (errorMessage == null || errorMessage.isEmpty()) {
                        errorMessage = "Błędny login lub hasło.";
                    }
                    System.out.println("[LoginViewController] Logowanie nieudane dla: " + user + ". Powód: " + errorMessage);
                    if (errorLabel != null) errorLabel.setText(errorMessage);
                }
            });

            loginTask.setOnFailed(e -> {
                // Ta metoda wykonuje się w wątku JavaFX, jeśli call() rzuciło wyjątek
                if (errorLabel != null) loginButton.setDisable(false); // Odblokuj przycisk
                Throwable ex = loginTask.getException();
                System.err.println("[LoginViewController] Logowanie zakończone błędem (Task.onFailed): " + ex.getMessage());
                if (errorLabel != null) errorLabel.setText("Błąd serwera: " + ex.getMessage());
                ex.printStackTrace(); // Dla debugowania
            });

            // Uruchom zadanie w nowym wątku
            new Thread(loginTask).start();
        });
    }

}
