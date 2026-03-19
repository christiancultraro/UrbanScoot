package stati;
import dominio.Monopattino;

public class Disponibile implements StatoMonopattino {
    @Override
    public void avviaCorsa(Monopattino m) {
        // Il pattern State delega qui il cambiamento dinamico di stato
        m.setStato(new InUso());
        System.out.println("Sblocco veicolo " + m.getId() + " effettuato.");
    }

    @Override
    public void terminaCorsa(Monopattino m) {
        System.out.println("Errore: Il monopattino è già disponibile e fermo.");
    }
}