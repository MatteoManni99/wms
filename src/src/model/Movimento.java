package model;

public class Movimento {
    private String data;
    private String tipoBene;
    private double quantita;
    private String tipo; // "CARICO" o "SCARICO"
    private String note;

    public Movimento(String data, String tipoBene, double quantita, String tipo, String note) {
        this.data = data;
        this.tipoBene = tipoBene;
        this.quantita = quantita;
        this.tipo = tipo;
        this.note = note;
    }

    public String getData() { return data; }
    public String getTipoBene() { return tipoBene; }
    public double getQuantita() { return quantita; }
    public String getTipo() { return tipo; }
    public String getNote() { return note; }
}