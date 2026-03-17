import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UrbanScoot {
    // Pattern Singleton per il Facade Controller
    private static UrbanScoot istanza;
    
    // Collezioni per le relazioni 1..*
    private Map<String, Monopattino> flotta;
    private Map<String, Utente> utentiRegistrati;
    private List<Corsa> storicoCorse;
    
    // Attributi di stato per memorizzare la sessione temporanea (il "corrente")
    private Corsa corsaCorrente;
    private Monopattino monopattinoSelezionato;
    private Utente utenteCorrente;

    private UrbanScoot() {
        flotta = new HashMap<>();
        utentiRegistrati = new HashMap<>();
        storicoCorse = new ArrayList<>();
    }

    public static UrbanScoot getInstance() {
        if (istanza == null) {
            istanza = new UrbanScoot();
        }
        return istanza;
    }

    // --- UC0: START UP ---
    public void loadDatiIniziali() {
        aggiungiNuovoMonopattino("MONO-01", 37.5f, 15.0f);
        utentiRegistrati.put("USR-01", new Utente("USR-01", 10.50f));
        System.out.println("Sistema avviato e dati caricati.\n");
    }

    // --- UC2: AGGIUNGI VEICOLO (Admin) ---
    public void aggiungiNuovoMonopattino(String idVeicolo, float lat, float lon) {
        // Pattern Creator: il Controller istanzia il Monopattino
        Monopattino m = new Monopattino(idVeicolo, lat, lon);
        // Pattern Information Expert: il Controller salva nella mappa
        flotta.put(idVeicolo, m);
        System.out.println("[Admin] Veicolo " + idVeicolo + " registrato nella flotta.");
    }

    // --- UC1: AVVIA CORSA (Utente) ---
    // Fase 1: Scansione QR e recupero dati
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

    // Fase 2: Conferma effettiva
    public void confermaAvvioCorsa() {
        if (monopattinoSelezionato == null || utenteCorrente == null) {
            System.out.println("[App] Errore: Nessun veicolo selezionato.");
            return;
        }

        // Recupero info per le pre-condizioni
        float cred = utenteCorrente.getCreditoResiduo();
        int batt = monopattinoSelezionato.getBatteria();

        // Verifica pre-condizioni
        if (cred >= 1.00 && batt > 10 && monopattinoSelezionato.getStatoCorrente() instanceof Disponibile) {
            // Creator: crea l'istanza di corsa
            Corsa c = new Corsa(utenteCorrente, monopattinoSelezionato);
            this.corsaCorrente = c; 
            
            // Transizione di stato
            monopattinoSelezionato.sblocca();
            
            // Archiviazione 
            storicoCorse.add(c);
            
            System.out.println("[App] Sblocco confermato, timer avviato alle: " + c.getOrarioInizio());
        } else {
            System.out.println("[App] Errore avvio: Credito/Batteria insufficienti o veicolo non disponibile.");
        }
    }
}