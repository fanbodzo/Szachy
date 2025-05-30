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

    private Pozycja pozycjaZaznaczonejFigury = null;
    private Figura aktualnieZaznaczonaFigura = null;
    //tablica ktora przechowuje obiekt pole z indkesmi odpowiadjacami szachownicy
    //nmo czyli kolor figury typ itd i jakie pole
    private Figura[][] figuryNaPolachLogic = new Figura[8][8];

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
                    kliknieciePola(rzad,kolumna);

                });
            }
        }
        //TODO przeniesc to do osobenj metody jak bedzie wieecej przyciskow
        cofnijButton.setOnAction(e -> {
           this.nawigator.nawigujDo(ViewManager.STRONA_GLOWNA);
        });
    }
    public void rysujFigure(int row , int col , Figura figuraDoNarysowania){
        StackPane kwadratDocelowy = polaSzachownicy[row][col];
        kwadratDocelowy.getChildren().clear();
        if (figuraDoNarysowania != null) {
            String nazwaFigury = figuraDoNarysowania.getSymbol();
            Label figuraLabel = new Label(nazwaFigury);

            // Twoje style dla Labela
            if (figuraDoNarysowania.getKolorFigur() == KolorFigur.BLACK) {
                figuraLabel.setStyle("-fx-text-fill: #ff8484; -fx-font-size: 24px; -fx-font-weight: bold;");
            } else {
                figuraLabel.setStyle("-fx-text-fill: #6e81ea; -fx-font-size: 24px; -fx-font-weight: bold;");
            }
            figuraLabel.setMouseTransparent(true);
            kwadratDocelowy.getChildren().add(figuraLabel);
        }

    }

    //zmienilem metode ale nie wiem czy tutaj bede zdjecia dodawa chyba tak zoabcze
    public void postawFigure(int rzad, int kolumna, TypFigury typ, KolorFigur kolor){

        Figura nowaFigura = FabrykaFigur.utworzFigure(typ, kolor);
        //wpisanie do tablicy
        figuryNaPolachLogic[rzad][kolumna] = nowaFigura;
        nowaFigura.setPozycja(new Pozycja(rzad, kolumna));
        rysujFigure(rzad, kolumna, nowaFigura);
    }

    // narazie wstaiwm labele zeby zoacbzyc czy jest git
    public void ustawieniePodstawoweFigur(){

        postawFigure(0, 0, TypFigury.WIEZA, KolorFigur.BLACK);
        postawFigure(0, 1, TypFigury.KON, KolorFigur.BLACK);
        postawFigure(0, 2, TypFigury.GONIEC, KolorFigur.BLACK);
        postawFigure(0, 3, TypFigury.HETMAN, KolorFigur.BLACK);
        postawFigure(0, 4, TypFigury.KROL, KolorFigur.BLACK);
        postawFigure(0, 5, TypFigury.GONIEC, KolorFigur.BLACK);
        postawFigure(0, 6, TypFigury.KON, KolorFigur.BLACK);
        postawFigure(0, 7, TypFigury.WIEZA, KolorFigur.BLACK);

        postawFigure(7, 0, TypFigury.WIEZA, KolorFigur.WHITE);
        postawFigure(7, 1, TypFigury.KON, KolorFigur.WHITE);
        postawFigure(7, 2, TypFigury.GONIEC, KolorFigur.WHITE);
        postawFigure(7, 3, TypFigury.HETMAN, KolorFigur.WHITE);
        postawFigure(7, 4, TypFigury.KROL, KolorFigur.WHITE);
        postawFigure(7, 5, TypFigury.GONIEC, KolorFigur.WHITE);
        postawFigure(7, 6, TypFigury.KON, KolorFigur.WHITE);
        postawFigure(7, 7, TypFigury.WIEZA, KolorFigur.WHITE);

        for(int i=0; i<8; i++){
            postawFigure(1, i, TypFigury.PION, KolorFigur.BLACK);
            postawFigure(6, i, TypFigury.PION, KolorFigur.WHITE);
        }
    }

    private void kliknieciePola(int row , int col){
        Pozycja kliknietePole = new Pozycja(row,col);
        System.out.println(kliknietePole);

        if(aktualnieZaznaczonaFigura == null){
            Figura figuraKliknieta = figuryNaPolachLogic[row][col];
            if(figuraKliknieta != null){
                aktualnieZaznaczonaFigura = figuraKliknieta;
                pozycjaZaznaczonejFigury = kliknietePole;
                //info co sie dzieje bedzie mozna wykorzystac pozniej do pisania logow
                System.out.println("Zaznaczona figura: " + aktualnieZaznaczonaFigura.getSymbol() + " na pozycji " + pozycjaZaznaczonejFigury);
            }else{
                System.out.println("klikneto puste pole");
            }

        }else{
            //odznaczanie jezeli kliknelismy jeszcze raz to samo majac juz zazanczone cos
            if (kliknietePole.equals(pozycjaZaznaczonejFigury)) {
                System.out.println("Odznaczono figurę: " + aktualnieZaznaczonaFigura.getSymbol());
                aktualnieZaznaczonaFigura = null;
                pozycjaZaznaczonejFigury = null;
            }else{
                //TODO tutaj trzeba dodac walidacje ruchu ale to pozniej

                figuryNaPolachLogic[pozycjaZaznaczonejFigury.getRzad()][pozycjaZaznaczonejFigury.getKolumna()] = null;
                //czysczenie pola dlatego null
                rysujFigure(pozycjaZaznaczonejFigury.getRzad(), pozycjaZaznaczonejFigury.getKolumna(), null);

                //bicie bez walidacji
                Figura figuraNaDocelowymPolu = figuryNaPolachLogic[row][col];
                if(figuraNaDocelowymPolu != null){
                    System.out.println("Figura " + figuraNaDocelowymPolu.getSymbol() + " zostala zbita");
                }
                figuryNaPolachLogic[row][col] = aktualnieZaznaczonaFigura;
                aktualnieZaznaczonaFigura.setPozycja(kliknietePole);

                rysujFigure(row, col, aktualnieZaznaczonaFigura);

                System.out.println("Przesunieto " + aktualnieZaznaczonaFigura.getSymbol() + " na " + kliknietePole);


                aktualnieZaznaczonaFigura = null;
                pozycjaZaznaczonejFigury = null;
            }
        }
    }
}
