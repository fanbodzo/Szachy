
package gui;

import Klient.KlientSieciowy;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class Nawigator {
    private final Stage stage;
    private final String cssUrl = "/resources/styles/gra.css";
    private final KlientSieciowy klientSieciowy;

    public Nawigator(Stage stage, KlientSieciowy klient) {
        this.stage = stage;
        this.klientSieciowy = klient;
    }

    public <T> T nawigujDo(ViewManager viewManager, Object... params) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(viewManager.getFxmlFile()));
            Parent root = loader.load();

            T controller = loader.getController();

            if (controller instanceof KontrolerNawigator) {
                ((KontrolerNawigator) controller).setNawigator(this);
            }

            if (controller instanceof KontrolerKlienta) {
                ((KontrolerKlienta) controller).setKlientSieciowy(this.klientSieciowy);
                System.out.println("[Nawigator] KlientSieciowy wstrzyknięty do kontrolera: " + controller.getClass().getSimpleName());
            }

            if (params.length > 0 && controller instanceof KontrolerDanychGry) {
                ((KontrolerDanychGry) controller).przekazDaneGry(params);
                System.out.println("[Nawigator] Przekazano dane do kontrolera: " + controller.getClass().getSimpleName());
            }

            Scene scene = stage.getScene();
            if (scene == null) {
                scene = new Scene(root);
                stage.setScene(scene);
            } else {
                scene.setRoot(root);
            }

            if (!stage.getScene().getStylesheets().contains(cssUrl)) {
                stage.getScene().getStylesheets().add(cssUrl);
            }
            stage.setTitle(nazwaOkna(viewManager));
            stage.centerOnScreen();

            if (!stage.isShowing()) {
                stage.show();
            }
            return controller;
        } catch (Exception e) { // Zmieniamy na ogólny Exception, aby łapać WSZYSTKO
            System.err.println("KRYTYCZNY BŁĄD NAWIGACJI: Nie udało się załadować widoku " + viewManager.name());
            e.printStackTrace();
            // Pokaż okno błędu użytkownikowi
            pokazBladKrytyczny("Błąd ładowania widoku",
                    "Wystąpił nieoczekiwany błąd podczas próby otwarcia okna: " + viewManager.name(), e);
            return null;
        }
    }

    public <T> T nawigujDo(ViewManager viewManager) {
        return nawigujDo(viewManager, new Object[]{});
    }

    public String nazwaOkna(ViewManager viewManager) {
        switch (viewManager) {
            case LOGIN:
                return "Logowanie - Szachy";
            case GRA:
                return "Gra - Szachy";
            case STRONA_GLOWNA:
                return "Strona Główna - Szachy";
            default:
                return "Szachy";
        }
    }

    // Metoda pomocnicza do wyświetlania błędów
    private void pokazBladKrytyczny(String title, String header, Exception ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(ex.getMessage());

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("Szczegóły błędu:");
        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        alert.getDialogPane().setExpandableContent(expContent);
        alert.showAndWait();
    }
}