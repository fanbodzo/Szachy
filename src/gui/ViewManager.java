package gui;

public enum ViewManager {
    LOGIN("/resources/loginView.fxml"),
    REJESTRACJA("/resources/registrationView.fxml"),
    STRONA_GLOWNA("/resources/glownaStronaView.fxml"),
    GRA("/resources/graView.fxml");

    private final String fxmlFile;

    ViewManager(String fxmlFile) {
        this.fxmlFile = fxmlFile;
    }

    public String getFxmlFile() {
        return fxmlFile;
    }
}
