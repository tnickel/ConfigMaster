package com.configmaster;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Verwaltet die Filter für die Suche nach Konfigurationsdateien.
 * Ermöglicht das Laden, Speichern, Hinzufügen, Bearbeiten und Löschen von Filtern.
 * Filter werden in einem menschenlesbaren Textformat gespeichert.
 */
public class FilterManager {
    
    private static final Logger logger = LogManager.getLogger(FilterManager.class);
    
    private static final String ROOT_PATH = "c:\\forex\\ConfigMaster";
    private static final String CONFIG_DIR = "config";
    private static final String FILTER_FILE = "filters.txt";
    
    private List<Filter> filters;
    
    /**
     * Erstellt einen neuen FilterManager und lädt die vorhandenen Filter.
     */
    public FilterManager() {
        filters = new ArrayList<>();
        loadFilters();
    }
    
    /**
     * Gibt die Liste aller Filter zurück.
     * 
     * @return Die Liste der Filter
     */
    public List<Filter> getFilters() {
        return filters;
    }
    
    /**
     * Fügt einen neuen Filter hinzu.
     * 
     * @param filter Der hinzuzufügende Filter
     */
    public void addFilter(Filter filter) {
        if (filter != null) {
            filters.add(filter);
            saveFilters();
        }
    }
    
    /**
     * Aktualisiert einen vorhandenen Filter.
     * 
     * @param index Die Position des zu aktualisierenden Filters
     * @param filter Der aktualisierte Filter
     */
    public void updateFilter(int index, Filter filter) {
        if (index >= 0 && index < filters.size() && filter != null) {
            filters.set(index, filter);
            saveFilters();
        }
    }
    
    /**
     * Entfernt einen Filter.
     * 
     * @param index Die Position des zu entfernenden Filters
     */
    public void removeFilter(int index) {
        if (index >= 0 && index < filters.size()) {
            filters.remove(index);
            saveFilters();
        }
    }
    
    /**
     * Lädt die Filter aus der Konfigurationsdatei.
     * Das Format der Datei ist:
     * [Filtername]
     * Schlüsselwort1
     * Schlüsselwort2
     * ...
     * [NächsterFiltername]
     * ...
     */
    private void loadFilters() {
        String filterFilePath = ROOT_PATH + File.separator + CONFIG_DIR + File.separator + FILTER_FILE;
        File filterFile = new File(filterFilePath);
        
        if (!filterFile.exists()) {
            logger.info("Keine Filter-Konfigurationsdatei gefunden. Es werden Standardfilter erstellt.");
            createDefaultFilters();
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filterFile))) {
            String line;
            Filter currentFilter = null;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                if (line.isEmpty()) {
                    continue; // Leere Zeilen überspringen
                }
                
                if (line.startsWith("[") && line.endsWith("]")) {
                    // Neuer Filter gefunden
                    String filterName = line.substring(1, line.length() - 1);
                    currentFilter = new Filter(filterName);
                    filters.add(currentFilter);
                } else if (currentFilter != null) {
                    // Schlüsselwort für den aktuellen Filter
                    currentFilter.addKeyword(line);
                }
            }
            
            logger.info("Filter geladen: {}", filters.size());
        } catch (IOException e) {
            logger.error("Fehler beim Laden der Filter: {}", e.getMessage(), e);
            createDefaultFilters();
        }
    }
    
    /**
     * Speichert die Filter in der Konfigurationsdatei.
     * Das Format der Datei ist:
     * [Filtername]
     * Schlüsselwort1
     * Schlüsselwort2
     * ...
     * [NächsterFiltername]
     * ...
     */
    private void saveFilters() {
        String filterFilePath = ROOT_PATH + File.separator + CONFIG_DIR + File.separator + FILTER_FILE;
        File filterFile = new File(filterFilePath);
        
        // Sicherstellen, dass das Verzeichnis existiert
        File configDir = filterFile.getParentFile();
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filterFile))) {
            for (Filter filter : filters) {
                // Filtername in eckigen Klammern
                writer.write("[" + filter.getName() + "]");
                writer.newLine();
                
                // Schlüsselwörter, jedes in einer eigenen Zeile
                for (String keyword : filter.getKeywords()) {
                    writer.write(keyword);
                    writer.newLine();
                }
                
                // Leere Zeile zwischen Filtern
                writer.newLine();
            }
            logger.info("Filter gespeichert: {}", filters.size());
        } catch (IOException e) {
            logger.error("Fehler beim Speichern der Filter: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Erstellt Standardfilter, wenn keine Filter gefunden wurden.
     */
    private void createDefaultFilters() {
        filters.clear();
        
        // Standardfilter "Default" erstellen
        Filter defaultFilter = new Filter("Standard");
        defaultFilter.addKeyword("default");
        filters.add(defaultFilter);
        
        saveFilters();
    }
}