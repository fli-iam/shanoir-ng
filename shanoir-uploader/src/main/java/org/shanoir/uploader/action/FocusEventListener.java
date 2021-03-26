package org.shanoir.uploader.action;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import org.shanoir.uploader.gui.MainWindow;

public class FocusEventListener implements FocusListener {

	private MainWindow mainWindow;

	public FocusEventListener(final MainWindow mainWindow) {
		this.mainWindow = mainWindow;
	}

	public void focusLost(FocusEvent event) {
		// displayMessage("Focus Lost", event);
	}

	public void focusGained(FocusEvent event) {
		// displayMessage("Focus gained", event);
		if (mainWindow.patientNameTF.getText().length() != 0)
			mainWindow.queryButton.setEnabled(true);
		else
			mainWindow.queryButton.setEnabled(false);
	}

}
