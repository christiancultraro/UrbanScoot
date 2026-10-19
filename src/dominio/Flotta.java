package dominio;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Flotta {
    private Map<String, Monopattino> monopattini;

    public Flotta() {
        this.monopattini = new HashMap<>();
    }

    /** @return false se nella flotta esiste già un veicolo con lo stesso id. */
    public boolean aggiungi(Monopattino m) {
        if (m == null || contiene(m.getId())) {
            return false;
        }
        monopattini.put(m.getId(), m);
        return true;
    }

    public boolean contiene(String id) {
        return monopattini.containsKey(id);
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