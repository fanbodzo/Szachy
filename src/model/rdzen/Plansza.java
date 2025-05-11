package model.rdzen;

import model.enums.KolorFigur;
import model.enums.TypFigury;
import model.fabryka.FabrykaFigur;
import model.figury.Figura;
import utils.Pozycja;

public class Plansza {
    private Figura[][] kwadraty;
    public static final int ROZMIAR_PLANSZY = 8;

    public Plansza(){
        this.kwadraty = new Figura[ROZMIAR_PLANSZY][ROZMIAR_PLANSZY];
    }

    //sprawdzanie czy dana pozycja miesci sie w planszy
    public boolean isValidPosition(Pozycja pozycja){
        if(pozycja == null){
            return false;
        }
        return pozycja.getKolumna() >=0 && pozycja.getKolumna() <= ROZMIAR_PLANSZY &&
                pozycja.getRzad() >= 0 && pozycja.getRzad() <= ROZMIAR_PLANSZY;
    }

    //zwracanie figury z danej pzycji
    public Figura getFigura(Pozycja pozycja){
        if(!isValidPosition(pozycja)){
            System.err.println("pole poza szachownica" + pozycja);
            return null;
        }
        return kwadraty[pozycja.getRzad()][pozycja.getKolumna()];
    }

    //ustawianie figury na danej pozycji
    public void setFigura(Pozycja pozycja, Figura figura){
        if(!isValidPosition(pozycja)){
            System.err.println("pole poza szachownica" + pozycja);
            return;
        }
        kwadraty[pozycja.getRzad()][pozycja.getKolumna()] = figura;
        if(figura != null){
            figura.setPozycja(pozycja);
        }
    }

    //usuwanie figury z pozycji
    public void removeFigura(Pozycja pozycja){
        setFigura(pozycja, null);
    }

    public void ulozenieStandardoweFigur(){
        setFigura(new Pozycja(0, 0), FabrykaFigur.utworzFigure(TypFigury.WIEZA, KolorFigur.BLACK));
        setFigura(new Pozycja(0 , 1) , FabrykaFigur.utworzFigure(TypFigury.KON, KolorFigur.BLACK));
        setFigura(new Pozycja(0 , 2) , FabrykaFigur.utworzFigure(TypFigury.GONIEC, KolorFigur.BLACK));
        setFigura(new Pozycja(0 , 3) , FabrykaFigur.utworzFigure(TypFigury.HETMAN, KolorFigur.BLACK));
        setFigura(new Pozycja(0 , 4) , FabrykaFigur.utworzFigure(TypFigury.KROL, KolorFigur.BLACK));
        setFigura(new Pozycja(0,  5), FabrykaFigur.utworzFigure(TypFigury.WIEZA, KolorFigur.BLACK));
        setFigura(new Pozycja(0 , 6) , FabrykaFigur.utworzFigure(TypFigury.KON, KolorFigur.BLACK));
        setFigura(new Pozycja(0 , 7) , FabrykaFigur.utworzFigure(TypFigury.GONIEC, KolorFigur.BLACK));

        setFigura(new Pozycja(7, 0), FabrykaFigur.utworzFigure(TypFigury.WIEZA, KolorFigur.WHITE));
        setFigura(new Pozycja(7 , 1) , FabrykaFigur.utworzFigure(TypFigury.KON, KolorFigur.WHITE));
        setFigura(new Pozycja(7 , 2) , FabrykaFigur.utworzFigure(TypFigury.GONIEC, KolorFigur.WHITE));
        setFigura(new Pozycja(7 , 3) , FabrykaFigur.utworzFigure(TypFigury.HETMAN, KolorFigur.WHITE));
        setFigura(new Pozycja(7 , 4) , FabrykaFigur.utworzFigure(TypFigury.KROL, KolorFigur.WHITE));
        setFigura(new Pozycja(7,  5), FabrykaFigur.utworzFigure(TypFigury.WIEZA, KolorFigur.WHITE));
        setFigura(new Pozycja(7 , 6) , FabrykaFigur.utworzFigure(TypFigury.KON, KolorFigur.WHITE));
        setFigura(new Pozycja(7 , 7) , FabrykaFigur.utworzFigure(TypFigury.GONIEC, KolorFigur.WHITE));

        for(int i=0; i<ROZMIAR_PLANSZY; i++){
            setFigura(new Pozycja(1 , i) , FabrykaFigur.utworzFigure(TypFigury.PION, KolorFigur.BLACK));
            setFigura(new Pozycja(6 , i) , FabrykaFigur.utworzFigure(TypFigury.PION, KolorFigur.WHITE));
        }


    }

    public void wyswietlaniePlanszy() {
        System.out.println("  a b c d e f g h");
        System.out.println("  -----------------");
        for (int r = 0; r < ROZMIAR_PLANSZY; r++) {
            System.out.print((8 - r) + "|");
            for (int k = 0; k < ROZMIAR_PLANSZY; k++) {
                Figura figura =  kwadraty[r][k];
                if (figura == null) {
                    System.out.print(" .");
                } else {
                    System.out.print(" " + figura.getSymbol());
                }
            }
            System.out.println(" |" + (8 - r));
        }
        System.out.println("  -----------------");
        System.out.println("  a b c d e f g h");
    }

}
