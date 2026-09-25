package controller;

import dominio.Monopattino;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Flotta {
    private Map<String, Monopattino> monopattini;

    public Flotta() {
        this.monopattini = new HashMap<>();
    }

    public void aggiungi(Monopattino m) {
        monopattini.put(m.getId(), m);
    }

    public Monopattino cerca(String id) {
        return monopattini.get(id);
    }

    public void rimuovi(String id) {
        monopattini.remove(id);
    }

    public List<Monopattino> getVeicoli() {
        return new ArrayList<>(monopattini.values());
    }

    public List<Monopattino> getVeicoliInManutenzione() {
        List<Monopattino> veicoliGuasti = new ArrayList<>();
        for (Monopattino m : monopattini.values()) {
            if (m.getStatoCorrente().isManutenzione()) {
                veicoliGuasti.add(m);
            }
        }
        return veicoliGuasti;
    }
}