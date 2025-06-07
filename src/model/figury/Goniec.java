package model.figury;

import model.enums.KolorFigur;
import model.enums.TypFigury;
import model.rdzen.Plansza;
import utils.Pozycja;

import java.util.ArrayList;
import java.util.List;

public class Goniec extends Figura {
    public Goniec(KolorFigur kolor) {
        super(kolor, TypFigury.GONIEC);
    }

    @Override
    public String getSymbol() {
        return getKolorFigur() == KolorFigur.WHITE ? "BG" : "CG";
    }

    @Override
    public List<Pozycja> getDostepneRuchy(Plansza plansza) {
        List<Pozycja> ruchy = new ArrayList<>();
        int[][] kierunki = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}}; // Po skosach

        for (int[] k : kierunki) {
            Pozycja nast = new Pozycja(pozycja.getRzad() + k[0], pozycja.getKolumna() + k[1]);
            while (plansza.isValidPosition(nast)) {
                Figura naDrodze = plansza.getFigura(nast);
                if (naDrodze == null) {
                    ruchy.add(nast);
                } else {
                    if (naDrodze.getKolorFigur() != this.getKolorFigur()) {
                        ruchy.add(nast);
                    }
                    break;
                }
                nast = new Pozycja(nast.getRzad() + k[0], nast.getKolumna() + k[1]);
            }
        }
        return ruchy;
    }
}