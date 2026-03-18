public class Dismesso implements StatoMonopattino {
    @Override
    public void avviaCorsa(Monopattino m) {
        System.out.println("Errore: Veicolo dismesso.");
    }
    @Override
    public void terminaCorsa(Monopattino m) {
        System.out.println("Errore: Veicolo non in uso.");
    }
}