package model.figury;

import model.enums.KolorFigur;
import model.enums.TypFigury;

public class Hetman extends Figura{
    public Hetman(KolorFigur kolor) {
        super(kolor, TypFigury.HETMAN);
    }
    @Override
    public String getSymbol(){
        // BH - bialy hetman, jak nie to CH - czarny hetman
        return getKolorFigur() == KolorFigur.WHITE ? "BH" : "CH";
    }
}
