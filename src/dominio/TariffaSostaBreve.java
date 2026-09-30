package dominio;

public class TariffaSostaBreve implements Tariffa {
    private float costoAlMinuto;

    public TariffaSostaBreve(float costoAlMinuto) {
        this.costoAlMinuto = costoAlMinuto;
    }

    @Override
    public float calcolaCosto(long minutiTrascorsi, int passiEffettuati) {
        // Durante la sosta i passi sono nulli; si fattura unicamente il tempo a tariffa agevolata
        return minutiTrascorsi * costoAlMinuto;
    }

    @Override
    public String getNomeTariffa() {
        return "Sosta Breve";
    }
}