package com.configmaster;

import java.io.File;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/**
 * Hauptfenster der ConfigMaster-Anwendung.
 * Enthält das Konfigurationsmenü und die Tabelle zur Anzeige der gefundenen Konfigurationsdateien.
 */
public class MainWindow {
    
    private static final String ROOT_PATH = "c:\\forex\\ConfigMaster";
    private static final String CONFIG_DIR = "config";
    private static final String CONFIG_FILE = "config.txt";
    private static final String LOG_CONFIG_FILE = "log4j2.xml";
    
    // Logger wird erst nach der Konfiguration initialisiert
    private static Logger logger;
    
    private Shell shell;
    private Text searchPathText;
    private Table configFilesTable;
    private ConfigScanner configScanner;
    private ConfigFileViewer configFileViewer;
    
    public MainWindow(Shell shell) {
        // Initialisiere Logger-Konfiguration
        initializeLogger();
        
        this.shell = shell;
        this.configScanner = new ConfigScanner();
        this.configFileViewer = new ConfigFileViewer();
        logger.info("ConfigMaster gestartet");
    }
    
    /**
     * Initialisiert die Logger-Konfiguration.
     */
    private void initializeLogger() {
        // Prüfe, ob die Log-Konfigurationsdatei existiert
        String logConfigPath = ROOT_PATH + File.separator + CONFIG_DIR + File.separator + LOG_CONFIG_FILE;
        File logConfigFile = new File(logConfigPath);
        
        // Wenn die Datei existiert, konfiguriere den Logger mit dieser Datei
        if (logConfigFile.exists()) {
            Configurator.initialize("ConfigMasterLogger", null, logConfigPath);
        }
        
        // Initialisiere den Logger
        logger = LogManager.getLogger(MainWindow.class);
    }
    
    /**
     * Erstellt alle UI-Komponenten im Hauptfenster.
     */
    public void createContents() {
        // Layout für das Hauptfenster setzen
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 10;
        layout.marginHeight = 10;
        layout.verticalSpacing = 10;
        shell.setLayout(layout);
        
        // Konfigurationsmenü erstellen
        createConfigMenu();
        
        // Tabelle für die Konfigurationsdateien erstellen
        createConfigFilesTable();
        
        // Gespeicherten Suchpfad laden
        loadSavedSearchPath();
    }
    
    /**
     * Lädt den gespeicherten Suchpfad aus der Konfigurationsdatei und zeigt ihn im Textfeld an.
     */
    private void loadSavedSearchPath() {
        String configFilePath = ROOT_PATH + File.separator + CONFIG_DIR + File.separator + CONFIG_FILE;
        File configFile = new File(configFilePath);
        
        // Prüfen, ob die Konfigurationsdatei existiert
        if (!configFile.exists()) {
            // Verzeichnis und leere Konfigurationsdatei erstellen, wenn sie nicht existieren
            File configDir = new File(ROOT_PATH + File.separator + CONFIG_DIR);
            if (!configDir.exists()) {
                configDir.mkdirs();
            }
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                logger.error("Fehler beim Erstellen der Konfigurationsdatei: {}", e.getMessage());
            }
            return;
        }
        
