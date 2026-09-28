package controller;
import java.util.ArrayList;
import java.util.List;
import dominio.Cliente;
import dominio.Corsa;

public class GestoreStorico {
    public List<Corsa> getStoricoCliente(Cliente cliente, List<Corsa> storicoGenerale) {
        List<Corsa> storicoPersonale = new ArrayList<>();
        for (Corsa c : storicoGenerale) {
            if (c.getCliente().equals(cliente)) {
                storicoPersonale.add(c);
            }
        }
        return storicoPersonale;
    }
}