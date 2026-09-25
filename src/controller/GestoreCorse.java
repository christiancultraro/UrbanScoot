package controller;

import dominio.Corsa;
import dominio.Monopattino;
import dominio.Tariffa;
import dominio.Utente;
import stati.InManutenzione;
import stati.Disponibile;

public class GestoreCorse {
    
    // Non richiede più lo "storico", si limita a restituire la Corsa valida
    public Corsa iniziaCorsa(Utente u, Monopattino m) {
        float cred = u.getCreditoResiduo();
        int batt = m.getBatteria();
        
        if (cred >= 1.00 && batt > 10 && m.getStatoCorrente().isDisponibile()) {
            Corsa c = new Corsa(u, m);
            m.sblocca(); 
            System.out.println("[App] Sblocco confermato, timer avviato.");
            return c;
        } else {
            System.out.println("[App] Errore avvio: Credito/Batteria insufficienti o veicolo in manutenzione.");
            return null;
        }
    }
    
    public void terminaCorsa(Corsa corsaCorrente, Monopattino monopattinoSelezionato, Utente utenteCorrente, 
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
            monopattinoSelezionato.setStato(InManutenzione.getInstance());
        } else {
            System.out.println("--- RICEVUTA NORMALE ---");
            if (monopattinoSelezionato.getBatteria() <= 10) {
                monopattinoSelezionato.setStato(InManutenzione.getInstance());
            } else {
                monopattinoSelezionato.setStato(Disponibile.getInstance());
            }
        }
        
        utenteCorrente.scalaCredito(costo);
        
        System.out.println("Distanza percorsa: " + passiEffettuati + " caselle");
        System.out.println("Costo Addebitato: " + String.format("%.2f", costo) + " EUR");
        System.out.println("Credito Residuo: " + String.format("%.2f", utenteCorrente.getCreditoResiduo()) + " EUR");
        System.out.println("Batteria Residua: " + monopattinoSelezionato.getBatteria() + "%");
        System.out.println("==================\n");
    }
}