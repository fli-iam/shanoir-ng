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

package org.shanoir.uploader.gui;

import java.awt.BorderLayout;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

public class ErrorDialog extends JDialog {
    public JDialog errorDialog;
    public JLabel errorLabel;
    public JButton errorButton;
    public JLabel connexionStatus;
    ResourceBundle resourceBundle;

    public ErrorDialog(JDialog jdialog, String title, ResourceBundle resourceBundle) {
        super(jdialog, title, true);
        errorDialog = new JDialog(this, resourceBundle.getString("shanoir.uploader.systemErrorDialog.title"));
        errorDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        errorDialog.setSize(400, 150);
        errorDialog.setLocationRelativeTo(this);

        errorLabel = new JLabel("<html><p>" + resourceBundle.getString("shanoir.uploader.systemErrorDialog.label") + "</p>");
        errorDialog.add(errorLabel, BorderLayout.NORTH);

        errorButton = new JButton(resourceBundle.getString("shanoir.uploader.systemErrorDialog.button"));
        errorDialog.add(errorButton, BorderLayout.SOUTH);

    }



}
