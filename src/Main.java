import Klient.KlientSieciowy;
import gui.Nawigator;
import gui.ViewManager;
import model.rdzen.Plansza;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    /*
        Projekt gry w szachy , z obsluga wielu graczy oraz wielowatkowoscia
     */
    private Nawigator nawigator;
    private KlientSieciowy klientSieciowy;

    @Override
    public void init() throws Exception {
        super.init();
        this.klientSieciowy = new KlientSieciowy();
        System.out.println("[Main] KlientSieciowy zainicjalizowany.");
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.nawigator = new Nawigator(primaryStage, this.klientSieciowy);
        //nawiguje do loginu
        nawigator.nawigujDo(ViewManager.LOGIN);

    }

    @Override
    public void stop() throws Exception {
        System.out.println("[Main] Aplikacja jest zamykana. Rozłączanie klienta sieciowego...");
        if (klientSieciowy != null) {
            klientSieciowy.disconnect();
        }
        super.stop();
        System.out.println("[Main] Klient sieciowy rozłączony. Aplikacja zamknięta.");
    }

    public static void main(String[] args) {
        Plansza szachownica = new Plansza();
        szachownica.ulozenieStandardoweFigur();
        //szachownica.wyswietlaniePlanszy();

        launch(args);
    }
}