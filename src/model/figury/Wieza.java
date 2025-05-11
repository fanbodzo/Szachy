package model.figury;

import model.enums.KolorFigur;

public class Wieza extends Figura{
    public Wieza(KolorFigur kolor) {
        super(kolor);
    }
    @Override
    public String getSymbol(){
        // BW - biala wieza , jak nie to CW - czarna wieza
        return getKolorFigur() == KolorFigur.WHITE ? "BW" : "CW";
    }
}
