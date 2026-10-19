package test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import controller.UrbanScoot;
import dominio.Corsa;
import dominio.Monopattino;
import dominio.EsitoOperazione;

@DisplayName("Test di Integrazione: Casi d'Uso e Controller UrbanScoot")
class UrbanScootIntegrationTest {

    private UrbanScoot controller;

    @BeforeEach
    void setUp() {
        UrbanScoot.resetInstancePerTest();
        controller = UrbanScoot.getInstance();
        controller.loadDatiIniziali();
    }

    @AfterEach
    void tearDown() {
        UrbanScoot.resetInstancePerTest();
    }

    private Monopattino veicolo(String id) {
        for (Monopattino m : controller.getFlotta()) {
            if (m.getId().equals(id)) return m;
        }
        return null;
    }

    private void avviaCorsaDaUtente() {
        controller.autenticaUtente("utente", "utente");
        controller.inserisciCodiceQR("MONO-01", "utente");
        controller.confermaAvvioCorsa();
    }

    // ------------------------------------------------- Autenticazione / Registrazione

    @Test
    @DisplayName("TI_ACC_01: Autenticazione di cliente e amministratore; credenziali errate rifiutate")
    void testAutenticazione() {
        assertEquals(EsitoOperazione.SUCCESSO, controller.autenticaUtente("utente", "utente"));
        assertFalse(controller.isSessioneAdmin());
        assertNotNull(controller.getClienteCorrente());

        assertEquals(EsitoOperazione.SUCCESSO, controller.autenticaUtente("admin", "admin"));
        assertTrue(controller.isSessioneAdmin());
        assertNull(controller.getClienteCorrente());

        assertEquals(EsitoOperazione.OPERAZIONE_NON_CONSENTITA, controller.autenticaUtente("utente", "sbagliata"));
        assertEquals(EsitoOperazione.OPERAZIONE_NON_CONSENTITA, controller.autenticaUtente("sconosciuto", "x"));
    }

    @Test
    @DisplayName("TI_ACC_02: Registrazione nuovo cliente; username duplicato o vuoto rifiutato")
    void testRegistrazione() {
        assertEquals(EsitoOperazione.SUCCESSO, controller.registraNuovoUtente("luigi", "pwd"));
        float credito = controller.getUltimoCreditoAssegnato();
        assertTrue(credito >= 10f && credito <= 60f, "Credito simulato tra 10 e 60 €");

        assertEquals(EsitoOperazione.SUCCESSO, controller.autenticaUtente("luigi", "pwd"));
        assertEquals(credito, controller.getCreditoResiduoCliente(), 0.001f);

        assertEquals(EsitoOperazione.OPERAZIONE_NON_CONSENTITA, controller.registraNuovoUtente("luigi", "altra"));
        assertEquals(EsitoOperazione.OPERAZIONE_NON_CONSENTITA, controller.registraNuovoUtente("admin", "x"));
        assertEquals(EsitoOperazione.OPERAZIONE_NON_CONSENTITA, controller.registraNuovoUtente("  ", "x"));
    }

    // ------------------------------------------------------------------ UC1

    @Test
    @DisplayName("TI_UC1_01: Scenario principale - login, scansione QR e avvio corsa")
    void testAvvioCorsaSuccesso() {
        assertEquals(EsitoOperazione.SUCCESSO, controller.autenticaUtente("utente", "utente"));
        assertEquals(EsitoOperazione.SUCCESSO, controller.inserisciCodiceQR("MONO-01", "utente"));
        assertEquals(EsitoOperazione.SUCCESSO, controller.confermaAvvioCorsa());

        assertNotNull(controller.getCorsaCorrente());
        assertTrue(veicolo("MONO-01").getStatoCorrente().isCorsaAttiva());
    }

    @Test
    @DisplayName("TI_UC1_02: Estensioni - utente non autenticato o veicolo inesistente")
    void testAvvioCorsaFallimento() {
        assertEquals(EsitoOperazione.OPERAZIONE_NON_CONSENTITA, controller.inserisciCodiceQR("MONO-01", "utente"));

        controller.autenticaUtente("utente", "utente");
        assertEquals(EsitoOperazione.OPERAZIONE_NON_CONSENTITA, controller.inserisciCodiceQR("MONO-999", "utente"));
    }

    @Test
    @DisplayName("TI_UC1_03: Estensione - veicolo già in uso da un altro cliente")
    void testVeicoloGiaInUso() {
        controller.registraNuovoUtente("luigi", "pwd");
        avviaCorsaDaUtente();

        controller.autenticaUtente("luigi", "pwd");
        assertEquals(EsitoOperazione.VEICOLO_IN_USO, controller.inserisciCodiceQR("MONO-01", "luigi"));
    }

    // ------------------------------------------------------------------ UC2

