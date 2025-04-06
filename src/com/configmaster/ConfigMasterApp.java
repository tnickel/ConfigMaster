package com.configmaster;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Hauptklasse der ConfigMaster-Anwendung.
 * Startet die SWT-Anwendung und initialisiert die UI-Komponenten.
 */
public class ConfigMasterApp {
    
    private static final String ROOT_PATH = "c:\\forex\\ConfigMaster";
    private static final String CONFIG_DIR = "config";
    private static final String LOGS_DIR = "logs";
    private static final String LOG_CONFIG_FILE = "log4j2.xml";
    
    public static void main(String[] args) {
        // Initialisieren des Konfigurationsverzeichnisses
        initConfigDirectory();
        
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
    
    /**
     * Initialisiert das Konfigurationsverzeichnis und die notwendigen Dateien.
     */
    private static void initConfigDirectory() {
        // Erstellen des Konfigurationsverzeichnisses
        File configDir = new File(ROOT_PATH, CONFIG_DIR);
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        
        // Erstellen des Logs-Verzeichnisses
        File logsDir = new File(ROOT_PATH, LOGS_DIR);
        if (!logsDir.exists()) {
            logsDir.mkdirs();
        }
        
        // Überprüfen und Erstellen der Log-Konfigurationsdatei
        File logConfigFile = new File(configDir, LOG_CONFIG_FILE);
        if (!logConfigFile.exists()) {
            createDefaultLogConfig(logConfigFile);
        }
    }
    
    /**
     * Erstellt die Standard-Log-Konfigurationsdatei.
     * 
     * @param logConfigFile Die zu erstellende Log-Konfigurationsdatei
     */
    private static void createDefaultLogConfig(File logConfigFile) {
        try {
            // Standard-Konfiguration als Ressource laden
            InputStream inputStream = ConfigMasterApp.class.getResourceAsStream("/log4j2.xml");
            if (inputStream != null) {
                // Konfiguration in die Datei kopieren
                Files.copy(inputStream, logConfigFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                inputStream.close();
            } else {
                // Wenn keine Standard-Konfiguration als Ressource gefunden wurde, erstelle eine einfache Konfiguration
                String defaultConfig = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<Configuration status=\"WARN\">\n" +
                        "    <Appenders>\n" +
                        "        <Console name=\"Console\" target=\"SYSTEM_OUT\">\n" +
                        "            <PatternLayout pattern=\"%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n\"/>\n" +
                        "        </Console>\n" +
                        "        <File name=\"File\" fileName=\"" + ROOT_PATH + "/logs/configmaster.log\">\n" +
                        "            <PatternLayout pattern=\"%d{yyyy-MM-dd HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n\"/>\n" +
                        "        </File>\n" +
                        "    </Appenders>\n" +
                        "    <Loggers>\n" +
                        "        <Root level=\"debug\">\n" +
                        "            <AppenderRef ref=\"Console\"/>\n" +
                        "            <AppenderRef ref=\"File\"/>\n" +
                        "        </Root>\n" +
                        "    </Loggers>\n" +
                        "</Configuration>";
                
                try (FileOutputStream outputStream = new FileOutputStream(logConfigFile)) {
                    outputStream.write(defaultConfig.getBytes());
                }
            }
        } catch (IOException e) {
            System.err.println("Fehler beim Erstellen der Log-Konfigurationsdatei: " + e.getMessage());
        }
    }
}