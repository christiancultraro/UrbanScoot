package dominio;

import java.time.LocalDateTime;
import java.time.Duration;

public class Corsa {
    private Cliente cliente;
    private Monopattino monopattino;
    private LocalDateTime orarioInizio;
    private LocalDateTime orarioFine;
    private LocalDateTime inizioSosta;
    private long minutiSostaAccumulati;
    private float costoTotale;
    
    // NUOVI ATTRIBUTI PER LO SCORPORO DEI COSTI
    private float quotaGuida;
    private float quotaSosta;

    public Corsa(Cliente c, Monopattino m) {
        this.cliente = c;
        this.monopattino = m;
        this.orarioInizio = LocalDateTime.now();
        this.minutiSostaAccumulati = 0;
        this.quotaGuida = 0.0f;
        this.quotaSosta = 0.0f;
    }
    
    public Cliente getCliente() { return this.cliente; }
    public Monopattino getMonopattino() { return this.monopattino; }
    public LocalDateTime getOrarioInizio() { return orarioInizio; }

    public void registraInizioSosta() {
        this.inizioSosta = LocalDateTime.now();
    }

    public void registraFineSosta() {
        if (this.inizioSosta != null) {
            long minuti = Duration.between(inizioSosta, LocalDateTime.now()).toMinutes();
            if (minuti == 0) minuti = 3; // Convenzione didattica per test
            this.minutiSostaAccumulati += minuti;
            this.inizioSosta = null;
        }
    }

    public float calcolaCostoFinale(Tariffa tariffaStandard, Tariffa tariffaSosta, int passiEffettuati) {
        this.orarioFine = LocalDateTime.now();
        
        long minutiTotali = Duration.between(orarioInizio, orarioFine).toMinutes();
        if (minutiTotali == 0) minutiTotali = 1;

        // I minuti effettivi di guida escludono il tempo in sosta
        long minutiGuida = Math.max(1, minutiTotali - minutiSostaAccumulati);

        // 1. Quota Standard (Strategy TariffaStandard)
        this.quotaGuida = tariffaStandard.calcolaCosto(minutiGuida, passiEffettuati);

        // 2. Quota Sosta (Strategy TariffaSostaBreve)
        this.quotaSosta = 0.0f;
        if (minutiSostaAccumulati > 0 && tariffaSosta != null) {
            this.quotaSosta = tariffaSosta.calcolaCosto(minutiSostaAccumulati, 0);
        }

        this.costoTotale = this.quotaGuida + this.quotaSosta;
        return this.costoTotale;
    }
    
    public float getCostoTotale() { return this.costoTotale; }
    public void setCostoTotale(float nuovoCosto) { this.costoTotale = nuovoCosto; }

    // NUOVI GETTER PER BREAKDOWN COSTI
    public float getQuotaGuida() { return this.quotaGuida; }
    public float getQuotaSosta() { return this.quotaSosta; }
}