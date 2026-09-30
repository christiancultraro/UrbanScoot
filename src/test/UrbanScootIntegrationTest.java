package test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import controller.UrbanScoot;
import dominio.Corsa;
import stati.EsitoOperazione;
import java.util.List;

@DisplayName("Test di Integrazione: Casi d'Uso e Controller UrbanScoot")
class UrbanScootIntegrationTest {

    private UrbanScoot controller;

    @BeforeEach
    void setUp() {
        // Setup: ripristino e inizializzazione ambiente pulito prima di ogni test
        UrbanScoot.resetInstancePerTest();
        controller = UrbanScoot.getInstance();
        controller.loadDatiIniziali();
    }

    @AfterEach
    void tearDown() {
        // Teardown: pulizia del Singleton
        UrbanScoot.resetInstancePerTest();
    }

    @Test
    @DisplayName("TI_UC1_01: Flusso Noleggio regolare (Login, Scan QR, Avvio Corsa)")
    void testAvvioCorsaSuccesso() {
        // 1. Login utente registrato
        boolean loginOk = controller.autenticaUtente("utente", "utente");
        assertTrue(loginOk, "L'autenticazione deve riuscire con credenziali valide");

        // 2. Scansione codice QR veicolo disponibile
        EsitoOperazione esitoQR = controller.inserisciCodiceQR("MONO-01", "utente");
        assertEquals(EsitoOperazione.SUCCESSO, esitoQR);
        assertNotNull(controller.getMonopattinoSelezionato(), "Il monopattino selezionato deve essere agganciato");

        // 3. Conferma avvio corsa
        EsitoOperazione esitoAvvio = controller.confermaAvvioCorsa();
        assertEquals(EsitoOperazione.SUCCESSO, esitoAvvio);
        assertNotNull(controller.getCorsaCorrente(), "La corsa deve essere registrata come attiva");
    }

    @Test
    @DisplayName("TI_UC1_02: Blocco noleggio su veicolo inesistente o senza autenticazione")
    void testAvvioCorsaFallimento() {
        // Tentativo di scansione senza aver effettuato l'accesso
        EsitoOperazione esitoSenzaLogin = controller.inserisciCodiceQR("MONO-01", "utente");
        assertEquals(EsitoOperazione.OPERAZIONE_NON_CONSENTITA, esitoSenzaLogin);

        // Login valido ma identificativo veicolo errato/non censito
        controller.autenticaUtente("utente", "utente");
        EsitoOperazione esitoVeicoloErrato = controller.inserisciCodiceQR("MONO-999", "utente");
        assertEquals(EsitoOperazione.OPERAZIONE_NON_CONSENTITA, esitoVeicoloErrato);
    }

    @Test
    @DisplayName("TI_UC3_01: Chiusura regolare della corsa con addebito e aggiornamento storico")
    void testTerminaCorsaRegolare() {
        controller.autenticaUtente("utente", "utente");
        float saldoIniziale = controller.getCreditoResiduoCliente();

        controller.inserisciCodiceQR("MONO-01", "utente");
        controller.confermaAvvioCorsa();

        // Conclusione della corsa: 10 caselle/passi percorsi, nessun incidente
        float costoAddebitato = controller.terminaCorsa(0f, 10f, false, 10);

        assertTrue(costoAddebitato > 0, "Il costo totale della corsa deve essere maggiore di zero");
        assertNull(controller.getCorsaCorrente(), "Al termine della corsa, la corsa attiva deve azzerarsi");
        assertEquals(saldoIniziale - costoAddebitato, controller.getCreditoResiduoCliente(), 0.01f,
                     "Il credito del cliente deve essere decurtato esattamente del costo calcolato");

        // Verifica aggiornamento storico
        List<Corsa> storico = controller.richiediStoricoCorse();
        assertEquals(1, storico.size(), "La corsa completata deve comparire nello storico personale");
    }

    @Test
    @DisplayName("TI_UC3_02: Gestione incidente/buca (rimborso corsa e invio veicolo in riparazione)")
    void testTerminaCorsaIncidente() {
        controller.autenticaUtente("utente", "utente");
        float saldoPrima = controller.getCreditoResiduoCliente();

        controller.inserisciCodiceQR("MONO-01", "utente");
        controller.confermaAvvioCorsa();

        // Termina corsa con guastoSegnalato = true (impatto buca)
        float costo = controller.terminaCorsa(2f, 2f, true, 4);

        assertEquals(0.0f, costo, "In caso di incidente la corsa deve essere rimborsata (costo 0€)");
        assertEquals(saldoPrima, controller.getCreditoResiduoCliente(), 0.001f,
                     "Il credito residuo del cliente non deve subire riduzioni");
        assertTrue(controller.isVeicoloInManutenzione("MONO-01"),
                   "Il monopattino incidentato deve passare allo stato di Manutenzione");
    }

    @Test
    @DisplayName("TI_UC4_01: Dismissione e rimozione definitiva veicolo dalla flotta (Contratto CO3)")
    void testManutenzioneEDismissione() {
        // Accesso amministratore
        controller.autenticaUtente("admin", "admin");
        assertTrue(controller.isSessioneAdmin(), "La sessione deve essere impostata come Admin");

        // Aggiunta veicolo
        controller.aggiungiNuovoMonopattino("MONO-TEST", 1f, 1f);

        // Dismissione veicolo
        controller.dismettiVeicolo("MONO-TEST");

        // Verifica post-condizione Contratto CO3: il mezzo non deve più esistere nella Flotta
        boolean trovato = false;
        for (var m : controller.getFlotta()) {
            if (m.getId().equals("MONO-TEST")) {
                trovato = true;
                break;
            }
        }
        assertFalse(trovato, "Il monopattino dismesso deve essere rimosso fisicamente dalla flotta");
    }
}