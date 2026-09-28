package controller;

import dominio.Corsa;
import dominio.Monopattino;
import dominio.Tariffa;
import dominio.Cliente;
import stati.InManutenzione;
import stati.Disponibile;

public class GestoreCorse {
    
    // Riceve direttamente un Cliente, garantendo la sicurezza del tipo senza cast
    public Corsa iniziaCorsa(Cliente cliente, Monopattino m) {
        float cred = cliente.getCreditoResiduo();
        int batt = m.getBatteria();
        
        if (cred >= 1.00 && batt > 10 && m.getStatoCorrente().isDisponibile()) {
            Corsa c = new Corsa(cliente, m);
            m.sblocca(); 
            System.out.println("[App] Sblocco confermato, timer avviato.");
            return c;
        } else {
            System.out.println("[App] Errore avvio: Credito/Batteria insufficienti o veicolo in manutenzione.");
            return null;
        }
    }
    
    // Sostituito Utente con Cliente come Information Expert per il pagamento
    public float terminaCorsa(Corsa corsaCorrente, Monopattino monopattinoSelezionato, Cliente cliente, 
                             float latArrivo, float lonArrivo, boolean guastoSegnalato, int passiEffettuati) {
                             
        monopattinoSelezionato.setPosizione(latArrivo, lonArrivo);
        
        Tariffa tariffaBase = new Tariffa("Standard", 1.00f, 0.20f, 0.05f);
        float costo = corsaCorrente.calcolaCostoFinale(tariffaBase, passiEffettuati);
        
        monopattinoSelezionato.consumaBatteria(passiEffettuati);
        
        System.out.println("\n=== FINE CORSA ===");
        
        if (guastoSegnalato) {
            System.out.println("[!] ALLARME: Rilevato incidente o guasto critico al veicolo.");
            System.out.println("[!] Politica Aziendale: La corsa viene rimborsata per il disagio causato.");
            costo = 0.0f; 
            corsaCorrente.setCostoTotale(0.0f);
            monopattinoSelezionato.setStato(InManutenzione.getInstance());
        } else {
            System.out.println("--- RICEVUTA NORMALE ---");
            if (monopattinoSelezionato.getBatteria() <= 10) {
                monopattinoSelezionato.setStato(InManutenzione.getInstance());
            } else {
                monopattinoSelezionato.setStato(Disponibile.getInstance());
            }
           
        }
        
        // Delega pulita senza assumere comportamenti fittizi per l'Amministratore
        cliente.scalaCredito(costo);
        
        System.out.println("Distanza percorsa: " + passiEffettuati + " caselle");
        System.out.println("Costo Addebitato: " + String.format("%.2f", costo) + " EUR");
        System.out.println("Credito Residuo: " + String.format("%.2f", cliente.getCreditoResiduo()) + " EUR");
        System.out.println("Batteria Residua: " + monopattinoSelezionato.getBatteria() + "%");
        System.out.println("==================\n");
        return costo;
    }
}