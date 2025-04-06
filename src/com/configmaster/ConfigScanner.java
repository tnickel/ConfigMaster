package com.configmaster;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Klasse zum Scannen von Verzeichnissen nach Konfigurationsdateien.
 * Sucht alle Dateien mit der Endung ".chr", deren Inhalt eines der angegebenen Suchmuster enthält.
 * Die Suchmuster werden durch Kommas getrennt angegeben (z.B. "pattern1,pattern2").
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
        // Die überladene Methode mit dem Standardmuster "default" aufrufen
        return scanForConfigFiles(searchDir, "default");
    }
    
    /**
     * Scannt das angegebene Suchverzeichnis rekursiv nach Konfigurationsdateien,
     * die eines der angegebenen Suchmuster im Inhalt enthalten.
     * 
     * @param searchDir Das zu durchsuchende Verzeichnis
     * @param searchPatterns Komma-getrennte Liste von Suchmustern
     * @return Eine Liste der gefundenen Konfigurationsdateien
     */
    public List<ConfigFile> scanForConfigFiles(File searchDir, String searchPatterns) {
        logger.info("Starte Scan für Konfigurationsdateien in: {} mit Mustern: {}", 
                searchDir.getAbsolutePath(), searchPatterns);
        
        List<ConfigFile> configFiles = new ArrayList<>();
        
        // Prüfen, ob das Suchmuster leer ist
        if (searchPatterns == null || searchPatterns.isEmpty()) {
            searchPatterns = "default"; // Standardwert verwenden, wenn kein Muster angegeben wurde
            logger.info("Leeres Suchmuster, verwende Standardwert: {}", searchPatterns);
        }
        
        // Suchmuster in ein Array aufteilen (Kommas als Trennzeichen)
        String[] patterns = searchPatterns.split(",");
        // Leerzeichen an Anfang und Ende jedes Musters entfernen
        for (int i = 0; i < patterns.length; i++) {
            patterns[i] = patterns[i].trim();
        }
        
        logger.info("Verwende folgende Suchmuster im Dateiinhalt: {}", Arrays.toString(patterns));
        
        // Rekursive Suche nach allen Dateien
        searchForChrFiles(searchDir, configFiles, patterns);
        
        if (configFiles.size() > 0) {
            logger.info("Scan abgeschlossen, gefundene Dateien: {}", configFiles.size());
        } else {
            logger.info("Scan abgeschlossen, keine Dateien gefunden.");
        }
        
        return configFiles;
    }
    
    /**
     * Sucht rekursiv nach Dateien mit der Endung ".chr", deren Inhalt eines der 
     * angegebenen Suchmuster enthält.
     * 
     * @param directory Das zu durchsuchende Verzeichnis
     * @param configFiles Die Liste, zu der gefundene Dateien hinzugefügt werden
     * @param patterns Ein Array mit Suchmustern
     */
    private void searchForChrFiles(File directory, List<ConfigFile> configFiles, String[] patterns) {
        if (!directory.exists() || !directory.isDirectory()) {
            return;
        }
        
        // Nach .chr Dateien suchen
        File[] files = directory.listFiles(
            (dir, name) -> name.toLowerCase().endsWith(".chr")
        );
        
        if (files != null && files.length > 0) {
            int foundFiles = 0;
            
            for (File file : files) {
                // Prüfen, ob der Dateiinhalt eines der Suchmuster enthält
                if (fileContainsAnyPattern(file, patterns)) {
                    logger.info("  - {}", file.getName());
                    configFiles.add(new ConfigFile(file.getName(), file.getAbsolutePath()));
                    foundFiles++;
                }
            }
            
            if (foundFiles > 0) {
                logger.info("Gefunden: {} passende Dateien in {}", foundFiles, directory.getAbsolutePath());
            }
        }
        
        // Rekursiv alle Unterverzeichnisse durchsuchen
        File[] subdirs = directory.listFiles(File::isDirectory);
        if (subdirs != null) {
            for (File subdir : subdirs) {
                searchForChrFiles(subdir, configFiles, patterns);
            }
        }
    }
    
    /**
     * Prüft, ob der Inhalt einer Datei eines der angegebenen Suchmuster enthält.
     * 
     * @param file Die zu prüfende Datei
     * @param patterns Die Suchmuster
     * @return true, wenn die Datei mindestens eines der Muster enthält
     */
    private boolean fileContainsAnyPattern(File file, String[] patterns) {
        try {
            // Datei mit verschiedenen Codierungen zu lesen versuchen
            Charset[] charsets = {
                StandardCharsets.UTF_8,
                StandardCharsets.ISO_8859_1,
                StandardCharsets.UTF_16,
                StandardCharsets.UTF_16BE,
                StandardCharsets.UTF_16LE
            };
            
            for (Charset charset : charsets) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(new FileInputStream(file), charset))) {
                    
                    String line;
                    while ((line = reader.readLine()) != null) {
                        for (String pattern : patterns) {
                            if (line.contains(pattern)) {
                                logger.debug("Muster '{}' gefunden in Datei: {} mit Codierung: {}", 
                                        pattern, file.getName(), charset.name());
                                return true;
                            }
                        }
                    }
                    
                } catch (IOException e) {
                    // Bei einem Fehler mit dieser Codierung die nächste versuchen
                    continue;
                }
            }
            
            // Wenn keine der Codierungen erfolgreich war oder kein Muster gefunden wurde
            return false;
            
        } catch (Exception e) {
            logger.error("Fehler beim Lesen der Datei {}: {}", file.getAbsolutePath(), e.getMessage());
            return false;
        }
    }
}