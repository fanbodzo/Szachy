package model.figury;

import model.enums.KolorFigur;
import model.enums.TypFigury;
import model.rdzen.Plansza;
import utils.Pozycja;

import java.util.ArrayList;
import java.util.List;

public class Hetman extends Figura {
    public Hetman(KolorFigur kolor) {
        super(kolor, TypFigury.HETMAN);
    }

    @Override
    public String getSymbol() {
        return getKolorFigur() == KolorFigur.WHITE ? "BH" : "CH";
    }

    @Override
    public List<Pozycja> getDostepneRuchy(Plansza plansza) {
        List<Pozycja> ruchy = new ArrayList<>();
        // Logika Hetmana to połączenie logiki Wieży i Gońca
        int[][] kierunki = {
                {0, 1}, {0, -1}, {1, 0}, {-1, 0}, // Kierunki Wieży
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1}  // Kierunki Gońca
        };

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