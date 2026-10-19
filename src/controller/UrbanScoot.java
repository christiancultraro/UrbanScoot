package controller;

import java.util.ArrayList;
import java.util.List;

import dominio.Cliente;
import dominio.Corsa;
import dominio.EsitoOperazione;
import dominio.Flotta;
import dominio.Monopattino;
import dominio.Tariffa;
import dominio.TariffaSostaBreve;
import dominio.TariffaStandard;
import dominio.Utente;

/**
 * Facade Controller (GRASP) e Singleton (GoF).
 * Riceve le operazioni di sistema dalla UI e le delega ai gestori (Pure Fabrication).
 *
 * Command-Query Separation: le operazioni che modificano lo stato del sistema
 * restituiscono solo un EsitoOperazione; i valori calcolati (costo, credito, quote)
 * si leggono con i metodi di interrogazione dedicati.
 */
public class UrbanScoot {

    private static UrbanScoot istanza;

    private Flotta flotta;
    private List<Corsa> storicoCorse;

    // Strategie di tariffazione in vigore (Pattern Strategy)
    private Tariffa tariffaStandard;
    private Tariffa tariffaSosta;

    // Sessione corrente
    private Utente utenteCorrente;
    private Cliente clienteCorrente;
    private Corsa corsaCorrente;
    private Monopattino monopattinoSelezionato;

    // Risultati dell'ultima operazione, consultabili dalla UI
    private float ultimoCostoAddebitato = 0.0f;
    private float ultimaQuotaGuida = 0.0f;
    private float ultimaQuotaSosta = 0.0f;
    private float ultimoCreditoAssegnato = 0.0f;

    // Gestori (Pure Fabrication)
    private GestoreAccount gestoreAccount;
    private GestoreCorse gestoreCorse;
    private GestoreManutenzione gestoreManutenzione;
    private GestoreStorico gestoreStorico;

    private UrbanScoot() {
        flotta = new Flotta();
        storicoCorse = new ArrayList<>();

        tariffaStandard = new TariffaStandard(1.00f, 0.20f, 0.05f);
        tariffaSosta = new TariffaSostaBreve(0.10f);

        gestoreAccount = new GestoreAccount();
        gestoreCorse = new GestoreCorse();
        gestoreManutenzione = new GestoreManutenzione();
        gestoreStorico = new GestoreStorico();
    }

    public static UrbanScoot getInstance() {
        if (istanza == null) {
            istanza = new UrbanScoot();
        }
        return istanza;
    }

    /** Caso d'uso di avviamento: dati iniziali in memoria. */
    public void loadDatiIniziali() {
        gestoreAccount.registraCliente("utente", "utente", 10.50f);
        gestoreAccount.registraAmministratore("admin", "admin");
        flotta.aggiungi(new Monopattino("MONO-01", 0f, 8f));
    }

    // =========================================================================
    // AUTENTICAZIONE E REGISTRAZIONE
    // =========================================================================
    public EsitoOperazione autenticaUtente(String username, String password) {
        Utente u = gestoreAccount.autentica(username, password);
        if (u == null) {
            return EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
        }

        this.utenteCorrente = u;
        // Polimorfismo: Cliente e Amministratore rispondono diversamente a isAdmin()
        this.clienteCorrente = u.isAdmin() ? null : (Cliente) u;

        // Ripristina l'eventuale corsa (o sosta) ancora attiva del cliente
        this.corsaCorrente = trovaCorsaAttivaPerCliente(clienteCorrente);
        this.monopattinoSelezionato = (corsaCorrente != null) ? corsaCorrente.getMonopattino() : null;
        return EsitoOperazione.SUCCESSO;
    }

    public boolean isSessioneAdmin() {
        return utenteCorrente != null && utenteCorrente.isAdmin();
    }

    /** Registra un nuovo cliente; il credito iniziale simula l'addebito sulla carta collegata. */
    public EsitoOperazione registraNuovoUtente(String username, String password) {
        float creditoSimulato = Math.round(((float) (Math.random() * 50) + 10) * 100.0f) / 100.0f;
        EsitoOperazione esito = gestoreAccount.registraCliente(username, password, creditoSimulato);
        if (esito == EsitoOperazione.SUCCESSO) {
            this.ultimoCreditoAssegnato = creditoSimulato;
        }
        return esito;
    }

    private Corsa trovaCorsaAttivaPerCliente(Cliente cl) {
        if (cl == null) return null;
        for (Corsa c : storicoCorse) {
            if (c.getCliente() == cl && c.getOrarioFine() == null
                    && c.getMonopattino().getStatoCorrente().isCorsaAttiva()) {
                return c;
            }
        }
        return null;
    }

