package view.controlers;

import Klient.KlientSieciowy;
import gui.KontrolerKlienta;
import gui.KontrolerNawigator;
import gui.Nawigator;
import gui.ViewManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import model.Uzytkownik;

import java.net.URL;
import java.util.ResourceBundle;

// implementuje wszystkie 3 interfejsy
public class GlownaStronaViewController implements Initializable, KontrolerNawigator, KontrolerKlienta {


    @FXML private TabPane bg;
    @FXML private Tab stronaGlowna;
    @FXML private Tab konto;
    @FXML private Button grajButton;

    // Pola FXML, do zakladki konta
    @FXML private Label loginLabel;
    @FXML private Label dataRejestracjiLabel;

    // Pola przechowujące instancje Nawigatora i Klienta
    private Nawigator nawigator;
    private KlientSieciowy klientSieciowy;

    @Override
    public void setKlientSieciowy(KlientSieciowy klient) {
        this.klientSieciowy = klient;

        wypelnijDaneKonta();
    }

    @Override
    public void setNawigator(Nawigator nawigator) {
        this.nawigator = nawigator;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Ta metoda jest teraz bardziej elegancka
        grajButton.setOnMouseClicked(event -> {
            if (nawigator != null) {
                nawigator.nawigujDo(ViewManager.GRA);
            }
        });
    }


    private void wypelnijDaneKonta() {
        // Sprawdzamy, czy mamy dostęp do klienta i czy jest w nim zalogowany użytkownik
        if (klientSieciowy != null && klientSieciowy.getCurrentUser() != null) {
            Uzytkownik zalogowanyUzytkownik = klientSieciowy.getCurrentUser();

            // Ustawiamy tekst w naszych etykietach
            if (loginLabel != null) {
                loginLabel.setText(zalogowanyUzytkownik.getLogin());
            }
            if (dataRejestracjiLabel != null) {
                // Na razie data rejestracji będzie pusta, bo nie pobieramy jej z serwera,

                dataRejestracjiLabel.setText("Brak danych (do zaimplementowania)");
            }
        } else {
            // Sytuacja awaryjna, gdyby dane były niedostępne
            if (loginLabel != null) loginLabel.setText("Błąd");
            if (dataRejestracjiLabel != null) dataRejestracjiLabel.setText("Brak danych");
        }
    }
}