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

    
    
    @Override
    public void scalaCredito(float importo) {
        this.creditoResiduo -= importo;
    }

    @Override
    public boolean puoNoleggiare() {
    return true; // Solo il cliente ha questo diritto
    }

    @Override
    public float getCreditoResiduo() {
        return this.creditoResiduo; 
    }

   
}