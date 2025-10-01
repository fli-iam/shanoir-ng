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

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.shanoir.uploader.gui.MainWindow;

public class RSDocumentListener implements DocumentListener {

    private MainWindow mainWindow;

    public RSDocumentListener(final MainWindow mainWindow) {
        this.mainWindow = mainWindow;
    }

    public void insertUpdate(DocumentEvent e) {
        if (mainWindow.lastNameTF.getText().length() != 0 & mainWindow.firstNameTF.getText().length() != 0
                & mainWindow.birthNameTF.getText().length() != 0 & mainWindow.birthDateTF.getText().length() != 0
                & (mainWindow.mSexR.isSelected() || mainWindow.fSexR.isSelected() || mainWindow.oSexR.isSelected())
                & mainWindow.isDicomObjectSelected)
            mainWindow.downloadOrCopyButton.setEnabled(true);
    }

    public void removeUpdate(DocumentEvent e) {
        if (mainWindow.lastNameTF.getText().length() == 0 || mainWindow.firstNameTF.getText().length() == 0
                || mainWindow.birthNameTF.getText().length() == 0 || mainWindow.birthDateTF.getText().length() == 0
                || !(mainWindow.mSexR.isSelected() || mainWindow.fSexR.isSelected() || mainWindow.oSexR.isSelected())
                || !mainWindow.isDicomObjectSelected)
            mainWindow.downloadOrCopyButton.setEnabled(false);
    }

    public void changedUpdate(DocumentEvent e) {
    }

}
