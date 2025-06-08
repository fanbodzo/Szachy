package gui;

import Klient.KlientSieciowy;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import view.controlers.LoginViewController;

import gui.KontrolerKlienta;

import java.io.IOException;


public class Nawigator {
    private final Stage stage;
    private final String cssUrl = "/resources/styles/gra.css";
    private final KlientSieciowy klientSieciowy;

    public Nawigator(Stage stage, KlientSieciowy klient) {
        this.stage = stage;
        this.klientSieciowy = klient;
    }

    public <T> T nawigujDo(ViewManager viewManager) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(viewManager.getFxmlFile()));
            Parent root = loader.load();

            T controller = loader.getController();
            //wstyrzkneicei nawigatora
            if (controller instanceof KontrolerNawigator) {
                ((KontrolerNawigator) controller).setNawigator(this);
                // przekazywanie this zeby moc wstrzykac nawitagora w innych klasach
            }
            //wstrzykniecie klientaSieciowego
            if (controller instanceof KontrolerKlienta) {
                ((KontrolerKlienta) controller).setKlientSieciowy(this.klientSieciowy);
                System.out.println("[Nawigator] KlientSieciowy wstrzyknięty do kontrolera: " + controller.getClass().getSimpleName());
            } else {
                // To nie jest blad po prostu informacja, że kontroler nie potrzebuje klienta sieciowego
                System.out.println("[Nawigator] Kontroler " + controller.getClass().getSimpleName() + " nie implementuje KontrolerKlienta.");
            }

            Scene scene = stage.getScene();
            if (scene == null) {
                scene = new Scene(root);
                stage.setScene(scene);
            } else {
                scene.setRoot(root);
            }
            stage.getScene().getStylesheets().add(cssUrl);
            stage.setTitle(nazwaOkna(viewManager));
            stage.centerOnScreen();

            if (!stage.isShowing()) {
                stage.show();
            }
            return controller;
        } catch (IOException e) {

            e.printStackTrace();
            return null;
        }

    }


    //to mozna zamienic w enum zeby dac po przecinku druga wartosc jako nazwe okna i nie zasmiecac tak kodu
    public String nazwaOkna(ViewManager viewManager) {
        switch (viewManager) {
            case LOGIN:
                return "Logowanie - Szachy";
            case GRA:
                return "Gra - Szachy";
            case STRONA_GLOWNA:
                return "Strona Glowna - Szachy";
            default:
                return "Szachy";
        }
    }
}
