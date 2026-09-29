package stati;

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
        m.setStato(InUso.getInstance());
        return EsitoOperazione.SUCCESSO;
    }

    @Override
    public EsitoOperazione terminaCorsa(Monopattino m) {
        return EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
    }

    @Override
    public boolean isDisponibile() {
        return true;
    }
}