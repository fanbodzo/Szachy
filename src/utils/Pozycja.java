package utils;

import java.util.Objects; // Importujemy klasę Objects

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


    @Override
    public boolean equals(Object o) {
        // 1. Sprawdzenie, czy obiekt jest porównywany sam ze sobą
        if (this == o) return true;
        // 2. Sprawdzenie, czy obiekt nie jest nullem lub czy jest innego typu
        if (o == null || getClass() != o.getClass()) return false;

        // 3. Rzutowanie obiektu i porównanie pól
        Pozycja pozycja = (Pozycja) o;
        return rzad == pozycja.rzad && kolumna == pozycja.kolumna;
    }

    @Override
    public int hashCode() {
        // Generowanie unikalnego kodu hash na podstawie wartości pól.
        return Objects.hash(rzad, kolumna);
    }
}