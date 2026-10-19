package stati;

import dominio.EsitoOperazione;
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
    public EsitoOperazione completaRiparazione(Monopattino m) {
        m.ricaricaBatteria();
        m.setStato(Disponibile.getInstance());
        return EsitoOperazione.SUCCESSO;
    }

    @Override
    public EsitoOperazione dismetti(Monopattino m) {
        // Veicolo irrecuperabile (UC4, estensione)
        m.setStato(Dismesso.getInstance());
        return EsitoOperazione.SUCCESSO;
    }

    @Override
    public EsitoOperazione verificaDisponibilita() {
        return EsitoOperazione.VEICOLO_IN_RIPARAZIONE;
    }

    @Override
    public boolean isRiparazione() {
        return true;
    }
}
