package model;

import model.RigaTrasporto;

import java.util.ArrayList;
import java.util.List;

public class DocumentoTrasporto {
    private String numero;
    private String data;
    private String destinatario;
    private String luogoDestinazione;
    private String causale;
    private List<RigaTrasporto> righe;
    private String aspettoEsteriore;
    private String numeroColli;
    private String trasportoACuraDi; // "MITTENTE" o "DESTINATARIO"
    private String inizioTrasporto;
    private String annotazioni;

    public DocumentoTrasporto() {
        this.righe = new ArrayList<>();
    }

    // Getters e Setters
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
    public String getDestinatario() { return destinatario; }
    public void setDestinatario(String destinatario) { this.destinatario = destinatario; }
    public String getLuogoDestinazione() { return luogoDestinazione; }
    public void setLuogoDestinazione(String luogo) { this.luogoDestinazione = luogo; }
    public String getCausale() { return causale; }
    public void setCausale(String causale) { this.causale = causale; }
    public List<RigaTrasporto> getRighe() { return righe; }
    public void addRiga(RigaTrasporto riga) { this.righe.add(riga); }
    public String getAspettoEsteriore() { return aspettoEsteriore; }
    public void setAspettoEsteriore(String aspetto) { this.aspettoEsteriore = aspetto; }
    public String getNumeroColli() { return numeroColli; }
    public void setNumeroColli(String numeroColli) { this.numeroColli = numeroColli; }
    public String getTrasportoACuraDi() { return trasportoACuraDi; }
    public void setTrasportoACuraDi(String trasporto) { this.trasportoACuraDi = trasporto; }
    public String getInizioTrasporto() { return inizioTrasporto; }
    public void setInizioTrasporto(String inizio) { this.inizioTrasporto = inizio; }
    public String getAnnotazioni() { return annotazioni; }
    public void setAnnotazioni(String annotazioni) { this.annotazioni = annotazioni; }
}