package model.figury;

import model.enums.KolorFigur;
import model.enums.TypFigury;

public class Krol extends Figura{
    public Krol(KolorFigur kolor) {
        super(kolor, TypFigury.KROL);
    }
    @Override
    public String getSymbol(){
        // BB - bialy krol , jak nie to CC - czarny krol
        return getKolorFigur() == KolorFigur.WHITE ? "BB" : "CC";
    }
}
