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

        // TODO: Roszada (castling) - do implementacji później

        return ruchy;
    }
}