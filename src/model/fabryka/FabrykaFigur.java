package model.fabryka;

import model.figury.*;
import model.enums.*;

public class FabrykaFigur {

    public static Figura utworzFigure(TypFigury typ , KolorFigur kolor){
        switch(typ){
            case PION -> {
                return new Pion(kolor);
            }
            case KON ->{
                return new Kon(kolor);
            }
            case KROL -> {
                return new Krol(kolor);
            }
            case WIEZA -> {
                return new Wieza(kolor);
            }
            case GONIEC -> {
                return new Goniec(kolor);
            }
            case HETMAN -> {
                return new Hetman(kolor);
            }
            default -> {
                throw new IllegalArgumentException("Unsupported figury type");
            }
        }

    }
}
