package model.figury;

import model.enums.KolorFigur;
import model.enums.TypFigury;
import utils.*;

import java.util.List;

public abstract class Figura {
    protected KolorFigur kolorFigur;
    //aktualna pozycja figury zarzadzana przez plansze
    protected Pozycja pozycja;
    //zeby wiedziec jaki to typ figury do obslugi ruchow  na peno si eprzyda
    protected TypFigury typFigury;

    public Figura(KolorFigur kolorFigur, TypFigury typ) {
        this.kolorFigur = kolorFigur;
        this.typFigury = typ;
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

    public TypFigury getTypFigury() {
        return typFigury;
    }

    @Override
    public String toString() {
        return getSymbol();
    }
}
