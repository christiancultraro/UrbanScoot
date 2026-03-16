import java.time.LocalDateTime;

public class Corsa {
    private LocalDateTime orarioInizio;
    private LocalDateTime orarioFine;
    private float costoTotale;
    private Utente utente;
    private Monopattino monopattino;

    public Corsa(Utente u, Monopattino m) {
        this.orarioInizio = LocalDateTime.now();
        this.utente = u;
        this.monopattino = m;
        System.out.println("Corsa creata alle: " + this.orarioInizio);
    }
}
