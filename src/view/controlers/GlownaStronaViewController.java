package view.controlers;

import gui.KontrolerNawigator;
import gui.Nawigator;
import gui.ViewManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.net.URL;
import java.util.ResourceBundle;

public class GlownaStronaViewController implements Initializable , KontrolerNawigator {
    @FXML private TabPane bg;
    @FXML private Tab stronaGlowna;
    @FXML private Tab konto;
    @FXML private Button grajButton;

    private Nawigator nawigator;

    public void setNawigator(Nawigator nawigator) {
        this.nawigator = nawigator;
    }

    public void initialize(URL location, ResourceBundle resources){
        test();
    }
    private void test(){
        grajButton.setOnMouseClicked(event -> {
            this.nawigator.nawigujDo(ViewManager.GRA);
        });
    }
}
