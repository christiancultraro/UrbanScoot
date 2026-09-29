package controller;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dominio.Corsa;
import dominio.Monopattino;
import dominio.Utente;
import dominio.Amministratore;
import dominio.Cliente;
import stati.EsitoOperazione;
import dominio.Tariffa;
import dominio.TariffaStandard;


public class UrbanScoot {

    private static UrbanScoot istanza;

    private Flotta flotta;
    private Map<String, Cliente> clientiRegistrati;
    private Map<String, Amministratore> adminRegistrati;
    private Map<String, String> credenzialiAccesso;
    private List<Corsa> storicoCorse;

    private Corsa corsaCorrente;
    private Monopattino monopattinoSelezionato;

    private Utente utenteCorrente;
    private Cliente clienteCorrente;

    private GestoreCorse gestoreCorse;
    private GestoreManutenzione gestoreManutenzione;
    private GestoreAccount gestoreAccount;
    private GestoreStorico gestoreStorico;

    private UrbanScoot() {
        flotta = new Flotta();
        clientiRegistrati = new HashMap<>();
        adminRegistrati = new HashMap<>();
        credenzialiAccesso = new HashMap<>();
        storicoCorse = new ArrayList<>();

        gestoreCorse = new GestoreCorse();
        gestoreManutenzione = new GestoreManutenzione();
        gestoreAccount = new GestoreAccount();
        gestoreStorico = new GestoreStorico();
    }

    public static UrbanScoot getInstance() {
        if (istanza == null) {
            istanza = new UrbanScoot();
        }
        return istanza;
    }

    public void loadDatiIniziali() {
        clientiRegistrati.put(
            "utente",
            new Cliente("utente", 10.50f)
        );

        Amministratore admin = new Amministratore("admin");
        adminRegistrati.put("admin", admin);

        credenzialiAccesso.put("admin", "admin");
        credenzialiAccesso.put("utente", "utente");

        aggiungiNuovoMonopattino("MONO-01", 0f, 8f);
    }

    public String autenticaUtente(String username, String password) {

        if (!credenzialiAccesso.containsKey(username)) {
            return "ERRORE";
        }

        if (!credenzialiAccesso.get(username).equals(password)) {
            return "ERRORE";
        }

        if (corsaCorrente != null) {
            return "ERRORE";
        }

        if (clientiRegistrati.containsKey(username)) {
            clienteCorrente = clientiRegistrati.get(username);
            utenteCorrente = clienteCorrente;
            return "UTENTE";
        }

        if (adminRegistrati.containsKey(username)) {
            clienteCorrente = null;
            utenteCorrente = adminRegistrati.get(username);
            return "ADMIN";
        }

        return "ERRORE";
    }

    public EsitoOperazione inserisciCodiceQR(
            String idVeicolo,
            String idUtente) {

        if (clienteCorrente == null) {
            return EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
        }

        if (!clienteCorrente.getIdCliente().equals(idUtente)) {
            return EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
        }

        if (corsaCorrente != null) {
            return EsitoOperazione.VEICOLO_IN_USO;
        }

        Monopattino m = flotta.cerca(idVeicolo);

        if (m == null) {
            return EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
        }

        monopattinoSelezionato = m;

        return EsitoOperazione.SUCCESSO;
    }

    public EsitoOperazione confermaAvvioCorsa() {

        if (monopattinoSelezionato == null) {
            return EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
        }

        if (clienteCorrente == null) {
            return EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
        }

        if (!clienteCorrente.puoNoleggiare()) {
            return EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
        }

        EsitoOperazione esito =
            gestoreCorse.iniziaCorsa(
                clienteCorrente,
                monopattinoSelezionato
            );

        if (esito == EsitoOperazione.SUCCESSO) {

            Corsa c =
                gestoreCorse.creaCorsa(
                    clienteCorrente,
                    monopattinoSelezionato
                );

            storicoCorse.add(c);
            corsaCorrente = c;
        }

        return esito;
    }

    public int getBatteriaSelezionato() {
        if (monopattinoSelezionato != null) {
            return monopattinoSelezionato.getBatteria();
        }

        return 0;
    }

