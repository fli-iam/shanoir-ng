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

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.action.init.SelectProfilePanelActionListener;
import org.shanoir.uploader.action.init.StartupStateContext;
import org.shanoir.uploader.gui.customcomponent.JComboBoxMandatory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("serial")
public class SelectProfileConfigurationPanel extends JPanel {

    public JLabel selectProfileLabel;
    @SuppressWarnings("rawtypes")
    public JComboBoxMandatory selectProfileCB;
    public JButton select;
    public JRadioButton rbRememberProfile;

    @Autowired
    private SelectProfilePanelActionListener selectProfilePanelActionListener;

    public void configure(StartupStateContext sSC) {
        Container container = new Container();
        container.setLayout(new GridBagLayout());
        GridBagConstraints shanoirStartupGBC = new GridBagConstraints();
        shanoirStartupGBC.fill = GridBagConstraints.HORIZONTAL;
        this.add(container);
        this.setBorder(new EmptyBorder(5, 5, 5, 5));

        selectProfileLabel = new JLabel(ShUpConfig.resourceBundle.getString("shanoir.uploader.profile.select.label"));
        selectProfileLabel.setHorizontalAlignment(SwingConstants.LEFT);
        shanoirStartupGBC.weightx = 0.2;
        shanoirStartupGBC.fill = GridBagConstraints.HORIZONTAL;
        shanoirStartupGBC.insets = new Insets(5, 10, 5, 5);
        shanoirStartupGBC.gridx = 1;
        shanoirStartupGBC.gridy = 0;
        container.add(selectProfileLabel, shanoirStartupGBC);

        selectProfileCB = new JComboBoxMandatory();
        selectProfileCB.setBackground(Color.WHITE);
        selectProfileCB.setAlignmentX(SwingConstants.CENTER);
        shanoirStartupGBC.weightx = 0.2;
        shanoirStartupGBC.fill = GridBagConstraints.HORIZONTAL;
        shanoirStartupGBC.insets = new Insets(5, 5, 5, 5);
        shanoirStartupGBC.gridx = 1;
        shanoirStartupGBC.gridy = 1;
        container.add(selectProfileCB, shanoirStartupGBC);

        selectProfileCB.removeAllItems();
        String[] profiles = ShUpConfig.profiles;
        for (int i = 0; i < profiles.length; i++) {
            selectProfileCB.addItem(profiles[i]);
        }

        rbRememberProfile = new JRadioButton(ShUpConfig.resourceBundle.getString("shanoir.uploader.profile.remember.label"));
        rbRememberProfile.setHorizontalAlignment(SwingConstants.LEFT);
        shanoirStartupGBC.weightx = 0.2;
        shanoirStartupGBC.fill = GridBagConstraints.HORIZONTAL;
        shanoirStartupGBC.insets = new Insets(5, 0, 5, 5);
        shanoirStartupGBC.gridx = 1;
        shanoirStartupGBC.gridy = 2;
        container.add(rbRememberProfile, shanoirStartupGBC);

        select = new JButton(ShUpConfig.resourceBundle.getString("shanoir.uploader.profile.select.button"));
        select.setPreferredSize(new Dimension(150, 20));
        select.setHorizontalAlignment(SwingConstants.CENTER);
        shanoirStartupGBC.weightx = 0.7;
        shanoirStartupGBC.fill = GridBagConstraints.HORIZONTAL;
        shanoirStartupGBC.insets = new Insets(10, 5, 5, 5);
        shanoirStartupGBC.gridx = 1;
        shanoirStartupGBC.gridy = 3;
        container.add(select, shanoirStartupGBC);

        selectProfilePanelActionListener.configure(this, sSC);
        select.addActionListener(selectProfilePanelActionListener);
    }

}
