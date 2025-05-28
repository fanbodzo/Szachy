package view.controlers;


import gui.KontrolerNawigator;
import gui.Nawigator;
import gui.ViewManager;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import javafx.scene.paint.Color;
import javafx.util.Duration;
import model.enums.KolorFigur;
import model.enums.TypFigury;
import model.fabryka.FabrykaFigur;
import model.figury.Figura;
import model.figury.Kon;
import utils.KolorToCSS;

import javafx.scene.control.Label;
import utils.Pozycja;

import javax.print.attribute.standard.MediaSize;
import java.net.URL;
import java.util.ResourceBundle;

public class GraViewController implements Initializable , KontrolerNawigator {
    //caly byt borderPane
    @FXML
    private BorderPane tlo;

    //centralne okno , szachownica
    @FXML
    private GridPane szachownica;

    //panele wokol
    @FXML
    private Pane lewo;
    @FXML
    private Pane prawo;
    @FXML
    private Pane gora;
    @FXML
    private Pane dol;
    @FXML private Button cofnijButton;

    private Nawigator nawigator;
    private StackPane[][] polaSzachownicy = new StackPane[8][8];

    public void setNawigator(Nawigator nawigator) {
        this.nawigator = nawigator;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        utworzSzachownice();
        ustawieniePodstawoweFigur();
    }

    private void utworzSzachownice(){
        szachownica.getChildren().clear();

        for(int i=0 ; i<8 ; i++){
            for(int j=0 ; j<8 ; j++){
                StackPane kwadrat = new StackPane();

                kwadrat.setPrefSize(75,75);
                Color currentcolor = ((i+j)%2 == 0)? Color.WHITE : Color.DARKSLATEGREY;
                final String originalStyle = "-fx-background-color: " + KolorToCSS.toWebColor(currentcolor) + ";";

                //uzycie metody statycznie na zamiane koloru na css bo inaczje nie dziala klasa: KolorToCSS  w utils
                kwadrat.setStyle(originalStyle);

                szachownica.add(kwadrat,j,i);
                polaSzachownicy[i][j] = kwadrat;

                //test dzialania pol
                final int rzad =  i;
                final int kolumna =  j;
                PauseTransition pause = new PauseTransition(Duration.millis(1000));
                kwadrat.setOnMouseClicked(event -> {

                    //to tak dla zabwy zeby zoabczyc jak to dziala , moze sie przydac jak bedizemy klikac na figure
                    // ibedzie pokazywac jakie sa dostepne ruchy dla figury
                    final String highlightedStyle = String.format(
                            "-fx-background-color: %s; -fx-border-color: %s; -fx-border-width: 3px;",
                            KolorToCSS.toWebColor(currentcolor),
                            KolorToCSS.toWebColor(Color.INDIANRED)
                    );

                    System.out.println("Kliknięto pole: [" + rzad + ", " + kolumna + "]");
                    kwadrat.setStyle(highlightedStyle);
                    pause.play();
                    pause.setOnFinished(e ->{
                        kwadrat.setStyle(originalStyle);
                    });

                    //handleSquareClick(finalRow, finalCol); // do logiki gry jak zrobie ta metode
                    // ale to chyab beareidzej b edzie handleSquareLeftClick zeby zoabczyc ruchy figury ale hmm
                    // to chyab bardziej byyloby w jakims do zarzadzaia figurami
                });
            }
        }
        //przeniesc to do osobenj metody narazie zostaje jako test tutaj
        cofnijButton.setOnAction(e -> {
           this.nawigator.nawigujDo(ViewManager.STRONA_GLOWNA);
        });
    }

    //w wersji pelnej tutaj beda jeszcze zdjecia figur , narazie skrot figury bedzie wysiweltany
    public void postawFigure(int rzad, int kolumna, TypFigury typ, KolorFigur kolor){

        StackPane kwadratDocelowy = polaSzachownicy[rzad][kolumna];
        Figura figura = FabrykaFigur.utworzFigure(typ, kolor);

        String nazwaFigury = figura.getSymbol();

        Label figuraLabel = new Label(nazwaFigury);
        figuraLabel.setStyle("-fx-font-weight: bold;");

        if (kolor == KolorFigur.BLACK) {
            // czerowny dla czarnychfigur
            figuraLabel.setStyle("-fx-text-fill: #ff8484; -fx-font-weight: bold;"); // Białe figury - czarny tekst
        } else {
            // niebieski dla bialych figur
            figuraLabel.setStyle("-fx-text-fill: #6e81ea; -fx-font-weight: bold;"); // Czarne figury - biały tekst
        }

        kwadratDocelowy.getChildren().clear();
        kwadratDocelowy.getChildren().add(figuraLabel);

    }
    // narazie wstaiwm labele zeby zoacbzyc czy jest git
    public void ustawieniePodstawoweFigur(){
        postawFigure(0, 0, TypFigury.WIEZA, KolorFigur.BLACK);
        postawFigure(0, 1, TypFigury.GONIEC, KolorFigur.BLACK);
        postawFigure(0, 2, TypFigury.KON, KolorFigur.BLACK);
        postawFigure(0, 3, TypFigury.HETMAN, KolorFigur.BLACK);
        postawFigure(0, 4, TypFigury.KROL, KolorFigur.BLACK);
        postawFigure(0, 5, TypFigury.KON, KolorFigur.BLACK);
        postawFigure(0, 6, TypFigury.GONIEC, KolorFigur.BLACK);
        postawFigure(0, 7, TypFigury.WIEZA, KolorFigur.BLACK);

        postawFigure(7, 0, TypFigury.WIEZA, KolorFigur.WHITE);
        postawFigure(7, 1, TypFigury.GONIEC, KolorFigur.WHITE);
        postawFigure(7, 2, TypFigury.KON, KolorFigur.WHITE);
        postawFigure(7, 3, TypFigury.HETMAN, KolorFigur.WHITE);
        postawFigure(7, 4, TypFigury.KROL, KolorFigur.WHITE);
        postawFigure(7, 5, TypFigury.KON, KolorFigur.WHITE);
        postawFigure(7, 6, TypFigury.GONIEC, KolorFigur.WHITE);
        postawFigure(7, 7, TypFigury.WIEZA, KolorFigur.WHITE);

        for(int i=0; i<8; i++){
            postawFigure(1, i, TypFigury.PION, KolorFigur.BLACK);
            postawFigure(6, i, TypFigury.PION, KolorFigur.WHITE);
        }
    }
}