    // =========================================================================
    // UC1: INIZIA CORSA
    // =========================================================================
    public EsitoOperazione inserisciCodiceQR(String idVeicolo, String idUtente) {
        if (clienteCorrente == null || !clienteCorrente.getIdCliente().equals(idUtente)) {
            return EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
        }
        if (corsaCorrente != null) {
            // Il cliente ha già una corsa attiva
            return EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
        }

        Monopattino m = flotta.cerca(idVeicolo);
        if (m == null) {
            return EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
        }

        EsitoOperazione esito = m.verificaDisponibilita();
        if (esito == EsitoOperazione.SUCCESSO) {
            this.monopattinoSelezionato = m;
        }
        return esito;
    }

    public EsitoOperazione confermaAvvioCorsa() {
        if (monopattinoSelezionato == null || clienteCorrente == null || corsaCorrente != null) {
            return EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
        }

        EsitoOperazione esito = gestoreCorse.iniziaCorsa(clienteCorrente, monopattinoSelezionato);
        if (esito == EsitoOperazione.SUCCESSO) {
            Corsa c = gestoreCorse.creaCorsa(clienteCorrente, monopattinoSelezionato);
            storicoCorse.add(c);
            this.corsaCorrente = c;
        }
        return esito;
    }

    /** Stima se il credito copre una corsa di passiPrevisti passi (1 minuto di guida). */
    public boolean puoPermettersiCorsa(int passiPrevisti) {
        if (clienteCorrente == null || !clienteCorrente.puoNoleggiare()) {
            return false;
        }
        float costoStimato = tariffaStandard.calcolaCosto(1, passiPrevisti);
        return clienteCorrente.getCreditoResiduo() >= costoStimato;
    }

    // =========================================================================
    // UC7: GESTIONE SOSTA BREVE
    // =========================================================================
    public EsitoOperazione mettiInSosta(float lat, float lon) {
        if (corsaCorrente == null || monopattinoSelezionato == null) {
            return EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
        }
        return gestoreCorse.mettiInSosta(corsaCorrente, monopattinoSelezionato, lat, lon);
    }

    public EsitoOperazione riprendiCorsa() {
        if (corsaCorrente == null || monopattinoSelezionato == null) {
            return EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
        }
        return gestoreCorse.riprendiCorsa(corsaCorrente, monopattinoSelezionato);
    }

    // =========================================================================
    // UC3: TERMINA CORSA E PARCHEGGIO
    // =========================================================================
    /**
     * In caso di successo il costo addebitato e lo scorporo sono consultabili con
     * getUltimoCostoAddebitato(), getUltimaQuotaGuida() e getUltimaQuotaSosta().
     */
    public EsitoOperazione terminaCorsa(float latArrivo, float lonArrivo, boolean guastoSegnalato, int passiEffettuati) {
        if (corsaCorrente == null || clienteCorrente == null) {
            return EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
        }

        EsitoOperazione esito = gestoreCorse.terminaCorsa(
                corsaCorrente,
                monopattinoSelezionato,
                clienteCorrente,
                latArrivo,
                lonArrivo,
                guastoSegnalato,
                passiEffettuati,
                tariffaStandard,
                tariffaSosta);

        if (esito != EsitoOperazione.SUCCESSO) {
            // Transizione non consentita (es. veicolo in sosta): la corsa resta attiva
            return esito;
        }

        this.ultimoCostoAddebitato = corsaCorrente.getCostoTotale();
        this.ultimaQuotaGuida = corsaCorrente.getQuotaGuida();
        this.ultimaQuotaSosta = corsaCorrente.getQuotaSosta();
        this.corsaCorrente = null;
        this.monopattinoSelezionato = null;
        return EsitoOperazione.SUCCESSO;
    }

    // =========================================================================
    // UC5: RICARICA CREDITO
    // =========================================================================
    /** In caso di successo il nuovo saldo è consultabile con getCreditoResiduoCliente(). */
    public EsitoOperazione ricaricaCredito(float importo) {
        return gestoreAccount.ricaricaCredito(clienteCorrente, importo);
    }

    // =========================================================================
    // UC6: VISUALIZZA STORICO CORSE
    // =========================================================================
    public List<Corsa> richiediStoricoCorse() {
        if (clienteCorrente == null) {
            return new ArrayList<>();
        }
        return gestoreStorico.getStoricoCliente(clienteCorrente, storicoCorse);
    }

