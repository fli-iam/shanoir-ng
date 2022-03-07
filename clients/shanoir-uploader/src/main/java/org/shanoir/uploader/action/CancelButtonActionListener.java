package org.shanoir.uploader.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import org.shanoir.uploader.gui.ImportDialog;

public class CancelButtonActionListener implements ActionListener {

	private ImportDialog importDialog;

	public CancelButtonActionListener(ImportDialog importDialog) {
		this.importDialog = importDialog;
	}

	public void actionPerformed(ActionEvent e) {
		importDialog.dispatchEvent(new WindowEvent(importDialog, WindowEvent.WINDOW_CLOSING));
	}

}