        // Konfigurationsdatei lesen
        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            String searchPath = reader.readLine();
            if (searchPath != null && !searchPath.isEmpty()) {
                searchPathText.setText(searchPath);
                logger.info("Suchpfad geladen: {}", searchPath);
            }
        } catch (IOException e) {
            logger.error("Fehler beim Lesen der Konfigurationsdatei: {}", e.getMessage());
        }
    }
    
    /**
     * Speichert den Suchpfad in der Konfigurationsdatei.
     * 
     * @param searchPath Der zu speichernde Suchpfad
     */
    private void saveSearchPath(String searchPath) {
        String configFilePath = ROOT_PATH + File.separator + CONFIG_DIR + File.separator + CONFIG_FILE;
        File configFile = new File(configFilePath);
        
        // Sicherstellen, dass das Verzeichnis existiert
        File configDir = configFile.getParentFile();
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        
        // Konfigurationsdatei schreiben
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile))) {
            writer.write(searchPath);
            logger.info("Suchpfad gespeichert: {}", searchPath);
        } catch (IOException e) {
            logger.error("Fehler beim Schreiben der Konfigurationsdatei: {}", e.getMessage());
        }
    }
    
    /**
     * Erstellt das Konfigurationsmenü mit Textfeld für den Suchpfad und dem ReadConfigs-Button.
     */
    private void createConfigMenu() {
        // Gruppe für die Konfigurationseinstellungen
        Group configGroup = new Group(shell, SWT.NONE);
        configGroup.setText("Konfiguration");
        configGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
        GridLayout groupLayout = new GridLayout(3, false);
        configGroup.setLayout(groupLayout);
        
        // Label für den Suchpfad
        Label searchPathLabel = new Label(configGroup, SWT.NONE);
        searchPathLabel.setText("Suchpfad:");
        
        // Textfeld für den Suchpfad
        searchPathText = new Text(configGroup, SWT.BORDER);
        searchPathText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        // Button zum Durchsuchen nach einem Verzeichnis
        Button browseButton = new Button(configGroup, SWT.PUSH);
        browseButton.setText("Browse...");
        browseButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                DirectoryDialog dialog = new DirectoryDialog(shell, SWT.OPEN);
                dialog.setText("Suchverzeichnis auswählen");
                dialog.setMessage("Wählen Sie das Verzeichnis, in dem nach Konfigurationsdateien gesucht werden soll");
                
                String selectedPath = dialog.open();
                if (selectedPath != null) {
                    searchPathText.setText(selectedPath);
                    saveSearchPath(selectedPath);
                }
            }
        });
        
        // Button zum Durchsuchen nach Konfigurationsdateien
        Button readConfigsButton = new Button(configGroup, SWT.PUSH);
        readConfigsButton.setText("ReadConfigs");
        GridData buttonData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        buttonData.horizontalSpan = 3;
        readConfigsButton.setLayoutData(buttonData);
        
        // Event-Handler für den ReadConfigs-Button
        readConfigsButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                readConfigs();
            }
        });
    }
    
    /**
     * Erstellt die Tabelle zur Anzeige der gefundenen Konfigurationsdateien.
     */
    private void createConfigFilesTable() {
        // Label für die Tabelle
        Label tableLabel = new Label(shell, SWT.NONE);
        tableLabel.setText("Gefundene Konfigurationsdateien:");
        
        // Tabelle erstellen
        configFilesTable = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
        configFilesTable.setHeaderVisible(true);
        configFilesTable.setLinesVisible(true);
        
        GridData tableData = new GridData(SWT.FILL, SWT.FILL, true, true);
        configFilesTable.setLayoutData(tableData);
        
        // Spalten für die Tabelle erstellen
        TableColumn nameColumn = new TableColumn(configFilesTable, SWT.NONE);
        nameColumn.setText("Name");
        nameColumn.setWidth(200);
        
        TableColumn pathColumn = new TableColumn(configFilesTable, SWT.NONE);
        pathColumn.setText("Pfad");
        pathColumn.setWidth(400);
        
        // Doppelklick-Event für die Tabelle hinzufügen
        configFilesTable.addListener(SWT.MouseDoubleClick, event -> {
            TableItem[] selection = configFilesTable.getSelection();
            if (selection.length > 0) {
                String filePath = selection[0].getText(1);
                openConfigFile(filePath);
            }
        });
    }
    
    /**
     * Durchsucht das Suchverzeichnis nach Konfigurationsdateien und zeigt diese in der Tabelle an.
     */
    private void readConfigs() {
        String searchPath = searchPathText.getText().trim();
        logger.info("Suche Konfigurationsdateien im Pfad: {}", searchPath);
        
        if (searchPath.isEmpty()) {
            MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
            messageBox.setText("Fehler");
            messageBox.setMessage("Bitte geben Sie einen gültigen Suchpfad an.");
            messageBox.open();
            return;
        }
        
        File searchDir = new File(searchPath);
        if (!searchDir.exists() || !searchDir.isDirectory()) {
            MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
            messageBox.setText("Fehler");
            messageBox.setMessage("Der angegebene Pfad existiert nicht oder ist kein Verzeichnis.");
            messageBox.open();
            return;
        }
        
        // Suchpfad speichern
        saveSearchPath(searchPath);
        
        // Tabelle leeren
        configFilesTable.removeAll();
        
        // Konfigurationsdateien durchsuchen
        List<ConfigFile> configFiles = configScanner.scanForConfigFiles(searchDir);
        
        // Gefundene Dateien in der Tabelle anzeigen
        for (ConfigFile configFile : configFiles) {
            TableItem item = new TableItem(configFilesTable, SWT.NONE);
            item.setText(new String[] { configFile.getName(), configFile.getPath() });
        }
        
        // Meldung anzeigen, wenn keine Dateien gefunden wurden
        if (configFiles.isEmpty()) {
            MessageBox messageBox = new MessageBox(shell, SWT.ICON_INFORMATION);
            messageBox.setText("Information");
            messageBox.setMessage("Keine Konfigurationsdateien gefunden.");
            messageBox.open();
        }
    }
    
    /**
     * Öffnet eine Konfigurationsdatei zur Anzeige.
     * 
     * @param filePath Pfad zur Konfigurationsdatei
     */
    private void openConfigFile(String filePath) {
        logger.info("Öffne Datei: {}", filePath);
        configFileViewer.viewConfigFile(shell, filePath);
    }
}