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
        // ZMIANA: Używamy unikalnych symboli BB/CC dla króla
        return getKolorFigur() == KolorFigur.WHITE ? "BB" : "CC";
    }

    @Override
    public List<Pozycja> getDostepneRuchy(Plansza plansza) {
        return getDostepneRuchy(plansza, true);
    }

    public List<Pozycja> getDostepneRuchy(Plansza plansza, boolean wlaczSprawdzanieRoszady) {
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

        if (wlaczSprawdzanieRoszady && isCzyPierwszyRuch()) {
            KolorFigur kolorPrzeciwnika = (getKolorFigur() == KolorFigur.WHITE) ? KolorFigur.BLACK : KolorFigur.WHITE;

            if (!plansza.czyPoleJestAtakowane(pozycja, kolorPrzeciwnika)) {
                sprawdzRoszade(plansza, ruchy, 1, kolorPrzeciwnika);
                sprawdzRoszade(plansza, ruchy, -1, kolorPrzeciwnika);
            }
        }

        return ruchy;
    }

    private void sprawdzRoszade(Plansza plansza, List<Pozycja> ruchy, int kierunek, KolorFigur kolorPrzeciwnika) {
        int rzadKrola = pozycja.getRzad();
        Pozycja pozycjaWiezy = new Pozycja(rzadKrola, (kierunek > 0) ? 7 : 0);
        Figura figuraWiezy = plansza.getFigura(pozycjaWiezy);

        if (figuraWiezy instanceof Wieza && figuraWiezy.isCzyPierwszyRuch()) {
            boolean drogaWolna = true;
            for (int k = pozycja.getKolumna() + kierunek; k != pozycjaWiezy.getKolumna(); k += kierunek) {
                if (plansza.getFigura(new Pozycja(rzadKrola, k)) != null) {
                    drogaWolna = false;
                    break;
                }
            }

            if (drogaWolna) {
                Pozycja polePosrednie1 = new Pozycja(rzadKrola, pozycja.getKolumna() + kierunek);
                Pozycja polePosrednie2 = new Pozycja(rzadKrola, pozycja.getKolumna() + 2 * kierunek);

                if (!plansza.czyPoleJestAtakowane(polePosrednie1, kolorPrzeciwnika) &&
                        !plansza.czyPoleJestAtakowane(polePosrednie2, kolorPrzeciwnika)) {
                    ruchy.add(polePosrednie2);
                }
            }
        }
    }
}