public class Monopattino {
    private String idVeicolo;
    private int livelloBatteria;
    private float latitudine;
    private float longitudine;
    private StatoMonopattino statoCorrente;

    public Monopattino(String idVeicolo, float latitudine, float longitudine) {
        this.idVeicolo = idVeicolo;
        this.latitudine = latitudine;
        this.longitudine = longitudine;
        this.livelloBatteria = 100; // Da contratto, parte al 100%
        this.statoCorrente = new StatoDisponibile(); // Pattern State iniziale
    }

    public String getIdVeicolo() { return idVeicolo; }
    public int getLivelloBatteria() { return livelloBatteria; }
    public StatoMonopattino getStatoCorrente() { return statoCorrente; }

    public void setStato(StatoMonopattino nuovoStato) {
        this.statoCorrente = nuovoStato;
    }

    public void sblocca() {
        statoCorrente.sblocca(this);
    }
}