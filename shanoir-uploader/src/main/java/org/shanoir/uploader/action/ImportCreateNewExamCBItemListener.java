/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.uploader.action;

import java.awt.Container;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.shanoir.uploader.gui.ImportDialog;

public class ImportCreateNewExamCBItemListener implements ItemListener {

    private static final Logger LOG = LoggerFactory.getLogger(ImportCreateNewExamCBItemListener.class);

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
