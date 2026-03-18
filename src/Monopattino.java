public class Monopattino {
    private String idVeicolo;
    private int livelloBatteria;
    private float latitudine;
    private float longitudine;
    private StatoMonopattino statoCorrente;

    public Monopattino(String id, float lat, float lon) {
        this.idVeicolo = id;
        this.latitudine = lat;
        this.longitudine = lon;
        this.livelloBatteria = 100; // I monopattini partono carichi
        this.statoCorrente = new Disponibile();
    }

    // --- GETTER ---
    public String getId() { return idVeicolo; }
    public int getBatteria() { return livelloBatteria; }
    public StatoMonopattino getStatoCorrente() { return statoCorrente; }

    // --- METODI DI BUSINESS E SETTER ---
    
    // Aggiorna lo stato (Pattern State)
    public void setStato(StatoMonopattino s) { 
        this.statoCorrente = s; 
    }

    // Questo è il metodo che faceva crashare il programma! Ora è corretto.
    public void setPosizione(float lat, float lon) {
        this.latitudine = lat;
        this.longitudine = lon;
    }

    // Serve all'admin per ricaricare il monopattino a 100
    public void setBatteria(int livello) {
        this.livelloBatteria = livello;
    }

    // Transizione per avviare la corsa
    public void sblocca() {
        this.statoCorrente = new InUso();
        System.out.println("Sblocco veicolo " + idVeicolo + " effettuato.");
    }
    public float getLatitudine() { return latitudine; }
    public float getLongitudine() { return longitudine; }
}