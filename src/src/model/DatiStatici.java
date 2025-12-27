package model;

public class DatiStatici {
    private String ragioneSociale;
    private String indirizzo;
    private String cap;
    private String citta;
    private String partitaIva;
    private String magazzino;

    public DatiStatici() {
        this.ragioneSociale = "D.G.M. snc di Deflippi Guido & C";
        this.indirizzo = "Str. Val Villata 4 Fraz. Bardassano";
        this.cap = "10020";
        this.citta = "GASSINO T.se (TO)";
        this.partitaIva = "06676620013";
        this.magazzino = "Magazzino Via Mandamentale 8\nMONTALDO T.se (TO)";
    }

    // Getters e Setters
    public String getRagioneSociale() {
        return ragioneSociale;
    }

    public String getIndirizzo() {
        return indirizzo;
    }

    public String getCap() {
        return cap;
    }

    public String getCitta() {
        return citta;
    }

    public String getPartitaIva() {
        return partitaIva;
    }

    public String getMagazzino() {
        return magazzino;
    }

    public void setRagioneSociale(String r) {
        this.ragioneSociale = r;
    }

    public void setIndirizzo(String i) {
        this.indirizzo = i;
    }

    public void setCap(String c) {
        this.cap = c;
    }

    public void setCitta(String c) {
        this.citta = c;
    }

    public void setPartitaIva(String p) {
        this.partitaIva = p;
    }

    public void setMagazzino(String m) {
        this.magazzino = m;
    }
}
