package model;

import model.enums.KolorFigur;
import model.enums.TypFigury;
import model.rdzen.Plansza;
import model.figury.Figura;
import model.figury.Pion;

import utils.Pozycja;

public class AktywnaGra {
    private final Plansza plansza;
    private final String graczBialyLogin;
    private final String graczCzarnyLogin;
    private KolorFigur kogoTura;
    private boolean graZakonczona = false;

    public AktywnaGra(String gracz1, String gracz2) {
        this.plansza = new Plansza();
        this.plansza.ulozenieStandardoweFigur();
        this.kogoTura = KolorFigur.WHITE;

        if (new java.util.Random().nextBoolean()) {
            this.graczBialyLogin = gracz1;
            this.graczCzarnyLogin = gracz2;
        } else {
            this.graczBialyLogin = gracz2;
            this.graczCzarnyLogin = gracz1;
        }
    }

    public Plansza getPlansza() {
        return plansza;
    }

    public String getGraczBialyLogin() {
        return graczBialyLogin;
    }

    public String getGraczCzarnyLogin() {
        return graczCzarnyLogin;
    }

    public boolean isGraZakonczona() {
        return graZakonczona;
    }

    public synchronized boolean wykonajRuch(String loginGracza, Pozycja start, Pozycja koniec) {
        if (graZakonczona) {
            System.err.println("[Gra] Ruch odrzucony: gra już się zakończyła.");
            return false;
        }

        KolorFigur kolorGracza = null;
        if (loginGracza.equals(graczBialyLogin)) {
            kolorGracza = KolorFigur.WHITE;
        } else if (loginGracza.equals(graczCzarnyLogin)) {
            kolorGracza = KolorFigur.BLACK;
        }

        if (kolorGracza == null || kolorGracza != kogoTura) {
            System.err.println("[Gra] Ruch odrzucony: nie tura gracza " + loginGracza + ". Oczekiwano tury " + kogoTura);
            return false;
        }

        if (plansza.czyRuchJestLegalny(start, koniec, kolorGracza)) {
            Figura ruszanaFigura = plansza.getFigura(start);
            plansza.wykonajRuch(start, koniec);
            boolean isPromotion = (ruszanaFigura.getTypFigury() == TypFigury.PION) &&
                    (koniec.getRzad() == 0 || koniec.getRzad() == 7);

            if (isPromotion) {
                // Automatycznie promuj na HETMANA
                plansza.promujPionka(koniec, TypFigury.HETMAN);
                System.out.println("[Gra] Wykryto automatyczną promocję piona na Hetmana.");
            }

            kogoTura = (kogoTura == KolorFigur.WHITE) ? KolorFigur.BLACK : KolorFigur.WHITE;
            System.out.println("[Gra] Ruch gracza " + loginGracza + " wykonany. Następna tura: " + kogoTura);
            return true;
        } else {
            System.err.println("[Gra] Ruch odrzucony: nielegalny ruch z " + start + " do " + koniec + " dla gracza " + loginGracza);
            return false;
        }
    }

    /**
     * Sprawdza stan gry dla gracza, który ma teraz ruch.
     * @return String z komunikatem o końcu gry lub null, jeśli gra toczy się dalej.
     */
    public String sprawdzStanGry() {
        // ===== POPRAWNE WYWOŁANIE =====
        if (plansza.getWszystkieLegalneRuchy(kogoTura).isEmpty()) {
            this.graZakonczona = true;
            if (plansza.czyKrolJestWszachu(kogoTura)) {
                // Szach-mat
                KolorFigur wygranyKolor = (kogoTura == KolorFigur.WHITE) ? KolorFigur.BLACK : KolorFigur.WHITE;
                String wygranyLogin = (wygranyKolor == KolorFigur.WHITE) ? graczBialyLogin : graczCzarnyLogin;
                System.out.println("[Gra] Koniec gry: Szach-mat! Wygrywa " + wygranyLogin);
                return "GAME_OVER:CHECKMATE:" + wygranyLogin;
            } else {
                // Pat
                System.out.println("[Gra] Koniec gry: Pat! Remis.");
                return "GAME_OVER:STALEMATE";
            }
        }
        return null; // Gra toczy się dalej
    }
}