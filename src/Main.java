import model.rdzen.Plansza;

public class Main {
    /*
        Projekt gry w szachy , z obsluga wielu graczy oraz wielowatkowoscia
     */
    public static void main(String[] args) {
        Plansza szachownica = new Plansza();
        szachownica.ulozenieStandardoweFigur();
        szachownica.wyswietlaniePlanszy();
    }
}