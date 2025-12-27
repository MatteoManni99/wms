package model;

public class Bene {
    private String nome;
    private String unitaMisura;

    public Bene(String nome, String unitaMisura) {
        this.nome = nome;
        this.unitaMisura = unitaMisura;
    }

    public String getNome() { return nome; }
    public String getUnitaMisura() { return unitaMisura; }
    public void setNome(String nome) { this.nome = nome; }
    public void setUnitaMisura(String unitaMisura) { this.unitaMisura = unitaMisura; }
}