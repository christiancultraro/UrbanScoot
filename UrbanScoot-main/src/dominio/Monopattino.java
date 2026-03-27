package dominio;

import stati.Disponibile;
import stati.StatoMonopattino;

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
        
        // CORREZIONE: Uso corretto del Singleton!
        this.statoCorrente = Disponibile.getInstance();
    }

    // --- GETTER ---
    public String getId() { return idVeicolo; }
    public int getBatteria() { return livelloBatteria; }
    public StatoMonopattino getStatoCorrente() { return statoCorrente; }
    public float getLatitudine() { return latitudine; }
    public float getLongitudine() { return longitudine; }

    // --- METODI DI BUSINESS E SETTER ---
    
    // Aggiorna lo stato (Pattern State)
    public void setStato(StatoMonopattino s) { 
        this.statoCorrente = s; 
    }

    public void setPosizione(float lat, float lon) {
        this.latitudine = lat;
        this.longitudine = lon;
    }

    // Serve all'admin per ricaricare il monopattino a 100
    public void setBatteria(int livello) {
        this.livelloBatteria = livello;
    }

    // CORREZIONE: Ora restituisce un boolean per gestire i fallimenti
    public boolean sblocca() {
        boolean successo = statoCorrente.avviaCorsa(this);
        if (successo) {
            System.out.println("Sblocco veicolo " + idVeicolo + " effettuato.");
        }
        return successo;
    }

    // Il monopattino calcola da solo il proprio consumo (Information Expert)
    public void consumaBatteria(int passiEffettuati) {
        int consumo = passiEffettuati * 4;
        this.livelloBatteria -= consumo;
        if (this.livelloBatteria < 0) {
            this.livelloBatteria = 0;
        }
    }
}