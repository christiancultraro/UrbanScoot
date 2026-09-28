package controller;
import dominio.Cliente;

public class GestoreAccount {
    public void ricaricaCredito(Cliente cliente, float importo) {
        if (importo > 0) {
            cliente.ricaricaCredito(importo);
        }
    }
}