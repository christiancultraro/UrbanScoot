package controller;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import dominio.Corsa;
import dominio.Monopattino;
import dominio.Tariffa;
import dominio.Utente;
import dominio.Amministratore;
import dominio.Cliente;
import stati.Dismesso;
import stati.Disponibile;
import stati.InManutenzione;
import stati.InRiparazione;


public class UrbanScoot {
    private static UrbanScoot istanza;
    
    private Map<String, Monopattino> flotta;
    private Map<String, Utente> utentiRegistrati;
    private List<Corsa> storicoCorse;
    private Map<String, String> credenzialiAccesso;
    private Map<String, String> ruoliUtenti;
    
    private Corsa corsaCorrente;
    private Monopattino monopattinoSelezionato;
    private Utente utenteCorrente;

    private UrbanScoot() {
        flotta = new HashMap<>();
        utentiRegistrati = new HashMap<>();
        storicoCorse = new ArrayList<>();
        credenzialiAccesso = new HashMap<>();
        ruoliUtenti = new HashMap<>();
    }

    public static UrbanScoot getInstance() {
        if (istanza == null) {
            istanza = new UrbanScoot();
        }
        return istanza;
    }

    public void loadDatiIniziali() {
        utentiRegistrati.put("USR-01", new Cliente("USR-01", 10.50f));
     
        aggiungiNuovoMonopattino("MONO-01", 0f, 8f);
        System.out.println("Sistema avviato e dati caricati.\n");
       
        Amministratore admin = new Amministratore("ADMIN-01");
        utentiRegistrati.put(admin.getId(), admin);
        // --- Dati per il Login ---
        credenzialiAccesso.put("admin", "admin");
        ruoliUtenti.put("admin", "ADMIN");
        
        credenzialiAccesso.put("utente", "utente");
        ruoliUtenti.put("utente", "UTENTE");

    }

    public void aggiungiNuovoMonopattino(String idVeicolo, float lat, float lon) {
        Monopattino m = new Monopattino(idVeicolo, lat, lon);
        flotta.put(idVeicolo, m);
        System.out.println("[Admin] Veicolo " + idVeicolo + " registrato.");
    }

    public void inserisciCodiceQR(String idVeicolo, String idUtente) {
        Monopattino m = flotta.get(idVeicolo);
        Utente u = utentiRegistrati.get(idUtente);
        
        if (m != null && u != null) {
            this.monopattinoSelezionato = m;
            this.utenteCorrente = u;
            System.out.println("[App] QR Letto. Veicolo: " + m.getId() + " | Batteria: " + m.getBatteria() + "%");
        } else {
            System.out.println("[App] Errore: Veicolo o Utente non trovato.");
        }
    }

   // 1. Ora restituisce un boolean!
    public boolean confermaAvvioCorsa() {
        if (monopattinoSelezionato == null || utenteCorrente == null) return false;


        if (!utenteCorrente.puoNoleggiare()) {
            System.out.println("[App] Errore: l'utente attuale non è abilitato al noleggio.");
            return false;
        }

// Se arriva qui, sappiamo che può noleggiare. 
// Chiamiamo getCreditoResiduo() direttamente sull'utenteCorrente!
// Il polimorfismo invocherà in automatico il metodo giusto del Cliente.
             float cred = utenteCorrente.getCreditoResiduo();
                  
            int batt = monopattinoSelezionato.getBatteria();
        // Le nostre regole (Pre-condizioni): Credito >= 1, Batteria > 10%, e deve essere Disponibile
       if (cred >= 1.00 && batt > 10 && monopattinoSelezionato.getStatoCorrente().isDisponibile()) {
            Corsa c = new Corsa(utenteCorrente, monopattinoSelezionato);
            this.corsaCorrente = c; 
            monopattinoSelezionato.sblocca();
            storicoCorse.add(c);
            System.out.println("[App] Sblocco confermato, timer avviato.");
            return true; // Sbloccato con successo!
        } else {
            System.out.println("[App] Errore avvio: Credito/Batteria insufficienti o veicolo in manutenzione.");
            return false; // Bloccato!
        }
    }

