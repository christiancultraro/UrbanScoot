package controller;

import dominio.Monopattino;
import stati.Dismesso;
import java.util.List;

public class GestoreManutenzione {

    public List<Monopattino> richiediVeicoliInManutenzione(Flotta flotta) {
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
    }

    public void segnaVeicoloDismesso(Monopattino m) {
        m.setStato(Dismesso.getInstance());
    }
}