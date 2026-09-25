package stati;

import dominio.Monopattino;

public class InRiparazione implements StatoMonopattino {
    
    // --- AGGIUNTO SINGLETON ---
    private static InRiparazione istanza;
    
    private InRiparazione() {}
    
    public static InRiparazione getInstance() {
        if (istanza == null) {
            istanza = new InRiparazione();
        }
        return istanza;
    }
    // --------------------------

    @Override
    public boolean avviaCorsa(Monopattino m) {
        System.out.println("Errore: Veicolo in riparazione.");
        return false;
    }

    @Override
    public boolean terminaCorsa(Monopattino m) {
        System.out.println("Errore: Veicolo non in uso.");
        return false;
    }
    
    @Override 
    public boolean isRiparazione() { 
        return true; 
    }

    @Override 
    public void completaRiparazione(Monopattino m) {
        m.setBatteria(100);
        m.setStato(Disponibile.getInstance()); 
    }
}