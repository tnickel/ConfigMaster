package com.configmaster;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Hauptklasse der ConfigMaster-Anwendung.
 * Startet die SWT-Anwendung und initialisiert die UI-Komponenten.
 */
public class ConfigMasterApp {
    
    public static void main(String[] args) {
        // Initialisieren des SWT-Displays
        Display display = new Display();
        
        // Hauptfenster erstellen
        Shell shell = new Shell(display);
        shell.setText("ConfigMaster");
        shell.setSize(800, 600);
        
        // UI-Komponenten initialisieren
        MainWindow mainWindow = new MainWindow(shell);
        mainWindow.createContents();
        
        // Shell öffnen und SWT-Event-Loop starten
        shell.open();
        
        // Event-Loop: Läuft, bis das Fenster geschlossen wird
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        
        // Ressourcen freigeben
        display.dispose();
    }
}