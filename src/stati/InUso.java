package stati;
import dominio.Monopattino;

public class InUso implements StatoMonopattino {
    @Override
    public void avviaCorsa(Monopattino m) {
        System.out.println("Errore: Il monopattino è già in uso da un altro utente.");
    }

    @Override
    public void terminaCorsa(Monopattino m) {
        m.setStato(new Disponibile());
        System.out.println("Corsa terminata. Veicolo " + m.getId() + " bloccato.");
    }

    public static StatoMonopattino getInstance() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getInstance'");
    }
}