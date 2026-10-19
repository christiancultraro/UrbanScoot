package controller;

import java.util.HashMap;
import java.util.Map;

import dominio.Amministratore;
import dominio.Cliente;
import dominio.EsitoOperazione;
import dominio.Utente;

/**
 * Pure Fabrication: gestione degli account (registro degli utenti, credenziali,
 * autenticazione, registrazione) e del portafoglio del cliente (UC5).
 * Mantiene alta la coesione del Facade Controller UrbanScoot.
 */
public class GestoreAccount {

    private final Map<String, Utente> utentiRegistrati = new HashMap<>();
    private final Map<String, String> credenzialiAccesso = new HashMap<>();

    // =========================================================================
    // AUTENTICAZIONE
    // =========================================================================

    /** @return l'utente autenticato, oppure null se le credenziali non sono valide. */
    public Utente autentica(String username, String password) {
        if (username == null || password == null) {
            return null;
        }
        String user = username.trim();
        String passwordAttesa = credenzialiAccesso.get(user);
        if (passwordAttesa == null || !passwordAttesa.equals(password.trim())) {
            return null;
        }
        return utentiRegistrati.get(user);
    }

    // =========================================================================
    // REGISTRAZIONE
    // =========================================================================

    public EsitoOperazione registraCliente(String username, String password, float creditoIniziale) {
        if (!credenzialiValide(username, password)) {
            return EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
        }
        String user = username.trim();
        registra(new Cliente(user, creditoIniziale), password.trim());
        return EsitoOperazione.SUCCESSO;
    }

    public EsitoOperazione registraAmministratore(String username, String password) {
        if (!credenzialiValide(username, password)) {
            return EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
        }
        registra(new Amministratore(username.trim()), password.trim());
        return EsitoOperazione.SUCCESSO;
    }

    public Cliente cercaCliente(String username) {
        Utente u = (username != null) ? utentiRegistrati.get(username.trim()) : null;
        return (u instanceof Cliente) ? (Cliente) u : null;
    }

    private boolean credenzialiValide(String username, String password) {
        return username != null && !username.trim().isEmpty()
                && password != null && !password.trim().isEmpty()
                && !utentiRegistrati.containsKey(username.trim());
    }

    private void registra(Utente u, String password) {
        utentiRegistrati.put(u.getId(), u);
        credenzialiAccesso.put(u.getId(), password);
    }

    // =========================================================================
    // UC5: RICARICA CREDITO
    // =========================================================================

    public EsitoOperazione ricaricaCredito(Cliente cliente, float importo) {
        if (cliente == null) {
            return EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
        }
        // La validità dell'importo è verificata da Cliente (Information Expert)
        return cliente.ricaricaCredito(importo)
                ? EsitoOperazione.SUCCESSO
                : EsitoOperazione.OPERAZIONE_NON_CONSENTITA;
    }
}
