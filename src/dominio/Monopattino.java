package dominio;

import stati.Disponibile;
import stati.StatoMonopattino;
import stati.EsitoOperazione;

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
        this.livelloBatteria = 100;
        this.statoCorrente = Disponibile.getInstance();
    }

    public String getId() {
        return idVeicolo;
    }

    public int getBatteria() {
        return livelloBatteria;
    }

    public StatoMonopattino getStatoCorrente() {
        return statoCorrente;
    }

    public float getLatitudine() {
        return latitudine;
    }

    public float getLongitudine() {
        return longitudine;
    }

    public void setStato(StatoMonopattino s) {
        this.statoCorrente = s;
    }

    public void setPosizione(float lat, float lon) {
        this.latitudine = lat;
        this.longitudine = lon;
    }

    public void setBatteria(int livello) {
        this.livelloBatteria = livello;
    }

    public EsitoOperazione sblocca() {
        return statoCorrente.avviaCorsa(this);
    }

    public EsitoOperazione terminaCorsa() {
        return statoCorrente.terminaCorsa(this);
    }

    public void consumaBatteria(int passiEffettuati) {
        int consumo = passiEffettuati * 4;
        livelloBatteria -= consumo;

        if (livelloBatteria < 0) {
            livelloBatteria = 0;
        }
    }
}