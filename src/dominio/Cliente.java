package dominio;
public class Cliente extends Utente{
    private float creditoResiduo;

    public Cliente(String idCliente, float creditoIniziale) {
        super(idCliente);
        this.creditoResiduo = creditoIniziale;
    }

    public String getIdCliente() {
        return this.getId();
    }

    public float getCreditoResiduo() {
        return creditoResiduo;
    }
    
    public void scalaCredito(float importo) {
        this.creditoResiduo -= importo;
    }

    // Metodo per restituire l'ID del cliente
    /*public String getId() {
        return this.idCliente; // Se la tua variabile si chiama in modo diverso, es. solo "id", metti "this.id"
    }*/
}