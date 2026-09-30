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
import dominio.Tariffa;
import dominio.TariffaStandard;
import dominio.TariffaSostaBreve;
import stati.EsitoOperazione;

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
    private boolean sessioneAdmin;

    private GestoreCorse gestoreCorse;
    private GestoreManutenzione gestoreManutenzione;
    private GestoreAccount gestoreAccount;
    private GestoreStorico gestoreStorico;
    private float ultimaQuotaGuida = 0.0f;
    private float ultimaQuotaSosta = 0.0f;

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
        clientiRegistrati.put("utente", new Cliente("utente", 10.50f));

        Amministratore admin = new Amministratore("admin");
        adminRegistrati.put("admin", admin);

        credenzialiAccesso.put("admin", "admin");
        credenzialiAccesso.put("utente", "utente");

        aggiungiNuovoMonopattino("MONO-01", 0f, 8f);
    }

    // =========================================================================
    // AUTENTICAZIONE E GESTIONE SESSIONE
    // =========================================================================
    public boolean autenticaUtente(String username, String password) {
        if (username == null || password == null) {
            return false;
        }

        String user = username.trim();
        String pass = password.trim();

        if (!credenzialiAccesso.containsKey(user) || !credenzialiAccesso.get(user).equals(pass)) {
            return false;
        }

        if (clientiRegistrati.containsKey(user)) {
            this.clienteCorrente = clientiRegistrati.get(user);
            this.utenteCorrente = this.clienteCorrente;
            this.sessioneAdmin = false;

            // Ripristina l'eventuale corsa o sosta attiva per questo specifico cliente
            this.corsaCorrente = trovaCorsaAttivaPerCliente(this.clienteCorrente);
            this.monopattinoSelezionato = (this.corsaCorrente != null) ? this.corsaCorrente.getMonopattino() : null;
            return true;
        }

        if (adminRegistrati.containsKey(user)) {
            this.clienteCorrente = null;
            this.utenteCorrente = adminRegistrati.get(user);
            this.sessioneAdmin = true;
            this.corsaCorrente = null;
            this.monopattinoSelezionato = null;
            return true;
        }

        return false;
    }

    public boolean isSessioneAdmin() {
        return this.sessioneAdmin;
    }

  private Corsa trovaCorsaAttivaPerCliente(Cliente cl) {
    if (cl == null) return null;
    for (Corsa c : storicoCorse) {
        if (c.getCliente() != null && c.getCliente().getIdCliente().equals(cl.getIdCliente())) {
            Monopattino m = c.getMonopattino();
            if (m != null && m.getStatoCorrente().isCorsaAttiva()) {
                return c;
            }
        }
    }
    return null;
}

    // =========================================================================
    // UC1: AVVIO CORSA
    // =========================================================================
  public EsitoOperazione inserisciCodiceQR(String idVeicolo, String idUtente) {
    if (clienteCorrente == null || !clienteCorrente.getIdCliente().equals(idUtente)) {
        return EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
    }
    
    // Se c'è una corsa pendente ma il monopattino è già Disponibile/Dismesso, azzera la corsa fantasma
    if (corsaCorrente != null && corsaCorrente.getMonopattino().getStatoCorrente().isDisponibile()) {
        corsaCorrente = null;
        monopattinoSelezionato = null;
    }

    if (corsaCorrente != null) {
        return EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
    }
    
    Monopattino m = flotta.cerca(idVeicolo);
    if (m == null) {
        return EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
    }
    
    EsitoOperazione esitoStato = m.verificaDisponibilita();
    if (esitoStato != EsitoOperazione.SUCCESSO) {
        return esitoStato;
    }
    this.monopattinoSelezionato = m;
    return EsitoOperazione.SUCCESSO;
}

    public EsitoOperazione confermaAvvioCorsa() {
        if (monopattinoSelezionato == null || clienteCorrente == null) {
            return EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
        }

        if (!clienteCorrente.puoNoleggiare()) {
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

    public int getBatteriaSelezionato() {
        if (monopattinoSelezionato != null) {
            return monopattinoSelezionato.getBatteria();
        }
        return 0;
    }

    public boolean puoPermettersiCorsa(int passiPrevisti) {
    if (clienteCorrente == null || !clienteCorrente.puoNoleggiare()) {
        return false;
    }
    // Usiamo direttamente la Strategy
    Tariffa tariffa = new TariffaStandard(1.00f, 0.20f, 0.05f);
    float costoStimato = tariffa.calcolaCosto(1, passiPrevisti);
    return clienteCorrente.getCreditoResiduo() >= costoStimato;
}

    // =========================================================================
    // UC7: SOSTA BREVE (Iterazione 4)
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
    public float terminaCorsa(float latArrivo, float lonArrivo, boolean guastoSegnalato, int passiEffettuati) {
        if (corsaCorrente == null || clienteCorrente == null) {
            return 0.0f;
        }

        Tariffa tariffaStandard = new TariffaStandard(1.00f, 0.20f, 0.05f);
        Tariffa tariffaSosta = new TariffaSostaBreve(0.10f);

        float costo = gestoreCorse.terminaCorsa(
                corsaCorrente,
                monopattinoSelezionato,
                clienteCorrente,
                latArrivo,
                lonArrivo,
                guastoSegnalato,
                passiEffettuati,
                tariffaStandard,
                tariffaSosta
        );
        if (guastoSegnalato) {
            this.ultimaQuotaGuida = 0.0f;
            this.ultimaQuotaSosta = 0.0f;
        } else {
            this.ultimaQuotaGuida = corsaCorrente.getQuotaGuida();
            this.ultimaQuotaSosta = corsaCorrente.getQuotaSosta();
        }
        this.corsaCorrente = null;
        this.monopattinoSelezionato = null;
        return costo;
    }

    // =========================================================================
    // GESTIONE ACCOUNT E REGISTRAZIONE
    // =========================================================================
    public float ricaricaCredito(float importo) {
        if (clienteCorrente == null) {
            return -1.0f;
        }
        gestoreAccount.ricaricaCredito(clienteCorrente, importo);
        return clienteCorrente.getCreditoResiduo();
    }

    public List<Corsa> richiediStoricoCorse() {
        if (clienteCorrente == null) {
            return new ArrayList<>();
        }
        return gestoreStorico.getStoricoCliente(clienteCorrente, storicoCorse);
    }

    public float registraNuovoUtente(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return -1.0f;
        }

        String user = username.trim();
        String pass = password.trim();

        if (clientiRegistrati.containsKey(user)
                || adminRegistrati.containsKey(user)
                || credenzialiAccesso.containsKey(user)) {
            return -1.0f;
        }

        float creditoGenerato = (float) (Math.random() * 50) + 10;
        creditoGenerato = Math.round(creditoGenerato * 100.0f) / 100.0f;

        Cliente nuovoCliente = new Cliente(user, creditoGenerato);
        clientiRegistrati.put(user, nuovoCliente);
        credenzialiAccesso.put(user, pass);

        return creditoGenerato;
    }

    // =========================================================================
    // UC2 / UC4: AMMINISTRAZIONE FLOTTA E MANUTENZIONE
    // =========================================================================
    public void aggiungiNuovoMonopattino(String idVeicolo, float lat, float lon) {
        Monopattino m = new Monopattino(idVeicolo, lat, lon);
        flotta.aggiungi(m);
    }

    public List<Monopattino> richiediVeicoliInManutenzione() {
        return gestoreManutenzione.richiediVeicoliInManutenzione(flotta);
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
        return m != null && m.getStatoCorrente().isManutenzione();
    }

    public boolean isVeicoloDismesso(String idVeicolo) {
        Monopattino m = flotta.cerca(idVeicolo);
        return m != null && m.getStatoCorrente().isDismesso();
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

    // =========================================================================
    // GETTER DI SESSIONE
    // =========================================================================
    public Cliente getClienteCorrente() {
        return clienteCorrente;
    }

    public Corsa getCorsaCorrente() {
        return corsaCorrente;
    }

    // QUERY PURA: restituisce un dato e non modifica nulla
    public float getCreditoResiduoCliente() {
            if (clienteCorrente != null) {
                return clienteCorrente.getCreditoResiduo();
            }
            return 0.0f;
    }

    public Monopattino getMonopattinoSelezionato() {
        return monopattinoSelezionato;
    }
    public float getUltimaQuotaGuida() {
        return this.ultimaQuotaGuida;
    }

    public float getUltimaQuotaSosta() {
        return this.ultimaQuotaSosta;
    }
}