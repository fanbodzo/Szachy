package model.figury;

import model.enums.KolorFigur;
import model.enums.TypFigury;
import model.rdzen.Plansza;
import utils.Pozycja;

import java.util.ArrayList;
import java.util.List;

public class Kon extends Figura {
    public Kon(KolorFigur kolor) {
        super(kolor, TypFigury.KON);
    }

    @Override
    public String getSymbol() {
        return getKolorFigur() == KolorFigur.WHITE ? "BK" : "CK";
    }

    @Override
    public List<Pozycja> getDostepneRuchy(Plansza plansza) {
        List<Pozycja> ruchy = new ArrayList<>();
        int[][] mozliwePrzesuniecia = {
                {-2, -1}, {-2, 1}, {2, -1}, {2, 1},
                {-1, -2}, {-1, 2}, {1, -2}, {1, 2}
        };

        for (int[] przesuniecie : mozliwePrzesuniecia) {
            Pozycja cel = new Pozycja(pozycja.getRzad() + przesuniecie[0], pozycja.getKolumna() + przesuniecie[1]);
            if (plansza.isValidPosition(cel)) {
                Figura naCelu = plansza.getFigura(cel);
                if (naCelu == null || naCelu.getKolorFigur() != this.getKolorFigur()) {
                    ruchy.add(cel);
                }
            }
        }
        return ruchy;
    }
}