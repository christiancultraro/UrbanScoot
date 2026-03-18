public class Utente {
    private String idCliente;
    private float creditoResiduo;

    public Utente(String idCliente, float creditoIniziale) {
        this.idCliente = idCliente;
        this.creditoResiduo = creditoIniziale;
    }

    public String getIdCliente() {
        return idCliente;
    }

    public float getCreditoResiduo() {
        return creditoResiduo;
    }
    
    public void scalaCredito(float importo) {
        this.creditoResiduo -= importo;
    }

    // Metodo per restituire l'ID dell'utente/admin
    public String getId() {
        return this.idCliente; // Se la tua variabile si chiama in modo diverso, es. solo "id", metti "this.id"
    }
}