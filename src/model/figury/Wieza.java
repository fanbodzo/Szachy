package model.figury;

import model.enums.KolorFigur;
import model.enums.TypFigury;
import model.rdzen.Plansza;
import utils.Pozycja;

import java.util.ArrayList;
import java.util.List;

public class Wieza extends Figura {
    public Wieza(KolorFigur kolor) {
        super(kolor, TypFigury.WIEZA);
    }

    @Override
    public String getSymbol() {
        return getKolorFigur() == KolorFigur.WHITE ? "BW" : "CW";
    }

    @Override
    public List<Pozycja> getDostepneRuchy(Plansza plansza) {
        List<Pozycja> ruchy = new ArrayList<>();
        int[][] kierunki = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}}; // Prawo, lewo, dół, góra

        for (int[] k : kierunki) {
            Pozycja nast = new Pozycja(pozycja.getRzad() + k[0], pozycja.getKolumna() + k[1]);
            while (plansza.isValidPosition(nast)) {
                Figura naDrodze = plansza.getFigura(nast);
                if (naDrodze == null) {
                    ruchy.add(nast); // Puste pole, dodaj i kontynuuj w tym kierunku
                } else {
                    if (naDrodze.getKolorFigur() != this.getKolorFigur()) {
                        ruchy.add(nast); // Figura przeciwnika, dodaj i zakończ
                    }
                    break; // Własna figura, zablokowane, zakończ
                }
                nast = new Pozycja(nast.getRzad() + k[0], nast.getKolumna() + k[1]);
            }
        }
        return ruchy;
    }
}