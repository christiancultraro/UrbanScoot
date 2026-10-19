package dominio;

import stati.Disponibile;
import stati.StatoMonopattino;

/**
 * Context del pattern State: le operazioni che cambiano lo stato sono
 * delegate allo stato corrente, che decide se e verso dove transitare.
 */
public class Monopattino {

    public static final int BATTERIA_PIENA = 100;
    /** Soglia (inclusa) sotto la quale il veicolo non è noleggiabile. */
    public static final int SOGLIA_BATTERIA_CRITICA = 10;
    /** Punti percentuali di batteria consumati per ogni passo (casella) percorso. */
    public static final int CONSUMO_PER_PASSO = 4;

    private String idVeicolo;
    private int livelloBatteria;
    private float latitudine;
    private float longitudine;
    private StatoMonopattino statoCorrente;

    public Monopattino(String id, float lat, float lon) {
        this.idVeicolo = id;
        this.latitudine = lat;
        this.longitudine = lon;
        this.livelloBatteria = BATTERIA_PIENA;
        this.statoCorrente = Disponibile.getInstance();
    }

    // ---------------------------------------------------------------------
    // OPERAZIONI DELEGATE ALLO STATO (Pattern State)
    // ---------------------------------------------------------------------

    public EsitoOperazione sblocca() {
        return statoCorrente.avviaCorsa(this);
    }

    public EsitoOperazione terminaCorsa(int passiEffettuati) {
        return statoCorrente.terminaCorsa(this, passiEffettuati);
    }

    public EsitoOperazione segnalaGuasto(int passiEffettuati) {
        return statoCorrente.segnalaGuasto(this, passiEffettuati);
    }

    public EsitoOperazione mettiInPausa() {
        return statoCorrente.mettiInPausa(this);
    }

    public EsitoOperazione riprendiCorsa() {
        return statoCorrente.riprendiCorsa(this);
    }

    public EsitoOperazione prendiInCarico() {
        return statoCorrente.inviaInRiparazione(this);
    }

    public EsitoOperazione completaRiparazione() {
        return statoCorrente.completaRiparazione(this);
    }

    public EsitoOperazione dismetti() {
        return statoCorrente.dismetti(this);
    }

    public EsitoOperazione verificaDisponibilita() {
        return statoCorrente.verificaDisponibilita();
    }

    // ---------------------------------------------------------------------
    // BATTERIA (Information Expert)
    // ---------------------------------------------------------------------

    public void consumaBatteria(int passiEffettuati) {
        livelloBatteria = Math.max(0, livelloBatteria - passiEffettuati * CONSUMO_PER_PASSO);
    }

    public void ricaricaBatteria() {
        this.livelloBatteria = BATTERIA_PIENA;
    }

    public boolean isBatteriaCritica() {
        return livelloBatteria <= SOGLIA_BATTERIA_CRITICA;
    }

    /** Numero massimo di passi percorribili con la carica attuale. */
    public int getAutonomiaPassi() {
        return livelloBatteria / CONSUMO_PER_PASSO;
    }

    // ---------------------------------------------------------------------
    // GETTER / SETTER
    // ---------------------------------------------------------------------

    public String getId() {
        return idVeicolo;
    }

    public int getBatteria() {
        return livelloBatteria;
    }

    public void setBatteria(int livello) {
        this.livelloBatteria = livello;
    }

    public StatoMonopattino getStatoCorrente() {
        return statoCorrente;
    }

    /** Usato esclusivamente dagli stati concreti per effettuare la transizione. */
    public void setStato(StatoMonopattino s) {
        this.statoCorrente = s;
    }

    public float getLatitudine() {
        return latitudine;
    }

    public float getLongitudine() {
        return longitudine;
    }

    public void setPosizione(float lat, float lon) {
        this.latitudine = lat;
        this.longitudine = lon;
    }
}