    @Test
    @DisplayName("TI_UC2_01: Scenario principale - l'amministratore aggiunge un veicolo")
    void testAggiuntaVeicolo() {
        controller.autenticaUtente("admin", "admin");

        assertEquals(EsitoOperazione.SUCCESSO, controller.aggiungiNuovoMonopattino("MONO-02", 2f, 2f));
        Monopattino m = veicolo("MONO-02");
        assertNotNull(m);
        assertTrue(m.getStatoCorrente().isDisponibile());
        assertEquals(100, m.getBatteria());
    }

    @Test
    @DisplayName("TI_UC2_02: Estensioni - id duplicato o utente non amministratore")
    void testAggiuntaVeicoloRifiutata() {
        controller.autenticaUtente("admin", "admin");
        assertEquals(EsitoOperazione.OPERAZIONE_NON_CONSENTITA, controller.aggiungiNuovoMonopattino("MONO-01", 5f, 5f));
        assertEquals(0f, veicolo("MONO-01").getLatitudine(), "Il veicolo esistente non deve essere sovrascritto");

        controller.autenticaUtente("utente", "utente");
        assertEquals(EsitoOperazione.OPERAZIONE_NON_CONSENTITA, controller.aggiungiNuovoMonopattino("MONO-03", 2f, 2f));
        assertNull(veicolo("MONO-03"));
    }

    // ------------------------------------------------------------------ UC3

    @Test
    @DisplayName("TI_UC3_01: Scenario principale - parcheggio, addebito e storico")
    void testTerminaCorsaRegolare() {
        controller.autenticaUtente("utente", "utente");
        float saldoIniziale = controller.getCreditoResiduoCliente();
        controller.inserisciCodiceQR("MONO-01", "utente");
        controller.confermaAvvioCorsa();

        assertEquals(EsitoOperazione.SUCCESSO, controller.terminaCorsa(0f, 10f, false, 10));
        float costo = controller.getUltimoCostoAddebitato();

        assertTrue(costo > 0);
        assertEquals(costo, controller.getUltimaQuotaGuida() + controller.getUltimaQuotaSosta(), 0.001f);
        assertNull(controller.getCorsaCorrente());
        assertEquals(saldoIniziale - costo, controller.getCreditoResiduoCliente(), 0.01f);
        assertTrue(veicolo("MONO-01").getStatoCorrente().isDisponibile());
        assertEquals(60, veicolo("MONO-01").getBatteria());
        assertEquals(1, controller.richiediStoricoCorse().size());
    }

    @Test
    @DisplayName("TI_UC3_02: Estensione 3b - guasto: rimborso integrale e veicolo in manutenzione")
    void testTerminaCorsaIncidente() {
        controller.autenticaUtente("utente", "utente");
        float saldoPrima = controller.getCreditoResiduoCliente();
        controller.inserisciCodiceQR("MONO-01", "utente");
        controller.confermaAvvioCorsa();

        assertEquals(EsitoOperazione.SUCCESSO, controller.terminaCorsa(2f, 2f, true, 4));

        assertEquals(0.0f, controller.getUltimoCostoAddebitato());
        assertEquals(saldoPrima, controller.getCreditoResiduoCliente(), 0.001f);
        assertTrue(controller.isVeicoloInManutenzione("MONO-01"));
    }

    @Test
    @DisplayName("TI_UC3_03: Estensione 6a - batteria critica a fine corsa: veicolo in manutenzione")
    void testTerminaCorsaBatteriaCritica() {
        avviaCorsaDaUtente();

        assertEquals(EsitoOperazione.SUCCESSO, controller.terminaCorsa(0f, 10f, false, 23)); // 100 - 92 = 8%

        assertTrue(controller.getUltimoCostoAddebitato() > 0, "La corsa viene comunque addebitata");
        assertTrue(controller.isVeicoloInManutenzione("MONO-01"));
    }

    @Test
    @DisplayName("TI_UC3_04: Termina corsa non consentita con veicolo in sosta breve")
    void testTerminaCorsaInSosta() {
        avviaCorsaDaUtente();
        controller.mettiInSosta(1f, 1f);

        assertEquals(EsitoOperazione.OPERAZIONE_NON_CONSENTITA, controller.terminaCorsa(0f, 10f, false, 5));
        assertNotNull(controller.getCorsaCorrente(), "La corsa deve restare attiva");
        assertTrue(veicolo("MONO-01").getStatoCorrente().isInSosta());
    }

    // ------------------------------------------------------------------ UC4

    @Test
    @DisplayName("TI_UC4_01: Presa in carico e riparazione di un veicolo guasto")
    void testCicloManutenzione() {
        avviaCorsaDaUtente();
        controller.terminaCorsa(2f, 2f, true, 4);

        controller.autenticaUtente("admin", "admin");
        assertEquals(1, controller.richiediVeicoliInManutenzione().size());

        assertEquals(EsitoOperazione.SUCCESSO, controller.prendiInCarico("MONO-01"));
        assertTrue(controller.isVeicoloInRiparazione("MONO-01"));

        assertEquals(EsitoOperazione.SUCCESSO, controller.registraManutenzioneCompletata("MONO-01"));
        assertTrue(veicolo("MONO-01").getStatoCorrente().isDisponibile());
        assertEquals(100, veicolo("MONO-01").getBatteria());
        assertTrue(controller.richiediVeicoliInManutenzione().isEmpty());
    }

