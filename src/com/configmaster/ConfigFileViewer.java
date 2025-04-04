package com.configmaster;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Klasse zur Anzeige des Inhalts einer Konfigurationsdatei.
 * Öffnet ein neues Fenster mit dem Inhalt der ausgewählten Datei.
 */
public class ConfigFileViewer {
    
    /**
     * Zeigt den Inhalt einer Konfigurationsdatei in einem neuen Fenster an.
     * 
     * @param parentShell Das Elternfenster
     * @param filePath Der Pfad zur anzuzeigenden Datei
     */
    public void viewConfigFile(Shell parentShell, String filePath) {
        // Neues Fenster erstellen
        Shell viewerShell = new Shell(parentShell, SWT.SHELL_TRIM);
        viewerShell.setText("Dateiansicht - " + filePath);
        viewerShell.setSize(700, 500);
        viewerShell.setLayout(new GridLayout(1, false));
        
        // Textfeld für den Dateiinhalt erstellen
        Text fileContentText = new Text(viewerShell, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        fileContentText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        fileContentText.setEditable(false);
        
        // Dateiinhalt laden
        try {
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }
            fileContentText.setText(content.toString());
        } catch (IOException e) {
            fileContentText.setText("Fehler beim Lesen der Datei: " + e.getMessage());
        }
        
        // Fenster öffnen
        viewerShell.open();
    }
}