import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
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
        //FXMLLoader loader = new FXMLLoader(getClass().getResource("resources/szachownicaView.fxml"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("resources/graView.fxml"));
        //GridPane gridPane = loader.load();
        BorderPane rootLayout = loader.load();

        // twrozenie sceny , trzeba bedzie to dac do osbnej klasy do handlowania tego jak bedziuemy zmieniac sceny
        //Scene scene = new Scene(gridPane);

        Scene scene = new Scene(rootLayout);
        primaryStage.setTitle("Szachy");
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