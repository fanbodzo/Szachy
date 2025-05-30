package model.figury;

import model.enums.KolorFigur;
import model.enums.TypFigury;

public class Wieza extends Figura{
    public Wieza(KolorFigur kolor) {
        super(kolor, TypFigury.WIEZA);
    }
    @Override
    public String getSymbol(){
        // BW - biala wieza , jak nie to CW - czarna wieza
        return getKolorFigur() == KolorFigur.WHITE ? "BW" : "CW";
    }
}
