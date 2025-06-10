package view.controlers;

import Klient.KlientSieciowy;
import gui.KontrolerDanychGry;
import gui.KontrolerKlienta;
import gui.KontrolerNawigator;
import gui.Nawigator;
import gui.ViewManager;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import model.enums.KolorFigur;
import model.figury.Figura;
import model.rdzen.Plansza;
import utils.KolorToCSS;
import utils.Pozycja;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class GraViewController implements Initializable, KontrolerNawigator, KontrolerDanychGry, KontrolerKlienta {
    @FXML private BorderPane tlo;
    @FXML private GridPane szachownica;
    @FXML private Pane lewo, prawo;
    @FXML private VBox gora;
    @FXML private Pane dol;
    @FXML private Label opponentInfoLabel;
    @FXML private Button cofnijButton;

    private Nawigator nawigator;
    private KlientSieciowy klientSieciowy;
    private Plansza plansza;
    private KolorFigur mojKolor;
    private KolorFigur kogoTura;
    private Figura zaznaczonaFigura;
    private List<Pozycja> dostepneRuchy;
    private boolean graZakonczona = false;

    private final StackPane[][] polaSzachownicy = new StackPane[Plansza.ROZMIAR_PLANSZY][Plansza.ROZMIAR_PLANSZY];
    private final Region[][] ramkiPodswietlenia = new Region[Plansza.ROZMIAR_PLANSZY][Plansza.ROZMIAR_PLANSZY];

    @Override
    public void setKlientSieciowy(KlientSieciowy klient) {
        this.klientSieciowy = klient;
        if (this.klientSieciowy != null) {
            this.klientSieciowy.setOnBoardUpdate(this::aktualizujPlanszeZSerwera);
            this.klientSieciowy.setOnGameOver(this::obsluzKoniecGry);
        }
    }

    @Override
    public void setNawigator(Nawigator nawigator) {
        this.nawigator = nawigator;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        utworzSzachowniceGUI();
        ustawResponsywnoscSzachownicy();
        cofnijButton.setOnAction(e -> {
            if (this.nawigator != null) {
                this.nawigator.nawigujDo(ViewManager.STRONA_GLOWNA);
            }
        });
    }

    @Override
    public void przekazDaneGry(Object... dane) {
        if (dane.length == 2 && dane[0] instanceof String && dane[1] instanceof KolorFigur) {
            String opponentLogin = (String) dane[0];
            this.mojKolor = (KolorFigur) dane[1];

            Platform.runLater(() -> {
                opponentInfoLabel.setText("Grasz przeciwko: " + opponentLogin + ". Twój kolor: " + this.mojKolor);
                this.plansza = new Plansza();
                this.plansza.ulozenieStandardoweFigur();
                this.kogoTura = KolorFigur.WHITE;
                this.graZakonczona = false;
                odswiezCalaPlansze();
                aktualizujTytulTury();
            });
        }
    }

    private void aktualizujPlanszeZSerwera(String stanPlanszy) {
        Platform.runLater(() -> {
            System.out.println("[GraViewController] Otrzymano aktualizację planszy od serwera.");
            this.plansza.odczytajZeStringa(stanPlanszy);
            this.kogoTura = (this.kogoTura == KolorFigur.WHITE) ? KolorFigur.BLACK : KolorFigur.WHITE;
            System.out.println("[GraViewController] Tura zmieniona na: " + this.kogoTura);
            odznaczWszystko();
            aktualizujTytulTury();
        });
    }

    private void aktualizujTytulTury() {
        if (graZakonczona) {
            tlo.setStyle("-fx-border-color: darkred; -fx-border-width: 4px; -fx-padding: -1;");
            return;
        }

        if (mojKolor == kogoTura) {
            System.out.println("[GraViewController] Twoja tura!");
            tlo.setStyle("-fx-border-color: limegreen; -fx-border-width: 4px; -fx-padding: -1;");
        } else {
            System.out.println("[GraViewController] Tura przeciwnika.");
            tlo.setStyle("-fx-border-color: transparent; -fx-border-width: 4px; -fx-padding: -1;");
        }
    }

    private void kliknieciePola(Pozycja kliknietePole) {
        if (graZakonczona) {
            System.out.println("[GraViewController] Kliknięcie zignorowane: gra zakończona.");
            return;
        }
        if (mojKolor != kogoTura) {
            System.out.println("[GraViewController] Kliknięcie zignorowane: nie twoja tura. Twoja: " + mojKolor + ", Oczekiwana: " + kogoTura);
            return;
        }

        Figura figuraNaPolu = plansza.getFigura(kliknietePole);

        if (zaznaczonaFigura != null && dostepneRuchy != null && dostepneRuchy.contains(kliknietePole)) {
            System.out.println("[GraViewController] Wykonuję ruch: z " + zaznaczonaFigura.getPozycja() + " do " + kliknietePole);
            klientSieciowy.sendMove(zaznaczonaFigura.getPozycja(), kliknietePole);
            odznaczWszystko();
        } else if (figuraNaPolu != null && figuraNaPolu.getKolorFigur() == mojKolor) {
            System.out.println("[GraViewController] Zaznaczam figurę: " + figuraNaPolu.getTypFigury() + " na " + kliknietePole);
            zaznaczFigure(figuraNaPolu);
        } else {
            System.out.println("[GraViewController] Odznaczam wszystko.");
            odznaczWszystko();
        }
    }

    private void obsluzKoniecGry(String wynikPayload) {
        Platform.runLater(() -> {
            graZakonczona = true;
            aktualizujTytulTury(); // Zmień ramkę na czerwoną
            String[] parts = wynikPayload.split(":");
            String tytul = "Koniec Gry";
            String wiadomosc;

            if (parts[0].equals("CHECKMATE")) {
                String ktoWygral = parts[1];
                tytul = "Szach-Mat!";
                wiadomosc = "Wygrywa gracz: " + ktoWygral;
            } else if (parts[0].equals("STALEMATE")) {
                tytul = "Pat!";
                wiadomosc = "Gra zakończona remisem.";
            } else {
                wiadomosc = "Gra zakończona z nieznanego powodu.";
            }

            // Najpierw pokaż okno z wynikiem
            pokazAlert(tytul, wiadomosc);

            // ---> DODAJ TĘ LINIĘ <---
            // Po zamknięciu okna, automatycznie wróć do menu
            nawigator.nawigujDo(ViewManager.STRONA_GLOWNA);
        });
    }

    private void pokazAlert(String tytul, String wiadomosc) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Koniec Gry");
        alert.setHeaderText(tytul);
        alert.setContentText(wiadomosc);
        alert.showAndWait();
    }

    private void zaznaczFigure(Figura figura) {
        odznaczWszystko();
        this.zaznaczonaFigura = figura;
        this.dostepneRuchy = plansza.getWszystkieLegalneRuchyDlaFigury(figura.getPozycja());

        if (this.dostepneRuchy != null && !this.dostepneRuchy.isEmpty()) {
            podswietlPole(figura.getPozycja(), Color.LIMEGREEN, "solid");
            for (Pozycja p : this.dostepneRuchy) {
                podswietlDostepnyRuch(p);
            }
        }
    }

    private void odznaczWszystko() {
        this.zaznaczonaFigura = null;
        if (this.dostepneRuchy != null) {
            this.dostepneRuchy.clear();
        }
        odswiezCalaPlansze();
    }

    private void odswiezCalaPlansze() {
        if (plansza == null) return;
        for (int r = 0; r < Plansza.ROZMIAR_PLANSZY; r++) {
            for (int k = 0; k < Plansza.ROZMIAR_PLANSZY; k++) {
                StackPane poleGUI = polaSzachownicy[r][k];
                if (poleGUI == null) continue;
                poleGUI.getChildren().clear();
                usunRamke(r, k);
                Color currentcolor = ((r + k) % 2 == 0) ? Color.web("#F0D9B5") : Color.web("#B58863");
                poleGUI.setStyle("-fx-background-color: " + KolorToCSS.toWebColor(currentcolor) + ";");
                Figura f = plansza.getFigura(new Pozycja(r, k));
                if (f != null) {
                    rysujSymbolFigury(poleGUI, f);
                }
            }
        }
    }

    private void utworzSzachowniceGUI() {
        szachownica.getChildren().clear();
        for (int i = 0; i < Plansza.ROZMIAR_PLANSZY; i++) {
            for (int j = 0; j < Plansza.ROZMIAR_PLANSZY; j++) {
                StackPane kwadrat = new StackPane();
                szachownica.add(kwadrat, j, i);
                polaSzachownicy[i][j] = kwadrat;
                final int rzad = i;
                final int kolumna = j;
                kwadrat.setOnMouseClicked(event -> kliknieciePola(new Pozycja(rzad, kolumna)));
            }
        }
    }

    private void ustawResponsywnoscSzachownicy() {
        Platform.runLater(this::przeliczRozmiarSzachownicy);
        if (tlo != null) {
            tlo.widthProperty().addListener((obs, oldVal, newVal) -> przeliczRozmiarSzachownicy());
            tlo.heightProperty().addListener((obs, oldVal, newVal) -> przeliczRozmiarSzachownicy());
        }
    }

    private void przeliczRozmiarSzachownicy() {
        if (tlo == null || lewo == null || prawo == null || gora == null || dol == null || szachownica == null) return;
        double szerokoscOkna = tlo.getWidth();
        double wysokoscOkna = tlo.getHeight();
        double dostepnaSzerokosc = szerokoscOkna - lewo.getWidth() - prawo.getWidth();
        double dostepnaWysokosc = wysokoscOkna - gora.getHeight() - dol.getHeight();
        double rozmiar = Math.min(dostepnaSzerokosc, dostepnaWysokosc);
        if (rozmiar > 0) {
            szachownica.setPrefSize(rozmiar, rozmiar);
            szachownica.setMaxSize(rozmiar, rozmiar);
        }
    }

    private void usunRamke(int r, int k) {
        if (ramkiPodswietlenia[r][k] != null) {
            polaSzachownicy[r][k].getChildren().remove(ramkiPodswietlenia[r][k]);
            ramkiPodswietlenia[r][k] = null;
        }
    }

    private void podswietlPole(Pozycja p, Color kolorRamki, String stylRamki) {
        if (p != null && plansza.isValidPosition(p)) {
            int r = p.getRzad();
            int k = p.getKolumna();
            usunRamke(r, k);
            Region ramka = new Region();
            ramka.setStyle("-fx-border-color: " + KolorToCSS.toWebColor(kolorRamki) + ";" +
                    "-fx-border-width: 4px;" +
                    "-fx-border-style: " + stylRamki + ";");
            ramka.setMouseTransparent(true);
            polaSzachownicy[r][k].getChildren().add(ramka);
            ramkiPodswietlenia[r][k] = ramka;
        }
    }

    private void podswietlDostepnyRuch(Pozycja p) {
        if (p != null && plansza.isValidPosition(p)) {
            StackPane poleGUI = polaSzachownicy[p.getRzad()][p.getKolumna()];
            if (plansza.getFigura(p) != null) {
                podswietlPole(p, Color.DARKRED, "dashed");
            } else {
                Label kropka = new Label("●");
                kropka.styleProperty().bind(Bindings.createStringBinding(() -> {
                    double size = Math.min(poleGUI.getWidth(), poleGUI.getHeight());
                    return "-fx-font-size: " + (size * 0.4) + "px; -fx-text-fill: rgba(0,0,0,0.4);";
                }, poleGUI.widthProperty(), poleGUI.heightProperty()));
                kropka.setMouseTransparent(true);
                poleGUI.getChildren().add(kropka);
            }
        }
    }

    private void rysujSymbolFigury(StackPane pole, Figura figura) {
        String symbolUnicode;
        switch (figura.getTypFigury()) {
            case KROL:   symbolUnicode = (figura.getKolorFigur() == KolorFigur.WHITE) ? "♔" : "♚"; break;
            case HETMAN: symbolUnicode = (figura.getKolorFigur() == KolorFigur.WHITE) ? "♕" : "♛"; break;
            case WIEZA:  symbolUnicode = (figura.getKolorFigur() == KolorFigur.WHITE) ? "♖" : "♜"; break;
            case GONIEC: symbolUnicode = (figura.getKolorFigur() == KolorFigur.WHITE) ? "♗" : "♝"; break;
            case KON:    symbolUnicode = (figura.getKolorFigur() == KolorFigur.WHITE) ? "♘" : "♞"; break;
            case PION:   symbolUnicode = (figura.getKolorFigur() == KolorFigur.WHITE) ? "♙" : "♟"; break;
            default:     symbolUnicode = "";
        }
        Label figuraLabel = new Label(symbolUnicode);
        figuraLabel.setMouseTransparent(true);
        figuraLabel.styleProperty().bind(Bindings.createStringBinding(() -> {
            double size = Math.min(pole.getWidth(), pole.getHeight());
            return "-fx-font-size: " + (size * 0.75) + "px;";
        }, pole.widthProperty(), pole.heightProperty()));
        pole.getChildren().add(figuraLabel);
    }
}