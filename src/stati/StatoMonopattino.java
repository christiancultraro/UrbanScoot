package stati;

import dominio.EsitoOperazione;
import dominio.Monopattino;

/**
 * Pattern State (GoF): ogni stato concreto decide quali operazioni sono lecite
 * e verso quale stato transita il Monopattino (Context).
 * Le operazioni non ridefinite da uno stato concreto sono, per default, non consentite.
 */
public interface StatoMonopattino {

    // ---------------------------------------------------------------------
    // TRANSIZIONI DEL CICLO DI NOLEGGIO
    // ---------------------------------------------------------------------

    /** Sblocco del veicolo da parte di un cliente (UC1). */
    default EsitoOperazione avviaCorsa(Monopattino m) {
        return EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
    }

    /**
     * Fine regolare della corsa (UC3): consuma la batteria in base ai passi e
     * porta il veicolo in Disponibile oppure, se la batteria è critica, in InManutenzione.
     */
    default EsitoOperazione terminaCorsa(Monopattino m, int passiEffettuati) {
        return EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
    }

    /** Fine anticipata della corsa per guasto/incidente (UC3, estensione 3b). */
    default EsitoOperazione segnalaGuasto(Monopattino m, int passiEffettuati) {
        return EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
    }

    /** Sosta breve (UC7). */
    default EsitoOperazione mettiInPausa(Monopattino m) {
        return EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
    }

    /** Ripresa della marcia dopo una sosta breve (UC7). */
    default EsitoOperazione riprendiCorsa(Monopattino m) {
        return EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
    }

    // ---------------------------------------------------------------------
    // TRANSIZIONI DI MANUTENZIONE (UC4)
    // ---------------------------------------------------------------------

    /** Presa in carico da parte del tecnico. */
    default EsitoOperazione inviaInRiparazione(Monopattino m) {
        return EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
    }

    /** Registrazione della riparazione completata. */
    default EsitoOperazione completaRiparazione(Monopattino m) {
        return EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
    }

    /** Dismissione logica del veicolo (non più utilizzabile). */
    default EsitoOperazione dismetti(Monopattino m) {
        return EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
    }

    // ---------------------------------------------------------------------
    // INTERROGAZIONI
    // ---------------------------------------------------------------------

    /** Esito restituito a chi prova a selezionare il veicolo per noleggiarlo. */
    EsitoOperazione verificaDisponibilita();

    default boolean isDisponibile() { return false; }
    default boolean isInSosta() { return false; }
    default boolean isCorsaAttiva() { return false; }
    default boolean isManutenzione() { return false; }
    default boolean isRiparazione() { return false; }
    default boolean isDismesso() { return false; }
}
