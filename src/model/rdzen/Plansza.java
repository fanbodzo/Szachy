package model.rdzen;

import model.enums.KolorFigur;
import model.enums.TypFigury;
import model.fabryka.FabrykaFigur;
import model.figury.Figura;
import model.figury.Krol;
import model.figury.Pion;
import utils.Pozycja;

import java.util.ArrayList;
import java.util.List;

public class Plansza {
    public static final int ROZMIAR_PLANSZY = 8;
    private Figura[][] pola;
    private Pozycja enPassantTargetSquare;

    public Plansza() {
        this.pola = new Figura[ROZMIAR_PLANSZY][ROZMIAR_PLANSZY];
    }

    public Plansza(Plansza inna) {
        this.pola = new Figura[ROZMIAR_PLANSZY][ROZMIAR_PLANSZY];
        for (int r = 0; r < ROZMIAR_PLANSZY; r++) {
            for (int k = 0; k < ROZMIAR_PLANSZY; k++) {
                if (inna.pola[r][k] != null) {
                    this.pola[r][k] = FabrykaFigur.utworzFigure(inna.pola[r][k].getTypFigury(), inna.pola[r][k].getKolorFigur());
                    this.pola[r][k].setPozycja(new Pozycja(r, k));
                    this.pola[r][k].setCzyPierwszyRuch(inna.pola[r][k].isCzyPierwszyRuch());
                }
            }
        }
        if (inna.enPassantTargetSquare != null) {
            this.enPassantTargetSquare = new Pozycja(inna.enPassantTargetSquare.getRzad(), inna.enPassantTargetSquare.getKolumna());
        }
    }

    public Figura getFigura(Pozycja p) {
        return isValidPosition(p) ? pola[p.getRzad()][p.getKolumna()] : null;
    }

    public Figura getFigura(int rzad, int kolumna) {
        return getFigura(new Pozycja(rzad, kolumna));
    }

    public void setFigura(Pozycja p, Figura f) {
        if (isValidPosition(p)) {
            pola[p.getRzad()][p.getKolumna()] = f;
            if (f != null) {
                f.setPozycja(p);
            }
        }
    }

    public Pozycja getEnPassantTargetSquare() {
        return enPassantTargetSquare;
    }

    public boolean isValidPosition(Pozycja p) {
        return p != null && p.getRzad() >= 0 && p.getRzad() < ROZMIAR_PLANSZY &&
                p.getKolumna() >= 0 && p.getKolumna() < ROZMIAR_PLANSZY;
    }

    public void wykonajRuch(Pozycja start, Pozycja koniec) {
        Figura figura = getFigura(start);
        if (figura == null) return;

        enPassantTargetSquare = null;
        if (figura.getTypFigury() == TypFigury.PION && Math.abs(start.getRzad() - koniec.getRzad()) == 2) {
            enPassantTargetSquare = new Pozycja((start.getRzad() + koniec.getRzad()) / 2, start.getKolumna());
        }

        figura.setCzyPierwszyRuch(false);
        setFigura(koniec, figura);
        setFigura(start, null);
    }

    public List<Pozycja> getWszystkieLegalneRuchyDlaFigury(Pozycja start) {
        Figura figura = getFigura(start);
        if (figura == null) {
            return new ArrayList<>();
        }
        KolorFigur kolor = figura.getKolorFigur();
        List<Pozycja> legalneRuchy = new ArrayList<>();
        List<Pozycja> potencjalneRuchy = figura.getDostepneRuchy(this);

        for (Pozycja cel : potencjalneRuchy) {
            Plansza kopia = new Plansza(this);
            kopia.wykonajRuch(start, cel);
            if (!kopia.czyKrolJestWszachu(kolor)) {
                legalneRuchy.add(cel);
            }
        }
        return legalneRuchy;
    }

    public boolean czyRuchJestLegalny(Pozycja start, Pozycja koniec, KolorFigur kolorGracza) {
        Figura figura = getFigura(start);
        if (figura == null || figura.getKolorFigur() != kolorGracza) {
            return false;
        }
        List<Pozycja> legalneRuchy = getWszystkieLegalneRuchyDlaFigury(start);
        return legalneRuchy.contains(koniec);
    }

