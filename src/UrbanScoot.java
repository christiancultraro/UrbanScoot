import java.util.HashMap;
import java.util.Map;

public class UrbanScoot {
    private Map<String, Utente> utentiRegistrati;
    private Map<String, Monopattino> flotta;
    
    // Variabili di stato per la sessione corrente (simulate)
    private Utente utenteCorrente;
    private Monopattino monopattinoCorrente;

    public UrbanScoot() {
        utentiRegistrati = new HashMap<>();
        flotta = new HashMap<>();
    }

    // UC0: Start-up (Pattern Creator)
    public void loadDatiIniziali() {
        // Creazione Utenti
        Utente u1 = new Utente("USR-01", 15.0f);
        Utente u2 = new Utente("USR-02", 0.50f); // Credito basso per testare l'errore
        utentiRegistrati.put(u1.getIdCliente(), u1);
        utentiRegistrati.put(u2.getIdCliente(), u2);

        // Creazione Monopattini tramite UC2
        confermaDatiVeicolo("MONO-01", 37.5f, 15.0f);
        confermaDatiVeicolo("MONO-02", 37.6f, 15.1f);
        
        System.out.println("--- Sistema Avviato e Dati Caricati ---\n");
    }

    // Simulazione Login
    public void loginUtente(String idUtente) {
        utenteCorrente = utentiRegistrati.get(idUtente);
        System.out.println("Login effettuato: " + utenteCorrente.getIdCliente() + " | Credito: " + utenteCorrente.getCreditoResiduo() + "€");
    }

    // UC1: Inizia Corsa
    public Monopattino inserisciCodiceQR(String idVeicolo) {
        monopattinoCorrente = flotta.get(idVeicolo);
        if (monopattinoCorrente != null) {
            System.out.println("Veicolo Trovato: " + idVeicolo + " | Batteria: " + monopattinoCorrente.getLivelloBatteria() + "% | Stato: " + monopattinoCorrente.getStatoCorrente().getNomeStato());
        } else {
            System.out.println("Veicolo non trovato.");
        }
        return monopattinoCorrente;
    }

    public void confermaAvvioCorsa() {
        if (utenteCorrente == null || monopattinoCorrente == null) {
            System.out.println("Errore: Selezionare utente e veicolo.");
            return;
        }

        float cred = utenteCorrente.getCreditoResiduo();
        int batt = monopattinoCorrente.getLivelloBatteria();

        // Controllo le precondizioni del Contratto
        if (cred >= 1.00f && batt > 10 && monopattinoCorrente.getStatoCorrente().getNomeStato().equals("Disponibile")) {
            Corsa c = new Corsa(utenteCorrente, monopattinoCorrente);
            monopattinoCorrente.sblocca(); // Cambia lo stato tramite il Pattern State
            System.out.println("Sblocco confermato, timer avviato!\n");
        } else {
            System.out.println("Errore Avvio: Credito insufficiente, batteria scarica o veicolo già in uso.\n");
        }
    }

    // UC2: Aggiungi Nuovo Monopattino
    public void avviaRegistrazioneVeicolo() {
        System.out.println("In attesa dei dati del nuovo veicolo...");
    }

    public void confermaDatiVeicolo(String idVeicolo, float lat, float lon) {
        if (!flotta.containsKey(idVeicolo)) {
            Monopattino m = new Monopattino(idVeicolo, lat, lon);
            flotta.put(idVeicolo, m);
            System.out.println("Veicolo Registrato: " + idVeicolo);
        } else {
            System.out.println("Errore: ID Veicolo già esistente.");
        }
    }
}