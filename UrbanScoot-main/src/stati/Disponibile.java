package stati;
import dominio.Monopattino;

public class Disponibile implements StatoMonopattino {
    
    // Pattern Singleton per stati senza variabili d'istanza
    private static Disponibile istanza;
    
    // DEVE essere private, altrimenti addio Singleton!
    private Disponibile() {} 
    
    public static Disponibile getInstance() {
        if (istanza == null) {
            istanza = new Disponibile();
        }
        return istanza;
    }
    
    @Override
    public boolean avviaCorsa(Monopattino m) {
        // Il pattern State delega qui il cambiamento dinamico di stato
        m.setStato(InUso.getInstance()); // Cambio stato dinamico!
        System.out.println("Sblocco veicolo " + m.getId() + " effettuato.");
        return true; // L'operazione va a buon fine
    }

    @Override
    public boolean terminaCorsa(Monopattino m) {
        System.out.println("Errore: Il monopattino è già disponibile e fermo.");
        return false; // Operazione non valida in questo stato
    }
}