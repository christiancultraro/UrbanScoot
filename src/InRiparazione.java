public class InRiparazione implements StatoMonopattino {
    @Override
    public void avviaCorsa(Monopattino m) {
        System.out.println("Errore: Veicolo in riparazione.");
    }
    @Override
    public void terminaCorsa(Monopattino m) {
        System.out.println("Errore: Veicolo non in uso.");
    }
}