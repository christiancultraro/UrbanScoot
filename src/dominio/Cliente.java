package dominio;

public class Cliente extends Utente {
    /** Credito minimo richiesto per sbloccare un veicolo. */
    public static final float CREDITO_MINIMO_NOLEGGIO = 1.00f;

    private float creditoResiduo;

    public Cliente(String idCliente, float creditoIniziale) {
        super(idCliente);
        this.creditoResiduo = creditoIniziale;
    }

    public String getIdCliente() {
        return this.getId();
    }

    public void scalaCredito(float importo) {
        this.creditoResiduo -= importo;
    }

    /** @return false se l'importo non è valido (deve essere strettamente positivo). */
    public boolean ricaricaCredito(float importo) {
        if (importo <= 0) {
            return false;
        }
        this.creditoResiduo += importo;
        return true;
    }

    public boolean puoNoleggiare() {
        return this.creditoResiduo >= CREDITO_MINIMO_NOLEGGIO;
    }

    public float getCreditoResiduo() {
        return this.creditoResiduo;
    }
}
