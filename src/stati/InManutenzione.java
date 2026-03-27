package stati;
import dominio.Monopattino;

public class InManutenzione implements StatoMonopattino {
    
    @Override
    public boolean avviaCorsa(Monopattino m) {
        System.out.println("Errore: Veicolo in manutenzione, non noleggiabile.");
        return false;
    }

    @Override
    public boolean terminaCorsa(Monopattino m) {
        System.out.println("Errore: Il veicolo non è attualmente in uso.");
        return false;
    }

    // --- METODI POLIMORFICI AGGIUNTI ---
    @Override 
    public boolean isManutenzione() { 
        return true; 
    }

    @Override 
    public void inviaInRiparazione(Monopattino m) {
        // Se InRiparazione non è un singleton, la new va benissimo!
        m.setStato(new InRiparazione()); 
    }
}