    public boolean czyKrolJestWszachu(KolorFigur kolorKrola) {
        Pozycja pozycjaKrola = znajdzKrola(kolorKrola);
        if (pozycjaKrola == null) {
            System.err.println("Krytyczny błąd: nie można znaleźć króla koloru " + kolorKrola + " na planszy!");
            return true;
        }
        KolorFigur kolorAtakujacego = (kolorKrola == KolorFigur.WHITE) ? KolorFigur.BLACK : KolorFigur.WHITE;
        return czyPoleJestAtakowane(pozycjaKrola, kolorAtakujacego);
    }

    public Pozycja znajdzKrola(KolorFigur kolor) {
        for (int r = 0; r < ROZMIAR_PLANSZY; r++) {
            for (int k = 0; k < ROZMIAR_PLANSZY; k++) {
                Figura f = pola[r][k];
                if (f != null && f.getTypFigury() == TypFigury.KROL && f.getKolorFigur() == kolor) {
                    return f.getPozycja();
                }
            }
        }
        return null;
    }
    public void promujPionka(Pozycja pozycja, TypFigury nowyTyp) {
        Figura pionek = getFigura(pozycja);
        if (pionek == null || pionek.getTypFigury() != TypFigury.PION) {
            System.err.println("BŁĄD: Próba promocji na polu bez piona: " + pozycja);
            return;
        }

        if (nowyTyp == TypFigury.PION || nowyTyp == TypFigury.KROL) {
            System.err.println("BŁĄD: Niedozwolony typ promocji: " + nowyTyp);
            return;
        }
        KolorFigur kolor = pionek.getKolorFigur();
        Figura nowaFigura = FabrykaFigur.utworzFigure(nowyTyp, kolor);
        setFigura(pozycja, nowaFigura);
    }

