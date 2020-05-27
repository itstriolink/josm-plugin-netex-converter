/* 
 * Copyright (C) 2020 Labian Gashi
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 */
package org.openstreetmap.josm.plugins.netex_converter.ui;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.netex_converter.exporter.NeTExExporter;
import org.xml.sax.SAXException;

/**
 *
 * @author labian
 */
public class ExportToNeTExDialog extends javax.swing.JFrame {

    private final static String NETEX_FILE_NAME = "NeTEx.xml";

    private NeTExExporter neTExExporter;

    public ExportToNeTExDialog() throws IOException, SAXException {
        initComponents();
        buildGUI();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        mainFileChooser = new javax.swing.JFileChooser();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        mainFileChooser.setAcceptAllFileFilterUsed(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mainFileChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 694, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainFileChooser, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buildGUI() throws IOException, SAXException {
        loadFileChooser(mainFileChooser);
    }

    private void loadFileChooser(JFileChooser chooser) throws IOException, SAXException {
        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        chooser.setSelectedFile(new File(NETEX_FILE_NAME));
        chooser.setDialogTitle("Save as NeTEx");
        chooser.setMultiSelectionEnabled(false);

        InputMap im = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getRootPane().getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
        am.put("cancel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        FileNameExtensionFilter extensionFilter = new FileNameExtensionFilter("XML files", "xml");
        chooser.setFileFilter(extensionFilter);

        File file = null;
        int selectedOption = JOptionPane.NO_OPTION;

        do {
            int returnVal = chooser.showSaveDialog(null);

            if (returnVal != JFileChooser.APPROVE_OPTION) {
                return;
            }

            file = chooser.getSelectedFile();

            if (!file.getName().toLowerCase().endsWith(".xml")) {
                file = new File(file.getParentFile(), file.getName() + ".xml");
            }

            if (file.exists()) {
                selectedOption = JOptionPane.showConfirmDialog(
                        null, "This file already exists, overwrite it?");

                if (selectedOption == JOptionPane.CANCEL_OPTION) {
                    return;
                }
            }
        }
        while (file.exists() && selectedOption == JOptionPane.NO_OPTION);

        MainApplication.getMap().mapView.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        neTExExporter = new NeTExExporter();
        try {
            neTExExporter.exportToNeTEx(file);
        }
        finally {
            MainApplication.getMap().mapView.setCursor(Cursor.getDefaultCursor());
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JFileChooser mainFileChooser;
    // End of variables declaration//GEN-END:variables
}
