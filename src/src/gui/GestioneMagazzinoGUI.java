package gui;

import model.*;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import service.DataService;
import service.ExcelService;
import service.PDFService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class GestioneMagazzinoGUI extends JFrame {
    private DataService dataService;
    private ExcelService excelService;
    private PDFService pdfService;

    private List<Bene> beni;
    private List<Movimento> movimenti;
    private DatiStatici datiStatici;

    private JTabbedPane tabbedPane;

    public GestioneMagazzinoGUI() {
        dataService = new DataService();
        excelService = new ExcelService();
        pdfService = new PDFService();

        beni = dataService.caricaBeni();
        movimenti = dataService.caricaMovimenti();
        datiStatici = dataService.caricaDatiStatici();

        setTitle("Gestione Magazzino - D.G.M. snc");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);

        creaGUI();
    }

    private void creaGUI() {
        tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Carico Materiali", creaPannelloCarico());
        tabbedPane.addTab("Documento Trasporto", creaPannelloTrasporto());
        tabbedPane.addTab("Movimenti", creaPannelloMovimenti());
        tabbedPane.addTab("Inventario", creaPannelloInventario());
        tabbedPane.addTab("Gestione Beni", creaPannelloGestioneBeni());
        tabbedPane.addTab("Impostazioni", creaPannelloImpostazioni());

        add(tabbedPane);
    }

    private JPanel creaPannelloCarico() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Data con calendario
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Data:"), gbc);

        gbc.gridx = 1;
        JPanel dataPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JTextField dataField = new JTextField(new SimpleDateFormat("dd/MM/yyyy").format(new Date()), 15);
        JButton calendarBtn = new JButton("📅");
        calendarBtn.addActionListener(e -> {
            Date selectedDate = mostraCalendario(this);
            if (selectedDate != null) {
                dataField.setText(new SimpleDateFormat("dd/MM/yyyy").format(selectedDate));
            }
        });
        dataPanel.add(dataField);
        dataPanel.add(calendarBtn);
        formPanel.add(dataPanel, gbc);

        // Bene
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Materiale:"), gbc);

        gbc.gridx = 1;
        JComboBox<String> beneCombo = new JComboBox<>();
        aggiornaComboBoxBeni(beneCombo);
        formPanel.add(beneCombo, gbc);

        // Quantità
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Quantità:"), gbc);

        gbc.gridx = 1;
        JTextField quantitaField = new JTextField(20);
        formPanel.add(quantitaField, gbc);

        // Unità di misura (readonly)
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Unità di Misura:"), gbc);

        gbc.gridx = 1;
        JTextField umField = new JTextField(20);
        umField.setEditable(false);
        formPanel.add(umField, gbc);

        // Note
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Note:"), gbc);

        gbc.gridx = 1;
        JTextField noteField = new JTextField(20);
        formPanel.add(noteField, gbc);

        // Update unità misura quando cambia bene
        beneCombo.addActionListener(e -> {
            String nomeBene = (String) beneCombo.getSelectedItem();
            if (nomeBene != null) {
                Bene bene = trovaBene(nomeBene);
                if (bene != null) {
                    umField.setText(bene.getUnitaMisura());
                }
            }
        });

        // Inizializza unità misura
        if (beneCombo.getItemCount() > 0) {
            beneCombo.setSelectedIndex(0);
        }

        // Pulsante registra
        JButton registraBtn = new JButton("Registra Carico");
        registraBtn.setBackground(new Color(220, 220, 220));
        registraBtn.setForeground(Color.BLACK);
        registraBtn.setFocusPainted(false);

        registraBtn.addActionListener(e -> {
            try {
                String data = dataField.getText();
                String nomeBene = (String) beneCombo.getSelectedItem();
                double quantita = Double.parseDouble(quantitaField.getText());
                String note = noteField.getText();

                Movimento mov = new Movimento(data, nomeBene, quantita, "CARICO", note);
                movimenti.add(mov);
                dataService.salvaMovimenti(movimenti);

                salvaInventarioExcel();

                JOptionPane.showMessageDialog(this,
                        "Carico registrato con successo!\nInventario salvato in inventario.xlsx",
                        "Successo", JOptionPane.INFORMATION_MESSAGE);

                quantitaField.setText("");
                noteField.setText("");
                dataField.setText(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Errore: inserire una quantità valida",
                        "Errore", JOptionPane.ERROR_MESSAGE);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 5, 5, 5);
        formPanel.add(registraBtn, gbc);

        panel.add(formPanel, BorderLayout.NORTH);

        return panel;
    }


    private JPanel creaPannelloMovimenti() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Tabella movimenti
        String[] columns = {"Data", "Tipo", "Bene", "Quantità", "Note"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Carica movimenti
        aggiornaTabMovimenti(tableModel);

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Pannello pulsanti
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton aggiornaBtn = new JButton("Aggiorna");
        aggiornaBtn.addActionListener(e -> aggiornaTabMovimenti(tableModel));

        JButton eliminaBtn = new JButton("Elimina Selezionato");
        eliminaBtn.setBackground(new Color(220, 220, 220));
        eliminaBtn.setForeground(Color.BLACK);
        eliminaBtn.setFocusPainted(false);

        eliminaBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Eliminare il movimento selezionato?\nQuesta operazione aggiornerà l'inventario.",
                        "Conferma Eliminazione",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                if (confirm == JOptionPane.YES_OPTION) {
                    movimenti.remove(selectedRow);
                    dataService.salvaMovimenti(movimenti);
                    try {
                        salvaInventarioExcel();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    aggiornaTabMovimenti(tableModel);

                    JOptionPane.showMessageDialog(this,
                            "Movimento eliminato e inventario aggiornato",
                            "Successo",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Selezionare un movimento da eliminare",
                        "Attenzione",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        btnPanel.add(aggiornaBtn);
        btnPanel.add(eliminaBtn);

        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void aggiornaTabMovimenti(DefaultTableModel tableModel) {
        tableModel.setRowCount(0);
        for (Movimento mov : movimenti) {
            tableModel.addRow(new Object[]{
                    mov.getData(),
                    mov.getTipo(),
                    mov.getTipoBene(),
                    String.format("%.2f", mov.getQuantita()),
                    mov.getNote() != null ? mov.getNote() : ""
            });
        }
    }

    private JPanel creaPannelloTrasporto() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Numero documento
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Numero Documento:"), gbc);
        gbc.gridx = 1;
        JTextField numeroField = new JTextField("35", 20);
        formPanel.add(numeroField, gbc);

        // Data con calendario
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Data:"), gbc);
        gbc.gridx = 1;
        JPanel dataPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JTextField dataField = new JTextField(new SimpleDateFormat("dd/MM/yyyy").format(new Date()), 15);
        JButton calendarBtn = new JButton("📅");
        calendarBtn.addActionListener(e -> {
            Date selectedDate = mostraCalendario(this);
            if (selectedDate != null) {
                dataField.setText(new SimpleDateFormat("dd/MM/yyyy").format(selectedDate));
            }
        });
        dataPanel.add(dataField);
        dataPanel.add(calendarBtn);
        formPanel.add(dataPanel, gbc);

        // Destinatario
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Destinatario:"), gbc);
        gbc.gridx = 1;
        JTextField destinatarioField = new JTextField("", 20);
        formPanel.add(destinatarioField, gbc);

        // Luogo destinazione
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Luogo Destinazione:"), gbc);
        gbc.gridx = 1;
        JTextField luogoField = new JTextField("Via ... (TO)", 20);
        formPanel.add(luogoField, gbc);

        // Causale
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Causale:"), gbc);
        gbc.gridx = 1;
        JTextField causaleField = new JTextField("", 20);
        formPanel.add(causaleField, gbc);

        // N° Colli
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("N° Colli:"), gbc);
        gbc.gridx = 1;
        JTextField colliField = new JTextField("", 20);
        formPanel.add(colliField, gbc);

        // Aspetto esteriore
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(new JLabel("Aspetto Esteriore:"), gbc);
        gbc.gridx = 1;
        JTextField aspettoField = new JTextField("", 20);
        formPanel.add(aspettoField, gbc);

        // Trasporto a cura di
        gbc.gridx = 0; gbc.gridy = 7;
        formPanel.add(new JLabel("Trasporto a cura di:"), gbc);
        gbc.gridx = 1;
        JPanel trasportoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ButtonGroup trasportoGroup = new ButtonGroup();
        JRadioButton mittenteRadio = new JRadioButton("Mittente", true);
        JRadioButton destinatarioRadio = new JRadioButton("Destinatario");
        trasportoGroup.add(mittenteRadio);
        trasportoGroup.add(destinatarioRadio);
        trasportoPanel.add(mittenteRadio);
        trasportoPanel.add(destinatarioRadio);
        formPanel.add(trasportoPanel, gbc);

        // Inizio trasporto con calendario
        gbc.gridx = 0; gbc.gridy = 8;
        formPanel.add(new JLabel("Inizio Trasporto:"), gbc);
        gbc.gridx = 1;
        JPanel inizioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JTextField inizioField = new JTextField(new SimpleDateFormat("dd/MM/yyyy").format(new Date()), 15);
        JButton calendarBtn2 = new JButton("📅");
        calendarBtn2.addActionListener(e -> {
            Date selectedDate = mostraCalendario(this);
            if (selectedDate != null) {
                inizioField.setText(new SimpleDateFormat("dd/MM/yyyy").format(selectedDate));
            }
        });
        inizioPanel.add(inizioField);
        inizioPanel.add(calendarBtn2);
        formPanel.add(inizioPanel, gbc);

        // Annotazioni (campo più grande)
        gbc.gridx = 0; gbc.gridy = 9;
        gbc.anchor = GridBagConstraints.NORTH;
        formPanel.add(new JLabel("Annotazioni:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        JTextArea annotazioniArea = new JTextArea(3, 20);
        annotazioniArea.setLineWrap(true);
        annotazioniArea.setWrapStyleWord(true);
        JScrollPane annotazioniScroll = new JScrollPane(annotazioniArea);
        formPanel.add(annotazioniScroll, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        panel.add(formPanel, BorderLayout.NORTH);

        // Tabella righe
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Materiali da Trasportare"));

        String[] columns = {"Bene", "Quantità", "U.M.", "Descrizione"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0);
        JTable table = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(table);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton aggiungiRigaBtn = new JButton("Aggiungi Materiale");
        aggiungiRigaBtn.addActionListener(e -> {
            JPanel addPanel = new JPanel(new GridLayout(4, 2, 5, 5));

            JComboBox<String> beneCombo = new JComboBox<>();
            aggiornaComboBoxBeni(beneCombo);
            JTextField qtaField = new JTextField();
            JTextField descField = new JTextField();
            JLabel umLabel = new JLabel("");

            beneCombo.addActionListener(ev -> {
                String nomeBene = (String) beneCombo.getSelectedItem();
                if (nomeBene != null) {
                    Bene bene = trovaBene(nomeBene);
                    if (bene != null) {
                        umLabel.setText(bene.getUnitaMisura());
                        if (descField.getText().isEmpty()) {
                            descField.setText(bene.getNome());
                        }
                    }
                }
            });

            if (beneCombo.getItemCount() > 0) {
                beneCombo.setSelectedIndex(0);
            }

            addPanel.add(new JLabel("Bene:"));
            addPanel.add(beneCombo);
            addPanel.add(new JLabel("Quantità:"));
            addPanel.add(qtaField);
            addPanel.add(new JLabel("Unità Misura:"));
            addPanel.add(umLabel);
            addPanel.add(new JLabel("Descrizione:"));
            addPanel.add(descField);

            int result = JOptionPane.showConfirmDialog(this, addPanel,
                    "Aggiungi Materiale", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                try {
                    String bene = (String) beneCombo.getSelectedItem();
                    double qta = Double.parseDouble(qtaField.getText());
                    String um = umLabel.getText();
                    String desc = descField.getText();

                    tableModel.addRow(new Object[]{bene, qta, um, desc});
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Quantità non valida",
                            "Errore", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton rimuoviRigaBtn = new JButton("Rimuovi Selezionato");
        rimuoviRigaBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                tableModel.removeRow(selectedRow);
            }
        });

        btnPanel.add(aggiungiRigaBtn);
        btnPanel.add(rimuoviRigaBtn);
        tablePanel.add(btnPanel, BorderLayout.SOUTH);

        panel.add(tablePanel, BorderLayout.CENTER);

        // Pulsante genera documenti
        JPanel bottomPanel = new JPanel(new FlowLayout());
        JButton generaBtn = new JButton("Genera Documento di Trasporto");
        generaBtn.setBackground(new Color(220, 220, 220));
        generaBtn.setForeground(Color.BLACK);
        generaBtn.setFocusPainted(false);

        generaBtn.addActionListener(e -> {
            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this,
                        "Aggiungere almeno un materiale",
                        "Errore", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Conferma prima di creare
            StringBuilder riepilogo = new StringBuilder();
            riepilogo.append("Documento N° ").append(numeroField.getText()).append("\n");
            riepilogo.append("Data: ").append(dataField.getText()).append("\n");
            riepilogo.append("Destinatario: ").append(destinatarioField.getText()).append("\n\n");
            riepilogo.append("Materiali:\n");
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                riepilogo.append("- ").append(tableModel.getValueAt(i, 1)).append(" ")
                        .append(tableModel.getValueAt(i, 2)).append(" ")
                        .append(tableModel.getValueAt(i, 0)).append("\n");
            }
            riepilogo.append("\nGenerare il documento di trasporto?");

            int confirm = JOptionPane.showConfirmDialog(this,
                    riepilogo.toString(),
                    "Conferma Generazione Documento",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            try {
                DocumentoTrasporto doc = new DocumentoTrasporto();
                doc.setNumero(numeroField.getText());
                doc.setData(dataField.getText());
                doc.setDestinatario(destinatarioField.getText());
                doc.setLuogoDestinazione(luogoField.getText());
                doc.setCausale(causaleField.getText());
                doc.setNumeroColli(colliField.getText());
                doc.setAspettoEsteriore(aspettoField.getText());
                doc.setTrasportoACuraDi(mittenteRadio.isSelected() ? "MITTENTE" : "DESTINATARIO");
                doc.setInizioTrasporto(inizioField.getText());
                doc.setAnnotazioni(annotazioniArea.getText());

                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    String beneName = (String) tableModel.getValueAt(i, 0);
                    double qta = Double.parseDouble(tableModel.getValueAt(i, 1).toString());
                    String um = (String) tableModel.getValueAt(i, 2);
                    String desc = (String) tableModel.getValueAt(i, 3);

                    doc.addRiga(new RigaTrasporto(qta, um, desc));

                    // Registra scarico
                    Movimento mov = new Movimento(dataField.getText(), beneName, qta, "SCARICO",
                            "Trasporto DDT n." + numeroField.getText());
                    movimenti.add(mov);
                }

                dataService.salvaMovimenti(movimenti);

                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String pdfFile = "DDT_" + numeroField.getText() + "_" + timestamp + ".pdf";
                String excelFile = "DDT_" + numeroField.getText() + "_" + timestamp + ".xlsx";

                pdfService.creaDocumentoTrasportoPDF(doc, datiStatici, pdfFile);
                excelService.creaDocumentoTrasportoExcel(doc, datiStatici, excelFile);
                salvaInventarioExcel();

                // Cambia tab verso Movimenti
                tabbedPane.setSelectedIndex(2); // Tab Movimenti

                JOptionPane.showMessageDialog(this,
                        "Documento di trasporto generato con successo!\n\n" +
                                "File creati:\n" +
                                "- " + pdfFile + "\n" +
                                "- " + excelFile + "\n\n" +
                                "Movimenti registrati e inventario aggiornato (inventario.xlsx)",
                        "Documento Creato",
                        JOptionPane.INFORMATION_MESSAGE);

                // Pulisci form
                tableModel.setRowCount(0);
                colliField.setText("");
                annotazioniArea.setText("");

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Errore durante la creazione del documento:\n" + ex.getMessage(),
                        "Errore",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        bottomPanel.add(generaBtn);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel creaPannelloInventario() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Visualizza bene:"));

        JComboBox<String> filtroCombo = new JComboBox<>();
        filtroCombo.addItem("TUTTI");
        aggiornaComboBoxBeni(filtroCombo);
        topPanel.add(filtroCombo);

        JButton aggiornaBtn = new JButton("Aggiorna");
        topPanel.add(aggiornaBtn);

        panel.add(topPanel, BorderLayout.NORTH);

        String[] columns = {"Bene", "Unità Misura", "Giacenza Totale"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);

        aggiornaBtn.addActionListener(e -> {
            tableModel.setRowCount(0);
            String filtro = (String) filtroCombo.getSelectedItem();
            Map<String, Double> giacenze = calcolaGiacenze();

            for (Bene bene : beni) {
                if ("TUTTI".equals(filtro) || bene.getNome().equals(filtro)) {
                    double giacenza = giacenze.getOrDefault(bene.getNome(), 0.0);
                    tableModel.addRow(new Object[]{
                            bene.getNome(),
                            bene.getUnitaMisura(),
                            String.format("%.2f", giacenza)
                    });
                }
            }
        });

        // Carica inizialmente
        aggiornaBtn.doClick();

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout());
        JButton esportaBtn = new JButton("Esporta in Excel");
        esportaBtn.addActionListener(e -> {
            try {
                salvaInventarioExcel();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            JOptionPane.showMessageDialog(this,
                    "Inventario esportato in inventario.xlsx",
                    "Successo", JOptionPane.INFORMATION_MESSAGE);
        });
        bottomPanel.add(esportaBtn);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel creaPannelloGestioneBeni() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        String[] columns = {"Nome Bene", "Unità di Misura"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0);
        JTable table = new JTable(tableModel);

        for (Bene bene : beni) {
            tableModel.addRow(new Object[]{bene.getNome(), bene.getUnitaMisura()});
        }

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout());

        JButton aggiungiBtn = new JButton("Aggiungi Nuovo Bene");
        aggiungiBtn.addActionListener(e -> {
            JPanel addPanel = new JPanel(new GridLayout(2, 2, 5, 5));
            JTextField nomeField = new JTextField();
            JTextField umField = new JTextField();

            addPanel.add(new JLabel("Nome Bene:"));
            addPanel.add(nomeField);
            addPanel.add(new JLabel("Unità Misura:"));
            addPanel.add(umField);

            int result = JOptionPane.showConfirmDialog(this, addPanel,
                    "Nuovo Bene", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                String nome = nomeField.getText().trim();
                String um = umField.getText().trim();

                if (!nome.isEmpty() && !um.isEmpty()) {
                    Bene nuovoBene = new Bene(nome, um);
                    beni.add(nuovoBene);
                    dataService.salvaBeni(beni);
                    tableModel.addRow(new Object[]{nome, um});

                    JOptionPane.showMessageDialog(this,
                            "Bene aggiunto con successo",
                            "Successo", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        JButton eliminaBtn = new JButton("Elimina Selezionato");
        eliminaBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Eliminare il bene selezionato?",
                        "Conferma", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    beni.remove(selectedRow);
                    dataService.salvaBeni(beni);
                    tableModel.removeRow(selectedRow);
                }
            }
        });

        btnPanel.add(aggiungiBtn);
        btnPanel.add(eliminaBtn);

        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel creaPannelloImpostazioni() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField ragioneSocialeField = new JTextField(datiStatici.getRagioneSociale(), 30);
        JTextField indirizzoField = new JTextField(datiStatici.getIndirizzo(), 30);
        JTextField capField = new JTextField(datiStatici.getCap(), 30);
        JTextField cittaField = new JTextField(datiStatici.getCitta(), 30);
        JTextField pivaField = new JTextField(datiStatici.getPartitaIva(), 30);
        JTextArea magazzinoArea = new JTextArea(datiStatici.getMagazzino(), 3, 30);
        magazzinoArea.setLineWrap(true);

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Ragione Sociale:"), gbc);
        gbc.gridx = 1;
        formPanel.add(ragioneSocialeField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Indirizzo:"), gbc);
        gbc.gridx = 1;
        formPanel.add(indirizzoField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("CAP:"), gbc);
        gbc.gridx = 1;
        formPanel.add(capField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Città:"), gbc);
        gbc.gridx = 1;
        formPanel.add(cittaField, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Partita IVA:"), gbc);
        gbc.gridx = 1;
        formPanel.add(pivaField, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Magazzino:"), gbc);
        gbc.gridx = 1;
        formPanel.add(new JScrollPane(magazzinoArea), gbc);

        JButton salvaBtn = new JButton("Salva Impostazioni");
        salvaBtn.setBackground(new Color(220, 220, 220));
        salvaBtn.setForeground(Color.BLACK);
        salvaBtn.setFocusPainted(false);

        salvaBtn.addActionListener(e -> {
            datiStatici.setRagioneSociale(ragioneSocialeField.getText());
            datiStatici.setIndirizzo(indirizzoField.getText());
            datiStatici.setCap(capField.getText());
            datiStatici.setCitta(cittaField.getText());
            datiStatici.setPartitaIva(pivaField.getText());
            datiStatici.setMagazzino(magazzinoArea.getText());

            dataService.salvaDatiStatici(datiStatici);

            JOptionPane.showMessageDialog(this,
                    "Impostazioni salvate con successo",
                    "Successo", JOptionPane.INFORMATION_MESSAGE);
        });

        gbc.gridx = 0; gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 5, 5, 5);
        formPanel.add(salvaBtn, gbc);

        panel.add(formPanel, BorderLayout.NORTH);

        return panel;
    }

    private void aggiornaComboBoxBeni(JComboBox<String> combo) {
        combo.removeAllItems();
        for (Bene bene : beni) {
            combo.addItem(bene.getNome());
        }
    }

    private Bene trovaBene(String nome) {
        for (Bene bene : beni) {
            if (bene.getNome().equals(nome)) {
                return bene;
            }
        }
        return null;
    }

    private Map<String, Double> calcolaGiacenze() {
        Map<String, Double> giacenze = new HashMap<>();

        for (Movimento mov : movimenti) {
            double attuale = giacenze.getOrDefault(mov.getTipoBene(), 0.0);
            if ("CARICO".equals(mov.getTipo())) {
                giacenze.put(mov.getTipoBene(), attuale + mov.getQuantita());
            } else {
                giacenze.put(mov.getTipoBene(), attuale - mov.getQuantita());
            }
        }

        return giacenze;
    }

    private void salvaInventarioExcel() throws IOException {
        Map<String, Double> giacenze = calcolaGiacenze();
        Workbook workbook = new XSSFWorkbook();
        excelService.salvaInventario(beni, giacenze, "inventario.xlsx");
    }

    // Metodo per mostrare un calendario e selezionare una data
    private Date mostraCalendario(Component parent) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent), "Seleziona Data", true);
        dialog.setLayout(new BorderLayout());

        Calendar calendar = Calendar.getInstance();
        final Date[] selectedDate = {calendar.getTime()};

        // Pannello per mese/anno
        JPanel topPanel = new JPanel(new FlowLayout());
        JComboBox<String> meseCombo = new JComboBox<>(new String[]{
                "Gennaio", "Febbraio", "Marzo", "Aprile", "Maggio", "Giugno",
                "Luglio", "Agosto", "Settembre", "Ottobre", "Novembre", "Dicembre"
        });
        meseCombo.setSelectedIndex(calendar.get(Calendar.MONTH));

        SpinnerNumberModel yearModel = new SpinnerNumberModel(calendar.get(Calendar.YEAR), 2000, 2100, 1);
        JSpinner yearSpinner = new JSpinner(yearModel);

        topPanel.add(new JLabel("Mese:"));
        topPanel.add(meseCombo);
        topPanel.add(new JLabel("Anno:"));
        topPanel.add(yearSpinner);

        // Pannello calendario
        JPanel calendarPanel = new JPanel(new GridLayout(7, 7, 2, 2));

        // Aggiorna calendario
        Runnable aggiornaCalendario = () -> {
            calendarPanel.removeAll();
            calendar.set(Calendar.MONTH, meseCombo.getSelectedIndex());
            calendar.set(Calendar.YEAR, (Integer) yearSpinner.getValue());

            // Header giorni settimana
            String[] giorni = {"Lun", "Mar", "Mer", "Gio", "Ven", "Sab", "Dom"};
            for (String giorno : giorni) {
                JLabel label = new JLabel(giorno, SwingConstants.CENTER);
                label.setFont(label.getFont().deriveFont(Font.BOLD));
                calendarPanel.add(label);
            }

            // Primo giorno del mese
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            int offset = (firstDayOfWeek == Calendar.SUNDAY) ? 6 : firstDayOfWeek - 2;

            // Spazi vuoti
            for (int i = 0; i < offset; i++) {
                calendarPanel.add(new JLabel(""));
            }

            // Giorni del mese
            int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            for (int day = 1; day <= maxDay; day++) {
                JButton dayBtn = new JButton(String.valueOf(day));
                final int finalDay = day;
                dayBtn.addActionListener(e -> {
                    calendar.set(Calendar.DAY_OF_MONTH, finalDay);
                    selectedDate[0] = calendar.getTime();
                    dialog.dispose();
                });
                calendarPanel.add(dayBtn);
            }

            calendarPanel.revalidate();
            calendarPanel.repaint();
        };

        meseCombo.addActionListener(e -> aggiornaCalendario.run());
        yearSpinner.addChangeListener(e -> aggiornaCalendario.run());

        aggiornaCalendario.run();

        JPanel bottomPanel = new JPanel(new FlowLayout());
        JButton oggiBtn = new JButton("Oggi");
        oggiBtn.addActionListener(e -> {
            selectedDate[0] = new Date();
            dialog.dispose();
        });
        JButton annullaBtn = new JButton("Annulla");
        annullaBtn.addActionListener(e -> {
            selectedDate[0] = null;
            dialog.dispose();
        });
        bottomPanel.add(oggiBtn);
        bottomPanel.add(annullaBtn);

        dialog.add(topPanel, BorderLayout.NORTH);
        dialog.add(calendarPanel, BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);

        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);

        return selectedDate[0];
    }
}