package model.figury;

import model.enums.KolorFigur;

public class Goniec extends Figura{
    public Goniec(KolorFigur kolor) {
        super(kolor);
    }
    @Override
    public String getSymbol(){
        // BG - bialy goniec , jak nie to CG - czarny goniec
        return getKolorFigur() == KolorFigur.WHITE ? "BG" : "CG";
    }
}
