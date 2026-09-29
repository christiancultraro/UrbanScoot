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
    public EsitoOperazione avviaCorsa(Monopattino m) {
        return EsitoOperazione.VEICOLO_IN_MANUTENZIONE;
    }

    @Override
    public EsitoOperazione terminaCorsa(Monopattino m) {
        return EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
    }

    @Override
    public boolean isManutenzione() {
        return true;
    }

    @Override
    public EsitoOperazione inviaInRiparazione(Monopattino m) {
        m.setStato(InRiparazione.getInstance());
        return EsitoOperazione.SUCCESSO;
    }
}