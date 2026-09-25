package controller;

import dominio.Monopattino;
import stati.Dismesso;
import java.util.List;

public class GestoreManutenzione {
    
    public List<Monopattino> richiediVeicoliInManutenzione(Flotta flotta) {
        // Delega direttamente a Flotta (Information Expert)
        return flotta.getVeicoliInManutenzione();
    }
    
    public void prendiInCarico(Monopattino m) {
        m.getStatoCorrente().inviaInRiparazione(m);
    }
    
    public void completaRiparazione(Monopattino m) {
        m.getStatoCorrente().completaRiparazione(m);
    }
    
    public void dismettiVeicolo(Monopattino m, Flotta flotta) {
        flotta.rimuovi(m.getId());
        System.out.println("[Admin] Veicolo " + m.getId() + " RIMOSSO DEFINITIVAMENTE dalla flotta.");
    }
    
    public void segnaVeicoloDismesso(Monopattino m) {
        m.setStato(Dismesso.getInstance());
    }
}