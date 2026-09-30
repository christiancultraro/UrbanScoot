package stati;

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
    public EsitoOperazione terminaCorsa(Monopattino m) {
        m.setStato(Disponibile.getInstance());
        return EsitoOperazione.SUCCESSO;
    }
    
    @Override
    public EsitoOperazione mettiInPausa(Monopattino m) {
        m.setStato(InSosta.getInstance());
        return EsitoOperazione.SUCCESSO;
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
    public boolean isCorsaAttiva() {
        return true;
}
}