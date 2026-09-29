package dominio;

public class TariffaStandard implements Tariffa {
    private float scattoIniziale;
    private float costoAlMinuto;
    private float costoPerCasella;

    public TariffaStandard(float scatto, float costoMinuto, float costoCasella) {
        this.scattoIniziale = scatto;
        this.costoAlMinuto = costoMinuto;
        this.costoPerCasella = costoCasella;
    }

    @Override
    public float calcolaCosto(long minutiTrascorsi, int passiEffettuati) {
        return scattoIniziale + (minutiTrascorsi * costoAlMinuto) + (passiEffettuati * costoPerCasella);
    }

    @Override
    public String getNomeTariffa() {
        return "Standard";
    }
}