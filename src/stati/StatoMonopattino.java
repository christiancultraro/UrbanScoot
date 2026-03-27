package stati;
import dominio.Monopattino;

public interface StatoMonopattino {
    boolean avviaCorsa(Monopattino m);
    boolean terminaCorsa(Monopattino m);

  
    
    // Metodi di interrogazione 
    default boolean isManutenzione() { return false; }
    default boolean isRiparazione() { return false; }
    default boolean isDismesso() { return false; }
    default boolean isDisponibile() { return false; }
    // Metodi per le transizioni di stato amministrative
    default void inviaInRiparazione(Monopattino m) {
        System.out.println("Operazione non consentita in questo stato.");
    }
    
    default void completaRiparazione(Monopattino m) {
        System.out.println("Operazione non consentita in questo stato.");
    }
}