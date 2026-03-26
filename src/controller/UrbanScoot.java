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
    private Amministratore admin;
    private Cliente cliente;

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
        aggiungiNuovoMonopattino("MONO-01", 37.5f, 15.0f);
        System.out.println("Sistema avviato e dati caricati.\n");
        // Creiamo il conto per i test dell'Amministratore (con fondi infiniti!)
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

        float cred = utenteCorrente.getCreditoResiduo();
        int batt = monopattinoSelezionato.getBatteria();

        // Le nostre regole (Pre-condizioni): Credito >= 1, Batteria > 10%, e deve essere Disponibile
        if (cred >= 1.00 && batt > 10 && monopattinoSelezionato.getStatoCorrente() instanceof Disponibile) {
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
        if (utenteCorrente == null) return false;
        
        // Simula la stessa formula di Corsa.java per capire quanto costerà
        float costoStimato = 1.00f + 0.20f + (passiPrevisti * 0.05f);
        
        return utenteCorrente.getCreditoResiduo() >= costoStimato;
    }
  // Aggiungi il parametro 'int passiEffettuati'
    public void terminaCorsa(float latArrivo, float lonArrivo, boolean guastoSegnalato, int passiEffettuati) {
        if (corsaCorrente == null) return;

        monopattinoSelezionato.setPosizione(latArrivo, lonArrivo);
        
        Tariffa tariffaBase = new Tariffa("Standard", 1.00f, 0.20f, 0.05f); 
        float costo = corsaCorrente.calcolaCostoFinale(tariffaBase, passiEffettuati);
        
        // --- FIX BATTERIA ---
        // Consuma 1% ogni 2 caselle. Forza il salvataggio della nuova batteria!
        // STRESS TEST: Consuma il 10% per ogni singola casella!
        int consumoBatteria = passiEffettuati * 4;
        int nuovaBatteria = monopattinoSelezionato.getBatteria() - consumoBatteria;
        if (nuovaBatteria < 0) nuovaBatteria = 0; // Evita batterie negative
        
        monopattinoSelezionato.setBatteria(nuovaBatteria); 
        // --------------------
        
        utenteCorrente.scalaCredito(costo);
        
        if (guastoSegnalato || monopattinoSelezionato.getBatteria() <= 10) {
            monopattinoSelezionato.setStato(new InManutenzione());
        } else {
            monopattinoSelezionato.setStato(new Disponibile());
        }
        
        System.out.println("\n--- RICEVUTA CORSA ---");
        System.out.println("Distanza percorsa: " + passiEffettuati + " caselle");
        System.out.println("Costo Totale: " + String.format("%.2f", costo) + " EUR");
        System.out.println("Credito Residuo: " + String.format("%.2f", utenteCorrente.getCreditoResiduo()) + " EUR");
        System.out.println("Batteria Residua: " + monopattinoSelezionato.getBatteria() + "%");
        
        corsaCorrente = null; 
        monopattinoSelezionato = null;
    }
    public List<Monopattino> richiediVeicoliInManutenzione() {
        List<Monopattino> veicoliGuasti = new ArrayList<>(); 
        for (Monopattino m : flotta.values()) { 
            if (m.getStatoCorrente() instanceof InManutenzione) {
                veicoliGuasti.add(m);
            }
        }
        return veicoliGuasti;
    }

    public void prendiInCarico(String idVeicolo) {
        Monopattino m = flotta.get(idVeicolo);
        if (m != null && m.getStatoCorrente() instanceof InManutenzione) {
            m.setStato(new InRiparazione());
            System.out.println("[Admin] Veicolo " + idVeicolo + " in riparazione.");
        }
    }

    public void registraManutenzioneCompletata(String idVeicolo) {
        Monopattino m = flotta.get(idVeicolo);
        if (m != null && m.getStatoCorrente() instanceof InRiparazione) {
            m.setBatteria(100);
            m.setStato(new Disponibile());
            System.out.println("[Admin] Manutenzione completata. Veicolo " + idVeicolo + " disponibile.");
        }
    }

    
    // --- NUOVO METODO PER LA GRAFICA ---
    // Permette alla mappa di sapere se un veicolo specifico è da riparare
    public boolean isVeicoloInManutenzione(String idVeicolo) {
        Monopattino m = flotta.get(idVeicolo);
        if (m != null) {
            return m.getStatoCorrente() instanceof InManutenzione;
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

    // Verifica se il veicolo è distrutto (Nero)
    public boolean isVeicoloDismesso(String idVeicolo) {
        Monopattino m = flotta.get(idVeicolo);
        if (m != null) {
            return m.getStatoCorrente() instanceof Dismesso;
        }
        return false;
    }

    // UC4 (Estensione): Rimuove il veicolo dalla circolazione
    public void dismettiVeicolo(String idVeicolo) {
        Monopattino m = flotta.get(idVeicolo);
        if (m != null) {
            m.setStato(new Dismesso());
            flotta.remove(idVeicolo); // Lo rimuoviamo fisicamente dalla mappa
            System.out.println("Veicolo " + idVeicolo + " dismesso e rimosso.");
        }
    }
}

