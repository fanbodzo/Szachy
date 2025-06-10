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
    @FXML private Button createGameButton;
    @FXML private Button refreshButton;
    @FXML private Button logoutButton;
    @FXML private Button refreshLeaderboardButton;
    @FXML private ListView<String> gamesListView;
    @FXML private ListView<String> historyListView;
    @FXML private ListView<String> leaderboardListView;
    @FXML private Label loginLabel;
    @FXML private Label dataRejestracjiLabel;
    @FXML private Label eloLabel;


    private Nawigator nawigator;
    private KlientSieciowy klientSieciowy;

    private ObservableList<String> openGamesData = FXCollections.observableArrayList();
    private ObservableList<String> historyData = FXCollections.observableArrayList();
    private ObservableList<String> leaderboardData = FXCollections.observableArrayList();

    private java.util.Map<String, String> gameDisplayToIdMap = new java.util.HashMap<>();


    @Override
    public void setKlientSieciowy(KlientSieciowy klient) {
        this.klientSieciowy = klient;
        wypelnijDaneKonta();

        if (klientSieciowy != null) {
            klientSieciowy.setOnGameListUpdate(this::updateGamesList);
            klientSieciowy.setOnGameStart(data -> Platform.runLater(() -> {
                // data[0] to login przeciwnika, data[1] to nasz kolor
                nawigator.nawigujDo(ViewManager.GRA, data[0], data[1]);
            }));
            klientSieciowy.setOnHistoryUpdate(this::updateHistory);
            klientSieciowy.setOnLeaderboardUpdate(this::updateLeaderboard);
            klientSieciowy.requestHistory();
            klientSieciowy.requestLeaderboard();
        }
    }

    @Override
    public void setNawigator(Nawigator nawigator) {
        this.nawigator = nawigator;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Powiązanie list z danymi
        gamesListView.setItems(openGamesData);
        historyListView.setItems(historyData);
        leaderboardListView.setItems(leaderboardData);

        // Ustawienie akcji dla wszystkich przycisków
        createGameButton.setOnAction(e -> {
            if (klientSieciowy != null) klientSieciowy.createGame();
        });
        refreshButton.setOnAction(e -> {
            if (klientSieciowy != null) klientSieciowy.refreshGamesList();
        });
        logoutButton.setOnAction(e -> handleLogout()); // Prawdopodobnie już to masz, upewnij się
        refreshLeaderboardButton.setOnAction(e -> {
            if (klientSieciowy != null) klientSieciowy.requestLeaderboard();
        });

        // Ustawienie akcji dołączania do gry
        gamesListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selectedItem = gamesListView.getSelectionModel().getSelectedItem();
                if (selectedItem != null && !selectedItem.contains("(Twoja gra, oczekiwanie...)")) {
                    String gameId = gameDisplayToIdMap.get(selectedItem);
                    if (gameId != null && klientSieciowy != null) {
                        klientSieciowy.joinGame(gameId);
                    }
                }
            }
        });
    }
    private void updateLeaderboard(List<String> data) {
        Platform.runLater(() -> {
            leaderboardData.clear();
            if (data.isEmpty() || (data.size() == 1 && data.get(0).isEmpty())) {
                leaderboardData.add("Ranking jest pusty.");
                return;
            }
            int rank = 1;
            for (String item : data) {
                String[] parts = item.split(",");
                if (parts.length < 2) continue;
                leaderboardData.add(String.format("%d. %-20s ELO: %s", rank++, parts[0], parts[1]));
                // Aktualizuj ELO zalogowanego gracza w zakładce "Konto"
                if(klientSieciowy != null && klientSieciowy.getCurrentUser() != null && klientSieciowy.getCurrentUser().getLogin().equals(parts[0])){
                    eloLabel.setText("Twoje ELO: " + parts[1]);
                }
            }
        });
    }

    private void updateHistory(List<String> data) {
        Platform.runLater(() -> {
            historyData.clear();
            if (data.isEmpty() || (data.size() == 1 && data.get(0).isEmpty())) {
                historyData.add("Brak historii gier.");
                return;
            }

            // Ta pętla teraz będzie działać poprawnie
            for (String item : data) {
                // Każdy "item" to teraz kompletna gra, np. "2025-06-10 20:42;admin;Przegrana"
                String[] parts = item.split(";");
                if (parts.length < 3) continue; // Zabezpieczenie

                // Formatujemy dane do jednej linii
                historyData.add(String.format("%s | vs %-15s | Wynik: %s", parts[0], parts[1], parts[2]));
            }
        });
    }

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
                openGamesData.add(displayName);
                gameDisplayToIdMap.put(displayName, gameId);
            }

            createGameButton.setDisable(amIHosting);
        });
    }

    private void wypelnijDaneKonta() {
        if (klientSieciowy != null && klientSieciowy.getCurrentUser() != null) {
            Uzytkownik zalogowanyUzytkownik = klientSieciowy.getCurrentUser();

            if (loginLabel != null) {
                loginLabel.setText(zalogowanyUzytkownik.getLogin());
            }

            if (dataRejestracjiLabel != null) {
                String rawDate = zalogowanyUzytkownik.getDataRejestracji();
                if (rawDate != null && !rawDate.equals("Brak danych")) {
                    // Usuwamy część z milisekundami (.0) dla lepszego wyglądu
                    if (rawDate.endsWith(".0")) {
                        rawDate = rawDate.substring(0, rawDate.length() - 2);
                    }
                    dataRejestracjiLabel.setText(rawDate);
                } else {
                    dataRejestracjiLabel.setText("Brak danych");
                }
            }
        }
    }
    @FXML
    private void handleLogout() {
        // Powiadom serwer o wylogowaniu
        if (klientSieciowy != null) {
            klientSieciowy.logout();
        }

        // Użyj nawigatora, aby wrócić do ekranu logowania
        // Dokładnie tak, jak jest to robione w LoginViewController po udanym logowaniu
        nawigator.nawigujDo(ViewManager.LOGIN);
    }
}