    // =========================================================================
    // UC2: AGGIUNGI MONOPATTINO ALLA FLOTTA (solo amministratore)
    // =========================================================================
    public EsitoOperazione aggiungiNuovoMonopattino(String idVeicolo, float lat, float lon) {
        if (!isSessioneAdmin() || idVeicolo == null || idVeicolo.trim().isEmpty()) {
            return EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
        }
        boolean aggiunto = flotta.aggiungi(new Monopattino(idVeicolo.trim(), lat, lon));
        return aggiunto ? EsitoOperazione.SUCCESSO : EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
    }

    // =========================================================================
    // UC4: GESTIONE MANUTENZIONE E RIMOZIONE VEICOLO (solo amministratore)
    // =========================================================================
    public List<Monopattino> richiediVeicoliInManutenzione() {
        if (!isSessioneAdmin()) {
            return new ArrayList<>();
        }
        return gestoreManutenzione.richiediVeicoliInManutenzione(flotta);
    }

    public EsitoOperazione prendiInCarico(String idVeicolo) {
        Monopattino m = cercaVeicoloPerAdmin(idVeicolo);
        if (m == null) {
            return EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
        }
        return gestoreManutenzione.prendiInCarico(m);
    }

    public EsitoOperazione registraManutenzioneCompletata(String idVeicolo) {
        Monopattino m = cercaVeicoloPerAdmin(idVeicolo);
        if (m == null) {
            return EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
        }
        return gestoreManutenzione.completaRiparazione(m);
    }

    /** Contratto CO4: dismissione logica (il veicolo resta censito in stato Dismesso). */
    public EsitoOperazione segnaVeicoloDismesso(String idVeicolo) {
        Monopattino m = cercaVeicoloPerAdmin(idVeicolo);
        if (m == null) {
            return EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
        }
        return gestoreManutenzione.segnaVeicoloDismesso(m);
    }

    /** Contratto CO3: rimozione definitiva dalla Flotta di un veicolo già dismesso. */
    public EsitoOperazione dismettiVeicolo(String idVeicolo) {
        Monopattino m = cercaVeicoloPerAdmin(idVeicolo);
        if (m == null) {
            return EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
        }
        return gestoreManutenzione.dismettiVeicolo(m, flotta);
    }

    private Monopattino cercaVeicoloPerAdmin(String idVeicolo) {
        if (!isSessioneAdmin() || idVeicolo == null) {
            return null;
        }
        return flotta.cerca(idVeicolo);
    }

    // =========================================================================
    // INTERROGAZIONI (query pure: non modificano lo stato del sistema)
    // =========================================================================
    public boolean isVeicoloInManutenzione(String idVeicolo) {
        Monopattino m = flotta.cerca(idVeicolo);
        return m != null && m.getStatoCorrente().isManutenzione();
    }

    public boolean isVeicoloInRiparazione(String idVeicolo) {
        Monopattino m = flotta.cerca(idVeicolo);
        return m != null && m.getStatoCorrente().isRiparazione();
    }

    public boolean isVeicoloDismesso(String idVeicolo) {
        Monopattino m = flotta.cerca(idVeicolo);
        return m != null && m.getStatoCorrente().isDismesso();
    }

    public List<Monopattino> getFlotta() {
        return flotta.getVeicoli();
    }

    public int getBatteriaSelezionato() {
        return (monopattinoSelezionato != null) ? monopattinoSelezionato.getBatteria() : 0;
    }

    /** Passi ancora percorribili con la carica del veicolo in uso (UC3, estensione 3a). */
    public int getAutonomiaSelezionato() {
        return (monopattinoSelezionato != null) ? monopattinoSelezionato.getAutonomiaPassi() : 0;
    }

    public Cliente getClienteCorrente() {
        return clienteCorrente;
    }

    public Corsa getCorsaCorrente() {
        return corsaCorrente;
    }

    public Monopattino getMonopattinoSelezionato() {
        return monopattinoSelezionato;
    }

    public float getCreditoResiduoCliente() {
        return (clienteCorrente != null) ? clienteCorrente.getCreditoResiduo() : 0.0f;
    }

    public float getUltimoCostoAddebitato() {
        return this.ultimoCostoAddebitato;
    }

    public float getUltimaQuotaGuida() {
        return this.ultimaQuotaGuida;
    }

    public float getUltimaQuotaSosta() {
        return this.ultimaQuotaSosta;
    }

    public float getUltimoCreditoAssegnato() {
        return this.ultimoCreditoAssegnato;
    }

    /**
     * Metodo ad uso esclusivo dei test per ripristinare il Singleton
     * e garantire l'indipendenza di ogni test case (Teardown).
     */
    public static void resetInstancePerTest() {
        istanza = null;
    }
}
