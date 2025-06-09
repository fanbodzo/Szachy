package view.controlers;

import Klient.KlientSieciowy;
import gui.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.concurrent.CompletableFuture;

public class RegistrationViewController implements KontrolerNawigator, KontrolerKlienta {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button registerButton;
    @FXML private Button backToLoginButton;
    @FXML private Label messageLabel;

    private Nawigator nawigator;
    private KlientSieciowy klientSieciowy;

    @Override
    public void setNawigator(Nawigator nawigator) {
        this.nawigator = nawigator;
    }

    @Override
    public void setKlientSieciowy(KlientSieciowy klient) {
        this.klientSieciowy = klient;
    }

    @FXML
    public void initialize() {
        registerButton.setOnAction(event -> handleRegister());
        backToLoginButton.setOnAction(event -> handleBackToLogin());
    }

    private void handleRegister() {
        String user = usernameField.getText();
        String pass = passwordField.getText();
        String confirmPass = confirmPasswordField.getText();

        if (user.trim().isEmpty() || pass.isEmpty()) {
            messageLabel.setText("Nazwa użytkownika i hasło nie mogą być puste.");
            return;
        }
        if (!pass.equals(confirmPass)) {
            messageLabel.setText("Hasła nie są zgodne.");
            return;
        }

        setUIState(true);

        Task<Boolean> registrationTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                try {
                    klientSieciowy.connect();
                    CompletableFuture<String> future = klientSieciowy.register(user, pass);
                    String result = future.get(); // Czeka na odpowiedź z serwera
                    if ("SUCCESS".equals(result)) {
                        return true;
                    } else {
                        updateMessage(result); // Przekaż komunikat błędu do UI
                        return false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    updateMessage("Błąd połączenia z serwerem.");
                    return false;
                }
            }
        };

        registrationTask.setOnSucceeded(e -> {
            boolean isRegistered = registrationTask.getValue();
            if (isRegistered) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Rejestracja udana");
                    alert.setHeaderText(null);
                    alert.setContentText("Konto zostało pomyślnie utworzone. Możesz się teraz zalogować.");
                    alert.showAndWait();
                    nawigator.nawigujDo(ViewManager.LOGIN);
                });
            } else {
                messageLabel.setText(registrationTask.getMessage());
                setUIState(false);
            }
        });

        new Thread(registrationTask).start();
    }

    private void handleBackToLogin() {
        nawigator.nawigujDo(ViewManager.LOGIN);
    }

    private void setUIState(boolean registering) {
        registerButton.setDisable(registering);
        usernameField.setDisable(registering);
        passwordField.setDisable(registering);
        confirmPasswordField.setDisable(registering);
        backToLoginButton.setDisable(registering);
        if (registering) {
            messageLabel.setText("Rejestrowanie...");
        }
    }
}