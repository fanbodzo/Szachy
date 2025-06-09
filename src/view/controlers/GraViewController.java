package view.controlers;

import gui.KontrolerNawigator;
import gui.Nawigator;
import gui.ViewManager;
import gui.KontrolerDanychGry; // NOWY IMPORT
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label; // NOWY IMPORT
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

public class GraViewController implements Initializable, KontrolerNawigator, KontrolerDanychGry {

    @FXML private BorderPane tlo;
    @FXML private GridPane szachownica;
    @FXML private Pane lewo, prawo, gora, dol;
    @FXML private Button cofnijButton;
    @FXML private Label opponentInfoLabel; // NOWOŚĆ: Etykieta na info o przeciwniku

    private Nawigator nawigator;
    private Plansza plansza;
    private KolorFigur aktualnyGracz;
    private Figura zaznaczonaFigura;
    private List<Pozycja> dostepneRuchy;
    private boolean graZakonczona = false;
    private String opponentLogin; // NOWOŚĆ: Pole na login przeciwnika

    private final StackPane[][] polaSzachownicy = new StackPane[Plansza.ROZMIAR_PLANSZY][Plansza.ROZMIAR_PLANSZY];
    private final Region[][] ramkiPodswietlenia = new Region[Plansza.ROZMIAR_PLANSZY][Plansza.ROZMIAR_PLANSZY];

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        utworzSzachowniceGUI();
        ustawResponsywnoscSzachownicy();
        nowaGra();

