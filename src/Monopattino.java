public class Monopattino {
    private String idVeicolo;
    private int livelloBatteria;
    private float latitudine;
    private float longitudine;
    
    // Riferimento al pattern State
    private StatoMonopattino stato;

    // Costruttore chiamato dal pattern Creator
    public Monopattino(String idVeicolo, float lat, float lon) {
        this.idVeicolo = idVeicolo;
        this.latitudine = lat;
        this.longitudine = lon;
        this.livelloBatteria = 100; // Inizializzato al 100% come da post-condizioni
        this.stato = new Disponibile(); // Stato di default
    }

    public String getId() {
        return idVeicolo;
    }

    public int getBatteria() {
        return livelloBatteria;
    }

    public void setStato(StatoMonopattino nuovoStato) {
        this.stato = nuovoStato;
    }
    
    public StatoMonopattino getStatoCorrente() {
        return stato;
    }
    
    // Delega allo stato concreto
    public void sblocca() {
        stato.avviaCorsa(this);
    }
}