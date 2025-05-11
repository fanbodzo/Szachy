package model.figury;

import model.enums.KolorFigur;

public class Hetman extends Figura{
    public Hetman(KolorFigur kolor) {
        super(kolor);
    }
    @Override
    public String getSymbol(){
        // BH - bialy hetman, jak nie to CH - czarny hetman
        return getKolorFigur() == KolorFigur.WHITE ? "BH" : "CH";
    }
}