    public boolean puoPermettersiCorsa(int passiPrevisti) {

        if (clienteCorrente == null) {
            return false;
        }

        if (!clienteCorrente.puoNoleggiare()) {
            return false;
        }

        float costoStimato =
            1.00f
            + 0.20f
            + (passiPrevisti * 0.05f);

        return clienteCorrente.getCreditoResiduo() >= costoStimato;
    }

   public float terminaCorsa(
            float latArrivo,
            float lonArrivo,
            boolean guastoSegnalato,
            int passiEffettuati) {

        if (corsaCorrente == null || clienteCorrente == null) {
            return 0.0f;
        }

        // Il Controller seleziona la strategia tariffaria da applicare
        // Qui potrai inserire la logica condizionale per TariffaSostaBreve
        Tariffa tariffaDaApplicare = new TariffaStandard(1.00f, 0.20f, 0.05f);

        float costo =
            gestoreCorse.terminaCorsa(
                corsaCorrente,
                monopattinoSelezionato,
                clienteCorrente,
                latArrivo,
                lonArrivo,
                guastoSegnalato,
                passiEffettuati,
                tariffaDaApplicare // Passaggio della strategia al dominio
            );

        corsaCorrente = null;
        monopattinoSelezionato = null;

        return costo;
    }
    public float ricaricaCredito(float importo) {

        if (clienteCorrente == null) {
            return -1.0f;
        }

        gestoreAccount.ricaricaCredito(
            clienteCorrente,
            importo
        );

        return clienteCorrente.getCreditoResiduo();
    }

    public List<Corsa> richiediStoricoCorse() {

        if (clienteCorrente == null) {
            return new ArrayList<>();
        }

        return gestoreStorico.getStoricoCliente(
            clienteCorrente,
            storicoCorse
        );
    }

    public void aggiungiNuovoMonopattino(
            String idVeicolo,
            float lat,
            float lon) {

        Monopattino m =
            new Monopattino(idVeicolo, lat, lon);

        flotta.aggiungi(m);
    }

    public List<Monopattino> richiediVeicoliInManutenzione() {
        return gestoreManutenzione
            .richiediVeicoliInManutenzione(flotta);
    }

    public void prendiInCarico(String idVeicolo) {

        Monopattino m = flotta.cerca(idVeicolo);

        if (m != null) {
            gestoreManutenzione.prendiInCarico(m);
        }
    }

    public void registraManutenzioneCompletata(String idVeicolo) {

        Monopattino m = flotta.cerca(idVeicolo);

        if (m != null) {
            gestoreManutenzione.completaRiparazione(m);
        }
    }

    public boolean isVeicoloInManutenzione(String idVeicolo) {

        Monopattino m = flotta.cerca(idVeicolo);

        return m != null
            && m.getStatoCorrente().isManutenzione();
    }

    public boolean isVeicoloDismesso(String idVeicolo) {

        Monopattino m = flotta.cerca(idVeicolo);

        return m != null
            && m.getStatoCorrente().isDismesso();
    }

    public List<Monopattino> getFlotta() {
        return flotta.getVeicoli();
    }

    public void dismettiVeicolo(String idVeicolo) {

        Monopattino m = flotta.cerca(idVeicolo);

        if (m != null) {
            gestoreManutenzione.dismettiVeicolo(m, flotta);
        }
    }

    public void segnaVeicoloDismesso(String idVeicolo) {

        Monopattino m = flotta.cerca(idVeicolo);

        if (m != null) {
            gestoreManutenzione.segnaVeicoloDismesso(m);
        }
    }

    public float registraNuovoUtente(
            String username,
            String password) {

        if (clientiRegistrati.containsKey(username)
                || adminRegistrati.containsKey(username)
                || credenzialiAccesso.containsKey(username)) {
            return -1.0f;
        }

        float creditoGenerato =
            (float) (Math.random() * 50) + 10;

        creditoGenerato =
            Math.round(creditoGenerato * 100.0f) / 100.0f;

        Cliente nuovoCliente =
            new Cliente(username, creditoGenerato);

        clientiRegistrati.put(username, nuovoCliente);
        credenzialiAccesso.put(username, password);

        return creditoGenerato;
    }
}