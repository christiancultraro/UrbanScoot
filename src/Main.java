public class Main {
    public static void main(String[] args) {
        // 1. Creazione dell'oggetto radice (Controller)
        UrbanScoot sistema = new UrbanScoot();

        // 2. Avviamento e caricamento dati (UC0)
        sistema.loadDatiIniziali();

        // 3. Test UC1 (Inizia Corsa) con Utente con credito sufficiente
        sistema.loginUtente("USR-01");
        sistema.inserisciCodiceQR("MONO-01");
        sistema.confermaAvvioCorsa();

        // 4. Test UC1 di un altro utente che prova a prendere lo stesso monopattino (Verifica Pattern State)
        sistema.loginUtente("USR-02");
        sistema.inserisciCodiceQR("MONO-01");
        sistema.confermaAvvioCorsa(); // Dovrebbe dare errore perché MONO-01 è già in uso
    }
}
