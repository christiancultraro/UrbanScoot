package stati;

import dominio.EsitoOperazione;
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
    public EsitoOperazione inviaInRiparazione(Monopattino m) {
        m.setStato(InRiparazione.getInstance());
        return EsitoOperazione.SUCCESSO;
    }

    @Override
    public EsitoOperazione dismetti(Monopattino m) {
        m.setStato(Dismesso.getInstance());
        return EsitoOperazione.SUCCESSO;
    }

    @Override
    public EsitoOperazione verificaDisponibilita() {
        return EsitoOperazione.VEICOLO_IN_MANUTENZIONE;
    }

    @Override
    public boolean isManutenzione() {
        return true;
    }
}
