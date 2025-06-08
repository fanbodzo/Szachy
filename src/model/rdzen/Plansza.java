package model.rdzen;

import model.enums.KolorFigur;
import model.enums.TypFigury;
import model.fabryka.FabrykaFigur;
import model.figury.Figura;
import model.figury.Pion;
import utils.Pozycja;

import java.util.ArrayList;
import java.util.List;

public class Plansza {
    private final Figura[][] kwadraty;
    public static final int ROZMIAR_PLANSZY = 8;
    private Pozycja enPassantTargetSquare;

    public Plansza() {
        this.kwadraty = new Figura[ROZMIAR_PLANSZY][ROZMIAR_PLANSZY];
    }

    public Plansza(Plansza inna) {
        this.kwadraty = new Figura[ROZMIAR_PLANSZY][ROZMIAR_PLANSZY];
        for (int r = 0; r < ROZMIAR_PLANSZY; r++) {
            for (int k = 0; k < ROZMIAR_PLANSZY; k++) {
                Pozycja p = new Pozycja(r, k);
                Figura f = inna.getFigura(p);
                if (f != null) {
                    Figura nowaFigura = FabrykaFigur.utworzFigure(f.getTypFigury(), f.getKolorFigur());
                    nowaFigura.setCzyPierwszyRuch(f.isCzyPierwszyRuch());
                    this.setFigura(p, nowaFigura);
                }
            }
        }
        this.enPassantTargetSquare = inna.enPassantTargetSquare;
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
        if (figura == null) {
            return;
        }

        Pozycja poprzedniEnPassantTarget = this.enPassantTargetSquare;
        this.enPassantTargetSquare = null;

        if (figura.getTypFigury() == TypFigury.PION && koniec.equals(poprzedniEnPassantTarget)) {
            int kierunekPionaZbitego = (figura.getKolorFigur() == KolorFigur.WHITE) ? 1 : -1;
            setFigura(new Pozycja(koniec.getRzad() + kierunekPionaZbitego, koniec.getKolumna()), null);
        }

        if (figura.getTypFigury() == TypFigury.KROL && Math.abs(start.getKolumna() - koniec.getKolumna()) == 2) {
            if (koniec.getKolumna() > start.getKolumna()) {
                Figura wieza = getFigura(new Pozycja(start.getRzad(), 7));
                setFigura(new Pozycja(start.getRzad(), 5), wieza);
                setFigura(new Pozycja(start.getRzad(), 7), null);
            } else {
                Figura wieza = getFigura(new Pozycja(start.getRzad(), 0));
                setFigura(new Pozycja(start.getRzad(), 3), wieza);
                setFigura(new Pozycja(start.getRzad(), 0), null);
            }
        }

        setFigura(koniec, figura);
        setFigura(start, null);
        figura.setCzyPierwszyRuch(false);

        if (figura.getTypFigury() == TypFigury.PION && Math.abs(start.getRzad() - koniec.getRzad()) == 2) {
            int kierunek = (figura.getKolorFigur() == KolorFigur.WHITE) ? -1 : 1;
            this.enPassantTargetSquare = new Pozycja(start.getRzad() + kierunek, start.getKolumna());
        }

        if (figura.getTypFigury() == TypFigury.PION) {
            if ((figura.getKolorFigur() == KolorFigur.WHITE && koniec.getRzad() == 0) ||
                    (figura.getKolorFigur() == KolorFigur.BLACK && koniec.getRzad() == 7)) {
                Figura hetman = FabrykaFigur.utworzFigure(TypFigury.HETMAN, figura.getKolorFigur());
                setFigura(koniec, hetman);
            }
        }
    }

    public Pozycja znajdzKrola(KolorFigur kolorKrola) {
        for (int r = 0; r < ROZMIAR_PLANSZY; r++) {
            for (int k = 0; k < ROZMIAR_PLANSZY; k++) {
                Figura f = getFigura(new Pozycja(r, k));
                if (f != null && f.getTypFigury() == TypFigury.KROL && f.getKolorFigur() == kolorKrola) {
                    return f.getPozycja();
                }
            }
        }
        return null;
    }

    /**
     * ZMIANA: Przerobiona metoda, aby unikać nieskończonej rekursji.
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
                    List<Pozycja> atakowanePola;

                    // Logika specjalna, aby przerwać pętlę rekursji
                    if (f.getTypFigury() == TypFigury.KROL) {
                        // Sprawdzamy atak króla bezpośrednio, bez wołania getDostepneRuchy
                        atakowanePola = new ArrayList<>();
                        int[][] kierunki = {
                                {-1, -1}, {-1, 0}, {-1, 1}, {0, -1},
                                {0, 1}, {1, -1}, {1, 0}, {1, 1}
                        };
                        for (int[] dir : kierunki) {
                            atakowanePola.add(new Pozycja(f.getPozycja().getRzad() + dir[0], f.getPozycja().getKolumna() + dir[1]));
                        }
                    } else if (f.getTypFigury() == TypFigury.PION) {
                        // Używamy nowej, precyzyjnej metody dla piona
                        atakowanePola = ((Pion) f).getAtakowanePola(this);
                    } else {
                        // Standardowa metoda dla reszty figur (Wieża, Goniec, Hetman, Kon)
                        atakowanePola = f.getDostepneRuchy(this);
                    }

                    if (atakowanePola.contains(pole)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean czyKrolJestWszachu(KolorFigur kolorKrola) {
        Pozycja pozycjaKrola = znajdzKrola(kolorKrola);
        if (pozycjaKrola == null) {
            return false;
        }
        KolorFigur kolorPrzeciwnika = (kolorKrola == KolorFigur.WHITE) ? KolorFigur.BLACK : KolorFigur.WHITE;
        return czyPoleJestAtakowane(pozycjaKrola, kolorPrzeciwnika);
    }

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

    public Pozycja getEnPassantTargetSquare() {
        return enPassantTargetSquare;
    }

    // Metody ulozenieStandardoweFigur() i wyswietlaniePlanszy() bez zmian...
    // ...
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