    public boolean czyPoleJestAtakowane(Pozycja p, KolorFigur kolorAtakujacego) {
        for (int r = 0; r < ROZMIAR_PLANSZY; r++) {
            for (int k = 0; k < ROZMIAR_PLANSZY; k++) {
                Figura f = pola[r][k];
                if (f != null && f.getKolorFigur() == kolorAtakujacego) {
                    List<Pozycja> atakowanePola;
                    if (f instanceof Pion) {
                        atakowanePola = ((Pion) f).getAtakowanePola(this);
                    } else if (f instanceof Krol) {
                        atakowanePola = ((Krol) f).getDostepneRuchy(this, false);
                    } else {
                        atakowanePola = f.getDostepneRuchy(this);
                    }
                    if (atakowanePola != null && atakowanePola.contains(p)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void ulozenieStandardoweFigur() {
        // Czarne figury
        setFigura(new Pozycja(0, 0), FabrykaFigur.utworzFigure(TypFigury.WIEZA, KolorFigur.BLACK));
        setFigura(new Pozycja(0, 1), FabrykaFigur.utworzFigure(TypFigury.KON, KolorFigur.BLACK));
        setFigura(new Pozycja(0, 2), FabrykaFigur.utworzFigure(TypFigury.GONIEC, KolorFigur.BLACK));
        setFigura(new Pozycja(0, 3), FabrykaFigur.utworzFigure(TypFigury.HETMAN, KolorFigur.BLACK));
        setFigura(new Pozycja(0, 4), FabrykaFigur.utworzFigure(TypFigury.KROL, KolorFigur.BLACK));
        setFigura(new Pozycja(0, 5), FabrykaFigur.utworzFigure(TypFigury.GONIEC, KolorFigur.BLACK));
        setFigura(new Pozycja(0, 6), FabrykaFigur.utworzFigure(TypFigury.KON, KolorFigur.BLACK));
        setFigura(new Pozycja(0, 7), FabrykaFigur.utworzFigure(TypFigury.WIEZA, KolorFigur.BLACK));
        for (int i = 0; i < 8; i++) {
            setFigura(new Pozycja(1, i), FabrykaFigur.utworzFigure(TypFigury.PION, KolorFigur.BLACK));
        }

        // Białe figury
        setFigura(new Pozycja(7, 0), FabrykaFigur.utworzFigure(TypFigury.WIEZA, KolorFigur.WHITE));
        setFigura(new Pozycja(7, 1), FabrykaFigur.utworzFigure(TypFigury.KON, KolorFigur.WHITE));
        setFigura(new Pozycja(7, 2), FabrykaFigur.utworzFigure(TypFigury.GONIEC, KolorFigur.WHITE));
        setFigura(new Pozycja(7, 3), FabrykaFigur.utworzFigure(TypFigury.HETMAN, KolorFigur.WHITE));
        setFigura(new Pozycja(7, 4), FabrykaFigur.utworzFigure(TypFigury.KROL, KolorFigur.WHITE));
        setFigura(new Pozycja(7, 5), FabrykaFigur.utworzFigure(TypFigury.GONIEC, KolorFigur.WHITE));
        setFigura(new Pozycja(7, 6), FabrykaFigur.utworzFigure(TypFigury.KON, KolorFigur.WHITE));
        setFigura(new Pozycja(7, 7), FabrykaFigur.utworzFigure(TypFigury.WIEZA, KolorFigur.WHITE));
        for (int i = 0; i < 8; i++) {
            setFigura(new Pozycja(6, i), FabrykaFigur.utworzFigure(TypFigury.PION, KolorFigur.WHITE));
        }
    }

    public String doZapisuString() {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < ROZMIAR_PLANSZY; r++) {
            for (int k = 0; k < ROZMIAR_PLANSZY; k++) {
                Figura f = getFigura(r, k);
                if (f == null) {
                    sb.append("--");
                } else {
                    sb.append(f.getSymbol());
                }
            }
        }
        return sb.toString();
    }

    public void odczytajZeStringa(String stan) {
        this.pola = new Figura[ROZMIAR_PLANSZY][ROZMIAR_PLANSZY];
        int index = 0;
        for (int r = 0; r < ROZMIAR_PLANSZY; r++) {
            for (int k = 0; k < ROZMIAR_PLANSZY; k++) {
                if (index + 2 > stan.length()) break;
                String symbol = stan.substring(index, index + 2);
                if (!symbol.equals("--")) {
                    KolorFigur kolor;
                    TypFigury typ;

                    if (symbol.equals("BB")) {
                        kolor = KolorFigur.WHITE;
                        typ = TypFigury.KROL;
                    } else if (symbol.equals("CC")) {
                        kolor = KolorFigur.BLACK;
                        typ = TypFigury.KROL;
                    } else {
                        kolor = symbol.charAt(0) == 'B' ? KolorFigur.WHITE : KolorFigur.BLACK;
                        char typSymbol = symbol.charAt(1);
                        typ = switch(typSymbol) {
                            case 'P' -> TypFigury.PION;
                            case 'W' -> TypFigury.WIEZA;
                            case 'K' -> TypFigury.KON;
                            case 'G' -> TypFigury.GONIEC;
                            case 'H' -> TypFigury.HETMAN;
                            default -> null;
                        };
                    }

                    if (typ != null) {
                        Figura nowaFigura = FabrykaFigur.utworzFigure(typ, kolor);
                        nowaFigura.setPozycja(new Pozycja(r, k));
                        this.pola[r][k] = nowaFigura;
                    }
                }
                index += 2;
            }
        }
    }

    /**
     * Zwraca listę wszystkich w pełni legalnych ruchów dla danego koloru.
     * Iteruje po wszystkich figurach danego koloru i zbiera ich legalne ruchy.
     * @param kolor Kolor gracza, dla którego sprawdzane są ruchy.
     * @return Lista legalnych ruchów (tablica dwuelementowa: [start, koniec]).
     */
    public List<Pozycja[]> getWszystkieLegalneRuchy(KolorFigur kolor) {
        List<Pozycja[]> wszystkieLegalneRuchy = new ArrayList<>();
        for (int r = 0; r < ROZMIAR_PLANSZY; r++) {
            for (int k = 0; k < ROZMIAR_PLANSZY; k++) {
                Figura figura = getFigura(r, k);
                if (figura != null && figura.getKolorFigur() == kolor) {
                    List<Pozycja> ruchyFigury = getWszystkieLegalneRuchyDlaFigury(figura.getPozycja());
                    for (Pozycja cel : ruchyFigury) {
                        wszystkieLegalneRuchy.add(new Pozycja[]{figura.getPozycja(), cel});
                    }
                }
            }
        }
        return wszystkieLegalneRuchy;
    }
}