package stati;

import dominio.EsitoOperazione;
import dominio.Monopattino;

public class InUso implements StatoMonopattino {

    private static InUso istanza;

    private InUso() {}

    public static InUso getInstance() {
        if (istanza == null) {
            istanza = new InUso();
        }
        return istanza;
    }

    @Override
    public EsitoOperazione avviaCorsa(Monopattino m) {
        return EsitoOperazione.VEICOLO_IN_USO;
    }

    @Override
    public EsitoOperazione terminaCorsa(Monopattino m, int passiEffettuati) {
        m.consumaBatteria(passiEffettuati);
        if (m.isBatteriaCritica()) {
            m.setStato(InManutenzione.getInstance());
        } else {
            m.setStato(Disponibile.getInstance());
        }
        return EsitoOperazione.SUCCESSO;
    }

    @Override
    public EsitoOperazione segnalaGuasto(Monopattino m, int passiEffettuati) {
        m.consumaBatteria(passiEffettuati);
        m.setStato(InManutenzione.getInstance());
        return EsitoOperazione.SUCCESSO;
    }

    @Override
    public EsitoOperazione mettiInPausa(Monopattino m) {
        m.setStato(InSosta.getInstance());
        return EsitoOperazione.SUCCESSO;
    }

    @Override
    public EsitoOperazione verificaDisponibilita() {
        return EsitoOperazione.VEICOLO_IN_USO;
    }

    @Override
    public boolean isCorsaAttiva() {
        return true;
    }
}
