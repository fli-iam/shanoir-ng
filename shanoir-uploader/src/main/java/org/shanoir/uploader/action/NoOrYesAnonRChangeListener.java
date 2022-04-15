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

import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.shanoir.uploader.gui.MainWindow;

public class NoOrYesAnonRChangeListener implements ChangeListener {

	private MainWindow mainWindow;
	
	public NoOrYesAnonRChangeListener(final MainWindow mainWindow) {
		this.mainWindow = mainWindow;
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
        if(mainWindow.noAnonR.isSelected()) {
        		mainWindow.lastNameLabel.setVisible(true);
        		mainWindow.lastNameTF.setVisible(true);
        		mainWindow.birthNameCopyButton.setVisible(true);
        		mainWindow.firstNameLabel.setVisible(true);
        		mainWindow.firstNameTF.setVisible(true);
        		mainWindow.birthNameLabel.setVisible(true);
        		mainWindow.birthNameTF.setVisible(true);
        		
        		mainWindow.editPanel.remove(mainWindow.newPatientIDLabel);
        		mainWindow.newPatientIDLabel.setVisible(false);
        		mainWindow.editPanel.remove(mainWindow.newPatientIDTF);
        		mainWindow.newPatientIDTF.setVisible(false);
        		mainWindow.editPanel.remove(mainWindow.dummyLabelAsLayoutBuffer);
        		mainWindow.dummyLabelAsLayoutBuffer.setVisible(false);

    			if (mainWindow.lastNameTF.getText().length() == 0
    					|| mainWindow.firstNameTF.getText().length() == 0
    					|| mainWindow.birthNameTF.getText().length() == 0
    					|| mainWindow.birthDateTF.getText().length() == 0
    					|| !(mainWindow.mSexR.isSelected() || mainWindow.fSexR.isSelected())
    					|| mainWindow.isDicomObjectSelected == false) {
    				mainWindow.downloadOrCopyButton.setEnabled(false);
    			} else {
    				mainWindow.downloadOrCopyButton.setEnabled(true);
    			}
    			
        		mainWindow.editPanel.revalidate();
        		mainWindow.editPanel.repaint();
        } else {
        		mainWindow.lastNameLabel.setVisible(false);
        		mainWindow.lastNameTF.setVisible(false);
        		mainWindow.birthNameCopyButton.setVisible(false);
        		mainWindow.firstNameLabel.setVisible(false);
        		mainWindow.firstNameTF.setVisible(false);
        		mainWindow.birthNameLabel.setVisible(false);
        		mainWindow.birthNameTF.setVisible(false);

        		GridBagConstraints gbc_newPatientIDLabel = new GridBagConstraints();
        		gbc_newPatientIDLabel.anchor = GridBagConstraints.EAST;
        		gbc_newPatientIDLabel.insets = new Insets(0, 0, 5, 5);
        		gbc_newPatientIDLabel.gridx = 0;
        		gbc_newPatientIDLabel.gridy = 2;
        		mainWindow.newPatientIDLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        		mainWindow.newPatientIDLabel.setVisible(true);
        		mainWindow.editPanel.add(mainWindow.newPatientIDLabel, gbc_newPatientIDLabel);
        		
        		GridBagConstraints gbc_newPatientIDTF = new GridBagConstraints();
        		gbc_newPatientIDTF.insets = new Insets(10, 10, 10, 10);
        		gbc_newPatientIDTF.fill = GridBagConstraints.HORIZONTAL;
        		gbc_newPatientIDTF.gridx = 1;
        		gbc_newPatientIDTF.gridy = 2;
        		gbc_newPatientIDTF.gridwidth = 2;
        		mainWindow.newPatientIDTF.setColumns(15);
        		mainWindow.newPatientIDTF.setVisible(true);
        		mainWindow.editPanel.add(mainWindow.newPatientIDTF, gbc_newPatientIDTF);
        		
        		GridBagConstraints gBCDummyLabelForLayout = new GridBagConstraints();
        		gBCDummyLabelForLayout.anchor = GridBagConstraints.EAST;
        		gBCDummyLabelForLayout.insets = new Insets(10, 10, 10, 10);
        		gBCDummyLabelForLayout.gridx = 3;
        		gBCDummyLabelForLayout.gridy = 2;
        		mainWindow.dummyLabelAsLayoutBuffer.setVisible(true);
        		mainWindow.editPanel.add(mainWindow.dummyLabelAsLayoutBuffer, gBCDummyLabelForLayout);
        		
    			if (mainWindow.newPatientIDTF.getText().length() == 0
    					|| mainWindow.birthDateTF.getText().length() == 0
    					|| !(mainWindow.mSexR.isSelected() || mainWindow.fSexR.isSelected())
    					|| mainWindow.isDicomObjectSelected == false) { 
    				mainWindow.downloadOrCopyButton.setEnabled(false);
    			} else {
    				mainWindow.downloadOrCopyButton.setEnabled(true);
    			}
        		
        		mainWindow.editPanel.revalidate();
        		mainWindow.editPanel.repaint(); 
        }
	}

}
