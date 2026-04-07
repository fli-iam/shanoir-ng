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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.action.init.LoginPanelActionListener;
import org.shanoir.uploader.action.init.StartupStateContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * This class is the Authentication GUI which allow a user to connect
 * with his login/password in log into the remote Shanoir server.
 * @author atouboul
 *
 */
@Component
public class LoginConfigurationPanel extends JPanel {

    public JLabel loginLabel;
    public JTextField loginText;
    public JLabel passwordLabel;
    public JPasswordField passwordText;
    public JButton connect;
    public JButton connectLater;

    @Autowired
    private LoginPanelActionListener loginPanelActionListener;

    public void configure(StartupStateContext sSC) {
        Container container = new Container();
        container.setLayout(new GridBagLayout());
        GridBagConstraints shanoirStartupGBC = new GridBagConstraints();
        shanoirStartupGBC.fill = GridBagConstraints.HORIZONTAL;
        this.add(container);

        this.setBorder(new EmptyBorder(5, 5, 5, 5));

        loginLabel = new JLabel(ShUpConfig.resourceBundle.getString("shanoir.uploader.login"));
        loginLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        shanoirStartupGBC.weightx = 0.2;
        shanoirStartupGBC.fill = GridBagConstraints.HORIZONTAL;
        shanoirStartupGBC.insets = new Insets(5, 5, 5, 5);
        shanoirStartupGBC.gridx = 0;
        shanoirStartupGBC.gridy = 0;
        container.add(loginLabel, shanoirStartupGBC);

        loginText = new JTextField("");
        loginText.setPreferredSize(new Dimension(200, 20));
        loginText.setHorizontalAlignment(SwingConstants.LEFT);
        shanoirStartupGBC.weightx = 0.7;
        shanoirStartupGBC.fill = GridBagConstraints.HORIZONTAL;
        shanoirStartupGBC.insets = new Insets(5, 5, 5, 5);
        shanoirStartupGBC.gridx = 2;
        shanoirStartupGBC.gridy = 0;
        container.add(loginText, shanoirStartupGBC);

        passwordLabel = new JLabel(ShUpConfig.resourceBundle.getString("shanoir.uploader.password"));
        passwordLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        shanoirStartupGBC.weightx = 0.2;
        shanoirStartupGBC.fill = GridBagConstraints.HORIZONTAL;
        shanoirStartupGBC.insets = new Insets(5, 5, 5, 5);
        shanoirStartupGBC.gridx = 0;
        shanoirStartupGBC.gridy = 1;
        container.add(passwordLabel, shanoirStartupGBC);

        passwordText = new JPasswordField();
        passwordText.setPreferredSize(new Dimension(200, 20));
        passwordText.setHorizontalAlignment(SwingConstants.LEFT);
        shanoirStartupGBC.weightx = 0.7;
        shanoirStartupGBC.fill = GridBagConstraints.HORIZONTAL;
        shanoirStartupGBC.insets = new Insets(5, 5, 5, 5);
        shanoirStartupGBC.gridx = 2;
        shanoirStartupGBC.gridy = 1;
        container.add(passwordText, shanoirStartupGBC);

        connect = new JButton(ShUpConfig.resourceBundle.getString("shanoir.uploader.connect"));
        connect.setPreferredSize(new Dimension(200, 20));
        connect.setHorizontalAlignment(SwingConstants.CENTER);
        shanoirStartupGBC.weightx = 0.7;
        shanoirStartupGBC.fill = GridBagConstraints.HORIZONTAL;
        shanoirStartupGBC.insets = new Insets(5, 5, 5, 5);
        shanoirStartupGBC.gridx = 2;
        shanoirStartupGBC.gridy = 2;
        container.add(connect, shanoirStartupGBC);

        connectLater = new JButton(ShUpConfig.resourceBundle.getString("shanoir.uploader.connect.later"));
        connectLater.setPreferredSize(new Dimension(200, 20));
        connectLater.setHorizontalAlignment(SwingConstants.CENTER);
        shanoirStartupGBC.weightx = 0.7;
        shanoirStartupGBC.fill = GridBagConstraints.HORIZONTAL;
        shanoirStartupGBC.insets = new Insets(5, 5, 5, 5);
        shanoirStartupGBC.gridx = 2;
        shanoirStartupGBC.gridy = 3;
        container.add(connectLater, shanoirStartupGBC);

        loginPanelActionListener.configure(this, sSC);
        connect.addActionListener(loginPanelActionListener);
        connectLater.addActionListener(loginPanelActionListener);
    }

}
