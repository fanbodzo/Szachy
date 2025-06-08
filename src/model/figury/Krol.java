package model.figury;

import model.enums.KolorFigur;
import model.enums.TypFigury;
import model.rdzen.Plansza;
import utils.Pozycja;

import java.util.ArrayList;
import java.util.List;

public class Krol extends Figura {
    public Krol(KolorFigur kolor) {
        super(kolor, TypFigury.KROL);
    }

    @Override
    public String getSymbol() {
        return getKolorFigur() == KolorFigur.WHITE ? "BB" : "CC";
    }

    @Override
    public List<Pozycja> getDostepneRuchy(Plansza plansza) {
        List<Pozycja> ruchy = new ArrayList<>();
        int[][] kierunki = {
                {-1, -1}, {-1, 0}, {-1, 1},
                {0, -1},           {0, 1},
                {1, -1}, {1, 0}, {1, 1}
        };

        for (int[] k : kierunki) {
            Pozycja cel = new Pozycja(pozycja.getRzad() + k[0], pozycja.getKolumna() + k[1]);
            if (plansza.isValidPosition(cel)) {
                Figura naCelu = plansza.getFigura(cel);
                if (naCelu == null || naCelu.getKolorFigur() != this.getKolorFigur()) {
                    ruchy.add(cel);
                }
            }
        }

        // Logika Roszady (Castling)
        // Sprawdzamy ją tylko wtedy, gdy król się jeszcze nie ruszył.
        if (isCzyPierwszyRuch()) {
            KolorFigur kolorPrzeciwnika = (getKolorFigur() == KolorFigur.WHITE) ? KolorFigur.BLACK : KolorFigur.WHITE;

            // Roszada jest możliwa tylko, gdy król nie jest aktualnie szachowany.
            if (!plansza.czyPoleJestAtakowane(pozycja, kolorPrzeciwnika)) {
                // Sprawdzamy roszadę po stronie króla (krótka)
                sprawdzRoszade(plansza, ruchy, 1, kolorPrzeciwnika);
                // Sprawdzamy roszadę po stronie hetmana (długa)
                sprawdzRoszade(plansza, ruchy, -1, kolorPrzeciwnika);
            }
        }

        return ruchy;
    }

    /**
     * Prywatna metoda pomocnicza do sprawdzania warunków roszady w danym kierunku.
     * @param plansza Aktualny stan planszy.
     * @param ruchy Lista ruchów do której dodamy ewentualny ruch roszady.
     * @param kierunek Kierunek sprawdzania (1 dla roszady krótkiej, -1 dla długiej).
     * @param kolorPrzeciwnika Kolor figur przeciwnika.
     */
    private void sprawdzRoszade(Plansza plansza, List<Pozycja> ruchy, int kierunek, KolorFigur kolorPrzeciwnika) {
        int rzadKrola = pozycja.getRzad();
        Pozycja pozycjaWiezy = new Pozycja(rzadKrola, (kierunek > 0) ? 7 : 0);
        Figura figuraWiezy = plansza.getFigura(pozycjaWiezy);

        // Warunek 1: Czy na rogu stoi wieża, która się nie ruszyła?
        if (figuraWiezy != null && figuraWiezy.getTypFigury() == TypFigury.WIEZA && figuraWiezy.isCzyPierwszyRuch()) {

            // Warunek 2: Czy droga między królem a wieżą jest pusta?
            boolean drogaWolna = true;
            for (int k = pozycja.getKolumna() + kierunek; k != pozycjaWiezy.getKolumna(); k += kierunek) {
                if (plansza.getFigura(new Pozycja(rzadKrola, k)) != null) {
                    drogaWolna = false;
                    break;
                }
            }

            if (drogaWolna) {
                // Warunek 3: Czy pola, przez które przechodzi król, nie są atakowane?
                Pozycja polePosrednie1 = new Pozycja(rzadKrola, pozycja.getKolumna() + kierunek);
                Pozycja polePosrednie2 = new Pozycja(rzadKrola, pozycja.getKolumna() + 2 * kierunek);

                if (!plansza.czyPoleJestAtakowane(polePosrednie1, kolorPrzeciwnika) &&
                        !plansza.czyPoleJestAtakowane(polePosrednie2, kolorPrzeciwnika)) {
                    // Jeśli wszystkie warunki są spełnione, dodajemy docelowe pole króla jako możliwy ruch.
                    ruchy.add(polePosrednie2);
                }
            }
        }
    }
}