        cofnijButton.setOnAction(e -> {
            if (this.nawigator != null) {
                // TODO: Dodać logikę informowania serwera o przerwaniu gry
                this.nawigator.nawigujDo(ViewManager.STRONA_GLOWNA);
            }
        });
    }

    // NOWOŚĆ: Implementacja metody z interfejsu KontrolerDanychGry
    @Override
    public void przekazDaneGry(Object... dane) {
        if (dane.length > 0 && dane[0] instanceof String) {
            this.opponentLogin = (String) dane[0];
            System.out.println("[GraViewController] Odebrano dane. Rozpoczynam grę z: " + this.opponentLogin);

            // Aktualizujemy etykietę w wątku JavaFX, aby uniknąć błędów
            Platform.runLater(() -> {
                if (opponentInfoLabel != null) {
                    opponentInfoLabel.setText("Grasz przeciwko: " + this.opponentLogin);
                }
            });
        }
    }

    // Reszta klasy (bez zmian)

    @Override
    public void setNawigator(Nawigator nawigator) { this.nawigator = nawigator; }

    private void ustawResponsywnoscSzachownicy() {
        Platform.runLater(this::przeliczRozmiarSzachownicy);
        tlo.widthProperty().addListener((obs, oldVal, newVal) -> przeliczRozmiarSzachownicy());
        tlo.heightProperty().addListener((obs, oldVal, newVal) -> przeliczRozmiarSzachownicy());
    }

    private void przeliczRozmiarSzachownicy() {
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

    // ... i tak dalej, cała reszta metod z GraViewController jest identyczna jak w twoim kodzie ...
    // (Poniżej wklejam resztę dla kompletności)

    private void odswiezCalaPlansze() {
        for (int r = 0; r < Plansza.ROZMIAR_PLANSZY; r++) {
            for (int k = 0; k < Plansza.ROZMIAR_PLANSZY; k++) {
                StackPane poleGUI = polaSzachownicy[r][k];
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
            if (plansza.getFigura(p) != null) {
                podswietlPole(p, Color.DARKRED, "dashed");
            } else {
                StackPane poleGUI = polaSzachownicy[p.getRzad()][p.getKolumna()];
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

    private void zaznaczFigure(Figura figura) {
        odznaczWszystko();
        this.zaznaczonaFigura = figura;
        List<Pozycja> potencjalneRuchy = figura.getDostepneRuchy(plansza);
        this.dostepneRuchy = new ArrayList<>();
        for (Pozycja cel : potencjalneRuchy) {
            Plansza kopia = new Plansza(plansza);
            kopia.wykonajRuch(figura.getPozycja(), cel);
            if (!kopia.czyKrolJestWszachu(aktualnyGracz)) {
                this.dostepneRuchy.add(cel);
            }
        }
        if (!this.dostepneRuchy.isEmpty()) {
            podswietlPole(figura.getPozycja(), Color.LIMEGREEN, "solid");
            for (Pozycja p : this.dostepneRuchy) {
                podswietlDostepnyRuch(p);
            }
        }
    }

    private void sprawdzStanGry() {
        boolean czySzach = plansza.czyKrolJestWszachu(aktualnyGracz);
        if (czySzach) {
            podswietlPole(plansza.znajdzKrola(aktualnyGracz), Color.RED, "solid");
        }
        List<Pozycja[]> wszystkieRuchy = plansza.getWszystkieLegalneRuchy(aktualnyGracz);
        if (wszystkieRuchy.isEmpty()) {
            graZakonczona = true;
            if (czySzach) {
                pokazAlert("Szach-Mat!", "Wygrywa " + ((aktualnyGracz == KolorFigur.WHITE) ? "CZARNY" : "BIAŁY"));
            } else {
                pokazAlert("Pat!", "Gra zakończona remisem.");
            }
        }
    }

    private void odznaczWszystko() {
        this.zaznaczonaFigura = null;
        if (this.dostepneRuchy != null) {
            this.dostepneRuchy.clear();
        }
        odswiezCalaPlansze();
        if (!graZakonczona && plansza.czyKrolJestWszachu(aktualnyGracz)) {
            podswietlPole(plansza.znajdzKrola(aktualnyGracz), Color.RED, "solid");
        }
    }

    private void nowaGra() {
        this.plansza = new Plansza();
        this.plansza.ulozenieStandardoweFigur();
        this.aktualnyGracz = KolorFigur.WHITE;
        this.zaznaczonaFigura = null;
        this.dostepneRuchy = new ArrayList<>();
        this.graZakonczona = false;
        odswiezCalaPlansze();
        System.out.println("Nowa gra. Zaczynają białe.");
    }

    private void rysujSymbolFigury(StackPane pole, Figura figura) {
        String symbolUnicode;
        switch(figura.getTypFigury()) {
            case KROL: symbolUnicode = (figura.getKolorFigur() == KolorFigur.WHITE) ? "♔" : "♚"; break;
            case HETMAN: symbolUnicode = (figura.getKolorFigur() == KolorFigur.WHITE) ? "♕" : "♛"; break;
            case WIEZA: symbolUnicode = (figura.getKolorFigur() == KolorFigur.WHITE) ? "♖" : "♜"; break;
            case GONIEC: symbolUnicode = (figura.getKolorFigur() == KolorFigur.WHITE) ? "♗" : "♝"; break;
            case KON: symbolUnicode = (figura.getKolorFigur() == KolorFigur.WHITE) ? "♘" : "♞"; break;
            case PION: symbolUnicode = (figura.getKolorFigur() == KolorFigur.WHITE) ? "♙" : "♟"; break;
            default: symbolUnicode = "";
        }
        Label figuraLabel = new Label(symbolUnicode);
        figuraLabel.setMouseTransparent(true);
        figuraLabel.styleProperty().bind(Bindings.createStringBinding(() -> {
            double size = Math.min(pole.getWidth(), pole.getHeight());
            return "-fx-font-size: " + (size * 0.7) + "px;";
        }, pole.widthProperty(), pole.heightProperty()));
        pole.getChildren().add(figuraLabel);
    }
    private void kliknieciePola(Pozycja kliknietePole) {
        if (graZakonczona) return;
        Figura figuraNaPolu = plansza.getFigura(kliknietePole);
        if (zaznaczonaFigura != null && dostepneRuchy.contains(kliknietePole)) {
            wykonajRuch(zaznaczonaFigura.getPozycja(), kliknietePole);
        } else if (figuraNaPolu != null && figuraNaPolu.getKolorFigur() == aktualnyGracz) {
            zaznaczFigure(figuraNaPolu);
        } else {
            odznaczWszystko();
        }
    }
    private void wykonajRuch(Pozycja start, Pozycja koniec) {
        plansza.wykonajRuch(start, koniec);
        aktualnyGracz = (aktualnyGracz == KolorFigur.WHITE) ? KolorFigur.BLACK : KolorFigur.WHITE;
        odznaczWszystko();
        sprawdzStanGry();
        System.out.println("Następna tura: " + aktualnyGracz);
    }
    private void pokazAlert(String tytul, String wiadomosc) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Koniec Gry");
        alert.setHeaderText(tytul);
        alert.setContentText(wiadomosc);
        alert.showAndWait();
    }
}