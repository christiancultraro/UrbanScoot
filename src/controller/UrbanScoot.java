
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
    private static UrbanScoot istanza;
    
    // Modificato: uso dell'oggetto Flotta al posto della Map
    private Flotta flotta;
    private Map<String, Utente> utentiRegistrati;
    private List<Corsa> storicoCorse;
    private Map<String, String> credenzialiAccesso;
    private Map<String, String> ruoliUtenti;
    
    private Corsa corsaCorrente;
    private Monopattino monopattinoSelezionato;
    private Utente utenteCorrente;
    
    private GestoreCorse gestoreCorse;
    private GestoreManutenzione gestoreManutenzione;

    private UrbanScoot() {
        flotta = new Flotta();
        utentiRegistrati = new HashMap<>();
        storicoCorse = new ArrayList<>();
        credenzialiAccesso = new HashMap<>();
        ruoliUtenti = new HashMap<>();
        
        gestoreCorse = new GestoreCorse();
        gestoreManutenzione = new GestoreManutenzione();
    }

    public static UrbanScoot getInstance() {
        if (istanza == null) {
            istanza = new UrbanScoot();
        }
        return istanza;
    }

    public void loadDatiIniziali() {
        utentiRegistrati.put("utente", new Cliente("utente", 10.50f));
        aggiungiNuovoMonopattino("MONO-01", 0f, 8f);
        System.out.println("Sistema avviato e dati caricati.\n");
       
        Amministratore admin = new Amministratore("admin");
        utentiRegistrati.put("admin", admin);
        
        credenzialiAccesso.put("admin", "admin");
        ruoliUtenti.put("admin", "ADMIN");
        
        credenzialiAccesso.put("utente", "utente");
        ruoliUtenti.put("utente", "UTENTE");
    }

    public void aggiungiNuovoMonopattino(String idVeicolo, float lat, float lon) {
        Monopattino m = new Monopattino(idVeicolo, lat, lon);
        flotta.aggiungi(m);
        System.out.println("[Admin] Veicolo " + idVeicolo + " registrato.");
    }

    public void inserisciCodiceQR(String idVeicolo, String idUtente) {
        Monopattino m = flotta.cerca(idVeicolo);
        Utente u = utentiRegistrati.get(idUtente);
        
        if (m != null && u != null) {
            this.monopattinoSelezionato = m;
            this.utenteCorrente = u;
            System.out.println("[App] QR Letto. Veicolo: " + m.getId() + " | Batteria: " + m.getBatteria() + "%");
        } else {
            System.out.println("[App] Errore: Veicolo o Utente non trovato.");
        }
    }

    public boolean confermaAvvioCorsa() {
        if (monopattinoSelezionato == null || utenteCorrente == null) return false;

        if (!utenteCorrente.puoNoleggiare()) {
            System.out.println("[App] Errore: l'utente attuale non è abilitato al noleggio.");
            return false;
        }

        // Il gestore crea la corsa, UrbanScoot gestisce l'aggiunta allo storico locale
        Corsa c = gestoreCorse.iniziaCorsa(utenteCorrente, monopattinoSelezionato);
        
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
        if (utenteCorrente == null || !utenteCorrente.puoNoleggiare()) {
            return false;
        }
        float costoStimato = 1.00f + 0.20f + (passiPrevisti * 0.05f);
        return utenteCorrente.getCreditoResiduo() >= costoStimato;
    }

    public void terminaCorsa(float latArrivo, float lonArrivo, boolean guastoSegnalato, int passiEffettuati) {
        if (corsaCorrente == null) return;

        gestoreCorse.terminaCorsa(corsaCorrente, monopattinoSelezionato, utenteCorrente, latArrivo, lonArrivo, guastoSegnalato, passiEffettuati);
        
        corsaCorrente = null; 
        monopattinoSelezionato = null;
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
        if (m != null) {
            return m.getStatoCorrente().isManutenzione();
        }
        return false;
    }
    
    public String autenticaUtente(String username, String password) {
        if (credenzialiAccesso.containsKey(username)) {
            if (credenzialiAccesso.get(username).equals(password)) {
                return ruoliUtenti.get(username); 
            }
        }
        return "ERRORE";
    }

    public List<Monopattino> getFlotta() {
        return flotta.getVeicoli();
    }

    public boolean isVeicoloDismesso(String idVeicolo) {
        Monopattino m = flotta.cerca(idVeicolo);
        if (m != null) {
            return m.getStatoCorrente().isDismesso();
        }
        return false;
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

    public float registraNuovoUtente(String username, String password) {
        if (utentiRegistrati.containsKey(username) || credenzialiAccesso.containsKey(username)) {
            System.out.println("[App] Errore: Username già in uso.");
            return -1.0f; 
        }

        float creditoGenerato = (float) (Math.random() * 50) + 10;
        creditoGenerato = Math.round(creditoGenerato * 100.0f) / 100.0f;

        Cliente nuovoCliente = new Cliente(username, creditoGenerato);
        
        utentiRegistrati.put(username, nuovoCliente);
        credenzialiAccesso.put(username, password);
        ruoliUtenti.put(username, "UTENTE");
        
        System.out.println("[Sistema] Utente registrato: " + username + " con credito: " + creditoGenerato);
        
        return creditoGenerato; 
    }
}