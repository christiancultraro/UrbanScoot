package controller;

import dominio.Corsa;
import dominio.Monopattino;
import dominio.Tariffa;
import dominio.Cliente;
import stati.InManutenzione;
import stati.Disponibile;
import stati.EsitoOperazione;

public class GestoreCorse {

    public EsitoOperazione iniziaCorsa(Cliente cliente, Monopattino m) {
        if (cliente == null || m == null) {
            return EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
        }
        if (!cliente.puoNoleggiare()) {
            return EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
    }
        if (m.getBatteria() <= 10) {
            return EsitoOperazione.VEICOLO_IN_MANUTENZIONE;
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
        m.setPosizione(lat, lon);
        EsitoOperazione esito = m.mettiInPausa();
        if (esito == EsitoOperazione.SUCCESSO) {
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

    public float terminaCorsa(
            Corsa corsaCorrente,
            Monopattino monopattinoSelezionato,
            Cliente cliente,
            float latArrivo,
            float lonArrivo,
            boolean guastoSegnalato,
            int passiEffettuati,
            Tariffa tariffaStandard,
            Tariffa tariffaSosta) {

        monopattinoSelezionato.setPosizione(latArrivo, lonArrivo);

        float costo = corsaCorrente.calcolaCostoFinale(
                tariffaStandard,
                tariffaSosta,
                passiEffettuati
        );

        monopattinoSelezionato.consumaBatteria(passiEffettuati);

        if (guastoSegnalato) {
            costo = 0.0f;
            corsaCorrente.setCostoTotale(0.0f);
            monopattinoSelezionato.setStato(InManutenzione.getInstance());
        } else {
            if (monopattinoSelezionato.getBatteria() <= 10) {
                monopattinoSelezionato.setStato(InManutenzione.getInstance());
            } else {
                monopattinoSelezionato.setStato(Disponibile.getInstance());
            }
            cliente.scalaCredito(costo);
        }

        return costo;
    }
}