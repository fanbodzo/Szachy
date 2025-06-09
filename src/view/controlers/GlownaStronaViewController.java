package view.controlers;

import Klient.KlientSieciowy;
import gui.KontrolerKlienta;
import gui.KontrolerNawigator;
import gui.Nawigator;
import gui.ViewManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import model.Uzytkownik;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class GlownaStronaViewController implements Initializable, KontrolerNawigator, KontrolerKlienta {

    // ... (istniejące pola)
    @FXML private Button createGameButton;
    @FXML private Button refreshButton; // NOWOŚĆ
    @FXML private ListView<String> gamesListView;
    @FXML private Label loginLabel;
    @FXML private Label dataRejestracjiLabel;

    private Nawigator nawigator;
    private KlientSieciowy klientSieciowy;
    private ObservableList<String> openGamesData = FXCollections.observableArrayList();
    private java.util.Map<String, String> gameDisplayToIdMap = new java.util.HashMap<>();


    @Override
    public void setKlientSieciowy(KlientSieciowy klient) {
        this.klientSieciowy = klient;
        wypelnijDaneKonta();

        if (klientSieciowy != null) {
            klientSieciowy.setOnGameListUpdate(this::updateGamesList);
            klientSieciowy.setOnGameStart(opponentLogin -> Platform.runLater(() -> {
                nawigator.nawigujDo(ViewManager.GRA, opponentLogin);
            }));
        }
    }

    @Override
    public void setNawigator(Nawigator nawigator) {
        this.nawigator = nawigator;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Akcja dla przycisku tworzenia gry
        createGameButton.setOnAction(event -> {
            if (klientSieciowy != null) {
                klientSieciowy.createGame();
                createGameButton.setDisable(true);
            }
        });

        // NOWOŚĆ: Akcja dla przycisku odświeżania
        refreshButton.setOnAction(event -> {
            if (klientSieciowy != null) {
                System.out.println("[GlownaStronaViewController] Wysyłanie prośby o odświeżenie listy gier.");
                klientSieciowy.refreshGamesList();
            }
        });

        gamesListView.setItems(openGamesData);

        // Akcja dla dołączania do gry
        gamesListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selectedGameDisplay = gamesListView.getSelectionModel().getSelectedItem();
                if (selectedGameDisplay != null) {
                    String gameId = gameDisplayToIdMap.get(selectedGameDisplay);
                    if (gameId != null && klientSieciowy != null) {
                        klientSieciowy.joinGame(gameId);
                    }
                }
            }
        });
    }

    // Ta metoda pozostaje bez zmian, ale będzie teraz wywoływana zarówno przez broadcast, jak i na żądanie
    private void updateGamesList(List<String> games) {
        Platform.runLater(() -> {
            openGamesData.clear();
            gameDisplayToIdMap.clear();

            List<String> validGames = games.stream().filter(g -> !g.isEmpty() && g.contains(",")).collect(Collectors.toList());

            if (klientSieciowy == null || klientSieciowy.getCurrentUser() == null) {
                return;
            }

            String myLogin = klientSieciowy.getCurrentUser().getLogin();
            boolean amIHosting = false;

            for (String gameData : validGames) {
                String[] parts = gameData.split(",");
                String gameId = parts[0];
                String hostLogin = parts[1];

                if (myLogin.equals(hostLogin)) {
                    amIHosting = true;
                }

                String displayName = "Gra gracza: " + hostLogin;
                if(myLogin.equals(hostLogin)){
                    displayName += " (Twoja gra, oczekiwanie...)";
                }
                openGamesData.add(displayName);
                gameDisplayToIdMap.put(displayName, gameId);
            }

            createGameButton.setDisable(amIHosting);
        });
    }

    private void wypelnijDaneKonta() {
        if (klientSieciowy != null && klientSieciowy.getCurrentUser() != null) {
            Uzytkownik zalogowanyUzytkownik = klientSieciowy.getCurrentUser();
            if (loginLabel != null) loginLabel.setText(zalogowanyUzytkownik.getLogin());
            if (dataRejestracjiLabel != null) dataRejestracjiLabel.setText("Brak danych");
        }
    }
}