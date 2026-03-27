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

    // Metodo per restituire l'ID dell'admin
    /*public String getId() {
        return this.idAdmin; // Se la tua variabile si chiama in modo diverso, es. solo "id", metti "this.id"
    }*/
}