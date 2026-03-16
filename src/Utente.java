public class Utente {
    private String idCliente;
    private float creditoResiduo;

    public Utente(String idCliente, float creditoResiduo) {
        this.idCliente = idCliente;
        this.creditoResiduo = creditoResiduo;
    }

    public String getIdCliente() { return idCliente; }
    public float getCreditoResiduo() { return creditoResiduo; }

    public void scalaCredito(float importo) {
        this.creditoResiduo -= importo;
    }
}