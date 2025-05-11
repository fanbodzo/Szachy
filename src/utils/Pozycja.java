package utils;

public class Pozycja {
    private final int rzad;
    private final int kolumna;

    public Pozycja(int rzad, int kolumna) {
        this.rzad = rzad;
        this.kolumna = kolumna;
    }
    public int getRzad() {
        return rzad;
    }
    public int getKolumna() {
        return kolumna;
    }

    @Override
    public String toString() {
        return "(" + rzad + "," + kolumna + ")";
    }
}
