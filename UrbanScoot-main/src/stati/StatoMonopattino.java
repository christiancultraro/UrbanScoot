package stati;
import dominio.Monopattino;

public interface StatoMonopattino {
    boolean avviaCorsa(Monopattino m);
    boolean terminaCorsa(Monopattino m);

    // --- NUOVI METODI POLIMORFICI ---
    
    // Metodi di interrogazione (nessun instanceof necessario!)
    default boolean isManutenzione() { return false; }
    default boolean isRiparazione() { return false; }
    default boolean isDismesso() { return false; }

    // Metodi per le transizioni di stato amministrative
    default void inviaInRiparazione(Monopattino m) {
        System.out.println("Operazione non consentita in questo stato.");
    }
    
    default void completaRiparazione(Monopattino m) {
        System.out.println("Operazione non consentita in questo stato.");
    }
}