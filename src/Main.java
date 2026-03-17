public class Main {
    public static void main(String[] args) {
        // Avvio dell'applicazione (UC0)
        UrbanScoot sistema = UrbanScoot.getInstance();
        sistema.loadDatiIniziali(); // Carica USR-01 e MONO-01

        // L'Amministratore aggiunge un nuovo veicolo (UC2)
        sistema.aggiungiNuovoMonopattino("MONO-02", 45.0f, 9.0f);
        System.out.println("-------------------------------------------------");

        // L'Utente si avvicina e scansiona il codice (UC1 - Fase 1)
        sistema.inserisciCodiceQR("MONO-02", "USR-01");

        // L'Utente conferma nell'app (UC1 - Fase 2)
        sistema.confermaAvvioCorsa();
        
        System.out.println("-------------------------------------------------");
        
        // Se un altro utente (o lo stesso) provasse a riprendere MONO-02:
        sistema.inserisciCodiceQR("MONO-02", "USR-01");
        sistema.confermaAvvioCorsa(); // Darà errore perché è già in uso!
    }
}