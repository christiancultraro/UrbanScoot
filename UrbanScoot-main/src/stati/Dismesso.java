package stati;
import dominio.Monopattino;

public class Dismesso implements StatoMonopattino {
    
    // --- PATTERN SINGLETON ---
    private static Dismesso istanza;
    
    private Dismesso() {} // Costruttore privato!
    
    public static Dismesso getInstance() {
        if (istanza == null) istanza = new Dismesso();
        return istanza;
    }
    // -------------------------

    @Override
    public boolean avviaCorsa(Monopattino m) {
        System.out.println("Errore: Veicolo dismesso.");
        return false; 
    }
    
    @Override
    public boolean terminaCorsa(Monopattino m) {
        System.out.println("Errore: Veicolo non in uso.");
        return false; 
    }

    @Override
    public boolean isDismesso() {
        return true; 
    }
}