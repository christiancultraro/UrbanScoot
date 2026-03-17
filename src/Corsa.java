import java.time.LocalDateTime;

public class Corsa {
    private LocalDateTime orarioInizio;
    private LocalDateTime orarioFine;
    private float costoTotale;
    
    // Visibilità verso gli altri oggetti (Associazioni)
    private Utente effettuataDa;
    private Monopattino relativaA;

    public Corsa(Utente u, Monopattino m) {
        this.orarioInizio = LocalDateTime.now(); // Timestamp generato dal sistema
        this.effettuataDa = u;
        this.relativaA = m;
    }
    
    public LocalDateTime getOrarioInizio() {
        return orarioInizio;
    }
}