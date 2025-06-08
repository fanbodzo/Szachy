package db;

import model.Uzytkownik; // Importujemy naszą klasę Uzytkownik

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement; // Będziemy go używać
import java.sql.ResultSet;      // Będziemy go używać
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class UserDatabaseManager {

    private String dbHost;
    private int dbPort;
    private String dbName;
    private String dbUser;
    private String dbPassword;

    private String jdbcUrl;    // Pełny URL do połączenia z konkretną bazą
    private String serverUrl;  // URL do serwera MySQL (bez nazwy bazy, do tworzenia bazy)

    public UserDatabaseManager() {
        // Wczytaj konfigurację z pliku db.properties
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                System.err.println("Krytyczny błąd: Nie można znaleźć pliku db.properties w folderze resources.");
                throw new RuntimeException("Nie znaleziono pliku db.properties. Konfiguracja bazy danych jest wymagana.");
            }
            Properties props = new Properties();
            props.load(input);

            this.dbHost = props.getProperty("DB_HOST");
            this.dbPort = Integer.parseInt(props.getProperty("DB_PORT"));
            this.dbName = props.getProperty("DB_NAME");
            this.dbUser = props.getProperty("DB_USER");
            this.dbPassword = props.getProperty("DB_PASSWORD");

            this.serverUrl = String.format(props.getProperty("DB_SERVER_URL_FORMAT"),
                    dbHost, dbPort, dbUser, dbPassword);
            this.jdbcUrl = String.format(props.getProperty("DB_JDBC_URL_FORMAT"),
                    dbHost, dbPort, dbName, dbUser, dbPassword);

            System.out.println("INFO: Odczytano konfigurację MySQL.");
            System.out.println("INFO: Host: " + dbHost + ", Port: " + dbPort + ", Baza: " + dbName);

        } catch (Exception e) {
            System.err.println("KRYTYCZNY BŁĄD: Problem z wczytywaniem konfiguracji bazy danych z db.properties: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Błąd konfiguracji bazy danych.", e);
        }

        // Rejestracja sterownika MySQL
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("INFO: Sterownik MySQL JDBC został pomyślnie załadowany.");
        } catch (ClassNotFoundException e) {
            System.err.println("KRYTYCZNY BŁĄD: Nie znaleziono sterownika MySQL JDBC. Upewnij się, że mysql-connector-j.jar jest dodany do bibliotek projektu (Modules -> Dependencies).");
            e.printStackTrace();
            throw new RuntimeException("Nie znaleziono sterownika MySQL JDBC", e);
        }

        ensureDatabaseExists();
        initializeUserTable();
        //tworzy defaultowego klienta jak nie ma nic w bazie
        createDefaultUserIfNeeded();
    }

    private Connection getConnectionToServer() throws SQLException {
        // Łączy się z serwerem MySQL (serverUrl nie zawiera nazwy konkretnej bazy danych)
        return DriverManager.getConnection(this.serverUrl);
    }

    private Connection getConnectionToDatabase() throws SQLException {
        // Łączy się z konkretną bazą danych (jdbcUrl zawiera nazwę bazy danych)
        return DriverManager.getConnection(this.jdbcUrl);
    }

    private void ensureDatabaseExists() {
        System.out.println("INFO: Sprawdzanie/tworzenie bazy danych (schema) '" + this.dbName + "'...");
        try (Connection tempConn = getConnectionToServer()) {
            try (Statement stmt = tempConn.createStatement()) {
                String createDbSql = "CREATE DATABASE IF NOT EXISTS `" + this.dbName + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;";
                stmt.executeUpdate(createDbSql);
                System.out.println("INFO: Baza danych (schema) '" + this.dbName + "' sprawdzona/utworzona.");
            }
        } catch (SQLException e) {
            System.err.println("KRYTYCZNY BŁĄD: Nie można sprawdzić/utworzyć bazy danych '" + this.dbName + "': " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Nie można utworzyć/zweryfikować bazy danych: " + this.dbName, e);
        }
    }

    public void initializeUserTable() {
        String sqlCreateUsersTable = "CREATE TABLE IF NOT EXISTS Uzytkownicy (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "login VARCHAR(255) UNIQUE NOT NULL," +
                "haslo_hash VARCHAR(255) NOT NULL," +
                "data_rejestracji TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

        System.out.println("INFO: Sprawdzanie/tworzenie tabeli 'Uzytkownicy' w bazie '" + this.dbName + "'...");
        try (Connection conn = getConnectionToDatabase();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sqlCreateUsersTable);
            System.out.println("INFO: Tabela 'Uzytkownicy' w bazie '" + this.dbName + "' została sprawdzona/utworzona.");
        } catch (SQLException e) {
            System.err.println("BŁĄD: Problem podczas inicjalizacji tabeli Uzytkownicy w bazie '" + this.dbName + "': " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Metody do implementacji później (rejestracja, logowanie)
    // Na razie są to tylko puste szablony (placeholdery)

    public boolean registerUser(String login, String plainPassword) {
        System.out.println("INFO: Próba rejestracji użytkownika: " + login);
        // Zgodnie z prośbą, na razie przechowujemy hasło jako zwykły tekst w kolumnie `haslo_hash`.
        // W produkcyjnej aplikacji NALEŻY użyć algorytmu do hashowania (np. BCrypt).
        String sql = "INSERT INTO Uzytkownicy(login, haslo_hash) VALUES(?, ?)";

        try (Connection conn = getConnectionToDatabase();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, login);
            pstmt.setString(2, plainPassword);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("INFO: Użytkownik '" + login + "' został pomyślnie zarejestrowany.");
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            // Kod 1062 to błąd duplikatu klucza unikalnego (login już istnieje)
            if (e.getErrorCode() == 1062) {
                System.err.println("BŁĄD REJESTRACJI: Użytkownik o loginie '" + login + "' już istnieje.");
            } else {
                System.err.println("BŁĄD REJESTRACJI: Problem SQL podczas rejestracji '" + login + "': " + e.getMessage());
            }
            return false;
        }
    }

    // Zmieniamy typ zwracany na boolean dla prostoty
    public boolean loginUser(String login, String plainPassword) {
        System.out.println("INFO: Próba logowania użytkownika: " + login);
        String sql = "SELECT haslo_hash FROM Uzytkownicy WHERE login = ?";

        try (Connection conn = getConnectionToDatabase();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, login);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Znaleziono użytkownika, teraz sprawdzamy hasło
                String storedPassword = rs.getString("haslo_hash");
                boolean passwordMatches = storedPassword.equals(plainPassword);
                if (passwordMatches) {
                    System.out.println("INFO: Logowanie użytkownika '" + login + "' powiodło się.");
                } else {
                    System.out.println("WARN: Nieprawidłowe hasło dla użytkownika '" + login + "'.");
                }
                return passwordMatches;
            } else {
                System.out.println("WARN: Nie znaleziono użytkownika o loginie '" + login + "'.");
                return false; // Nie ma takiego użytkownika
            }
        } catch (SQLException e) {
            System.err.println("BŁĄD LOGOWANIA: Problem SQL podczas logowania '" + login + "': " + e.getMessage());
            return false;
        }
    }

    // Główna metoda do testowania inicjalizacji
    public static void main(String[] args) {
        System.out.println("--- START TESTU UserDatabaseManager ---");
        try {
            // Upewnij się, że XAMPP i MySQL są uruchomione!
            System.out.println("INFO: Tworzenie instancji UserDatabaseManager...");
            UserDatabaseManager dbManager = new UserDatabaseManager();
            System.out.println("INFO: Instancja UserDatabaseManager utworzona. Baza i tabela powinny być zainicjalizowane.");

            // Opcjonalny test, czy można się połączyć i wykonać proste zapytanie
            System.out.println("INFO: Testowanie połączenia z bazą danych '" + dbManager.dbName + "'...");
            try (Connection conn = dbManager.getConnectionToDatabase();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Uzytkownicy")) {
                if (rs.next()) {
                    System.out.println("INFO: Testowe zapytanie do tabeli Uzytkownicy wykonane pomyślnie. Liczba użytkowników (początkowo): " + rs.getInt(1));
                }
            } catch (SQLException e) {
                System.err.println("BŁĄD: Problem podczas testowego zapytania do tabeli Uzytkownicy: " + e.getMessage());
                e.printStackTrace();
            }

        } catch (Exception e) {
            System.err.println("KRYTYCZNY BŁĄD podczas testu UserDatabaseManager: " + e.getMessage());
            e.printStackTrace();
            System.err.println("--- Upewnij się, że serwer MySQL (XAMPP) jest uruchomiony! ---");
            System.err.println("--- Sprawdź konfigurację w pliku src/resources/db.properties ---");
            System.err.println("--- Sprawdź, czy sterownik MySQL Connector/J jest poprawnie dodany do projektu ---");
        }
        System.out.println("--- KONIEC TESTU UserDatabaseManager ---");
    }
    // W pliku db/UserDatabaseManager.java

    private void createDefaultUserIfNeeded() {
        String defaultLogin = "admin";
        String defaultPassword = "admin";

        // Zapytanie, które sprawdzi, czy użytkownik o danym loginie już istnieje
        String sqlCheckUser = "SELECT COUNT(*) AS user_count FROM Uzytkownicy WHERE login = ?";

        try (Connection conn = getConnectionToDatabase();
             PreparedStatement pstmt = conn.prepareStatement(sqlCheckUser)) {

            pstmt.setString(1, defaultLogin);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int userCount = rs.getInt("user_count");
                if (userCount == 0) {
                    // Jeśli nie ma takiego użytkownika, to go rejestrujemy
                    System.out.println("INFO: Domyślny użytkownik '" + defaultLogin + "' nie istnieje. Tworzenie...");
                    // Używamy naszej istniejącej metody registerUser!
                    registerUser(defaultLogin, defaultPassword);
                } else {
                    // Jeśli użytkownik już jest w bazie, nic nie robimy
                    System.out.println("INFO: Domyślny użytkownik '" + defaultLogin + "' już istnieje w bazie danych.");
                }
            }

        } catch (SQLException e) {
            System.err.println("BŁĄD: Nie można było sprawdzić/utworzyć domyślnego użytkownika: " + e.getMessage());
            e.printStackTrace();
        }
    }
}