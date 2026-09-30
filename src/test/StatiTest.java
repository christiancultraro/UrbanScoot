package test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dominio.Monopattino;
import stati.*;

@DisplayName("Test Unitari: State Pattern del Monopattino")
class StatiTest {

    private Monopattino monopattino;

    @BeforeEach
    void setUp() {
        // Setup fixture: monopattino nuovo in posizione (0,0)
        monopattino = new Monopattino("TEST-01", 0f, 0f);
    }

    @Test
    @DisplayName("TU_STATO_01: Transizioni valide nel ciclo di noleggio (Disponibile -> InUso -> InSosta -> InUso -> Disponibile)")
    void testCicloStatiCompleto() {
        // 1. All'inizio è Disponibile
        assertTrue(monopattino.getStatoCorrente().isDisponibile());
        
        // 2. Sblocco veicolo -> InUso
        EsitoOperazione esitoSblocco = monopattino.sblocca();
        assertEquals(EsitoOperazione.SUCCESSO, esitoSblocco);
        assertTrue(monopattino.getStatoCorrente().isCorsaAttiva());

        // 3. Messa in pausa -> InSosta
        EsitoOperazione esitoPausa = monopattino.mettiInPausa();
        assertEquals(EsitoOperazione.SUCCESSO, esitoPausa);
        assertTrue(monopattino.getStatoCorrente().isInSosta());

        // 4. Da InSosta NON si può terminare direttamente la corsa
        assertEquals(EsitoOperazione.OPERAZIONE_NON_CONSENTITA, monopattino.terminaCorsa());

        // 5. Ripresa corsa -> InUso
        EsitoOperazione esitoRipresa = monopattino.riprendiCorsa();
        assertEquals(EsitoOperazione.SUCCESSO, esitoRipresa);
        assertFalse(monopattino.getStatoCorrente().isInSosta());

        // 6. Termina corsa -> Disponibile
        EsitoOperazione esitoFine = monopattino.terminaCorsa();
        assertEquals(EsitoOperazione.SUCCESSO, esitoFine);
        assertTrue(monopattino.getStatoCorrente().isDisponibile());
    }

    @Test
    @DisplayName("TU_STATO_02: Operazioni illegali e divieti di transizione")
    void testTransizioniIllegali() {
        // Un veicolo Disponibile non può terminare una corsa mai iniziata
        assertEquals(EsitoOperazione.OPERAZIONE_NON_CONSENTITA, monopattino.terminaCorsa());

        // Veicolo Dismesso: non può essere sbloccato
        monopattino.setStato(Dismesso.getInstance());
        assertEquals(EsitoOperazione.VEICOLO_DISMESSO, monopattino.sblocca());

        // Veicolo in Manutenzione: non può essere sbloccato
        monopattino.setStato(InManutenzione.getInstance());
        assertEquals(EsitoOperazione.VEICOLO_IN_MANUTENZIONE, monopattino.sblocca());
    }

    @Test
    @DisplayName("TU_STATO_03: Ciclo di riparazione (InManutenzione -> InRiparazione -> Disponibile)")
    void testCicloRiparazione() {
        monopattino.setBatteria(10);
        monopattino.setStato(InManutenzione.getInstance());

        // Presa in carico del tecnico
        monopattino.getStatoCorrente().inviaInRiparazione(monopattino);
        assertTrue(monopattino.getStatoCorrente().isRiparazione());

        // Fine manutenzione: ricarica al 100% e ritorno allo stato Disponibile
        monopattino.getStatoCorrente().completaRiparazione(monopattino);
        assertTrue(monopattino.getStatoCorrente().isDisponibile());
        assertEquals(100, monopattino.getBatteria(), "A fine riparazione la batteria deve tornare al 100%");
    }
}