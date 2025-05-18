package view.controlers;

import gui.KontrolerNawigator;
import gui.Nawigator;
import gui.ViewManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginViewController implements Initializable , KontrolerNawigator {
    @FXML
    private Pane bg;
    @FXML
    private TextField username;
    @FXML
    private PasswordField password;
    @FXML
    private ImageView ikona;
    @FXML
    private Button loginButton;

    private Nawigator nawigator;

    public void setNawigator(Nawigator nawigator) {
        this.nawigator = nawigator;
    }
    public void initialize(URL location, ResourceBundle resources) {
        login();
    }

    private void login() {

        loginButton.setOnMouseClicked(event -> {
            //pozniej dodac w nawigatorze kolejna dana jaka obiekt zeby user byl obiektem
            // uwzam ze to moze byc przydatne przekazanie usera np do wyswietlania kto z kim gra
            this.nawigator.nawigujDo(ViewManager.STRONA_GLOWNA);

        });
    }
}
