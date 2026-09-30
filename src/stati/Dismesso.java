package stati;

import dominio.Monopattino;

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
    public EsitoOperazione terminaCorsa(Monopattino m) {
        return EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
    }

    @Override
    public boolean isDismesso() {
        return true;
    }
    @Override
public EsitoOperazione verificaDisponibilita() {
    return EsitoOperazione.VEICOLO_DISMESSO;
}
}