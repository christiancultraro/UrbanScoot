package stati;
import dominio.Monopattino;

public class Dismesso implements StatoMonopattino {
    
    @Override
    public boolean avviaCorsa(Monopattino m) {
        System.out.println("Errore: Veicolo dismesso.");
        return false; // Restituisce falso perché l'azione fallisce
    }
    
    @Override
    public boolean terminaCorsa(Monopattino m) {
        System.out.println("Errore: Veicolo non in uso.");
        return false; // Restituisce falso perché l'azione fallisce
    }

    // --- METODO POLIMORFICO AGGIUNTO ---
    @Override
    public boolean isDismesso() {
        return true; // Questo stato risponde "Sì, sono dismesso!"
    }
    
}