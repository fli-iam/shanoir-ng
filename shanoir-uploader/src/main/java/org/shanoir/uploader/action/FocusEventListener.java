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
