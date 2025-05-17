import gui.Nawigator;
import gui.ViewManager;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
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
    private Nawigator nawigator;
    @Override
    public void start(Stage primaryStage) throws Exception {
        this.nawigator = new Nawigator(primaryStage);
        //nawiguje do loginu
        nawigator.nawigujDo(ViewManager.LOGIN);


        // ladowanie fxml
        //FXMLLoader loader = new FXMLLoader(getClass().getResource("resources/szachownicaView.fxml"));
//        FXMLLoader loader = new FXMLLoader(getClass().getResource("resources/graView.fxml"));
//        FXMLLoader loaderLogin = new FXMLLoader(getClass().getResource("resources/loginView.fxml"));
//        //GridPane gridPane = loader.load();
//        BorderPane rootLayout = loader.load();
//        Pane loginPane = loaderLogin.load();


        // twrozenie sceny , trzeba bedzie to dac do osbnej klasy do handlowania tego jak bedziuemy zmieniac sceny
        //Scene scene = new Scene(gridPane);

        //wlaczac zaleznie od podgladu
//        //Scene scene = new Scene(rootLayout);
//        Scene scene = new Scene(loginPane);
//        //ladownie pliku css
//        scene.getStylesheets().add(getClass().getResource("resources/styles/gra.css").toExternalForm());
//        primaryStage.setTitle("Szachy");
//        primaryStage.setScene(scene);
//        primaryStage.show();
    }

    public static void main(String[] args) {
        Plansza szachownica = new Plansza();
        szachownica.ulozenieStandardoweFigur();
        szachownica.wyswietlaniePlanszy();

        launch(args);
    }
}