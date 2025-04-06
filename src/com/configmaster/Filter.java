package com.configmaster;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Repräsentiert einen Filter für die Suche nach Konfigurationsdateien.
 * Ein Filter besteht aus einem Namen und mehreren Schlüsselwörtern.
 */
public class Filter implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String name;
    private List<String> keywords;
    
    /**
     * Erstellt einen neuen Filter mit dem angegebenen Namen.
     * 
     * @param name Der Name des Filters
     */
    public Filter(String name) {
        this.name = name;
        this.keywords = new ArrayList<>();
    }
    
    /**
     * Gibt den Namen des Filters zurück.
     * 
     * @return Der Name des Filters
     */
    public String getName() {
        return name;
    }
    
    /**
     * Setzt den Namen des Filters.
     * 
     * @param name Der neue Name des Filters
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Gibt die Liste der Schlüsselwörter zurück.
     * 
     * @return Die Liste der Schlüsselwörter
     */
    public List<String> getKeywords() {
        return keywords;
    }
    
    /**
     * Fügt ein Schlüsselwort zum Filter hinzu.
     * 
     * @param keyword Das hinzuzufügende Schlüsselwort
     */
    public void addKeyword(String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            keywords.add(keyword.trim());
        }
    }
    
    /**
     * Aktualisiert ein Schlüsselwort an der angegebenen Position.
     * 
     * @param index Die Position des zu aktualisierenden Schlüsselworts
     * @param keyword Das neue Schlüsselwort
     */
    public void updateKeyword(int index, String keyword) {
        if (index >= 0 && index < keywords.size() && keyword != null && !keyword.trim().isEmpty()) {
            keywords.set(index, keyword.trim());
        }
    }
    
    /**
     * Entfernt ein Schlüsselwort an der angegebenen Position.
     * 
     * @param index Die Position des zu entfernenden Schlüsselworts
     */
    public void removeKeyword(int index) {
        if (index >= 0 && index < keywords.size()) {
            keywords.remove(index);
        }
    }
    
    /**
     * Konvertiert die Liste der Schlüsselwörter in einen kommagetennten String.
     * 
     * @return Ein kommagetennter String mit allen Schlüsselwörtern
     */
    public String getKeywordsAsString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keywords.size(); i++) {
            sb.append(keywords.get(i));
            if (i < keywords.size() - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return name;
    }
}