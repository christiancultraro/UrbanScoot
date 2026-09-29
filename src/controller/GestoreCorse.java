package controller;

import dominio.Corsa;
import dominio.Monopattino;
import dominio.Tariffa; // Importiamo l'interfaccia Strategy
import dominio.Cliente;
import stati.InManutenzione;
import stati.Disponibile;
import stati.EsitoOperazione;

public class GestoreCorse {

    public EsitoOperazione iniziaCorsa(Cliente cliente, Monopattino m) {

        float cred = cliente.getCreditoResiduo();
        int batt = m.getBatteria();

        // Controllo per lo sblocco iniziale
        if (cred < 1.00f) {
            return EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
        }

        if (batt <= 10) {
            return EsitoOperazione.VEICOLO_IN_MANUTENZIONE;
        }

        if (!m.getStatoCorrente().isDisponibile()) {
            return m.sblocca();
        }

        return m.sblocca();
    }

    public Corsa creaCorsa(Cliente cliente, Monopattino m) {
        return new Corsa(cliente, m);
    }

    // NUOVA FIRMA: Il metodo ora riceve la strategia tariffaria da applicare, senza istanziarla internamente
    public float terminaCorsa(
            Corsa corsaCorrente,
            Monopattino monopattinoSelezionato,
            Cliente cliente,
            float latArrivo,
            float lonArrivo,
            boolean guastoSegnalato,
            int passiEffettuati,
            Tariffa tariffaApplicata) { // <-- Parametro Strategy aggiunto

        monopattinoSelezionato.setPosizione(latArrivo, lonArrivo);

        // DELEGA: GestoreCorse inoltra la Strategy a Corsa, applicando l'Information Expert
        float costo = corsaCorrente.calcolaCostoFinale(
            tariffaApplicata, 
            passiEffettuati
        );

        monopattinoSelezionato.consumaBatteria(passiEffettuati);

        if (guastoSegnalato) {

            costo = 0.0f;
            corsaCorrente.setCostoTotale(0.0f);

            monopattinoSelezionato.setStato(
                InManutenzione.getInstance()
            );

        } else {

            if (monopattinoSelezionato.getBatteria() <= 10) {
                monopattinoSelezionato.setStato(
                    InManutenzione.getInstance()
                );
            } else {
                monopattinoSelezionato.setStato(
                    Disponibile.getInstance()
                );
            }
        }

        cliente.scalaCredito(costo);

        return costo;
    }
}