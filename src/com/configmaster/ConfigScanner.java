package com.configmaster;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Klasse zum Scannen von Verzeichnissen nach Konfigurationsdateien.
 * Sucht alle Dateien mit der Endung ".chr", deren Pfad "default" enthält.
 */
public class ConfigScanner {
    
    private static final Logger logger = LogManager.getLogger(ConfigScanner.class);
    
    /**
     * Scannt das angegebene Suchverzeichnis rekursiv nach Konfigurationsdateien.
     * 
     * @param searchDir Das zu durchsuchende Verzeichnis
     * @return Eine Liste der gefundenen Konfigurationsdateien
     */
    public List<ConfigFile> scanForConfigFiles(File searchDir) {
        logger.info("Starte Scan für Konfigurationsdateien in: {}", searchDir.getAbsolutePath());
        
        List<ConfigFile> configFiles = new ArrayList<>();
        
        // Rekursive Suche nach allen Dateien
        searchForChrFiles(searchDir, configFiles);
        
        if (configFiles.size() > 0) {
            logger.info("Scan abgeschlossen, gefundene Dateien: {}", configFiles.size());
        } else {
            logger.info("Scan abgeschlossen, keine Dateien gefunden.");
        }
        
        return configFiles;
    }
    
    /**
     * Sucht rekursiv nach Dateien mit der Endung ".chr" in Pfaden,
     * die "default" enthalten.
     * 
     * @param directory Das zu durchsuchende Verzeichnis
     * @param configFiles Die Liste, zu der gefundene Dateien hinzugefügt werden
     */
    private void searchForChrFiles(File directory, List<ConfigFile> configFiles) {
        if (!directory.exists() || !directory.isDirectory()) {
            return;
        }
        
        // Prüfen, ob der aktuelle Verzeichnispfad "default" enthält
        String dirPath = directory.getAbsolutePath();
        boolean containsDefault = dirPath.toLowerCase().contains("default");
        
        // Wenn der Pfad "default" enthält, nach *.chr Dateien suchen
        if (containsDefault) {
            File[] files = directory.listFiles(
                (dir, name) -> name.toLowerCase().endsWith(".chr")
            );
            
            if (files != null && files.length > 0) {
                logger.info("Gefunden: {} Dateien in {}", files.length, directory.getAbsolutePath());
                
                for (File file : files) {
                    logger.info("  - {}", file.getName());
                    configFiles.add(new ConfigFile(file.getName(), file.getAbsolutePath()));
                }
            }
        }
        
        // Rekursiv alle Unterverzeichnisse durchsuchen
        File[] subdirs = directory.listFiles(File::isDirectory);
        if (subdirs != null) {
            for (File subdir : subdirs) {
                searchForChrFiles(subdir, configFiles);
            }
        }
    }
}