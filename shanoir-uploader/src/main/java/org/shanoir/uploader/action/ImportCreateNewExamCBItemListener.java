package org.shanoir.uploader.action;

import java.awt.Container;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.shanoir.uploader.gui.ImportDialog;

public class ImportCreateNewExamCBItemListener implements ItemListener {

	private static final Logger logger = LoggerFactory.getLogger(ImportCreateNewExamCBItemListener.class);

	private ImportDialog importDialog;

	public ImportCreateNewExamCBItemListener(ImportDialog importDialog) {
		this.importDialog = importDialog;
	}

	public void itemStateChanged(ItemEvent e) {
		int state = e.getStateChange();
		if (state == ItemEvent.SELECTED) {
			importDialog.mrExaminationExistingExamCB.setEnabled(false);
			importDialog.mrExaminationExamExecutiveCB.setEnabled(true);
			((Container) importDialog.mrExaminationDateDP).getComponent(1).setEnabled(true);
			importDialog.mrExaminationCommentTF.setEnabled(true);
		} else {
			importDialog.mrExaminationExistingExamCB.setEnabled(true);
			importDialog.mrExaminationExamExecutiveCB.setEnabled(false);
			((Container) importDialog.mrExaminationDateDP).getComponent(1).setEnabled(false);
			importDialog.mrExaminationCommentTF.setEnabled(false);
		}
	}

}
