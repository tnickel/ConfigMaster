package com.configmaster;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Klasse zum Scannen von Verzeichnissen nach Konfigurationsdateien.
 * Sucht nach Dateien mit dem Muster "Chart*.chr" in den Verzeichnissen
 * "Profiles/default" und "Profiles/charts/default".
 */
public class ConfigScanner {
    
    /**
     * Scannt das angegebene Root-Verzeichnis nach Konfigurationsdateien.
     * 
     * @param rootDir Das Root-Verzeichnis des Projekts
     * @return Eine Liste der gefundenen Konfigurationsdateien
     */
    public List<ConfigFile> scanForConfigFiles(File rootDir) {
        List<ConfigFile> configFiles = new ArrayList<>();
        
        // Suche in Profiles/default
        searchInDirectory(new File(rootDir, "Profiles/default"), configFiles);
        
        // Suche in Profiles/charts/default
        searchInDirectory(new File(rootDir, "Profiles/charts/default"), configFiles);
        
        return configFiles;
    }
    
    /**
     * Durchsucht ein Verzeichnis nach Dateien mit dem Muster "Chart*.chr".
     * 
     * @param directory Das zu durchsuchende Verzeichnis
     * @param configFiles Die Liste, zu der gefundene Dateien hinzugefügt werden
     */
    private void searchInDirectory(File directory, List<ConfigFile> configFiles) {
        if (!directory.exists() || !directory.isDirectory()) {
            return;
        }
        
        File[] files = directory.listFiles(
            (dir, name) -> name.matches("Chart.*\\.chr")
        );
        
        if (files != null) {
            for (File file : files) {
                configFiles.add(new ConfigFile(file.getName(), file.getAbsolutePath()));
            }
        }
    }
}