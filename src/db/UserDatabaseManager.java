package db;

import model.AktywnaGra;
import model.Uzytkownik;

import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class UserDatabaseManager {

    private String dbHost;
    private int dbPort;
    private String dbName;
    private String dbUser;
    private String dbPassword;
    private String jdbcUrl;
    private String serverUrl;

    public UserDatabaseManager() {
        // Wczytywanie konfiguracji (bez zmian)
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                System.err.println("Krytyczny błąd: Nie można znaleźć pliku db.properties.");
                throw new RuntimeException("Nie znaleziono pliku db.properties.");
            }
            Properties props = new Properties();
            props.load(input);
            this.dbHost = props.getProperty("DB_HOST");
            this.dbPort = Integer.parseInt(props.getProperty("DB_PORT"));
            this.dbName = props.getProperty("DB_NAME");
            this.dbUser = props.getProperty("DB_USER");
            this.dbPassword = props.getProperty("DB_PASSWORD");
            this.serverUrl = String.format(props.getProperty("DB_SERVER_URL_FORMAT"), dbHost, dbPort, dbUser, dbPassword);
            this.jdbcUrl = String.format(props.getProperty("DB_JDBC_URL_FORMAT"), dbHost, dbPort, dbName, dbUser, dbPassword);
            System.out.println("INFO: Odczytano konfigurację MySQL.");
        } catch (Exception e) {
            System.err.println("KRYTYCZNY BŁĄD: Problem z wczytywaniem konfiguracji: " + e.getMessage());
            throw new RuntimeException("Błąd konfiguracji bazy danych.", e);
        }

        // Rejestracja sterownika (bez zmian)
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("INFO: Sterownik MySQL JDBC załadowany.");
        } catch (ClassNotFoundException e) {
            System.err.println("KRYTYCZNY BŁĄD: Nie znaleziono sterownika MySQL JDBC.");
            throw new RuntimeException("Nie znaleziono sterownika MySQL JDBC", e);
        }

        // Sekwencja startowa
        ensureDatabaseExists();
        ensureTablesExist();
        createDefaultUserIfNeeded();
    }

    private Connection getConnectionToServer() throws SQLException {
        return DriverManager.getConnection(this.serverUrl);
    }

    private Connection getConnectionToDatabase() throws SQLException {
        return DriverManager.getConnection(this.jdbcUrl);
    }

    private void ensureDatabaseExists() {
        System.out.println("INFO: Sprawdzanie/tworzenie bazy danych '" + this.dbName + "'...");
        try (Connection tempConn = getConnectionToServer(); Statement stmt = tempConn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + this.dbName + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;");
            System.out.println("INFO: Baza danych '" + this.dbName + "' gotowa.");
        } catch (SQLException e) {
            System.err.println("KRYTYCZNY BŁĄD: Nie można utworzyć bazy danych: " + e.getMessage());
            throw new RuntimeException("Nie można utworzyć bazy danych", e);
        }
    }

    // ZAKTUALIZOWANA METODA TWORZĄCA POPRAWNE TABELE
    public void ensureTablesExist() {
        System.out.println("INFO: Sprawdzanie/tworzenie tabel...");
        try (Connection conn = getConnectionToDatabase(); Statement stmt = conn.createStatement()) {
            // Tabela Uzytkownicy Z ELO
            String sqlCreateUsersTable = "CREATE TABLE IF NOT EXISTS Uzytkownicy (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "login VARCHAR(255) UNIQUE NOT NULL," +
                    "haslo_hash VARCHAR(255) NOT NULL," +
                    "elo INT NOT NULL DEFAULT 600," + // <-- ELO JEST TUTAJ
                    "data_rejestracji TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";
            stmt.execute(sqlCreateUsersTable);
            System.out.println("INFO: Tabela 'Uzytkownicy' z ELO jest gotowa.");

            // Uproszczona tabela historia_gier BEZ ELO
            String sqlCreateHistoryTable = "CREATE TABLE IF NOT EXISTS historia_gier (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," + // id_meczu
                    "id_gracza_bialego INT NOT NULL," +
                    "id_gracza_czarnego INT NOT NULL," +
                    "kto_wygral_id INT," + // Przechowuje ID zwycięzcy, NULL dla remisu
                    "data_gry TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (id_gracza_bialego) REFERENCES Uzytkownicy(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (id_gracza_czarnego) REFERENCES Uzytkownicy(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (kto_wygral_id) REFERENCES Uzytkownicy(id) ON DELETE SET NULL" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";
            stmt.execute(sqlCreateHistoryTable);
            System.out.println("INFO: Tabela 'historia_gier' (prosta) jest gotowa.");
        } catch (SQLException e) {
            System.err.println("BŁĄD: Problem podczas inicjalizacji tabel: " + e.getMessage());
            throw new RuntimeException("Błąd inicjalizacji tabel.", e);
        }
    }

    // --- NOWE, POPRAWNE METODY OBSŁUGI ---

    public synchronized void zaktualizujEloIZapiszGre(AktywnaGra gra, String gameOverMessage) {
        String[] parts = gameOverMessage.split(":");
        String wynikTyp = parts[1]; // np. CHECKMATE, STALEMATE, RESIGNATION
        String graczBialyLogin = gra.getGraczBialyLogin();
        String graczCzarnyLogin = gra.getGraczCzarnyLogin();

        Integer ktoWygralId = null;

        // ZMIANA: Obsługujemy zarówno mata, jak i poddanie partii
        if (wynikTyp.equals("CHECKMATE") || wynikTyp.equals("RESIGNATION")) {
            String wygranyLogin = parts[2];
            ktoWygralId = getIdUzytkownika(wygranyLogin);

            // Logika zmiany ELO jest taka sama w obu przypadkach
            if (ktoWygralId != null) {
                if (wygranyLogin.equals(graczBialyLogin)) {
                    zmienElo(graczBialyLogin, 20);
                    zmienElo(graczCzarnyLogin, -20);
                } else {
                    zmienElo(graczBialyLogin, -20);
                    zmienElo(graczCzarnyLogin, 20);
                }
            }
        }
        // STALEMATE (remis) nie zmienia ELO, więc nie ma tu dla niego bloku `if`

        // Zapisz do historii BEZ ELO
        zapiszGreDoHistorii(getIdUzytkownika(graczBialyLogin), getIdUzytkownika(graczCzarnyLogin), ktoWygralId);
    }

    private void zmienElo(String login, int zmiana) {
        String sql = "UPDATE Uzytkownicy SET elo = elo + ? WHERE login = ?";
        try (Connection conn = getConnectionToDatabase(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, zmiana);
            pstmt.setString(2, login);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void zapiszGreDoHistorii(int idBialego, int idCzarnego, Integer idZwyciezcy) {
        String sql = "INSERT INTO historia_gier(id_gracza_bialego, id_gracza_czarnego, kto_wygral_id) VALUES(?, ?, ?)";
        try (Connection conn = getConnectionToDatabase(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idBialego);
            pstmt.setInt(2, idCzarnego);
            if (idZwyciezcy != null) {
                pstmt.setInt(3, idZwyciezcy);
            } else {
                pstmt.setNull(3, java.sql.Types.INTEGER);
            }
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private int getIdUzytkownika(String login) {
        String sql = "SELECT id FROM Uzytkownicy WHERE login = ?";
        try (Connection conn = getConnectionToDatabase(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, login);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    public List<String> getLeaderboard() {
        List<String> leaderboard = new ArrayList<>();
        String sql = "SELECT login, elo FROM Uzytkownicy ORDER BY elo DESC LIMIT 20";
        try (Connection conn = getConnectionToDatabase(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                leaderboard.add(rs.getString("login") + "," + rs.getInt("elo"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return leaderboard;
    }

    public List<String> getMatchHistory(String login) {
        List<String> history = new ArrayList<>();
        int userId = getIdUzytkownika(login);
        if (userId == -1) return history;

        String sql = "SELECT h.data_gry, h.kto_wygral_id, ub.login AS login_bialy, uc.login AS login_czarny FROM historia_gier h " +
                "JOIN Uzytkownicy ub ON h.id_gracza_bialego = ub.id " +
                "JOIN Uzytkownicy uc ON h.id_gracza_czarnego = uc.id " +
                "WHERE h.id_gracza_bialego = ? OR h.id_gracza_czarnego = ? ORDER BY h.data_gry DESC LIMIT 30";

        try (Connection conn = getConnectionToDatabase(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String opponent = rs.getString("login_bialy").equals(login) ? rs.getString("login_czarny") : rs.getString("login_bialy");
                int winnerId = rs.getInt("kto_wygral_id");
                String resultText = rs.wasNull() ? "Remis" : (winnerId == userId ? "Wygrana" : "Przegrana");
                String date = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(rs.getTimestamp("data_gry"));
                history.add(String.join(";", date, opponent, resultText));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return history;
    }

    // --- ISTNIEJĄCE METODY BEZ ZMIAN ---

    public String registerUser(String login, String plainPassword) {
        System.out.println("INFO: Próba rejestracji użytkownika: " + login);
        String sql = "INSERT INTO Uzytkownicy(login, haslo_hash) VALUES(?, ?)";

        try (Connection conn = getConnectionToDatabase();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, login);
            pstmt.setString(2, plainPassword);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("INFO: Użytkownik '" + login + "' został pomyślnie zarejestrowany.");
                return "SUCCESS";
            } else {
                return "Rejestracja nie powiodła się z nieznanego powodu.";
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                System.err.println("BŁĄD REJESTRACJI: Użytkownik o loginie '" + login + "' już istnieje.");
                return "Użytkownik o tej nazwie już istnieje.";
            } else {
                System.err.println("BŁĄD REJESTRACJI: Problem SQL podczas rejestracji '" + login + "': " + e.getMessage());
                return "Błąd serwera bazy danych.";
            }
        }
    }

    public Uzytkownik loginUser(String login, String plainPassword) {
        System.out.println("INFO: Próba logowania użytkownika: " + login);
        // Zmieniamy zapytanie, aby pobierało również datę rejestracji
        String sql = "SELECT haslo_hash, data_rejestracji FROM Uzytkownicy WHERE login = ?";

        try (Connection conn = getConnectionToDatabase();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, login);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("haslo_hash");
                if (storedPassword.equals(plainPassword)) {
                    System.out.println("INFO: Logowanie użytkownika '" + login + "' powiodło się.");

                    // ---> KLUCZOWY FRAGMENT <---
                    // Pobieramy datę rejestracji z wyniku zapytania
                    String dataRejestracji = rs.getTimestamp("data_rejestracji").toString();

                    // Tworzymy i zwracamy obiekt Uzytkownik z pobranymi danymi
                    model.Uzytkownik user = new model.Uzytkownik(login); // Używamy pełnej nazwy, aby uniknąć konfliktów
                    user.setDataRejestracji(dataRejestracji);
                    return user;
                    // ---> KONIEC KLUCZOWEGO FRAGMENTU <---

                } else {
                    System.out.println("WARN: Nieprawidłowe hasło dla użytkownika '" + login + "'.");
                    return null; // Hasło się nie zgadza
                }
            } else {
                System.out.println("WARN: Nie znaleziono użytkownika o loginie '" + login + "'.");
                return null; // Nie ma takiego użytkownika
            }
        } catch (SQLException e) {
            System.err.println("BŁĄD LOGOWANIA: Problem SQL podczas logowania '" + login + "': " + e.getMessage());
            return null;
        }
    }

    private void createDefaultUserIfNeeded() {
        String defaultLogin = "admin";
        String defaultPassword = "admin";
        String sqlCheckUser = "SELECT COUNT(*) AS user_count FROM Uzytkownicy WHERE login = ?";
        try (Connection conn = getConnectionToDatabase(); PreparedStatement pstmt = conn.prepareStatement(sqlCheckUser)) {
            pstmt.setString(1, defaultLogin);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt("user_count") == 0) {
                System.out.println("INFO: Tworzenie domyślnego użytkownika 'admin'...");
                registerUser(defaultLogin, defaultPassword);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println("--- START TESTU UserDatabaseManager ---");
        try {
            System.out.println("INFO: Tworzenie instancji UserDatabaseManager...");
            UserDatabaseManager dbManager = new UserDatabaseManager();
            System.out.println("INFO: Instancja UserDatabaseManager utworzona.");
        } catch (Exception e) {
            System.err.println("KRYTYCZNY BŁĄD podczas testu UserDatabaseManager: " + e.getMessage());
        }
        System.out.println("--- KONIEC TESTU UserDatabaseManager ---");
    }
}