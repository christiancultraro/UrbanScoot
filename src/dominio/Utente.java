package dominio;

public abstract class Utente {
    private String idUtente;

    public Utente(String idUtente) {
        this.idUtente = idUtente;
    }

    public String getId() {
        return this.idUtente;
    }
}