public class InManutenzione implements StatoMonopattino {
    @Override
    public void avviaCorsa(Monopattino m) {
        System.out.println("Errore: Veicolo in manutenzione, non noleggiabile.");
    }

    @Override
    public void terminaCorsa(Monopattino m) {
        System.out.println("Errore: Il veicolo non è attualmente in uso.");
    }
}