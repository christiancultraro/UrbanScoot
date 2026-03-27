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

    public boolean puoNoleggiare() {
    return false; // Di default un utente generico non noleggia
    }
    public float getCreditoResiduo() {
        return 0.0f;
    }
    // Polimorfismo: metodo di default per scalare il credito
    public void scalaCredito(float importo) {
        // Nessuna operazione di default. Sarà il Cliente a sovrascriverlo!
    }
}