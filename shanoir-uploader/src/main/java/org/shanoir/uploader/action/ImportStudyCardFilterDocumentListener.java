package org.shanoir.uploader.action;

 import java.util.ArrayList;
 import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import org.shanoir.uploader.gui.MainWindow;
  import org.shanoir.uploader.model.rest.StudyCard;
 
  public class ImportStudyCardFilterDocumentListener implements DocumentListener {
 
    private MainWindow mainWindow;
 
    private List<StudyCard> defaultStudyCards = new ArrayList<StudyCard>();

    public boolean isUpdating;
 
    public ImportStudyCardFilterDocumentListener(final MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.isUpdating = false;
    }
 
    @Override
    public void insertUpdate(DocumentEvent e) {
        if (!isUpdating) {
            filter(e);
        }
    }
 
    @Override
    public void removeUpdate(DocumentEvent e) {
        if (!isUpdating) {
            filter(e);
        }
    }
 
    @Override
    public void changedUpdate(DocumentEvent e) {
    }
 
      private void filter(DocumentEvent e) {
            try {
                String filter = e.getDocument().getText(0, e.getDocument().getLength());
                SwingUtilities.invokeLater(() -> {
                    this.isUpdating = true;
                    DefaultComboBoxModel<StudyCard> model = (DefaultComboBoxModel<StudyCard>) mainWindow.importDialog.studyCardCB.getModel();
                    model.removeAllElements();

                    for (StudyCard studyCard : defaultStudyCards) {
                        if (studyCard.toString().toLowerCase().contains(filter.toLowerCase())) {
                            model.addElement(studyCard);
                        }
                    }
                    JTextComponent editor = (JTextComponent) mainWindow.importDialog.studyCardCB.getEditor().getEditorComponent();
                    editor.setText(filter);
                    if (model.getSize() > 0) {
                        mainWindow.importDialog.studyCardCB.setSelectedIndex(0);
                    }
                    // We deactivate the import button if the user has not selected any study card or emptied filter
                    if (filter.isEmpty()) {
                        mainWindow.importDialog.exportButton.setEnabled(false);
                    } else {
                        mainWindow.importDialog.exportButton.setEnabled(true);
                    }
                    this.isUpdating = false;
                });    
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        
    }

 	public void addDefaultStudyCard(StudyCard defaultStudyCard) {
 		this.defaultStudyCards.add(defaultStudyCard);
 	}

 	public void cleanDefaultStudyCards() {
 		this.defaultStudyCards.clear();
 	}

    public boolean isUpdating() {
        return isUpdating;
    }

    public void setUpdating(boolean isUpdating) {
        this.isUpdating = isUpdating;
    }

 }
