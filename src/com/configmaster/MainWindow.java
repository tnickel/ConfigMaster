package com.configmaster;

import java.io.File;
import java.util.List;

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
    
    private Shell shell;
    private Text rootPathText;
    private Table configFilesTable;
    private ConfigScanner configScanner;
    private ConfigFileViewer configFileViewer;
    
    public MainWindow(Shell shell) {
        this.shell = shell;
        this.configScanner = new ConfigScanner();
        this.configFileViewer = new ConfigFileViewer();
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
    }
    
    /**
     * Erstellt das Konfigurationsmenü mit Textfeld für den Root-Path und dem ReadConfigs-Button.
     */
    private void createConfigMenu() {
        // Gruppe für die Konfigurationseinstellungen
        Group configGroup = new Group(shell, SWT.NONE);
        configGroup.setText("Konfiguration");
        configGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
        GridLayout groupLayout = new GridLayout(3, false);
        configGroup.setLayout(groupLayout);
        
        // Label für den Root-Path
        Label rootPathLabel = new Label(configGroup, SWT.NONE);
        rootPathLabel.setText("Root-Path:");
        
        // Textfeld für den Root-Path
        rootPathText = new Text(configGroup, SWT.BORDER);
        rootPathText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        // Button zum Durchsuchen nach einem Verzeichnis
        Button browseButton = new Button(configGroup, SWT.PUSH);
        browseButton.setText("Browse...");
        browseButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                DirectoryDialog dialog = new DirectoryDialog(shell, SWT.OPEN);
                dialog.setText("Projektverzeichnis auswählen");
                dialog.setMessage("Wählen Sie das Root-Verzeichnis des Projekts");
                
                String selectedPath = dialog.open();
                if (selectedPath != null) {
                    rootPathText.setText(selectedPath);
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
     * Durchsucht das Root-Verzeichnis nach Konfigurationsdateien und zeigt diese in der Tabelle an.
     */
    private void readConfigs() {
        String rootPath = rootPathText.getText().trim();
        if (rootPath.isEmpty()) {
            MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
            messageBox.setText("Fehler");
            messageBox.setMessage("Bitte geben Sie einen gültigen Root-Path an.");
            messageBox.open();
            return;
        }
        
        File rootDir = new File(rootPath);
        if (!rootDir.exists() || !rootDir.isDirectory()) {
            MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
            messageBox.setText("Fehler");
            messageBox.setMessage("Der angegebene Pfad existiert nicht oder ist kein Verzeichnis.");
            messageBox.open();
            return;
        }
        
        // Tabelle leeren
        configFilesTable.removeAll();
        
        // Konfigurationsdateien durchsuchen
        List<ConfigFile> configFiles = configScanner.scanForConfigFiles(rootDir);
        
        // Gefundene Dateien in der Tabelle anzeigen
        for (ConfigFile configFile : configFiles) {
            TableItem item = new TableItem(configFilesTable, SWT.NONE);
            item.setText(new String[] { configFile.getName(), configFile.getPath() });
        }
    }
    
    /**
     * Öffnet eine Konfigurationsdatei zur Anzeige.
     * 
     * @param filePath Pfad zur Konfigurationsdatei
     */
    private void openConfigFile(String filePath) {
        configFileViewer.viewConfigFile(shell, filePath);
    }
}