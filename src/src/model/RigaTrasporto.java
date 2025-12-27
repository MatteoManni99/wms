package model;

public class RigaTrasporto {
    private double quantita;
    private String unitaMisura;
    private String descrizione;

    public RigaTrasporto(double quantita, String unitaMisura, String descrizione) {
        this.quantita = quantita;
        this.unitaMisura = unitaMisura;
        this.descrizione = descrizione;
    }

    public double getQuantita() {
        return quantita;
    }

    public String getUnitaMisura() {
        return unitaMisura;
    }

    public String getDescrizione() {
        return descrizione;
    }
}
