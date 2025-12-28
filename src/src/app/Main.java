package app;

import gui.GestioneMagazzinoGUI;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            GestioneMagazzinoGUI gui = new GestioneMagazzinoGUI();
            gui.setVisible(true);
        });
    }
}