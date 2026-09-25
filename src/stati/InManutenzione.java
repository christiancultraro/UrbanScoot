package stati;
import dominio.Monopattino;

public class InManutenzione implements StatoMonopattino {
    
    private static InManutenzione istanza;
    
    private InManutenzione() {}
    
    public static InManutenzione getInstance() {
        if (istanza == null) {
            istanza = new InManutenzione();
        }
        return istanza;
    }

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

    @Override 
    public boolean isManutenzione() { 
        return true; 
    }

   @Override 
    public void inviaInRiparazione(Monopattino m) {
        // Ora usa correttamente il Singleton!
        m.setStato(InRiparazione.getInstance()); 
    }
}