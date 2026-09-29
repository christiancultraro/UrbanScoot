package dominio;

public interface Tariffa {
    float calcolaCosto(long minutiTrascorsi, int passiEffettuati);
    String getNomeTariffa();
}