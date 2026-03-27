package dominio;
public class Utente {
    private String idUtente;

    public Utente(String idUtente) {
        this.idUtente = idUtente;
    }

    public String getUtente() {
        return idUtente;
    }

    // Metodo per restituire l'ID dell'utente
    public String getId() {
        return this.idUtente; // Se la tua variabile si chiama in modo diverso, es. solo "id", metti "this.id"
    }
}