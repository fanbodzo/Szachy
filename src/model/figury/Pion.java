package model.figury;

import model.enums.KolorFigur;
import model.enums.TypFigury;

public class Pion extends Figura {

    public Pion(KolorFigur kolor) {
        super(kolor, TypFigury.PION);
    }
    @Override
    public String getSymbol(){
        //jezeli figura jest biala to zwraca BP - bialy pion , jak nie to CP - czarny pion
        return getKolorFigur() == KolorFigur.WHITE ? "BP" : "CP";
    }
}
