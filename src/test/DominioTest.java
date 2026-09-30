package test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dominio.Cliente;
import dominio.TariffaStandard;
import dominio.TariffaSostaBreve;

@DisplayName("Test Unitari: Logica di Dominio e Regole di Business")
class DominioTest {

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        // Setup fixture
        cliente = new Cliente("mario_rossi", 5.00f);
    }

    @Test
    @DisplayName("TU_DOM_01: Verifica calcolo costo con TariffaStandard (Strategy)")
    void testCalcoloTariffaStandard() {
        
        TariffaStandard tariffa = new TariffaStandard(1.00f, 0.20f, 0.05f);

        //  5 minuti e 10 passi -> 1.00 + (5 * 0.20) + (10 * 0.05) = 2.50€
        float costoEffettivo = tariffa.calcolaCosto(5, 10);

        
        assertEquals(2.50f, costoEffettivo, 0.001f, "Il calcolo della tariffa standard deve corrispondere alla formula");
    }

    @Test
    @DisplayName("TU_DOM_02: Verifica calcolo costo con TariffaSostaBreve")
    void testCalcoloTariffaSostaBreve() {
        TariffaSostaBreve tariffaSosta = new TariffaSostaBreve(0.10f);

        //  12 minuti di pausa
        float costoEffettivo = tariffaSosta.calcolaCosto(12, 0);

        // 12 * 0.10 = 1.20€
        assertEquals(1.20f, costoEffettivo, 0.001f, "La tariffa di sosta deve calcolare 0.10€ per ogni minuto");
    }

    @Test
    @DisplayName("TU_DOM_03: Verifica scalatura credito e ricarica")
    void testGestioneCredito() {
        cliente.ricaricaCredito(10.00f);
        assertEquals(15.00f, cliente.getCreditoResiduo(), 0.001f, "La ricarica deve incrementare il credito");

        cliente.scalaCredito(4.50f);
        assertEquals(10.50f, cliente.getCreditoResiduo(), 0.001f, "La spesa deve decrementare il credito");
    }

    @Test
    @DisplayName("TU_DOM_04: Verifica vincolo di credito minimo (puoNoleggiare)")
    void testVincoloCreditoMinimo() {
        // Credito 5.00€ -> OK
        assertTrue(cliente.puoNoleggiare(), "Con credito >= 1.00€ deve essere abilitato al noleggio");

        // Riduciamo a 0.50€ -> Blocco
        cliente.scalaCredito(4.50f);
        assertFalse(cliente.puoNoleggiare(), "Con credito < 1.00€ il noleggio deve essere vietato");
    }
}