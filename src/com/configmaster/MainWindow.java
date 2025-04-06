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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/**
 * Hauptfenster der ConfigMaster-Anwendung.
 * Enthält die Menüleiste, Konfigurationsoptionen und die Tabelle zur Anzeige der gefundenen Konfigurationsdateien.
 */
public class MainWindow {
    
    private static final String ROOT_PATH = "c:\\forex\\ConfigMaster";
    private static final String CONFIG_DIR = "config";
    private static final String CONFIG_FILE = "config.txt";
    private static final String LOG_CONFIG_FILE = "log4j2.xml";
    
    // Logger wird erst nach der Konfiguration initialisiert
    private static Logger logger;
    
    private Shell shell;
    private Table configFilesTable;
    private ConfigScanner configScanner;
    private ConfigFileViewer configFileViewer;
    
    // Gespeicherte Konfigurationswerte
    private String searchPath = "";
    private String searchPattern = "default";
    
    // Musterbeispiel, das im Dialog angezeigt wird
    private static final String PATTERN_EXAMPLE = "pattern1,pattern2";
    
    // FilterManager für die Filterverwaltung
    private FilterManager filterManager;
    
    public MainWindow(Shell shell) {
        // Initialisiere Logger-Konfiguration
        initializeLogger();
        
        this.shell = shell;
        this.configScanner = new ConfigScanner();
        this.configFileViewer = new ConfigFileViewer();
        this.filterManager = new FilterManager();
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
        
        // Menüleiste erstellen
        createMenuBar();
        
        // Tabelle für die Konfigurationsdateien erstellen
        createConfigFilesTable();
        
        // "ReadConfigs" Button unter der Tabelle erstellen
        createReadConfigsButton();
        
        // Gespeicherte Konfiguration laden
        loadSavedConfig();
    }
    
