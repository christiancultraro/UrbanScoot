package stati;
import dominio.Monopattino;

public class Disponibile implements StatoMonopattino {
    //esempio con singleton, non è necessario ma è un pattern comune per stati che non hanno variabili d'istanza
    private static Disponibile istanza;
    
    public Disponibile() {} // Costruttore privato
    
    public static Disponibile getInstance() {
        if (istanza == null) istanza = new Disponibile();
        return istanza;
    }
    @Override
    public void avviaCorsa(Monopattino m) {
        // Il pattern State delega qui il cambiamento dinamico di stato
        m.setStato(InUso.getInstance()); // Cambio stato dinamico!
        System.out.println("Sblocco veicolo " + m.getId() + " effettuato.");
    }

    @Override
    public void terminaCorsa(Monopattino m) {
        System.out.println("Errore: Il monopattino è già disponibile e fermo.");
    }
}