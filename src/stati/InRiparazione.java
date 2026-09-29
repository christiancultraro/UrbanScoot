package stati;

import dominio.Monopattino;

public class InRiparazione implements StatoMonopattino {

    private static InRiparazione istanza;

    private InRiparazione() {}

    public static InRiparazione getInstance() {
        if (istanza == null) {
            istanza = new InRiparazione();
        }
        return istanza;
    }

    @Override
    public EsitoOperazione avviaCorsa(Monopattino m) {
        return EsitoOperazione.VEICOLO_IN_RIPARAZIONE;
    }

    @Override
    public EsitoOperazione terminaCorsa(Monopattino m) {
        return EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
    }

    @Override
    public boolean isRiparazione() {
        return true;
    }

    @Override
    public EsitoOperazione completaRiparazione(Monopattino m) {
        m.setBatteria(100);
        m.setStato(Disponibile.getInstance());
        return EsitoOperazione.SUCCESSO;
    }
}