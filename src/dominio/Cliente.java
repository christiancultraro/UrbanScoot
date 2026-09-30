package dominio;

public class Cliente extends Utente {
    private float creditoResiduo;

    public Cliente(String idCliente, float creditoIniziale) {
        super(idCliente);
        this.creditoResiduo = creditoIniziale;
    }

    public String getIdCliente() {
        return this.getId();
    }

    // Metodi propri del Cliente (non più ereditati/sovrascritti da Utente)
    public void scalaCredito(float importo) {
        this.creditoResiduo -= importo;
    }

    public void ricaricaCredito(float importo) {
        if (importo > 0) {
            this.creditoResiduo += importo;
        }
    }

   public boolean puoNoleggiare() {
    return this.creditoResiduo >= 1.00f;
}

    public float getCreditoResiduo() {
        return this.creditoResiduo; 
    }
}