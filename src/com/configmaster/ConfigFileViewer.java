package com.configmaster;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Klasse zur Anzeige des Inhalts einer Konfigurationsdatei.
 * Öffnet ein neues Fenster mit dem Inhalt der ausgewählten Datei.
 * Unterstützt verschiedene Zeichencodierungen (UTF-8, UTF-16, etc.).
 */
public class ConfigFileViewer {
    
    private static final Logger logger = LogManager.getLogger(ConfigFileViewer.class);
    
    /**
     * Zeigt den Inhalt einer Konfigurationsdatei in einem neuen Fenster an.
     * 
     * @param parentShell Das Elternfenster
     * @param filePath Der Pfad zur anzuzeigenden Datei
     */
    public void viewConfigFile(Shell parentShell, String filePath) {
        // Neues Fenster erstellen
        Shell viewerShell = new Shell(parentShell, SWT.SHELL_TRIM);
        viewerShell.setText("Dateiansicht - " + filePath);
        viewerShell.setSize(700, 500);
        viewerShell.setLayout(new GridLayout(1, false));
        
        // Textfeld für den Dateiinhalt erstellen
        Text fileContentText = new Text(viewerShell, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        fileContentText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        fileContentText.setEditable(false);
        
        // Dateiinhalt laden
        try {
            // Versuche die Dateicodierung zu erkennen und zu lesen
            String content = readFileWithProperEncoding(filePath);
            fileContentText.setText(content);
        } catch (IOException e) {
            logger.error("Fehler beim Lesen der Datei: {}", e.getMessage(), e);
            fileContentText.setText("Fehler beim Lesen der Datei: " + e.getMessage());
        }
        
        // Fenster öffnen
        viewerShell.open();
    }
    
    /**
     * Liest eine Datei mit der erkannten Codierung.
     * 
     * @param filePath Der Pfad zur Datei
     * @return Der Inhalt der Datei als String
     * @throws IOException Wenn ein Fehler beim Lesen der Datei auftritt
     */
    private String readFileWithProperEncoding(String filePath) throws IOException {
        logger.info("Lese Datei: {} mit Codierungserkennung", filePath);
        
        // Datei einlesen
        byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
        
        // BOM erkennen und Codierung bestimmen
        Charset charset = detectCharset(fileBytes);
        logger.info("Erkannte Codierung: {}", charset.name());
        
        // Datei mit erkannter Codierung lesen
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), charset))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        
        return content.toString();
    }
    
    /**
     * Erkennt die Codierung einer Datei anhand der Byte-Order-Mark (BOM) oder
     * durch Testen verschiedener Codierungen.
     * 
     * @param bytes Die Bytes der Datei
     * @return Die erkannte Codierung
     */
    private Charset detectCharset(byte[] bytes) {
        // BOM-basierte Erkennung
        if (bytes.length >= 3 && bytes[0] == (byte)0xEF && bytes[1] == (byte)0xBB && bytes[2] == (byte)0xBF) {
            return StandardCharsets.UTF_8;
        } else if (bytes.length >= 2 && bytes[0] == (byte)0xFE && bytes[1] == (byte)0xFF) {
            return StandardCharsets.UTF_16BE;
        } else if (bytes.length >= 2 && bytes[0] == (byte)0xFF && bytes[1] == (byte)0xFE) {
            return StandardCharsets.UTF_16LE;
        }
        
        // Keine BOM gefunden, versuche verschiedene Codierungen
        if (isValidUTF8(bytes)) {
            return StandardCharsets.UTF_8;
        }
        
        if (isValidEncoding(bytes, StandardCharsets.UTF_16LE)) {
            return StandardCharsets.UTF_16LE;
        }
        
        if (isValidEncoding(bytes, StandardCharsets.UTF_16BE)) {
            return StandardCharsets.UTF_16BE;
        }
        
        // Fallback: Wenn keine spezifische Codierung erkannt wurde, verwende ISO-8859-1 (Latin-1)
        // Dies ist eine 8-Bit-Codierung, die alle möglichen Byte-Werte abdeckt
        return StandardCharsets.ISO_8859_1;
    }
    
    /**
     * Überprüft, ob die Bytes in UTF-8 codiert sind.
     * 
     * @param bytes Die zu überprüfenden Bytes
     * @return true, wenn die Bytes in UTF-8 codiert sind
     */
    private boolean isValidUTF8(byte[] bytes) {
        try {
            CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
            decoder.decode(ByteBuffer.wrap(bytes));
            return true;
        } catch (CharacterCodingException e) {
            return false;
        }
    }
    
    /**
     * Überprüft, ob die Bytes in der angegebenen Codierung codiert sind.
     * 
     * @param bytes Die zu überprüfenden Bytes
     * @param charset Die zu testende Codierung
     * @return true, wenn die Bytes in der angegebenen Codierung codiert sind
     */
    private boolean isValidEncoding(byte[] bytes, Charset charset) {
        try {
            CharsetDecoder decoder = charset.newDecoder();
            decoder.decode(ByteBuffer.wrap(bytes));
            return true;
        } catch (CharacterCodingException e) {
            return false;
        }
    }
}