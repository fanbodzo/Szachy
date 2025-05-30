package model.figury;

import model.enums.KolorFigur;
import model.enums.TypFigury;

public class Kon extends Figura {
    public Kon(KolorFigur kolor) {
        super(kolor, TypFigury.KON);
    }
    @Override
    public String getSymbol(){
        // BK - bialy kon , jak nie to CK - czarny kon
        return getKolorFigur() == KolorFigur.WHITE ? "BK" : "CK";
    }
}
