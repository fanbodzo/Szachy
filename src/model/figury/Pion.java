package model.figury;

import model.enums.KolorFigur;
import model.enums.TypFigury;
import model.rdzen.Plansza;
import utils.Pozycja;

import java.util.ArrayList;
import java.util.List;

public class Pion extends Figura {

    public Pion(KolorFigur kolor) {
        super(kolor, TypFigury.PION);
    }

    @Override
    public String getSymbol() {
        return getKolorFigur() == KolorFigur.WHITE ? "BP" : "CP";
    }

    @Override
    public List<Pozycja> getDostepneRuchy(Plansza plansza) {
        List<Pozycja> ruchy = new ArrayList<>();
        int kierunek = (getKolorFigur() == KolorFigur.WHITE) ? -1 : 1; // Białe idą w górę (malejące rzędy), czarne w dół

        // 1. Ruch do przodu o jedno pole
        Pozycja ruch1 = new Pozycja(pozycja.getRzad() + kierunek, pozycja.getKolumna());
        if (plansza.isValidPosition(ruch1) && plansza.getFigura(ruch1) == null) {
            ruchy.add(ruch1);

            // 2. Ruch do przodu o dwa pola (tylko przy pierwszym ruchu)
            if (isCzyPierwszyRuch()) {
                Pozycja ruch2 = new Pozycja(pozycja.getRzad() + 2 * kierunek, pozycja.getKolumna());
                if (plansza.isValidPosition(ruch2) && plansza.getFigura(ruch2) == null) {
                    ruchy.add(ruch2);
                }
            }
        }

        // 3. Bicie po skosie
        int[] kolumnyBicia = {-1, 1};
        for (int dk : kolumnyBicia) {
            Pozycja bicie = new Pozycja(pozycja.getRzad() + kierunek, pozycja.getKolumna() + dk);
            if (plansza.isValidPosition(bicie)) {
                Figura figuraNaCelu = plansza.getFigura(bicie);
                if (figuraNaCelu != null && figuraNaCelu.getKolorFigur() != this.getKolorFigur()) {
                    ruchy.add(bicie);
                }
            }
        }

        // Bicie w przelocie (en passant)
        Pozycja enPassantTarget = plansza.getEnPassantTargetSquare();
        if (enPassantTarget != null) {
            // Sprawdzamy, czy cel bicia w przelocie jest jednym z możliwych pól ataku tego piona
            Pozycja lewyAtak = new Pozycja(pozycja.getRzad() + kierunek, pozycja.getKolumna() - 1);
            Pozycja prawyAtak = new Pozycja(pozycja.getRzad() + kierunek, pozycja.getKolumna() + 1);

            if (enPassantTarget.equals(lewyAtak) || enPassantTarget.equals(prawyAtak)) {
                ruchy.add(enPassantTarget);
            }
        }

        return ruchy;
    }

    //Metoda zwracająca tylko te pola, które pion atakuje.

    public List<Pozycja> getAtakowanePola(Plansza plansza) {
        List<Pozycja> ataki = new ArrayList<>();
        int kierunek = (getKolorFigur() == KolorFigur.WHITE) ? -1 : 1;
        int[] kolumnyBicia = {-1, 1};
        for (int dk : kolumnyBicia) {
            Pozycja poleAtaku = new Pozycja(pozycja.getRzad() + kierunek, pozycja.getKolumna() + dk);
            if (plansza.isValidPosition(poleAtaku)) {
                ataki.add(poleAtaku);
            }
        }
        return ataki;
    }
}