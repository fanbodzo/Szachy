import javafx.fxml.Initializable;
import model.rdzen.Plansza;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class Main extends Application {
    /*
        Projekt gry w szachy , z obsluga wielu graczy oraz wielowatkowoscia
     */

    @Override
    public void start(Stage primaryStage) throws Exception {
        // ladowanie fxml
        FXMLLoader loader = new FXMLLoader(getClass().getResource("resources/szachownicaView.fxml"));
        GridPane gridPane = loader.load();

        // twrozenie sceny , trzeba bedzie to dac do osbnej klasy do handlowania tego jak bedziuemy zmieniac sceny
        Scene scene = new Scene(gridPane);
        primaryStage.setTitle("Szachownica");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        Plansza szachownica = new Plansza();
        szachownica.ulozenieStandardoweFigur();
        szachownica.wyswietlaniePlanszy();

        launch(args);
    }
}