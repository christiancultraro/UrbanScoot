public class StatoInUso implements StatoMonopattino {
    @Override
    public void sblocca(Monopattino m) {
        System.out.println("Errore: Il monopattino è già in uso da un altro utente.");
    }

    @Override
    public void blocca(Monopattino m) {
        System.out.println("Corsa terminata. Il monopattino torna DISPONIBILE.");
        m.setStato(new StatoDisponibile());
    }

    @Override
    public String getNomeStato() {
        return "In Uso";
    }
}