package controller;

import dominio.Cliente;
import dominio.Corsa;
import dominio.Monopattino;
import dominio.Tariffa;
import dominio.EsitoOperazione;

/**
 * Pure Fabrication: coordina il ciclo di vita del noleggio (UC1, UC3, UC7).
 * Non decide le transizioni di stato del veicolo: le delega al Monopattino (State).
 */
public class GestoreCorse {

    public EsitoOperazione iniziaCorsa(Cliente cliente, Monopattino m) {
        if (cliente == null || m == null || !cliente.puoNoleggiare()) {
            return EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
        }
        return m.sblocca();
    }

    public Corsa creaCorsa(Cliente cliente, Monopattino m) {
        return new Corsa(cliente, m);
    }

    public EsitoOperazione mettiInSosta(Corsa c, Monopattino m, float lat, float lon) {
        if (m == null || c == null) {
            return EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
        }
        EsitoOperazione esito = m.mettiInPausa();
        if (esito == EsitoOperazione.SUCCESSO) {
            m.setPosizione(lat, lon);
            c.registraInizioSosta();
        }
        return esito;
    }

    public EsitoOperazione riprendiCorsa(Corsa c, Monopattino m) {
        if (m == null || c == null) {
            return EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
        }
        EsitoOperazione esito = m.riprendiCorsa();
        if (esito == EsitoOperazione.SUCCESSO) {
            c.registraFineSosta();
        }
        return esito;
    }

    /**
     * Chiude la corsa: transizione di stato del veicolo, calcolo del costo e addebito
     * (oppure rimborso integrale in caso di guasto). Il costo addebitato resta
     * consultabile tramite corsa.getCostoTotale().
     */
    public EsitoOperazione terminaCorsa(
            Corsa corsa,
            Monopattino m,
            Cliente cliente,
            float latArrivo,
            float lonArrivo,
            boolean guastoSegnalato,
            int passiEffettuati,
            Tariffa tariffaStandard,
            Tariffa tariffaSosta) {

        // 1. Transizione di stato (Pattern State): consumo batteria e nuovo stato
        EsitoOperazione esito = guastoSegnalato
                ? m.segnalaGuasto(passiEffettuati)
                : m.terminaCorsa(passiEffettuati);
        if (esito != EsitoOperazione.SUCCESSO) {
            return esito;
        }

        // 2. Posizione finale del veicolo
        m.setPosizione(latArrivo, lonArrivo);

        // 3. Calcolo del costo (Pattern Strategy)
        corsa.calcolaCostoFinale(tariffaStandard, tariffaSosta, passiEffettuati);

        // 4. Addebito oppure rimborso integrale
        if (guastoSegnalato) {
            corsa.annullaAddebito();
        } else {
            cliente.scalaCredito(corsa.getCostoTotale());
        }
        return EsitoOperazione.SUCCESSO;
    }
}
