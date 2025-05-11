package model.figury;

import model.enums.KolorFigur;

public class Kon extends Figura {
    public Kon(KolorFigur kolor) {
        super(kolor);
    }
    @Override
    public String getSymbol(){
        // BK - bialy kon , jak nie to CK - czarny kon
        return getKolorFigur() == KolorFigur.WHITE ? "BK" : "CK";
    }
}
