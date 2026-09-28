package dominio;
import java.time.LocalDateTime;
import java.time.Duration;

public class Corsa {
    private Cliente cliente;
    private Monopattino monopattino;
    private LocalDateTime orarioInizio;
    private LocalDateTime orarioFine;
    private float costoTotale;

    public Corsa(Cliente c, Monopattino m) {
        this.cliente = c;
        this.monopattino = m;
        this.orarioInizio = LocalDateTime.now();
    }
    public Cliente getCliente() {
        return this.cliente;
    }
    public Monopattino getMonopattino() {
        return this.monopattino;
    }
    public LocalDateTime getOrarioInizio() {
        return orarioInizio;
    }

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
    public float getCostoTotale() {
        return this.costoTotale;
    }
    public void setCostoTotale(float nuovoCosto) {
        this.costoTotale = nuovoCosto;
    }
}