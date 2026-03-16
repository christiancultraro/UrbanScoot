public class StatoDisponibile implements StatoMonopattino {
    @Override
    public void sblocca(Monopattino m) {
        System.out.println("Sblocco in corso... Il monopattino è ora IN USO.");
        m.setStato(new StatoInUso());
    }

    @Override
    public void blocca(Monopattino m) {
        System.out.println("Il monopattino è già bloccato e disponibile.");
    }

    @Override
    public String getNomeStato() {
        return "Disponibile";
    }
}
