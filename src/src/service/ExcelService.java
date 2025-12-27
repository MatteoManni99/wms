package service;

import model.Bene;
import model.DatiStatici;
import model.DocumentoTrasporto;
import model.RigaTrasporto;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ExcelService {

    public void salvaInventario(List<Bene> beni, Map<String, Double> giacenze, String fileName) throws IOException {
        Workbook workbook = new XSSFWorkbook();

        Sheet sheet = workbook.createSheet("Inventario");

        // Header
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        String[] headers = {"Bene", "Unità di Misura", "Giacenza"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Dati
        int rowNum = 1;
        for (Bene bene : beni) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(bene.getNome());
            row.createCell(1).setCellValue(bene.getUnitaMisura());
            double giacenza = giacenze.getOrDefault(bene.getNome(), 0.0);
            row.createCell(2).setCellValue(giacenza);
        }

        // Auto-size colonne
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        FileOutputStream fileOut = new FileOutputStream(fileName);
        workbook.write(fileOut);
        fileOut.close();

    }

    public void creaDocumentoTrasportoExcel(DocumentoTrasporto doc, DatiStatici dati, String fileName) {
        Workbook workbook = null;
        FileOutputStream fileOut = null;
        try {
            workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Documento Trasporto");

            CellStyle boldStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldStyle.setFont(boldFont);

            CellStyle borderStyle = workbook.createCellStyle();
            borderStyle.setBorderTop(BorderStyle.THIN);
            borderStyle.setBorderBottom(BorderStyle.THIN);
            borderStyle.setBorderLeft(BorderStyle.THIN);
            borderStyle.setBorderRight(BorderStyle.THIN);

            int rowNum = 0;

            // Header azienda
            Row row = sheet.createRow(rowNum++);
            Cell cell = row.createCell(0);
            cell.setCellValue(dati.getRagioneSociale());
            cell.setCellStyle(boldStyle);

            sheet.createRow(rowNum++).createCell(0).setCellValue(dati.getIndirizzo());
            sheet.createRow(rowNum++).createCell(0).setCellValue(dati.getCap() + " " + dati.getCitta());
            sheet.createRow(rowNum++).createCell(0).setCellValue("P. IVA: " + dati.getPartitaIva());

            rowNum++;

            // Info documento
            row = sheet.createRow(rowNum++);
            cell = row.createCell(0);
            cell.setCellValue("DOCUMENTO DI TRASPORTO (D.D.T.)");
            cell.setCellStyle(boldStyle);

            sheet.createRow(rowNum++).createCell(0).setCellValue("N° " + doc.getNumero() + " del " + doc.getData());
            rowNum++;

            // Destinatario
            sheet.createRow(rowNum++).createCell(0).setCellValue("Destinatario: " + doc.getDestinatario());
            sheet.createRow(rowNum++).createCell(0).setCellValue("Luogo destinazione: " + doc.getLuogoDestinazione());
            sheet.createRow(rowNum++).createCell(0).setCellValue("Causale: " + doc.getCausale());
            rowNum++;

            // Tabella beni
            row = sheet.createRow(rowNum++);
            String[] headers = {"QUANTITÀ", "DESCRIZIONE DEI BENI"};
            for (int i = 0; i < headers.length; i++) {
                cell = row.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(boldStyle);
            }

            for (RigaTrasporto riga : doc.getRighe()) {
                row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(riga.getQuantita() + " " + riga.getUnitaMisura());
                row.createCell(1).setCellValue(riga.getDescrizione());
            }

            rowNum++;
            sheet.createRow(rowNum++).createCell(0).setCellValue("N° Colli: " + (doc.getNumeroColli() != null ? doc.getNumeroColli() : ""));
            sheet.createRow(rowNum++).createCell(0).setCellValue("Aspetto esteriore: " + (doc.getAspettoEsteriore() != null ? doc.getAspettoEsteriore() : ""));
            sheet.createRow(rowNum++).createCell(0).setCellValue("Trasporto a cura di: " + (doc.getTrasportoACuraDi() != null ? doc.getTrasportoACuraDi() : ""));
            sheet.createRow(rowNum++).createCell(0).setCellValue("Inizio trasporto: " + (doc.getInizioTrasporto() != null ? doc.getInizioTrasporto() : ""));

            if (doc.getAnnotazioni() != null && !doc.getAnnotazioni().isEmpty()) {
                rowNum++;
                sheet.createRow(rowNum++).createCell(0).setCellValue("ANNOTAZIONI:");
                sheet.createRow(rowNum++).createCell(0).setCellValue(doc.getAnnotazioni());
            }

            // Auto-size
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            sheet.setColumnWidth(1, 15000);

            fileOut = new FileOutputStream(fileName);
            workbook.write(fileOut);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Errore nella creazione del file Excel: " + e.getMessage());
        } finally {
            try {
                if (fileOut != null) fileOut.close();
                if (workbook != null) workbook.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
