package model.rdzen;

import model.enums.KolorFigur;
import model.enums.TypFigury;
import model.fabryka.FabrykaFigur;
import model.figury.Figura;
import utils.Pozycja;

import java.util.ArrayList;
import java.util.List;

public class Plansza {
    private final Figura[][] kwadraty;
    public static final int ROZMIAR_PLANSZY = 8;

    public Plansza() {
        this.kwadraty = new Figura[ROZMIAR_PLANSZY][ROZMIAR_PLANSZY];
    }

    /**
     * Konstruktor kopiujący. Tworzy głęboką kopię planszy, co jest kluczowe dla symulacji ruchów.
     * @param inna Plansza do skopiowania.
     */
    public Plansza(Plansza inna) {
        this.kwadraty = new Figura[ROZMIAR_PLANSZY][ROZMIAR_PLANSZY];
        for (int r = 0; r < ROZMIAR_PLANSZY; r++) {
            for (int k = 0; k < ROZMIAR_PLANSZY; k++) {
                Pozycja p = new Pozycja(r, k);
                Figura f = inna.getFigura(p);
                if (f != null) {
                    // Tworzymy nową instancję figury, aby uniknąć modyfikacji oryginalnej planszy
                    Figura nowaFigura = FabrykaFigur.utworzFigure(f.getTypFigury(), f.getKolorFigur());
                    nowaFigura.setCzyPierwszyRuch(f.isCzyPierwszyRuch()); // Kopiujemy też stan pierwszego ruchu
                    this.setFigura(p, nowaFigura);
                }
            }
        }
    }

    public boolean isValidPosition(Pozycja pozycja) {
        if (pozycja == null) {
            return false;
        }
        return pozycja.getKolumna() >= 0 && pozycja.getKolumna() < ROZMIAR_PLANSZY &&
                pozycja.getRzad() >= 0 && pozycja.getRzad() < ROZMIAR_PLANSZY;
    }

    public Figura getFigura(Pozycja pozycja) {
        if (!isValidPosition(pozycja)) {
            return null;
        }
        return kwadraty[pozycja.getRzad()][pozycja.getKolumna()];
    }

    public void setFigura(Pozycja pozycja, Figura figura) {
        if (!isValidPosition(pozycja)) {
            return;
        }
        kwadraty[pozycja.getRzad()][pozycja.getKolumna()] = figura;
        if (figura != null) {
            figura.setPozycja(pozycja);
        }
    }

    public void wykonajRuch(Pozycja start, Pozycja koniec) {
        Figura figura = getFigura(start);
        if (figura != null) {
            setFigura(koniec, figura);
            setFigura(start, null);
            figura.setCzyPierwszyRuch(false);
        }
    }

    /**
     * Znajduje pozycję króla danego koloru.
     * @param kolorKrola Kolor króla do znalezienia.
     * @return Pozycja króla lub null, jeśli nie znaleziono.
     */
    public Pozycja znajdzKrola(KolorFigur kolorKrola) {
        for (int r = 0; r < ROZMIAR_PLANSZY; r++) {
            for (int k = 0; k < ROZMIAR_PLANSZY; k++) {
                Figura f = getFigura(new Pozycja(r, k));
                if (f != null && f.getTypFigury() == TypFigury.KROL && f.getKolorFigur() == kolorKrola) {
                    return f.getPozycja();
                }
            }
        }
        return null; // Sytuacja awaryjna, nie powinna wystąpić w normalnej grze
    }

