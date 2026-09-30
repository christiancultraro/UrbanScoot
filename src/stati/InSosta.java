package stati;

import dominio.Monopattino;

public class InSosta implements StatoMonopattino {

    private static InSosta istanza;

    private InSosta() {}

    public static InSosta getInstance() {
        if (istanza == null) {
            istanza = new InSosta();
        }
        return istanza;
    }

    @Override
    public EsitoOperazione avviaCorsa(Monopattino m) {
        // Veicolo bloccato per altri utenti
        return EsitoOperazione.VEICOLO_IN_USO;
    }

    @Override
    public EsitoOperazione terminaCorsa(Monopattino m) {
        return EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
    }

    @Override
    public EsitoOperazione riprendiCorsa(Monopattino m) {
        m.setStato(InUso.getInstance());
        return EsitoOperazione.SUCCESSO;
    }

    @Override
    public boolean isInSosta() {
        return true;
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