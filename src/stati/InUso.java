package stati;
import dominio.Monopattino;

public class InUso implements StatoMonopattino {
    private static InUso istanza;
    
    private InUso() {} // Costruttore privato
    
    public static InUso getInstance() {
        if (istanza == null) istanza = new InUso();
        return istanza;
    }

    @Override
    public boolean avviaCorsa(Monopattino m) {
        System.out.println("Errore: Il monopattino è già in uso da un altro utente.");
        return false; // L'avvio fallisce
    }

    @Override
    public boolean terminaCorsa(Monopattino m) {
        // Usa il Singleton di Disponibile, NON fare la 'new'!
        m.setStato(Disponibile.getInstance()); 
        System.out.println("Corsa terminata. Veicolo " + m.getId() + " bloccato.");
        return true; // La fine della corsa ha successo
    }
}