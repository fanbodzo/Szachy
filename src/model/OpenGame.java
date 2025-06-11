package model;

import java.io.Serializable;
import java.util.UUID;

// Prosta klasa do reprezentowania gry oczekującej na drugiego gracza.
public class OpenGame implements Serializable {
    private final String gameId;
    private final String hostPlayerLogin;

    public OpenGame(String hostPlayerLogin) {
        // Używamy UUID do generowania unikalnego ID dla każdej gry
        this.gameId = UUID.randomUUID().toString();
        this.hostPlayerLogin = hostPlayerLogin;
    }

    public String getGameId() {
        return gameId;
    }

    public String getHostPlayerLogin() {
        return hostPlayerLogin;
    }

    // Przesłaniamy toString(), aby ładnie wyświetlać grę w ListView
    @Override
    public String toString() {
        return "Gra gracza: " + hostPlayerLogin;
    }
}