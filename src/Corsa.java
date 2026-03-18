import java.time.LocalDateTime;
import java.time.Duration;

public class Corsa {
    private Utente utente;
    private Monopattino monopattino;
    private LocalDateTime orarioInizio;
    private LocalDateTime orarioFine;
    private float costoTotale;

    public Corsa(Utente u, Monopattino m) {
        this.utente = u;
        this.monopattino = m;
        this.orarioInizio = LocalDateTime.now();
    }

    public LocalDateTime getOrarioInizio() {
        return orarioInizio;
    }

    // --- METODO AGGIORNATO ---
    // Ora riceve la tariffa e i passi effettuati, non più lat e lon
    public float calcolaCostoFinale(Tariffa t, int passiEffettuati) {
        this.orarioFine = LocalDateTime.now();
        
        long minutiTrascorsi = Duration.between(orarioInizio, orarioFine).toMinutes();
        if (minutiTrascorsi == 0) minutiTrascorsi = 1; 
        
        // LA VERA FORMULA COMPLETA:
        // Scatto iniziale + (Costo Tempo) + (Costo Distanza)
        this.costoTotale = t.getScattoIniziale() + 
                           (minutiTrascorsi * t.getCostoAlMinuto()) + 
                           (passiEffettuati * t.getCostoPerCasella());
                           
        return this.costoTotale;
    }
}