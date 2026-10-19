package stati;

import dominio.EsitoOperazione;
import dominio.Monopattino;

/** Stato finale: il veicolo può solo essere rimosso dalla Flotta. */
public class Dismesso implements StatoMonopattino {

    private static Dismesso istanza;

    private Dismesso() {}

    public static Dismesso getInstance() {
        if (istanza == null) {
            istanza = new Dismesso();
        }
        return istanza;
    }

    @Override
    public EsitoOperazione avviaCorsa(Monopattino m) {
        return EsitoOperazione.VEICOLO_DISMESSO;
    }

    @Override
    public EsitoOperazione verificaDisponibilita() {
        return EsitoOperazione.VEICOLO_DISMESSO;
    }

    @Override
    public boolean isDismesso() {
        return true;
    }
}
