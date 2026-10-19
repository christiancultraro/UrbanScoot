package test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dominio.Monopattino;
import dominio.EsitoOperazione;
import stati.*;

@DisplayName("Test Unitari: State Pattern del Monopattino")
class StatiTest {

    private Monopattino monopattino;

    @BeforeEach
    void setUp() {
        monopattino = new Monopattino("TEST-01", 0f, 0f);
    }

    @Test
    @DisplayName("TU_STATO_01: Ciclo valido Disponibile -> InUso -> InSosta -> InUso -> Disponibile")
    void testCicloStatiCompleto() {
        assertTrue(monopattino.getStatoCorrente().isDisponibile());

        assertEquals(EsitoOperazione.SUCCESSO, monopattino.sblocca());
        assertTrue(monopattino.getStatoCorrente().isCorsaAttiva());

        assertEquals(EsitoOperazione.SUCCESSO, monopattino.mettiInPausa());
        assertTrue(monopattino.getStatoCorrente().isInSosta());

        // Da InSosta non si può terminare direttamente la corsa
        assertEquals(EsitoOperazione.OPERAZIONE_NON_CONSENTITA, monopattino.terminaCorsa(0));
        assertTrue(monopattino.getStatoCorrente().isInSosta(), "Lo stato non deve cambiare");

        assertEquals(EsitoOperazione.SUCCESSO, monopattino.riprendiCorsa());
        assertFalse(monopattino.getStatoCorrente().isInSosta());

        assertEquals(EsitoOperazione.SUCCESSO, monopattino.terminaCorsa(5));
        assertTrue(monopattino.getStatoCorrente().isDisponibile());
        assertEquals(80, monopattino.getBatteria(), "5 passi * 4 = 20 punti di batteria consumati");
    }

    @Test
    @DisplayName("TU_STATO_02: Operazioni illegali rifiutate")
    void testTransizioniIllegali() {
        assertEquals(EsitoOperazione.OPERAZIONE_NON_CONSENTITA, monopattino.terminaCorsa(0));
        assertEquals(EsitoOperazione.OPERAZIONE_NON_CONSENTITA, monopattino.riprendiCorsa());
        assertEquals(EsitoOperazione.OPERAZIONE_NON_CONSENTITA, monopattino.segnalaGuasto(0));

        monopattino.sblocca();
        assertEquals(EsitoOperazione.VEICOLO_IN_USO, monopattino.sblocca(), "Un veicolo in uso non si sblocca");
        assertEquals(EsitoOperazione.OPERAZIONE_NON_CONSENTITA, monopattino.riprendiCorsa(), "Non si riprende una corsa non in sosta");
        assertEquals(EsitoOperazione.OPERAZIONE_NON_CONSENTITA, monopattino.dismetti(), "Non si dismette un veicolo in uso");

        monopattino.mettiInPausa();
        assertEquals(EsitoOperazione.VEICOLO_IN_USO, monopattino.sblocca(), "Un veicolo in sosta è riservato");
    }

    @Test
    @DisplayName("TU_STATO_03: Ciclo di riparazione InManutenzione -> InRiparazione -> Disponibile")
    void testCicloRiparazione() {
        monopattino.sblocca();
        monopattino.segnalaGuasto(3);
        assertTrue(monopattino.getStatoCorrente().isManutenzione());
        assertEquals(EsitoOperazione.VEICOLO_IN_MANUTENZIONE, monopattino.sblocca());

        // La riparazione non può essere completata senza presa in carico
        assertEquals(EsitoOperazione.OPERAZIONE_NON_CONSENTITA, monopattino.completaRiparazione());

        assertEquals(EsitoOperazione.SUCCESSO, monopattino.prendiInCarico());
        assertTrue(monopattino.getStatoCorrente().isRiparazione());
        assertEquals(EsitoOperazione.VEICOLO_IN_RIPARAZIONE, monopattino.sblocca());

        assertEquals(EsitoOperazione.SUCCESSO, monopattino.completaRiparazione());
        assertTrue(monopattino.getStatoCorrente().isDisponibile());
        assertEquals(100, monopattino.getBatteria());
    }

    @Test
    @DisplayName("TU_STATO_04: Fine corsa con batteria critica -> InManutenzione (valori limite)")
    void testFineCorsaBatteriaCritica() {
        Monopattino sopraSoglia = new Monopattino("A", 0, 0);
        sopraSoglia.sblocca();
        sopraSoglia.terminaCorsa(22);             // 100 - 88 = 12% (> 10)
        assertTrue(sopraSoglia.getStatoCorrente().isDisponibile());

        Monopattino sullaSoglia = new Monopattino("B", 0, 0);
        sullaSoglia.setBatteria(14);
        sullaSoglia.sblocca();
        sullaSoglia.terminaCorsa(1);              // 14 - 4 = 10% (limite incluso)
        assertTrue(sullaSoglia.getStatoCorrente().isManutenzione());
    }

    @Test
    @DisplayName("TU_STATO_05: Sblocco rifiutato con batteria critica")
    void testSbloccoBatteriaCritica() {
        monopattino.setBatteria(10);
        assertEquals(EsitoOperazione.VEICOLO_IN_MANUTENZIONE, monopattino.sblocca());
        assertTrue(monopattino.getStatoCorrente().isDisponibile(), "Lo stato non deve cambiare");
    }

    @Test
    @DisplayName("TU_STATO_06: Dismissione dagli stati ammessi; Dismesso è uno stato finale")
    void testDismissione() {
        assertEquals(EsitoOperazione.SUCCESSO, monopattino.dismetti());
        assertTrue(monopattino.getStatoCorrente().isDismesso());

        assertEquals(EsitoOperazione.VEICOLO_DISMESSO, monopattino.sblocca());
        assertEquals(EsitoOperazione.OPERAZIONE_NON_CONSENTITA, monopattino.prendiInCarico());
        assertEquals(EsitoOperazione.OPERAZIONE_NON_CONSENTITA, monopattino.dismetti());
    }
}
