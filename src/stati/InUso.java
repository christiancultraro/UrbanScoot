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
}