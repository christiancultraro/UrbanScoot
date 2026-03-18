public class Tariffa {
    private String nomeTariffa;
    private float scattoIniziale;
    private float costoAlMinuto;
    private float costoPerCasella; // <-- NUOVO ATTRIBUTO

    public Tariffa(String nome, float scatto, float costoMinuto, float costoCasella) {
        this.nomeTariffa = nome;
        this.scattoIniziale = scatto;
        this.costoAlMinuto = costoMinuto;
        this.costoPerCasella = costoCasella;
    }

    public float getScattoIniziale() { return scattoIniziale; }
    public float getCostoAlMinuto() { return costoAlMinuto; }
    public float getCostoPerCasella() { return costoPerCasella; }
}