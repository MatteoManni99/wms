package service;

import com.google.gson.*;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import model.DatiStatici;
import model.DocumentoTrasporto;
import model.RigaTrasporto;

import java.io.FileOutputStream;

public class PDFService {

    public void creaDocumentoTrasportoPDF(DocumentoTrasporto doc, DatiStatici dati, String fileName) {
        Document document = null;
        try {
            document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            Font normalFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
            Font smallFont = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL);

            // Header azienda
            Paragraph header = new Paragraph(dati.getRagioneSociale(), boldFont);
            header.setAlignment(Element.ALIGN_LEFT);
            document.add(header);

            document.add(new Paragraph(dati.getIndirizzo(), smallFont));
            document.add(new Paragraph(dati.getCap() + " " + dati.getCitta(), smallFont));
            document.add(new Paragraph("P. IVA: " + dati.getPartitaIva(), smallFont));
            document.add(Chunk.NEWLINE);

            // Titolo documento
            Paragraph titolo = new Paragraph("DOCUMENTO DI TRASPORTO (D.D.T.)", boldFont);
            titolo.setAlignment(Element.ALIGN_CENTER);
            document.add(titolo);

            Paragraph numeroDoc = new Paragraph("N° " + doc.getNumero() + " del " + doc.getData(), normalFont);
            numeroDoc.setAlignment(Element.ALIGN_CENTER);
            document.add(numeroDoc);
            document.add(Chunk.NEWLINE);

            // Info destinatario
            document.add(new Paragraph("Destinatario: " + doc.getDestinatario(), normalFont));
            document.add(new Paragraph("Luogo destinazione: " + doc.getLuogoDestinazione(), normalFont));
            document.add(new Paragraph("Causale: " + doc.getCausale(), normalFont));
            document.add(Chunk.NEWLINE);

            // Tabella beni
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1, 3});

            PdfPCell headerCell1 = new PdfPCell(new Phrase("QUANTITÀ", boldFont));
            headerCell1.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell1.setPadding(5);
            table.addCell(headerCell1);

            PdfPCell headerCell2 = new PdfPCell(new Phrase("DESCRIZIONE DEI BENI", boldFont));
            headerCell2.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell2.setPadding(5);
            table.addCell(headerCell2);

            for (RigaTrasporto riga : doc.getRighe()) {
                PdfPCell cell1 = new PdfPCell(new Phrase(riga.getQuantita() + " " + riga.getUnitaMisura(), normalFont));
                cell1.setPadding(5);
                table.addCell(cell1);

                PdfPCell cell2 = new PdfPCell(new Phrase(riga.getDescrizione(), normalFont));
                cell2.setPadding(5);
                table.addCell(cell2);
            }

            document.add(table);
            document.add(Chunk.NEWLINE);

            // Info aggiuntive
            document.add(new Paragraph("N° Colli: " + (doc.getNumeroColli() != null ? doc.getNumeroColli() : "_____"), normalFont));
            document.add(new Paragraph("Aspetto esteriore dei beni: " + (doc.getAspettoEsteriore() != null ? doc.getAspettoEsteriore() : ""), normalFont));
            document.add(Chunk.NEWLINE);

            // Trasporto a cura di
            String trasportoTesto = "TRASPORTO A CURA DEL ( ";
            if (doc.getTrasportoACuraDi() != null) {
                if ("MITTENTE".equals(doc.getTrasportoACuraDi())) {
                    trasportoTesto += "X ) MITTENTE  (   ) DESTINATARIO";
                } else {
                    trasportoTesto += "  ) MITTENTE  ( X ) DESTINATARIO";
                }
            } else {
                trasportoTesto += "  ) MITTENTE  (   ) DESTINATARIO";
            }
            document.add(new Paragraph(trasportoTesto, normalFont));

            document.add(new Paragraph("Inizio trasporto o consegna: " + (doc.getInizioTrasporto() != null ? doc.getInizioTrasporto() : ""), normalFont));

            if (doc.getAnnotazioni() != null && !doc.getAnnotazioni().isEmpty()) {
                document.add(Chunk.NEWLINE);
                Paragraph annotazioniTitle = new Paragraph("ANNOTAZIONI:", boldFont);
                document.add(annotazioniTitle);
                document.add(new Paragraph(doc.getAnnotazioni(), smallFont));
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Errore nella creazione del file PDF: " + e.getMessage());
        } finally {
            if (document != null && document.isOpen()) {
                document.close();
            }
        }
    }
}