    /**
     * Erstellt die Menüleiste mit dem Konfigurationsmenü.
     */
    private void createMenuBar() {
        Menu menuBar = new Menu(shell, SWT.BAR);
        shell.setMenuBar(menuBar);
        
        // Menüpunkt "Konfiguration" erstellen
        MenuItem configMenuItem = new MenuItem(menuBar, SWT.CASCADE);
        configMenuItem.setText("Konfiguration");
        
        // Untermenü für "Konfiguration" erstellen
        Menu configMenu = new Menu(shell, SWT.DROP_DOWN);
        configMenuItem.setMenu(configMenu);
        
        // Menüpunkt "Suchpfad festlegen" erstellen
        MenuItem setSearchPathItem = new MenuItem(configMenu, SWT.PUSH);
        setSearchPathItem.setText("Suchpfad festlegen...");
        setSearchPathItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                openSearchPathDialog();
            }
        });
        
        // Menüpunkt "Suchmuster festlegen" erstellen
        MenuItem setSearchPatternItem = new MenuItem(configMenu, SWT.PUSH);
        setSearchPatternItem.setText("Suchmuster für Dateiinhalt festlegen...");
        setSearchPatternItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                openSearchPatternDialog();
            }
        });
        
        // Trennlinie im Menü
        new MenuItem(configMenu, SWT.SEPARATOR);
        
        // Menüpunkt "Filterverwaltung" erstellen
        MenuItem filterManagerItem = new MenuItem(configMenu, SWT.PUSH);
        filterManagerItem.setText("Filterverwaltung...");
        filterManagerItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                openFilterManager();
            }
        });
        
        // Menüpunkt "Filter" erstellen
        MenuItem filterMenuItem = new MenuItem(menuBar, SWT.CASCADE);
        filterMenuItem.setText("Filter");
        
        // Untermenü für "Filter" erstellen
        Menu filterMenu = new Menu(shell, SWT.DROP_DOWN);
        filterMenuItem.setMenu(filterMenu);
        
        // Menüpunkte für die Filter dynamisch erstellen
        updateFilterMenu(filterMenu);
    }
    
    /**
     * Aktualisiert das Filter-Menü mit allen verfügbaren Filtern.
     * 
     * @param filterMenu Das Filter-Menü
     */
    private void updateFilterMenu(Menu filterMenu) {
        // Bestehende Menüpunkte entfernen
        for (MenuItem item : filterMenu.getItems()) {
            item.dispose();
        }
        
        // Filtern hinzufügen
        for (Filter filter : filterManager.getFilters()) {
            MenuItem filterItem = new MenuItem(filterMenu, SWT.PUSH);
            filterItem.setText(filter.getName());
            
            // Event-Handler für den Filter
            filterItem.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    // Filter anwenden
                    searchPattern = filter.getKeywordsAsString();
                    saveConfig();
                    
                    // Meldung anzeigen
                    MessageBox messageBox = new MessageBox(shell, SWT.ICON_INFORMATION);
                    messageBox.setText("Filter angewendet");
                    messageBox.setMessage("Der Filter '" + filter.getName() + "' wurde angewendet.\n" +
                                        "Schlüsselwörter: " + searchPattern);
                    messageBox.open();
                }
            });
        }
    }
    
    /**
     * Öffnet den Dialog zur Filterverwaltung.
     */
    private void openFilterManager() {
        FilterDialog filterDialog = new FilterDialog(shell, filterManager);
        filterDialog.open();
        
        // Filter-Menü aktualisieren, nachdem der Dialog geschlossen wurde
        Menu filterMenu = shell.getMenuBar().getItems()[1].getMenu();
        updateFilterMenu(filterMenu);
    }
    
    /**
     * Öffnet einen Dialog zum Festlegen des Suchpfads.
     */
    private void openSearchPathDialog() {
        DirectoryDialog dialog = new DirectoryDialog(shell, SWT.OPEN);
        dialog.setText("Suchverzeichnis auswählen");
        dialog.setMessage("Wählen Sie das Verzeichnis, in dem nach Konfigurationsdateien gesucht werden soll");
        
        if (!searchPath.isEmpty()) {
            dialog.setFilterPath(searchPath);
        }
        
        String selectedPath = dialog.open();
        if (selectedPath != null) {
            searchPath = selectedPath;
            saveConfig();
        }
    }
    
    /**
     * Öffnet einen Dialog zum Festlegen der Suchmuster.
     */
    private void openSearchPatternDialog() {
        // Ein einfaches Dialogfenster mit einem Textfeld erstellen
        Shell dialogShell = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        dialogShell.setText("Suchmuster festlegen");
        dialogShell.setSize(450, 200);
        
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 10;
        layout.marginHeight = 10;
        dialogShell.setLayout(layout);
        
        Label label = new Label(dialogShell, SWT.NONE);
        label.setText("Suchmuster (kommagetrennt, z.B. \"pattern1,pattern2\"):");
        GridData labelData = new GridData();
        labelData.horizontalSpan = 2;
        label.setLayoutData(labelData);
        
        Text patternText = new Text(dialogShell, SWT.BORDER);
        patternText.setText(searchPattern);
        GridData textData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textData.horizontalSpan = 2;
        patternText.setLayoutData(textData);
        
        Label infoLabel = new Label(dialogShell, SWT.NONE);
        infoLabel.setText("Es werden nur .chr Dateien angezeigt, deren Inhalt eines der\n" +
                         "angegebenen Muster enthält.");
        GridData infoData = new GridData();
        infoData.horizontalSpan = 2;
        infoData.verticalIndent = 10;
        infoLabel.setLayoutData(infoData);
        
        Button okButton = new Button(dialogShell, SWT.PUSH);
        okButton.setText("OK");
        okButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
        okButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                searchPattern = patternText.getText().trim();
                if (searchPattern.isEmpty()) {
                    searchPattern = "default"; // Standardwert, wenn nichts eingegeben wurde
                }
                saveConfig();
                dialogShell.close();
            }
        });
        
        Button cancelButton = new Button(dialogShell, SWT.PUSH);
        cancelButton.setText("Abbrechen");
        cancelButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        cancelButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                dialogShell.close();
            }
        });
        
        dialogShell.open();
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
     * Erstellt den ReadConfigs-Button unter der Tabelle.
     */
    private void createReadConfigsButton() {
        Button readConfigsButton = new Button(shell, SWT.PUSH);
        readConfigsButton.setText("ReadConfigs");
        readConfigsButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
        
        // Event-Handler für den ReadConfigs-Button
        readConfigsButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                readConfigs();
            }
        });
    }
    
    /**
     * Lädt die gespeicherte Konfiguration aus der Konfigurationsdatei.
     */
    private void loadSavedConfig() {
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
            String line;
            
            // Erste Zeile: Suchpfad
            if ((line = reader.readLine()) != null) {
                searchPath = line.trim();
                logger.info("Suchpfad geladen: {}", searchPath);
            }
            
            // Zweite Zeile: Suchmuster
            if ((line = reader.readLine()) != null) {
                searchPattern = line.trim();
                if (searchPattern.isEmpty()) {
                    searchPattern = "default"; // Standardwert
                }
                logger.info("Suchmuster geladen: {}", searchPattern);
            }
        } catch (IOException e) {
            logger.error("Fehler beim Lesen der Konfigurationsdatei: {}", e.getMessage());
        }
    }
    
    /**
     * Speichert die Konfiguration in der Konfigurationsdatei.
     */
    private void saveConfig() {
        String configFilePath = ROOT_PATH + File.separator + CONFIG_DIR + File.separator + CONFIG_FILE;
        File configFile = new File(configFilePath);
        
        // Sicherstellen, dass das Verzeichnis existiert
        File configDir = configFile.getParentFile();
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        
        // Konfigurationsdatei schreiben
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile))) {
            // Suchpfad in erster Zeile
            writer.write(searchPath);
            writer.newLine();
            
            // Suchmuster in zweiter Zeile
            writer.write(searchPattern);
            
            logger.info("Konfiguration gespeichert. Suchpfad: {}, Suchmuster: {}", searchPath, searchPattern);
        } catch (IOException e) {
            logger.error("Fehler beim Schreiben der Konfigurationsdatei: {}", e.getMessage());
        }
    }
    
    /**
     * Durchsucht das Suchverzeichnis nach Konfigurationsdateien und zeigt diese in der Tabelle an.
     */
    private void readConfigs() {
        logger.info("Suche Konfigurationsdateien im Pfad: {} mit Muster: {}", searchPath, searchPattern);
        
        if (searchPath.isEmpty()) {
            MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
            messageBox.setText("Fehler");
            messageBox.setMessage("Bitte legen Sie unter 'Konfiguration > Suchpfad festlegen' einen gültigen Suchpfad fest.");
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
        
        // Tabelle leeren
        configFilesTable.removeAll();
        
        // Konfigurationsdateien durchsuchen
        List<ConfigFile> configFiles = configScanner.scanForConfigFiles(searchDir, searchPattern);
        
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