package com.configmaster;

/**
 * Repräsentiert eine Konfigurationsdatei im ConfigMaster-System.
 * Speichert den Namen und den Pfad der Datei.
 */
public class ConfigFile {
    private String name;
    private String path;
    
    /**
     * Erstellt ein neues ConfigFile-Objekt.
     * 
     * @param name Der Name der Konfigurationsdatei
     * @param path Der absolute Pfad zur Konfigurationsdatei
     */
    public ConfigFile(String name, String path) {
        this.name = name;
        this.path = path;
    }
    
    /**
     * Gibt den Namen der Konfigurationsdatei zurück.
     * 
     * @return Der Name der Datei
     */
    public String getName() {
        return name;
    }
    
    /**
     * Setzt den Namen der Konfigurationsdatei.
     * 
     * @param name Der neue Name der Datei
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Gibt den absoluten Pfad zur Konfigurationsdatei zurück.
     * 
     * @return Der Pfad zur Datei
     */
    public String getPath() {
        return path;
    }
    
    /**
     * Setzt den Pfad zur Konfigurationsdatei.
     * 
     * @param path Der neue Pfad zur Datei
     */
    public void setPath(String path) {
        this.path = path;
    }
}