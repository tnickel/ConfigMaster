package com.configmaster;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog zur Auswahl eines Filters.
 * Zeigt alle verfügbaren Filter an und ermöglicht die Auswahl eines Filters.
 */
public class FilterSelectionDialog extends Dialog {
    
    private Shell shell;
    private FilterManager filterManager;
    private Filter selectedFilter = null;
    
    /**
     * Erstellt einen neuen FilterSelectionDialog.
     * 
     * @param parent Das Elternfenster
     * @param filterManager Der FilterManager zur Verwaltung der Filter
     */
    public FilterSelectionDialog(Shell parent, FilterManager filterManager) {
        super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        this.filterManager = filterManager;
    }
    
    /**
     * Öffnet den Dialog und gibt den ausgewählten Filter zurück.
     * 
     * @return Der ausgewählte Filter oder null, wenn kein Filter ausgewählt wurde
     */
    public Filter open() {
        Shell parent = getParent();
        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        shell.setText("Filter auswählen");
        shell.setSize(400, 300);
        
        createContents();
        
        shell.open();
        Display display = parent.getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        
        // Rückgabe des ausgewählten Filters
        return selectedFilter;
    }
    
    /**
     * Erstellt den Inhalt des Dialogs.
     */
    private void createContents() {
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 10;
        layout.marginHeight = 10;
        shell.setLayout(layout);
        
        Label label = new Label(shell, SWT.NONE);
        label.setText("Wählen Sie einen Filter:");
        
        // Liste der Filter
        List filterList = new List(shell, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
        GridData filterListData = new GridData(SWT.FILL, SWT.FILL, true, true);
        filterList.setLayoutData(filterListData);
        
        // Filter zur Liste hinzufügen
        for (Filter filter : filterManager.getFilters()) {
            filterList.add(filter.getName());
        }
        
        // Wenn es Filter gibt, erstes Element selektieren
        if (filterList.getItemCount() > 0) {
            filterList.select(0);
        }
        
        // Button-Bereich
        GridLayout buttonLayout = new GridLayout(2, true);
        GridData buttonLayoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        
        // Container für die Buttons
        org.eclipse.swt.widgets.Composite buttonComposite = new org.eclipse.swt.widgets.Composite(shell, SWT.NONE);
        buttonComposite.setLayout(buttonLayout);
        buttonComposite.setLayoutData(buttonLayoutData);
        
        // OK-Button
        Button okButton = new Button(buttonComposite, SWT.PUSH);
        okButton.setText("OK");
        okButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        okButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int selectedIndex = filterList.getSelectionIndex();
                if (selectedIndex >= 0) {
                    selectedFilter = filterManager.getFilters().get(selectedIndex);
                }
                shell.close();
            }
        });
        
        // Abbrechen-Button
        Button cancelButton = new Button(buttonComposite, SWT.PUSH);
        cancelButton.setText("Abbrechen");
        cancelButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        cancelButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                selectedFilter = null;
                shell.close();
            }
        });
        
        // Default-Button setzen
        shell.setDefaultButton(okButton);
    }
}