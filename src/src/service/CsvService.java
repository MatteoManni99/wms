package service;

import model.Bene;
import model.DatiStatici;
import model.DocumentoTrasporto;
import model.RigaTrasporto;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class CsvService {

    /**
     * Salva l'inventario in formato CSV
     */
    public void salvaInventario(List<Bene> beni, Map<String, Double> giacenze, String fileName) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            // Header
            writer.write("Bene,Unità di Misura,Giacenza");
            writer.newLine();

            // Dati
            for (Bene bene : beni) {
                String nome = escapeCsv(bene.getNome());
                String unitaMisura = escapeCsv(bene.getUnitaMisura());
                double giacenza = giacenze.getOrDefault(bene.getNome(), 0.0);

                writer.write(nome + "," + unitaMisura + "," + giacenza);
                writer.newLine();
            }
        }
    }

    /**
     * Crea il documento di trasporto in formato CSV
     */
    public void creaDocumentoTrasportoCSV(DocumentoTrasporto doc, DatiStatici dati, String fileName) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            // Header azienda
            writer.write(escapeCsv(dati.getRagioneSociale()));
            writer.newLine();
            writer.write(escapeCsv(dati.getIndirizzo()));
            writer.newLine();
            writer.write(escapeCsv(dati.getCap() + " " + dati.getCitta()));
            writer.newLine();
            writer.write("P. IVA: " + escapeCsv(dati.getPartitaIva()));
            writer.newLine();
            writer.newLine();

            // Info documento
            writer.write("DOCUMENTO DI TRASPORTO (D.D.T.)");
            writer.newLine();
            writer.write("N° " + escapeCsv(doc.getNumero()) + " del " + escapeCsv(doc.getData()));
            writer.newLine();
            writer.newLine();

            // Destinatario
            writer.write("Destinatario: " + escapeCsv(doc.getDestinatario()));
            writer.newLine();
            writer.write("Luogo destinazione: " + escapeCsv(doc.getLuogoDestinazione()));
            writer.newLine();
            writer.write("Causale: " + escapeCsv(doc.getCausale()));
            writer.newLine();
            writer.newLine();

            // Tabella beni - Header
            writer.write("QUANTITÀ,DESCRIZIONE DEI BENI");
            writer.newLine();

            // Tabella beni - Dati
            for (RigaTrasporto riga : doc.getRighe()) {
                String quantita = escapeCsv(riga.getQuantita() + " " + riga.getUnitaMisura());
                String descrizione = escapeCsv(riga.getDescrizione());
                writer.write(quantita + "," + descrizione);
                writer.newLine();
            }

            writer.newLine();
            writer.write("N° Colli: " + escapeCsv(doc.getNumeroColli() != null ? doc.getNumeroColli() : ""));
            writer.newLine();
            writer.write("Aspetto esteriore: " + escapeCsv(doc.getAspettoEsteriore() != null ? doc.getAspettoEsteriore() : ""));
            writer.newLine();
            writer.write("Trasporto a cura di: " + escapeCsv(doc.getTrasportoACuraDi() != null ? doc.getTrasportoACuraDi() : ""));
            writer.newLine();
            writer.write("Inizio trasporto: " + escapeCsv(doc.getInizioTrasporto() != null ? doc.getInizioTrasporto() : ""));
            writer.newLine();

            if (doc.getAnnotazioni() != null && !doc.getAnnotazioni().isEmpty()) {
                writer.newLine();
                writer.write("ANNOTAZIONI:");
                writer.newLine();
                writer.write(escapeCsv(doc.getAnnotazioni()));
            }
        }
    }

    /**
     * Escapa i valori CSV: se contengono virgole, spazi doppi o newline,
     * li racchiude tra virgolette doppie
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}