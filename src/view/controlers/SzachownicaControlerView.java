package view.controlers;

import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;

public class SzachownicaControlerView {
    //tutaj dodac kontole pliku fxml
    @FXML
    private GridPane szachownica;

    @FXML
    public void initialize() {
        szachownica.setGridLinesVisible(true);
    }
}
