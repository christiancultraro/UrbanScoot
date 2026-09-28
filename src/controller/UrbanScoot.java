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

public class UrbanScoot {
    // Singleton[cite: 6]
    private static UrbanScoot istanza;
    
    private Flotta flotta;
    // Mappe separate per evitare cast e type-checking
    private Map<String, Cliente> clientiRegistrati;
    private Map<String, Amministratore> adminRegistrati;
    private List<Corsa> storicoCorse;
    private Map<String, String> credenzialiAccesso;
    private Map<String, String> ruoliUtenti;
    
    private Corsa corsaCorrente;
    private Monopattino monopattinoSelezionato;
    
    // Sessioni separate per il polimorfismo implicito
    private Utente utenteCorrente; 
    private Cliente clienteCorrente; 
    
    // Gestori (Pure Fabrication)
    private GestoreCorse gestoreCorse;
    private GestoreManutenzione gestoreManutenzione;
    private GestoreAccount gestoreAccount;
    private GestoreStorico gestoreStorico;

    private UrbanScoot() {
        flotta = new Flotta();
        clientiRegistrati = new HashMap<>();
        adminRegistrati = new HashMap<>();
        storicoCorse = new ArrayList<>();
        credenzialiAccesso = new HashMap<>();
        ruoliUtenti = new HashMap<>();
        
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
        aggiungiNuovoMonopattino("MONO-01", 0f, 8f);
        System.out.println("Sistema avviato e dati caricati.\n");
       
        Amministratore admin = new Amministratore("admin");
        adminRegistrati.put("admin", admin);
        
        credenzialiAccesso.put("admin", "admin");
        ruoliUtenti.put("admin", "ADMIN");
        
        credenzialiAccesso.put("utente", "utente");
        ruoliUtenti.put("utente", "UTENTE");
    }

    // Autenticazione pulita basata sulle mappe
    public String autenticaUtente(String username, String password) {
        if (credenzialiAccesso.containsKey(username) && credenzialiAccesso.get(username).equals(password)) {
            if (clientiRegistrati.containsKey(username)) {
                this.clienteCorrente = clientiRegistrati.get(username);
                this.utenteCorrente = this.clienteCorrente; 
                return "UTENTE";
            } else if (adminRegistrati.containsKey(username)) {
                this.clienteCorrente = null;
                this.utenteCorrente = adminRegistrati.get(username);
                return "ADMIN";
            }
        }
        return "ERRORE";
    }

    public void inserisciCodiceQR(String idVeicolo, String idUtente) {
        Monopattino m = flotta.cerca(idVeicolo);
        
        // La lettura del QR è consentita solo a un Cliente registrato
        if (m != null && clientiRegistrati.containsKey(idUtente)) {
            this.monopattinoSelezionato = m;
            this.clienteCorrente = clientiRegistrati.get(idUtente);
            this.utenteCorrente = this.clienteCorrente;
            System.out.println("[App] QR Letto. Veicolo: " + m.getId() + " | Batteria: " + m.getBatteria() + "%");
        } else {
            System.out.println("[App] Errore: Veicolo o Utente non trovato.");
        }
    }

    // --- GESTIONE CORSE (UC1 e UC3) ---

    public boolean confermaAvvioCorsa() {
        // Usiamo clienteCorrente al posto di utenteCorrente
        if (monopattinoSelezionato == null || clienteCorrente == null) return false;

        if (!clienteCorrente.puoNoleggiare()) {
            System.out.println("[App] Errore: l'utente attuale non è abilitato al noleggio.");
            return false;
        }

        Corsa c = gestoreCorse.iniziaCorsa(clienteCorrente, monopattinoSelezionato);
        
        if (c != null) {
            storicoCorse.add(c);
            corsaCorrente = c;
            return true;
        }
        return false;
    }

    public int getBatteriaSelezionato() {
        if (monopattinoSelezionato != null) {
            return monopattinoSelezionato.getBatteria();
        }
        return 0;
    }

