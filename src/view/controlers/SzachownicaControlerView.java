package view.controlers;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import javafx.scene.paint.Color;
import javafx.util.Duration;
import utils.KolorToCSS;

import java.net.URL;
import java.util.ResourceBundle;

public class SzachownicaControlerView implements Initializable {
    //tutaj dodac kontole pliku fxml
    @FXML
    private GridPane szachownica;
    //referencaj do pol naplanszy zeby bylo latwiej
    private StackPane[][] polaSzachownicy = new StackPane[8][8];

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        szachownica.setGridLinesVisible(true);
        utworzSzachownice();
    }

    private void utworzSzachownice(){
        szachownica.getChildren().clear();

        for(int i=0 ; i<8 ; i++){
            for(int j=0 ; j<8 ; j++){
                StackPane kwadrat = new StackPane();

                kwadrat.setPrefSize(60,60);
                Color currentcolor = ((i+j)%2 == 0)? Color.WHITE : Color.DARKSLATEGREY;
                final String originalStyle = "-fx-background-color: " + KolorToCSS.toWebColor(currentcolor) + ";";

                //uzycie metody statycznie na zamiane koloru na css bo inaczje nie dziala klasa: KolorToCSS  w utils
                kwadrat.setStyle(originalStyle);

                szachownica.add(kwadrat,i,j);
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

                    // handleSquareClick(finalRow, finalCol); // do logiki gry
                });
            }
        }
    }
}
