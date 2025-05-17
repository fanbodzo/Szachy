package gui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import view.controlers.LoginViewController;

import java.io.IOException;

import static gui.ViewManager.LOGIN;

public class Nawigator {
    private final Stage stage;
    private final String globalCssPath = "/styles/gra.css";
    private final String cssUrl = "/resources/styles/gra.css";

    public Nawigator(Stage stage) {
        this.stage = stage;
    }

    public <T> T nawigujDo(ViewManager viewManager) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(viewManager.getFxmlFile()));
            Parent root = loader.load();

            T controller = loader.getController();
            if (controller instanceof LoginViewController) {
                ((LoginViewController) controller).setNawigator(this);
                // przekazywanie this zeby moc wstrzykac nawitagora w innych klasach
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

    public String nazwaOkna(ViewManager viewManager) {
        switch (viewManager) {
            case LOGIN:
                return "Logowanie - Szachy";
            case GRA:
                return "Gra - Szachy";
            default:
                return "szachy";
        }
    }
}