    /**
     * Sprawdza, czy dane pole jest atakowane przez figury przeciwnika.
     * @param pole Pole do sprawdzenia.
     * @param kolorAtakujacego Kolor figur, które mogą atakować.
     * @return true, jeśli pole jest atakowane.
     */
    public boolean czyPoleJestAtakowane(Pozycja pole, KolorFigur kolorAtakujacego) {
        for (int r = 0; r < ROZMIAR_PLANSZY; r++) {
            for (int k = 0; k < ROZMIAR_PLANSZY; k++) {
                Figura f = getFigura(new Pozycja(r, k));
                if (f != null && f.getKolorFigur() == kolorAtakujacego) {
                    List<Pozycja> atakowanePola = f.getDostepneRuchy(this);
                    if (atakowanePola.contains(pole)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Sprawdza, czy król danego koloru jest aktualnie w szachu.
     * @param kolorKrola Kolor króla do sprawdzenia.
     * @return true, jeśli król jest w szachu.
     */
    public boolean czyKrolJestWszachu(KolorFigur kolorKrola) {
        Pozycja pozycjaKrola = znajdzKrola(kolorKrola);
        if (pozycjaKrola == null) {
            return false;
        }
        KolorFigur kolorPrzeciwnika = (kolorKrola == KolorFigur.WHITE) ? KolorFigur.BLACK : KolorFigur.WHITE;
        return czyPoleJestAtakowane(pozycjaKrola, kolorPrzeciwnika);
    }

    /**
     * Generuje listę WSZYSTKICH możliwych ruchów dla gracza, które są w 100% legalne (nie zostawiają króla w szachu).
     * @param kolorGracza Kolor gracza, dla którego generujemy ruchy.
     * @return Lista legalnych ruchów, gdzie każdy ruch to tablica [pozycja_startowa, pozycja_koncowa].
     */
    public List<Pozycja[]> getWszystkieLegalneRuchy(KolorFigur kolorGracza) {
        List<Pozycja[]> legalneRuchy = new ArrayList<>();
        for (int r = 0; r < ROZMIAR_PLANSZY; r++) {
            for (int k = 0; k < ROZMIAR_PLANSZY; k++) {
                Figura f = getFigura(new Pozycja(r, k));
                if (f != null && f.getKolorFigur() == kolorGracza) {
                    List<Pozycja> potencjalneRuchy = f.getDostepneRuchy(this);
                    for (Pozycja cel : potencjalneRuchy) {
                        Plansza kopia = new Plansza(this);
                        kopia.wykonajRuch(f.getPozycja(), cel);
                        if (!kopia.czyKrolJestWszachu(kolorGracza)) {
                            legalneRuchy.add(new Pozycja[]{f.getPozycja(), cel});
                        }
                    }
                }
            }
        }
        return legalneRuchy;
    }

    public void ulozenieStandardoweFigur() {
        for (int r = 0; r < ROZMIAR_PLANSZY; r++) {
            for (int k = 0; k < ROZMIAR_PLANSZY; k++) {
                setFigura(new Pozycja(r, k), null);
            }
        }

        // Czarne figury
        setFigura(new Pozycja(0, 0), FabrykaFigur.utworzFigure(TypFigury.WIEZA, KolorFigur.BLACK));
        setFigura(new Pozycja(0, 1), FabrykaFigur.utworzFigure(TypFigury.KON, KolorFigur.BLACK));
        setFigura(new Pozycja(0, 2), FabrykaFigur.utworzFigure(TypFigury.GONIEC, KolorFigur.BLACK));
        setFigura(new Pozycja(0, 3), FabrykaFigur.utworzFigure(TypFigury.HETMAN, KolorFigur.BLACK));
        setFigura(new Pozycja(0, 4), FabrykaFigur.utworzFigure(TypFigury.KROL, KolorFigur.BLACK));
        setFigura(new Pozycja(0, 5), FabrykaFigur.utworzFigure(TypFigury.GONIEC, KolorFigur.BLACK));
        setFigura(new Pozycja(0, 6), FabrykaFigur.utworzFigure(TypFigury.KON, KolorFigur.BLACK));
        setFigura(new Pozycja(0, 7), FabrykaFigur.utworzFigure(TypFigury.WIEZA, KolorFigur.BLACK));

        for (int i = 0; i < ROZMIAR_PLANSZY; i++) {
            setFigura(new Pozycja(1, i), FabrykaFigur.utworzFigure(TypFigury.PION, KolorFigur.BLACK));
        }

        // Białe figury
        setFigura(new Pozycja(7, 0), FabrykaFigur.utworzFigure(TypFigury.WIEZA, KolorFigur.WHITE));
        setFigura(new Pozycja(7, 1), FabrykaFigur.utworzFigure(TypFigury.KON, KolorFigur.WHITE));
        setFigura(new Pozycja(7, 2), FabrykaFigur.utworzFigure(TypFigury.GONIEC, KolorFigur.WHITE));
        setFigura(new Pozycja(7, 3), FabrykaFigur.utworzFigure(TypFigury.HETMAN, KolorFigur.WHITE));
        setFigura(new Pozycja(7, 4), FabrykaFigur.utworzFigure(TypFigury.KROL, KolorFigur.WHITE));
        setFigura(new Pozycja(7, 5), FabrykaFigur.utworzFigure(TypFigury.GONIEC, KolorFigur.WHITE));
        setFigura(new Pozycja(7, 6), FabrykaFigur.utworzFigure(TypFigury.KON, KolorFigur.WHITE));
        setFigura(new Pozycja(7, 7), FabrykaFigur.utworzFigure(TypFigury.WIEZA, KolorFigur.WHITE));

        for (int i = 0; i < ROZMIAR_PLANSZY; i++) {
            setFigura(new Pozycja(6, i), FabrykaFigur.utworzFigure(TypFigury.PION, KolorFigur.WHITE));
        }
    }

    public void wyswietlaniePlanszy() {
        System.out.println("  a  b  c  d  e  f  g  h");
        System.out.println("-------------------------");
        for (int r = 0; r < ROZMIAR_PLANSZY; r++) {
            System.out.print((8 - r) + "|");
            for (int k = 0; k < ROZMIAR_PLANSZY; k++) {
                Figura figura = kwadraty[r][k];
                if (figura == null) {
                    System.out.print(" . ");
                } else {
                    // Używam symboli unicode dla lepszej czytelności w konsoli
                    String symbol;
                    switch(figura.getTypFigury()) {
                        case KROL: symbol = (figura.getKolorFigur() == KolorFigur.WHITE) ? "♔" : "♚"; break;
                        case HETMAN: symbol = (figura.getKolorFigur() == KolorFigur.WHITE) ? "♕" : "♛"; break;
                        case WIEZA: symbol = (figura.getKolorFigur() == KolorFigur.WHITE) ? "♖" : "♜"; break;
                        case GONIEC: symbol = (figura.getKolorFigur() == KolorFigur.WHITE) ? "♗" : "♝"; break;
                        case KON: symbol = (figura.getKolorFigur() == KolorFigur.WHITE) ? "♘" : "♞"; break;
                        case PION: symbol = (figura.getKolorFigur() == KolorFigur.WHITE) ? "♙" : "♟"; break;
                        default: symbol = "?";
                    }
                    System.out.print(" " + symbol + " ");
                }
            }
            System.out.println("|" + (8 - r));
        }
        System.out.println("-------------------------");
        System.out.println("  a  b  c  d  e  f  g  h");
    }
}