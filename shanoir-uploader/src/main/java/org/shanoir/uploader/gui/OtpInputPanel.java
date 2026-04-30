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
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.action.init.AuthCancelActionListener;
import org.shanoir.uploader.action.init.AuthCancelActionListener.CancelTarget;
import org.shanoir.uploader.action.init.OtpInputSubmitActionListener;
import org.shanoir.uploader.action.init.StartupStateContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Startup panel displayed when Keycloak requires a TOTP one-time code (2FA
 * already configured on the user account). The user enters the 6-digit code
 * from their authenticator app and clicks Submit.
 */
@Component
public class OtpInputPanel extends JPanel {

    public JLabel otpLabel;
    public JTextField otpText;
    public JPanel buttonPanel;
    public JButton cancel;
    public JButton submit;

    @Autowired
    private AuthCancelActionListener authCancelActionListener;

    @Autowired
    private OtpInputSubmitActionListener otpInputSubmitActionListener;

    public void configure(StartupStateContext sSC) {
        this.removeAll();
        this.revalidate();
        this.repaint();

        Container container = new Container();
        container.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(container);
        this.setBorder(new EmptyBorder(5, 5, 5, 5));

        otpLabel = new JLabel(ShUpConfig.resourceBundle.getString("shanoir.uploader.otp.code"));
        otpLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        gbc.weightx = 0.2;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        container.add(otpLabel, gbc);

        otpText = new JTextField("");
        otpText.setPreferredSize(new Dimension(200, 20));
        otpText.setHorizontalAlignment(SwingConstants.LEFT);
        gbc.weightx = 0.7;
        gbc.gridx = 2;
        gbc.gridy = 0;
        container.add(otpText, gbc);

        cancel = new JButton(ShUpConfig.resourceBundle.getString("shanoir.uploader.cancel"));
        cancel.setPreferredSize(new Dimension(100, 20));
        cancel.setHorizontalAlignment(SwingConstants.CENTER);
        authCancelActionListener.configure(sSC, CancelTarget.LOGIN);
        cancel.addActionListener(authCancelActionListener);

        submit = new JButton(ShUpConfig.resourceBundle.getString("shanoir.uploader.otp.submit"));
        submit.setPreferredSize(new Dimension(100, 20));
        submit.setHorizontalAlignment(SwingConstants.CENTER);
        otpInputSubmitActionListener.configure(this, sSC);
        submit.addActionListener(otpInputSubmitActionListener);

        buttonPanel = new JPanel();
        buttonPanel.add(cancel);
        buttonPanel.add(submit);
        gbc.gridx = 0;
        gbc.gridx = 2;
        gbc.gridy = 1;

        container.add(buttonPanel, gbc);
    }
}
