package model;

public class Uzytkownik {
    private int id;
    private String login;
    private String hasloHash; // Przechowuje zahashowane hasło
    private String dataRejestracji; // Może być String lub java.sql.Timestamp

    // Konstruktor pełny (np. przy odczycie z bazy)
    public Uzytkownik(int id, String login, String hasloHash, String dataRejestracji) {
        this.id = id;
        this.login = login;
        this.hasloHash = hasloHash;
        this.dataRejestracji = dataRejestracji;
    }

    // Konstruktor dla nowych użytkowników (przed zapisem do bazy, ID nada baza)
    public Uzytkownik(String login, String hasloHash) {
        this.login = login;
        this.hasloHash = hasloHash;
    }
    public Uzytkownik(String login) {
        this.login = login;
    }


    // Gettery
    public int getId() { return id; }
    public String getLogin() { return login; }
    public String getHasloHash() { return hasloHash; }
    public String getDataRejestracji() { return dataRejestracji; }

    // Settery (jeśli potrzebne, np. do zmiany hasła lub loginu)
    public void setId(int id) { this.id = id; } // Czasem przydatny
    public void setLogin(String login) { this.login = login; }
    public void setHasloHash(String hasloHash) { this.hasloHash = hasloHash; }
    public void setDataRejestracji(String dataRejestracji) { this.dataRejestracji = dataRejestracji; }

    @Override
    public String toString() {
        return "Uzytkownik{" +
                "id=" + id +
                ", login='" + login + '\'' +
                ", dataRejestracji='" + dataRejestracji + '\'' +
                '}';
    }
}