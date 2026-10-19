package test;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dominio.Cliente;
import dominio.Corsa;
import dominio.Monopattino;
import dominio.TariffaSostaBreve;
import dominio.TariffaStandard;

@DisplayName("Test Unitari: Logica di Dominio e Regole di Business")
class DominioTest {

    private Cliente cliente;
    private final TariffaStandard tariffaStandard = new TariffaStandard(1.00f, 0.20f, 0.05f);
    private final TariffaSostaBreve tariffaSosta = new TariffaSostaBreve(0.10f);

    /** Orologio controllabile dal test, per rendere deterministici i calcoli sui tempi. */
    private static class OrologioDiTest extends Clock {
        private Instant adesso = Instant.parse("2026-01-01T10:00:00Z");

        void avanza(Duration d) { adesso = adesso.plus(d); }

        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return adesso; }
    }

    @BeforeEach
    void setUp() {
        cliente = new Cliente("mario_rossi", 5.00f);
    }

    @Test
    @DisplayName("TU_DOM_01: Calcolo costo con TariffaStandard (Strategy)")
    void testCalcoloTariffaStandard() {
        // 1.00 + (5 * 0.20) + (10 * 0.05) = 2.50 €
        assertEquals(2.50f, tariffaStandard.calcolaCosto(5, 10), 0.001f);
    }

    @Test
    @DisplayName("TU_DOM_02: Calcolo costo con TariffaSostaBreve (Strategy)")
    void testCalcoloTariffaSostaBreve() {
        // 12 * 0.10 = 1.20 €
        assertEquals(1.20f, tariffaSosta.calcolaCosto(12, 0), 0.001f);
    }

    @Test
    @DisplayName("TU_DOM_03: Ricarica e addebito del credito")
    void testGestioneCredito() {
        assertTrue(cliente.ricaricaCredito(10.00f));
        assertEquals(15.00f, cliente.getCreditoResiduo(), 0.001f);

        cliente.scalaCredito(4.50f);
        assertEquals(10.50f, cliente.getCreditoResiduo(), 0.001f);
    }

    @Test
    @DisplayName("TU_DOM_04: Valori limite del credito minimo per il noleggio (1.00 €)")
    void testVincoloCreditoMinimoValoriLimite() {
        assertTrue(new Cliente("a", 1.00f).puoNoleggiare(), "1.00 € (limite incluso): noleggio consentito");
        assertTrue(new Cliente("b", 1.01f).puoNoleggiare(), "1.01 € (sopra il limite): noleggio consentito");
        assertFalse(new Cliente("c", 0.99f).puoNoleggiare(), "0.99 € (sotto il limite): noleggio vietato");
    }

    @Test
    @DisplayName("TU_DOM_05: Ricarica con importo non valido (zero o negativo) rifiutata")
    void testRicaricaImportoNonValido() {
        assertFalse(cliente.ricaricaCredito(0f));
        assertFalse(cliente.ricaricaCredito(-5f));
        assertEquals(5.00f, cliente.getCreditoResiduo(), 0.001f, "Il credito non deve cambiare");
    }

    @Test
    @DisplayName("TU_DOM_06: Scorporo quota marcia / quota sosta nella Corsa")
    void testScorporoQuoteCorsa() {
        OrologioDiTest orologio = new OrologioDiTest();
        Corsa corsa = new Corsa(cliente, new Monopattino("M", 0, 0), orologio);

        orologio.avanza(Duration.ofMinutes(2));   // 2 minuti di guida
        corsa.registraInizioSosta();
        orologio.avanza(Duration.ofMinutes(5));   // 5 minuti di sosta
        corsa.registraFineSosta();
        orologio.avanza(Duration.ofMinutes(5));   // altri 5 minuti di guida

        float totale = corsa.calcolaCostoFinale(tariffaStandard, tariffaSosta, 10);

        // Guida: 12 - 5 = 7 min -> 1.00 + 7*0.20 + 10*0.05 = 2.90 €; Sosta: 5*0.10 = 0.50 €
        assertEquals(5, corsa.getMinutiSostaAccumulati());
        assertEquals(2.90f, corsa.getQuotaGuida(), 0.001f);
        assertEquals(0.50f, corsa.getQuotaSosta(), 0.001f);
        assertEquals(3.40f, totale, 0.001f);
    }

    @Test
    @DisplayName("TU_DOM_07: Arrotondamento per eccesso delle frazioni di minuto")
    void testArrotondamentoMinuti() {
        OrologioDiTest orologio = new OrologioDiTest();
        Corsa corsa = new Corsa(cliente, new Monopattino("M", 0, 0), orologio);

        orologio.avanza(Duration.ofSeconds(61)); // 1 minuto e 1 secondo -> 2 minuti

        float totale = corsa.calcolaCostoFinale(tariffaStandard, tariffaSosta, 0);
        assertEquals(1.40f, totale, 0.001f, "1.00 + 2*0.20 = 1.40 €");
    }

    @Test
    @DisplayName("TU_DOM_08: Annullamento addebito in caso di guasto")
    void testAnnullaAddebito() {
        Corsa corsa = new Corsa(cliente, new Monopattino("M", 0, 0), new OrologioDiTest());
        corsa.calcolaCostoFinale(tariffaStandard, tariffaSosta, 10);

        corsa.annullaAddebito();

        assertEquals(0f, corsa.getCostoTotale(), 0.001f);
        assertEquals(0f, corsa.getQuotaGuida(), 0.001f);
        assertEquals(0f, corsa.getQuotaSosta(), 0.001f);
    }

    @Test
    @DisplayName("TU_DOM_09: Consumo batteria e autonomia in passi")
    void testConsumoBatteria() {
        Monopattino m = new Monopattino("M", 0, 0);
        assertEquals(25, m.getAutonomiaPassi(), "100% / 4 punti per passo = 25 passi");

        m.consumaBatteria(22);  // 100 - 88 = 12%
        assertEquals(12, m.getBatteria());
        assertFalse(m.isBatteriaCritica(), "12% è sopra la soglia");

        m.consumaBatteria(1);   // 8%
        assertTrue(m.isBatteriaCritica(), "8% è sotto la soglia");

        m.consumaBatteria(10);
        assertEquals(0, m.getBatteria(), "La batteria non scende sotto lo 0%");
    }
}
