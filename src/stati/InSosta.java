package stati;

import dominio.EsitoOperazione;
import dominio.Monopattino;

/**
 * Sosta breve: la corsa resta attiva e il veicolo è riservato al suo cliente.
 * Per terminare la corsa occorre prima riprendere la marcia (riprendiCorsa).
 */
public class InSosta implements StatoMonopattino {

    private static InSosta istanza;

    private InSosta() {}

    public static InSosta getInstance() {
        if (istanza == null) {
            istanza = new InSosta();
        }
        return istanza;
    }

    @Override
    public EsitoOperazione avviaCorsa(Monopattino m) {
        // Veicolo riservato al cliente che ha attivato la sosta
        return EsitoOperazione.VEICOLO_IN_USO;
    }

    @Override
    public EsitoOperazione riprendiCorsa(Monopattino m) {
        m.setStato(InUso.getInstance());
        return EsitoOperazione.SUCCESSO;
    }

    @Override
    public EsitoOperazione verificaDisponibilita() {
        return EsitoOperazione.VEICOLO_IN_USO;
    }

    @Override
    public boolean isInSosta() {
        return true;
    }

    @Override
    public boolean isCorsaAttiva() {
        return true;
    }
}
