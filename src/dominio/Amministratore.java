package dominio;
public class Amministratore extends Utente{

    public Amministratore(String idAdmin) {
        super(idAdmin);
    }

    public String getIdAdmin() {
        return this.getId();
    }

    @Override
    public boolean puoNoleggiare() {
    return false; // L'admin gestisce, non noleggia
    }

  
}