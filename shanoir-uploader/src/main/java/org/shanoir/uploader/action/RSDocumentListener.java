package org.shanoir.uploader.action;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.shanoir.uploader.gui.MainWindow;

public class RSDocumentListener implements DocumentListener{
	

	private MainWindow mainWindow;
	
	public RSDocumentListener(final MainWindow mainWindow) {
			this.mainWindow = mainWindow;

	}

    public void insertUpdate(DocumentEvent e) { 
    	
    	if (mainWindow.lastNameTF.getText().length()!=0 & mainWindow.firstNameTF.getText().length()!=0 & mainWindow.birthNameTF.getText().length()!=0 & mainWindow.birthDateTF.getText().length()!=0 & (mainWindow.msexR.isSelected() || mainWindow.fsexR.isSelected()) & mainWindow.isDicomObjectSelected == true)
    		mainWindow.downloadOrCopyButton.setEnabled(true);
    } 
    public void removeUpdate(DocumentEvent e) { 
    	if (mainWindow.lastNameTF.getText().length()==0 || mainWindow.firstNameTF.getText().length()==0 || mainWindow.birthNameTF.getText().length()==0 || 
    			mainWindow.birthDateTF.getText().length()==0 || !(mainWindow.msexR.isSelected() || mainWindow.fsexR.isSelected()) || mainWindow.isDicomObjectSelected == false)
    		mainWindow.downloadOrCopyButton.setEnabled(false);
    } 
    public void changedUpdate(DocumentEvent e) { 
    } 

	

}
