package model.figury;

import model.enums.KolorFigur;
import model.enums.TypFigury;
import model.rdzen.Plansza; // NOWOŚĆ
import utils.Pozycja;

import java.util.List; // NOWOŚĆ

public abstract class Figura {
    protected KolorFigur kolorFigur;
    protected Pozycja pozycja;
    protected TypFigury typFigury;
    protected boolean czyPierwszyRuch = true; // Ważne dla piona, roszady

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

    public boolean isCzyPierwszyRuch() { // NOWOŚĆ
        return czyPierwszyRuch;
    }

    public void setCzyPierwszyRuch(boolean czyPierwszyRuch) { // NOWOŚĆ
        this.czyPierwszyRuch = czyPierwszyRuch;
    }

    public String getSymbol() {
        // Ta metoda jest teraz nadpisywana w każdej klasie potomnej, więc implementacja tutaj jest zbędna
        return "";
    }

    public TypFigury getTypFigury() {
        return typFigury;
    }


    public abstract List<Pozycja> getDostepneRuchy(Plansza plansza);

    @Override
    public String toString() {
        return getSymbol();
    }
}