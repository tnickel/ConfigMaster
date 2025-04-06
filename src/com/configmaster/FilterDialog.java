package com.configmaster;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog zur Verwaltung der Filter.
 * Ermöglicht das Anzeigen, Hinzufügen, Bearbeiten und Löschen von Filtern und deren Schlüsselwörtern.
 */
public class FilterDialog extends Dialog {
    
    private static final Logger logger = LogManager.getLogger(FilterDialog.class);
    
    private Shell shell;
    private FilterManager filterManager;
    private List filterList;
    private List keywordList;
    private Filter selectedFilter;
    
    /**
     * Erstellt einen neuen FilterDialog.
     * 
     * @param parent Das Elternfenster
     * @param filterManager Der FilterManager zur Verwaltung der Filter
     */
    public FilterDialog(Shell parent, FilterManager filterManager) {
        super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        this.filterManager = filterManager;
    }
    
    /**
     * Öffnet den Dialog.
     */
    public void open() {
        Shell parent = getParent();
        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
        shell.setText("Filterverwaltung");
        shell.setSize(600, 450);
        
        createContents();
        
        shell.open();
        Display display = parent.getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }
    
    /**
     * Erstellt den Inhalt des Dialogs.
     */
    private void createContents() {
        GridLayout layout = new GridLayout(3, false);
        layout.marginWidth = 10;
        layout.marginHeight = 10;
        layout.horizontalSpacing = 10;
        shell.setLayout(layout);
        
        // Linke Seite: Filterliste
        createFilterListSection();
        
        // Rechte Seite: Schlüsselwortliste
        createKeywordListSection();
        
        // Filterauswahl-Hinweis
        Label noteLabel = new Label(shell, SWT.NONE);
        noteLabel.setText("Hinweis: Wählen Sie einen Filter aus, um seine Schlüsselwörter zu bearbeiten.");
        GridData noteLabelData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        noteLabelData.horizontalSpan = 3;
        noteLabel.setLayoutData(noteLabelData);
        
        // Schließen-Button
        Button closeButton = new Button(shell, SWT.PUSH);
        closeButton.setText("Schließen");
        GridData closeButtonData = new GridData(SWT.CENTER, SWT.CENTER, true, false);
        closeButtonData.horizontalSpan = 3;
        closeButton.setLayoutData(closeButtonData);
        closeButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                shell.close();
            }
        });
        
        // Initialisiere die Listen
        updateFilterList();
    }
    
    /**
     * Erstellt den Bereich für die Filterliste.
     */
    private void createFilterListSection() {
        // Überschrift
        Label filterLabel = new Label(shell, SWT.NONE);
        filterLabel.setText("Filter:");
        GridData filterLabelData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        filterLabelData.horizontalSpan = 3;
        filterLabel.setLayoutData(filterLabelData);
        
        // Filterliste
        filterList = new List(shell, SWT.BORDER | SWT.V_SCROLL);
        GridData filterListData = new GridData(SWT.FILL, SWT.FILL, true, true);
        filterListData.horizontalSpan = 3;
        filterListData.heightHint = 150;
        filterList.setLayoutData(filterListData);
        
        // Selection Listener für die Filterliste
        filterList.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int selectedIndex = filterList.getSelectionIndex();
                if (selectedIndex != -1) {
                    selectedFilter = filterManager.getFilters().get(selectedIndex);
                    updateKeywordList();
                }
            }
        });
        
        // Buttons für die Filterliste
        Button addFilterButton = new Button(shell, SWT.PUSH);
        addFilterButton.setText("Hinzufügen");
        addFilterButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        addFilterButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                addFilter();
            }
        });
        
        Button editFilterButton = new Button(shell, SWT.PUSH);
        editFilterButton.setText("Bearbeiten");
        editFilterButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        editFilterButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                editFilter();
            }
        });
        
        Button deleteFilterButton = new Button(shell, SWT.PUSH);
        deleteFilterButton.setText("Löschen");
        deleteFilterButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        deleteFilterButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                deleteFilter();
            }
        });
    }
    
    /**
     * Erstellt den Bereich für die Schlüsselwortliste.
     */
    private void createKeywordListSection() {
        // Überschrift
        Label keywordLabel = new Label(shell, SWT.NONE);
        keywordLabel.setText("Schlüsselwörter:");
        GridData keywordLabelData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        keywordLabelData.horizontalSpan = 3;
        keywordLabel.setLayoutData(keywordLabelData);
        
        // Schlüsselwortliste
        keywordList = new List(shell, SWT.BORDER | SWT.V_SCROLL);
        GridData keywordListData = new GridData(SWT.FILL, SWT.FILL, true, true);
        keywordListData.horizontalSpan = 3;
        keywordListData.heightHint = 150;
        keywordList.setLayoutData(keywordListData);
        
        // Buttons für die Schlüsselwortliste
        Button addKeywordButton = new Button(shell, SWT.PUSH);
        addKeywordButton.setText("Hinzufügen");
        addKeywordButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        addKeywordButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                addKeyword();
            }
        });
        
        Button editKeywordButton = new Button(shell, SWT.PUSH);
        editKeywordButton.setText("Bearbeiten");
        editKeywordButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        editKeywordButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                editKeyword();
            }
        });
        
        Button deleteKeywordButton = new Button(shell, SWT.PUSH);
        deleteKeywordButton.setText("Löschen");
        deleteKeywordButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        deleteKeywordButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                deleteKeyword();
            }
        });
    }
    
    /**
     * Aktualisiert die Filterliste.
     */
    private void updateFilterList() {
        filterList.removeAll();
        for (Filter filter : filterManager.getFilters()) {
            filterList.add(filter.getName());
        }
        
        // Wenn es Filter gibt, selektiere den ersten
        if (filterList.getItemCount() > 0) {
            filterList.select(0);
            selectedFilter = filterManager.getFilters().get(0);
            updateKeywordList();
        } else {
            selectedFilter = null;
            keywordList.removeAll();
        }
    }
    
    /**
     * Aktualisiert die Schlüsselwortliste für den ausgewählten Filter.
     */
    private void updateKeywordList() {
        keywordList.removeAll();
        if (selectedFilter != null) {
            for (String keyword : selectedFilter.getKeywords()) {
                keywordList.add(keyword);
            }
        }
    }
    
    /**
     * Fügt einen neuen Filter hinzu.
     */
    private void addFilter() {
        // Dialog zum Eingeben des Filternamens
        Shell dialogShell = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        dialogShell.setText("Neuen Filter hinzufügen");
        dialogShell.setSize(300, 120);
        
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 10;
        layout.marginHeight = 10;
        dialogShell.setLayout(layout);
        
        Label nameLabel = new Label(dialogShell, SWT.NONE);
        nameLabel.setText("Filtername:");
        
        Text nameText = new Text(dialogShell, SWT.BORDER);
        nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        Button okButton = new Button(dialogShell, SWT.PUSH);
        okButton.setText("OK");
        okButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
        okButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String name = nameText.getText().trim();
                if (!name.isEmpty()) {
                    Filter newFilter = new Filter(name);
                    filterManager.addFilter(newFilter);
                    updateFilterList();
                    
                    // Selektiere den neuen Filter
                    int newIndex = filterManager.getFilters().size() - 1;
                    filterList.select(newIndex);
                    selectedFilter = filterManager.getFilters().get(newIndex);
                    updateKeywordList();
                    
                    dialogShell.close();
                } else {
                    MessageBox messageBox = new MessageBox(dialogShell, SWT.ICON_ERROR);
                    messageBox.setText("Fehler");
                    messageBox.setMessage("Bitte geben Sie einen Filternamen ein.");
                    messageBox.open();
                }
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
     * Bearbeitet den ausgewählten Filter.
     */
    private void editFilter() {
        int selectedIndex = filterList.getSelectionIndex();
        if (selectedIndex == -1) {
            MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
            messageBox.setText("Fehler");
            messageBox.setMessage("Bitte wählen Sie einen Filter aus.");
            messageBox.open();
            return;
        }
        
        // Dialog zum Bearbeiten des Filternamens
        Shell dialogShell = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        dialogShell.setText("Filter bearbeiten");
        dialogShell.setSize(300, 120);
        
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 10;
        layout.marginHeight = 10;
        dialogShell.setLayout(layout);
        
        Label nameLabel = new Label(dialogShell, SWT.NONE);
        nameLabel.setText("Filtername:");
        
        Text nameText = new Text(dialogShell, SWT.BORDER);
        nameText.setText(selectedFilter.getName());
        nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        Button okButton = new Button(dialogShell, SWT.PUSH);
        okButton.setText("OK");
        okButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
        okButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String name = nameText.getText().trim();
                if (!name.isEmpty()) {
                    selectedFilter.setName(name);
                    filterManager.updateFilter(selectedIndex, selectedFilter);
                    updateFilterList();
                    
                    // Selektiere den bearbeiteten Filter wieder
                    filterList.select(selectedIndex);
                    
                    dialogShell.close();
                } else {
                    MessageBox messageBox = new MessageBox(dialogShell, SWT.ICON_ERROR);
                    messageBox.setText("Fehler");
                    messageBox.setMessage("Bitte geben Sie einen Filternamen ein.");
                    messageBox.open();
                }
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
     * Löscht den ausgewählten Filter.
     */
    private void deleteFilter() {
        int selectedIndex = filterList.getSelectionIndex();
        if (selectedIndex == -1) {
            MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
            messageBox.setText("Fehler");
            messageBox.setMessage("Bitte wählen Sie einen Filter aus.");
            messageBox.open();
            return;
        }
        
        // Bestätigungsdialog
        MessageBox confirmBox = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
        confirmBox.setText("Filter löschen");
        confirmBox.setMessage("Möchten Sie den Filter '" + selectedFilter.getName() + "' wirklich löschen?");
        int response = confirmBox.open();
        
        if (response == SWT.YES) {
            filterManager.removeFilter(selectedIndex);
            updateFilterList();
        }
    }
    
    /**
     * Fügt ein neues Schlüsselwort zum ausgewählten Filter hinzu.
     */
    private void addKeyword() {
        if (selectedFilter == null) {
            MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
            messageBox.setText("Fehler");
            messageBox.setMessage("Bitte wählen Sie zuerst einen Filter aus.");
            messageBox.open();
            return;
        }
        
        // Dialog zum Eingeben des Schlüsselworts
        Shell dialogShell = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        dialogShell.setText("Neues Schlüsselwort hinzufügen");
        dialogShell.setSize(300, 120);
        
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 10;
        layout.marginHeight = 10;
        dialogShell.setLayout(layout);
        
        Label keywordLabel = new Label(dialogShell, SWT.NONE);
        keywordLabel.setText("Schlüsselwort:");
        
        Text keywordText = new Text(dialogShell, SWT.BORDER);
        keywordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        Button okButton = new Button(dialogShell, SWT.PUSH);
        okButton.setText("OK");
        okButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
        okButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String keyword = keywordText.getText().trim();
                if (!keyword.isEmpty()) {
                    selectedFilter.addKeyword(keyword);
                    int selectedIndex = filterList.getSelectionIndex();
                    filterManager.updateFilter(selectedIndex, selectedFilter);
                    updateKeywordList();
                    dialogShell.close();
                } else {
                    MessageBox messageBox = new MessageBox(dialogShell, SWT.ICON_ERROR);
                    messageBox.setText("Fehler");
                    messageBox.setMessage("Bitte geben Sie ein Schlüsselwort ein.");
                    messageBox.open();
                }
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
     * Bearbeitet das ausgewählte Schlüsselwort.
     */
    private void editKeyword() {
        if (selectedFilter == null) {
            MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
            messageBox.setText("Fehler");
            messageBox.setMessage("Bitte wählen Sie zuerst einen Filter aus.");
            messageBox.open();
            return;
        }
        
        int selectedKeywordIndex = keywordList.getSelectionIndex();
        if (selectedKeywordIndex == -1) {
            MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
            messageBox.setText("Fehler");
            messageBox.setMessage("Bitte wählen Sie ein Schlüsselwort aus.");
            messageBox.open();
            return;
        }
        
        // Dialog zum Bearbeiten des Schlüsselworts
        Shell dialogShell = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        dialogShell.setText("Schlüsselwort bearbeiten");
        dialogShell.setSize(300, 120);
        
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 10;
        layout.marginHeight = 10;
        dialogShell.setLayout(layout);
        
        Label keywordLabel = new Label(dialogShell, SWT.NONE);
        keywordLabel.setText("Schlüsselwort:");
        
        Text keywordText = new Text(dialogShell, SWT.BORDER);
        keywordText.setText(selectedFilter.getKeywords().get(selectedKeywordIndex));
        keywordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        Button okButton = new Button(dialogShell, SWT.PUSH);
        okButton.setText("OK");
        okButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
        okButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String keyword = keywordText.getText().trim();
                if (!keyword.isEmpty()) {
                    selectedFilter.updateKeyword(selectedKeywordIndex, keyword);
                    int selectedFilterIndex = filterList.getSelectionIndex();
                    filterManager.updateFilter(selectedFilterIndex, selectedFilter);
                    updateKeywordList();
                    dialogShell.close();
                } else {
                    MessageBox messageBox = new MessageBox(dialogShell, SWT.ICON_ERROR);
                    messageBox.setText("Fehler");
                    messageBox.setMessage("Bitte geben Sie ein Schlüsselwort ein.");
                    messageBox.open();
                }
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
     * Löscht das ausgewählte Schlüsselwort.
     */
    private void deleteKeyword() {
        if (selectedFilter == null) {
            MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
            messageBox.setText("Fehler");
            messageBox.setMessage("Bitte wählen Sie zuerst einen Filter aus.");
            messageBox.open();
            return;
        }
        
        int selectedKeywordIndex = keywordList.getSelectionIndex();
        if (selectedKeywordIndex == -1) {
            MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
            messageBox.setText("Fehler");
            messageBox.setMessage("Bitte wählen Sie ein Schlüsselwort aus.");
            messageBox.open();
            return;
        }
        
        // Bestätigungsdialog
        MessageBox confirmBox = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
        confirmBox.setText("Schlüsselwort löschen");
        confirmBox.setMessage("Möchten Sie das Schlüsselwort '" + selectedFilter.getKeywords().get(selectedKeywordIndex) + "' wirklich löschen?");
        int response = confirmBox.open();
        
        if (response == SWT.YES) {
            selectedFilter.removeKeyword(selectedKeywordIndex);
            int selectedFilterIndex = filterList.getSelectionIndex();
            filterManager.updateFilter(selectedFilterIndex, selectedFilter);
            updateKeywordList();
        }
    }
}