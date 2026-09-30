package stati;

import dominio.Monopattino;

public interface StatoMonopattino {

    EsitoOperazione avviaCorsa(Monopattino m);

    EsitoOperazione terminaCorsa(Monopattino m);

    default boolean isManutenzione() {
        return false;
    }

    default boolean isRiparazione() {
        return false;
    }

    default boolean isDismesso() {
        return false;
    }

    default boolean isDisponibile() {
        return false;
    }

    default EsitoOperazione inviaInRiparazione(Monopattino m) {
        return EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
    }

    default EsitoOperazione completaRiparazione(Monopattino m) {
        return EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
    }
    default EsitoOperazione mettiInPausa(Monopattino m) {
        return EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
    }

    default EsitoOperazione riprendiCorsa(Monopattino m) {
        return EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
    }

    default boolean isInSosta() {
        return false;
    }
   
    default EsitoOperazione verificaDisponibilita() {
    return EsitoOperazione.SUCCESSO;
}
default boolean isCorsaAttiva() {
    return false;
}
}