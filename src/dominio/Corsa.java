package dominio;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Context del pattern Strategy: il calcolo del costo è delegato alle Tariffe.
 * Regole di tariffazione: le frazioni di minuto sono arrotondate per eccesso;
 * il tempo di guida fatturato è almeno 1 minuto e ogni sosta breve almeno 1 minuto.
 */
public class Corsa {
    private final Clock clock;

    private Cliente cliente;
    private Monopattino monopattino;
    private LocalDateTime orarioInizio;
    private LocalDateTime orarioFine;
    private LocalDateTime inizioSosta;
    private long minutiSostaAccumulati;
    private float costoTotale;
    private float quotaGuida;
    private float quotaSosta;

    public Corsa(Cliente c, Monopattino m) {
        this(c, m, Clock.systemDefaultZone());
    }

    /** Il Clock è iniettabile per rendere ripetibili i test sui tempi. */
    public Corsa(Cliente c, Monopattino m, Clock clock) {
        this.clock = clock;
        this.cliente = c;
        this.monopattino = m;
        this.orarioInizio = LocalDateTime.now(clock);
        this.minutiSostaAccumulati = 0;
        this.quotaGuida = 0.0f;
        this.quotaSosta = 0.0f;
    }

    public void registraInizioSosta() {
        this.inizioSosta = LocalDateTime.now(clock);
    }

    public void registraFineSosta() {
        if (this.inizioSosta != null) {
            // Ogni sosta attivata è fatturata almeno 1 minuto
            this.minutiSostaAccumulati += Math.max(1, minutiArrotondati(inizioSosta, LocalDateTime.now(clock)));
            this.inizioSosta = null;
        }
    }

    public float calcolaCostoFinale(Tariffa tariffaStandard, Tariffa tariffaSosta, int passiEffettuati) {
        this.orarioFine = LocalDateTime.now(clock);

        long minutiTotali = minutiArrotondati(orarioInizio, orarioFine);
        // I minuti di guida escludono il tempo trascorso in sosta breve
        long minutiGuida = Math.max(1, minutiTotali - minutiSostaAccumulati);

        // 1. Quota di marcia (Strategy TariffaStandard)
        this.quotaGuida = tariffaStandard.calcolaCosto(minutiGuida, passiEffettuati);

        // 2. Quota di sosta (Strategy TariffaSostaBreve)
        this.quotaSosta = 0.0f;
        if (minutiSostaAccumulati > 0 && tariffaSosta != null) {
            this.quotaSosta = tariffaSosta.calcolaCosto(minutiSostaAccumulati, 0);
        }

        this.costoTotale = this.quotaGuida + this.quotaSosta;
        return this.costoTotale;
    }

    /** Rimborso integrale in caso di guasto/incidente (UC3, estensione 3b). */
    public void annullaAddebito() {
        this.costoTotale = 0.0f;
        this.quotaGuida = 0.0f;
        this.quotaSosta = 0.0f;
    }

    private static long minutiArrotondati(LocalDateTime da, LocalDateTime a) {
        long millis = Math.max(0, Duration.between(da, a).toMillis());
        return (millis + 59_999) / 60_000;
    }

    public Cliente getCliente() { return this.cliente; }
    public Monopattino getMonopattino() { return this.monopattino; }
    public LocalDateTime getOrarioInizio() { return orarioInizio; }
    public LocalDateTime getOrarioFine() { return orarioFine; }
    public long getMinutiSostaAccumulati() { return minutiSostaAccumulati; }
    public float getCostoTotale() { return this.costoTotale; }
    public float getQuotaGuida() { return this.quotaGuida; }
    public float getQuotaSosta() { return this.quotaSosta; }
}
