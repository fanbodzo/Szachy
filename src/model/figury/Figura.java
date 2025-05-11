package model.figury;

import model.enums.KolorFigur;
import utils.*;

import java.util.List;

public class Figura {
    protected KolorFigur kolorFigur;
    //aktualna pozycja figury zarzadzana przez plansze
    protected Pozycja pozycja;

    public Figura(KolorFigur kolorFigur) {
        this.kolorFigur = kolorFigur;
    }
    public KolorFigur getKolorFigur() {
        return kolorFigur;
    }

    public Pozycja getPozycja() {
        return pozycja;
    }
    public void setPozycja(Pozycja pozycja) {
        this.pozycja = pozycja;
    }

    //metoda abstrkcyja ktora beda implementowac figury
    //naraie zwraca null ale bedzie przydatne
    // repreznatacja np BP - bialy pionek BH- bialy Hetman
    public String getSymbol() {
        return null;
    }

    @Override
    public String toString() {
        return getSymbol();
    }
}