    // 2. Metodo per far leggere la batteria all'interfaccia grafica
    public int getBatteriaSelezionato() {
        if (monopattinoSelezionato != null) {
            return monopattinoSelezionato.getBatteria();
        }
        return 0;
    }
// --- NUOVO METODO: PREVENTIVO ---
 public boolean puoPermettersiCorsa(int passiPrevisti) {
        //  Controllo base: c'è un utente ed è abilitato al noleggio?
        // (Se è un Admin, puoNoleggiare() restituirà false e bloccherà tutto subito)
        if (utenteCorrente == null || !utenteCorrente.puoNoleggiare()) {
            return false;
        }
        
        // Calcolo preventivo del costo
        float costoStimato = 1.00f + 0.20f + (passiPrevisti * 0.05f);
        
        // Polimorfismo puro: chiediamo direttamente all'Utente il suo credito, 
        // senza chiederci se è un Cliente o no!
        return utenteCorrente.getCreditoResiduo() >= costoStimato;
    }
  // Aggiungi il parametro 'int passiEffettuati'
  public void terminaCorsa(float latArrivo, float lonArrivo, boolean guastoSegnalato, int passiEffettuati) {
        if (corsaCorrente == null) return;

        // 1. Aggiorniamo la posizione del veicolo
        monopattinoSelezionato.setPosizione(latArrivo, lonArrivo);
        
        // 2. Creazione della tariffa e calcolo del costo base
        Tariffa tariffaBase = new Tariffa("Standard", 1.00f, 0.20f, 0.05f); 
        float costo = corsaCorrente.calcolaCostoFinale(tariffaBase, passiEffettuati);
        
        // 3. Information Expert: è il monopattino a fare i calcoli sulla propria batteria!
        monopattinoSelezionato.consumaBatteria(passiEffettuati); 
        
        System.out.println("\n=== FINE CORSA ===");
        
        // 4. Logica Aziendale: Gestione guasti e rimborsi
        if (guastoSegnalato) {
            System.out.println("[!] ALLARME: Rilevato incidente o guasto critico al veicolo.");
            System.out.println("[!] Politica Aziendale: La corsa viene rimborsata per il disagio causato.");
            costo = 0.0f; // Sconto del 100% per l'incidente
            monopattinoSelezionato.setStato(new InManutenzione());
        } else {
            System.out.println("--- RICEVUTA NORMALE ---");
            if (monopattinoSelezionato.getBatteria() <= 10) {
                monopattinoSelezionato.setStato(new InManutenzione());
            } else {
                // Uso corretto del Singleton per il Pattern State
                monopattinoSelezionato.setStato(Disponibile.getInstance()); 
            }
        }
        
        //  Polimorfismo puro: nessun "instanceof". Se è un Cliente scala, se è Admin ignora!
        utenteCorrente.scalaCredito(costo);
        
        //  Stampa a console della ricevuta finale
        System.out.println("Distanza percorsa: " + passiEffettuati + " caselle");
        System.out.println("Costo Addebitato: " + String.format("%.2f", costo) + " EUR");
        System.out.println("Credito Residuo: " + String.format("%.2f", utenteCorrente.getCreditoResiduo()) + " EUR");
        System.out.println("Batteria Residua: " + monopattinoSelezionato.getBatteria() + "%");
        System.out.println("==================\n");
        
        //  Reset delle variabili di sessione per permettere nuovi noleggi
        corsaCorrente = null; 
        monopattinoSelezionato = null;
    }
    public List<Monopattino> richiediVeicoliInManutenzione() {
        List<Monopattino> veicoliGuasti = new ArrayList<>(); 
        for (Monopattino m : flotta.values()) { 
            // Niente instanceof! È l'oggetto a rispondere.
            if (m.getStatoCorrente().isManutenzione()) {
                veicoliGuasti.add(m);
            }
        }
        return veicoliGuasti;
    }

    public void prendiInCarico(String idVeicolo) {
        Monopattino m = flotta.get(idVeicolo);
        if (m != null) {
            // Deleghiamo la transizione allo stato corrente
            m.getStatoCorrente().inviaInRiparazione(m);
        }
    }

    public void registraManutenzioneCompletata(String idVeicolo) {
        Monopattino m = flotta.get(idVeicolo);
        if (m != null) {
            // Deleghiamo la transizione allo stato corrente
            m.getStatoCorrente().completaRiparazione(m);
        }
    }

    // Usato dalla grafica
    public boolean isVeicoloInManutenzione(String idVeicolo) {
        Monopattino m = flotta.get(idVeicolo);
        if (m != null) {
            return m.getStatoCorrente().isManutenzione();
        }
        return false;
    }
    // --- UC0: AUTENTICAZIONE ---
    public String autenticaUtente(String username, String password) {
        if (credenzialiAccesso.containsKey(username)) {
            if (credenzialiAccesso.get(username).equals(password)) {
                return ruoliUtenti.get(username); // Ritorna "ADMIN" o "UTENTE"
            }
        }
        return "ERRORE";
    }
    // Restituisce la lista di tutti i monopattini per farli disegnare alla mappa
 public List<Monopattino> getFlotta() {
        return new ArrayList<>(flotta.values());
    }

    // Verifica se il veicolo è distrutto (Nero) - POLIMORFICO
    public boolean isVeicoloDismesso(String idVeicolo) {
        Monopattino m = flotta.get(idVeicolo);
        if (m != null) {
            // Usiamo il metodo che abbiamo aggiunto all'interfaccia Stato!
            return m.getStatoCorrente().isDismesso();
        }
        return false;
    }

    
   // UC4 (Estensione): Rimuove il veicolo dalla circolazione
    public void dismettiVeicolo(String idVeicolo) {
        Monopattino m = flotta.get(idVeicolo);
        if (m != null) {
            // Uso corretto del Singleton!
            m.setStato(Dismesso.getInstance()); 
            
            System.out.println("[Admin] Veicolo " + idVeicolo + " impostato come DISMESSO.");
        }
    }
    // Estensione: usato da eventi di gioco (es. trappola) senza rimozione dalla flotta
    // Estensione: usato da eventi di gioco (es. trappola) senza rimozione dalla flotta
    public void segnaVeicoloDismesso(String idVeicolo) {
        Monopattino m = flotta.get(idVeicolo);
        if (m != null) {
            // Sostituito "new" con il Singleton corretto
            m.setStato(Dismesso.getInstance());
        }
    }
}