    public boolean puoPermettersiCorsa(int passiPrevisti) {
        // Delega all'Information Expert (Cliente)
        if (clienteCorrente == null || !clienteCorrente.puoNoleggiare()) {
            return false;
        }
        float costoStimato = 1.00f + 0.20f + (passiPrevisti * 0.05f);
        return clienteCorrente.getCreditoResiduo() >= costoStimato;
    }

    public float terminaCorsa(float latArrivo, float lonArrivo, boolean guastoSegnalato, int passiEffettuati) {
        if (corsaCorrente == null || clienteCorrente == null) return 0.0f;

        float costo = gestoreCorse.terminaCorsa(corsaCorrente, monopattinoSelezionato, clienteCorrente, latArrivo, lonArrivo, guastoSegnalato, passiEffettuati);
        
        corsaCorrente = null; 
        monopattinoSelezionato = null;
        return costo;
    }

    // --- NUOVI METODI ITERAZIONE 3 (UC5 e UC6) ---

    public float ricaricaCredito(float importo) {
        if (this.clienteCorrente == null) return -1.0f; 
        gestoreAccount.ricaricaCredito(this.clienteCorrente, importo);
        return this.clienteCorrente.getCreditoResiduo();
    }

    public List<Corsa> richiediStoricoCorse() {
        if (this.clienteCorrente != null) {
            return gestoreStorico.getStoricoCliente(this.clienteCorrente, this.storicoCorse);
        }
        return new ArrayList<>();
    }

    // --- GESTIONE MANUTENZIONE E FLOTTA (UC2 e UC4) ---

    public void aggiungiNuovoMonopattino(String idVeicolo, float lat, float lon) {
        Monopattino m = new Monopattino(idVeicolo, lat, lon);
        flotta.aggiungi(m);
        System.out.println("[Admin] Veicolo " + idVeicolo + " registrato.");
    }
    
    public List<Monopattino> richiediVeicoliInManutenzione() {
        return gestoreManutenzione.richiediVeicoliInManutenzione(flotta);
    }

    public void prendiInCarico(String idVeicolo) {
        Monopattino m = flotta.cerca(idVeicolo);
        if (m != null) gestoreManutenzione.prendiInCarico(m);
    }

    public void registraManutenzioneCompletata(String idVeicolo) {
        Monopattino m = flotta.cerca(idVeicolo);
        if (m != null) gestoreManutenzione.completaRiparazione(m);
    }

    public boolean isVeicoloInManutenzione(String idVeicolo) {
        Monopattino m = flotta.cerca(idVeicolo);
        if (m != null) return m.getStatoCorrente().isManutenzione();
        return false;
    }

    public List<Monopattino> getFlotta() {
        return flotta.getVeicoli();
    }

    public boolean isVeicoloDismesso(String idVeicolo) {
        Monopattino m = flotta.cerca(idVeicolo);
        if (m != null) return m.getStatoCorrente().isDismesso();
        return false;
    }
    
    public void dismettiVeicolo(String idVeicolo) {
        Monopattino m = flotta.cerca(idVeicolo);
        if (m != null) gestoreManutenzione.dismettiVeicolo(m, flotta);
    }
    
    public void segnaVeicoloDismesso(String idVeicolo) {
        Monopattino m = flotta.cerca(idVeicolo);
        if (m != null) gestoreManutenzione.segnaVeicoloDismesso(m);
    }

    // --- UTILITY ---

    public float registraNuovoUtente(String username, String password) {
        // Controllo aggiornato per usare la mappa corretta (clientiRegistrati)
        if (clientiRegistrati.containsKey(username) || credenzialiAccesso.containsKey(username)) {
            System.out.println("[App] Errore: Username già in uso.");
            return -1.0f; 
        }

        float creditoGenerato = (float) (Math.random() * 50) + 10;
        creditoGenerato = Math.round(creditoGenerato * 100.0f) / 100.0f;

        Cliente nuovoCliente = new Cliente(username, creditoGenerato);
        
        clientiRegistrati.put(username, nuovoCliente);
        credenzialiAccesso.put(username, password);
        ruoliUtenti.put(username, "UTENTE");
        
        System.out.println("[Sistema] Utente registrato: " + username + " con credito: " + creditoGenerato);
        
        return creditoGenerato; 
    }
}