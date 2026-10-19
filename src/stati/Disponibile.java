package stati;

import dominio.EsitoOperazione;
import dominio.Monopattino;

public class Disponibile implements StatoMonopattino {

    private static Disponibile istanza;

    private Disponibile() {}

    public static Disponibile getInstance() {
        if (istanza == null) {
            istanza = new Disponibile();
        }
        return istanza;
    }

    @Override
    public EsitoOperazione avviaCorsa(Monopattino m) {
        // Un veicolo con batteria critica non può essere sbloccato
        if (m.isBatteriaCritica()) {
            return EsitoOperazione.VEICOLO_IN_MANUTENZIONE;
        }
        m.setStato(InUso.getInstance());
        return EsitoOperazione.SUCCESSO;
    }

    @Override
    public EsitoOperazione dismetti(Monopattino m) {
        m.setStato(Dismesso.getInstance());
        return EsitoOperazione.SUCCESSO;
    }

    @Override
    public EsitoOperazione verificaDisponibilita() {
        return EsitoOperazione.SUCCESSO;
    }

    @Override
    public boolean isDisponibile() {
        return true;
    }
}