    @Test
    @DisplayName("TI_UC4_02: Dismissione logica e rimozione definitiva (Contratti CO3/CO4)")
    void testDismissioneERimozione() {
        controller.autenticaUtente("admin", "admin");
        controller.aggiungiNuovoMonopattino("MONO-TEST", 1f, 1f);

        // Rimozione di un veicolo non ancora dismesso: rifiutata
        assertEquals(EsitoOperazione.OPERAZIONE_NON_CONSENTITA, controller.dismettiVeicolo("MONO-TEST"));
        assertNotNull(veicolo("MONO-TEST"));

        assertEquals(EsitoOperazione.SUCCESSO, controller.segnaVeicoloDismesso("MONO-TEST"));
        assertTrue(controller.isVeicoloDismesso("MONO-TEST"));

        assertEquals(EsitoOperazione.SUCCESSO, controller.dismettiVeicolo("MONO-TEST"));
        assertNull(veicolo("MONO-TEST"), "Il veicolo deve essere rimosso dalla flotta");
    }

    @Test
    @DisplayName("TI_UC4_03: Un veicolo in uso non può essere dismesso")
    void testDismissioneVeicoloInUso() {
        avviaCorsaDaUtente();

        controller.autenticaUtente("admin", "admin");
        assertEquals(EsitoOperazione.OPERAZIONE_NON_CONSENTITA, controller.segnaVeicoloDismesso("MONO-01"));
        assertTrue(veicolo("MONO-01").getStatoCorrente().isCorsaAttiva());
    }

    // ------------------------------------------------------------------ UC5

    @Test
    @DisplayName("TI_UC5_01: Ricarica credito valida e non valida")
    void testRicaricaCredito() {
        controller.autenticaUtente("utente", "utente");

        assertEquals(EsitoOperazione.SUCCESSO, controller.ricaricaCredito(5.00f));
        assertEquals(15.50f, controller.getCreditoResiduoCliente(), 0.001f);
        assertEquals(EsitoOperazione.OPERAZIONE_NON_CONSENTITA, controller.ricaricaCredito(0f));
        assertEquals(EsitoOperazione.OPERAZIONE_NON_CONSENTITA, controller.ricaricaCredito(-3f));
        assertEquals(15.50f, controller.getCreditoResiduoCliente(), 0.001f);
    }

    // ------------------------------------------------------------------ UC6

    @Test
    @DisplayName("TI_UC6_01: Lo storico contiene solo le corse del cliente autenticato")
    void testStoricoPersonale() {
        controller.registraNuovoUtente("luigi", "pwd");
        avviaCorsaDaUtente();
        controller.terminaCorsa(0f, 10f, false, 2);

        controller.autenticaUtente("luigi", "pwd");
        assertTrue(controller.richiediStoricoCorse().isEmpty());

        controller.autenticaUtente("utente", "utente");
        List<Corsa> storico = controller.richiediStoricoCorse();
        assertEquals(1, storico.size());
        assertEquals("utente", storico.get(0).getCliente().getIdCliente());
    }

    // ------------------------------------------------------------------ UC7

    @Test
    @DisplayName("TI_UC7_01: Sosta breve e ripresa della corsa")
    void testSostaBreveERipresa() {
        avviaCorsaDaUtente();

        assertEquals(EsitoOperazione.SUCCESSO, controller.mettiInSosta(3f, 4f));
        Monopattino m = veicolo("MONO-01");
        assertTrue(m.getStatoCorrente().isInSosta());
        assertEquals(3f, m.getLatitudine());
        assertEquals(4f, m.getLongitudine());

        assertEquals(EsitoOperazione.SUCCESSO, controller.riprendiCorsa());
        assertTrue(m.getStatoCorrente().isCorsaAttiva());
        assertFalse(m.getStatoCorrente().isInSosta());
        assertEquals(1, controller.getCorsaCorrente().getMinutiSostaAccumulati(),
                "La frazione di minuto di sosta viene arrotondata a 1 minuto");
    }

    @Test
    @DisplayName("TI_UC7_02: Veicolo in sosta non sbloccabile da un altro cliente; ripristino al login")
    void testSostaRiservataAlCliente() {
        controller.registraNuovoUtente("luigi", "pwd");
        avviaCorsaDaUtente();
        controller.mettiInSosta(3f, 4f);

        controller.autenticaUtente("luigi", "pwd");
        assertEquals(EsitoOperazione.VEICOLO_IN_USO, controller.inserisciCodiceQR("MONO-01", "luigi"));

        controller.autenticaUtente("utente", "utente");
        assertNotNull(controller.getCorsaCorrente(), "La corsa in sosta viene ripristinata al login");
        assertEquals(EsitoOperazione.SUCCESSO, controller.riprendiCorsa());
    }
}
