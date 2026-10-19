package controller;

import dominio.Flotta;
import dominio.Monopattino;
import dominio.EsitoOperazione;
import java.util.List;

/**
 * Pure Fabrication: operazioni di manutenzione della flotta (UC4).
 * Le transizioni di stato sono decise dagli stati concreti del Monopattino.
 */
public class GestoreManutenzione {

    public List<Monopattino> richiediVeicoliInManutenzione(Flotta flotta) {
        return flotta.getVeicoliInManutenzione();
    }

    public EsitoOperazione prendiInCarico(Monopattino m) {
        return m.prendiInCarico();
    }

    public EsitoOperazione completaRiparazione(Monopattino m) {
        return m.completaRiparazione();
    }

    /** Dismissione logica: il veicolo resta nella flotta in stato Dismesso. */
    public EsitoOperazione segnaVeicoloDismesso(Monopattino m) {
        return m.dismetti();
    }

    /** Rimozione definitiva: consentita solo per veicoli già dismessi. */
    public EsitoOperazione dismettiVeicolo(Monopattino m, Flotta flotta) {
        if (!m.getStatoCorrente().isDismesso()) {
            return EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
        }
        flotta.rimuovi(m.getId());
        return EsitoOperazione.SUCCESSO;
    }
}
