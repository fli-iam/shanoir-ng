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
import java.awt.Image;
import java.awt.Insets;
import java.io.ByteArrayInputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.action.init.OtpSetupPanelActionListener;
import org.shanoir.uploader.action.init.StartupStateContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Startup panel displayed when Keycloak requires initial TOTP enrollment (2FA
 * not yet configured on the user account). Shows the QR code (or the manual
 * base32 key as a fallback), then asks the user to enter the first code from
 * their authenticator app together with an optional device label.
 */
@Component
public class OtpSetupPanel extends JPanel {

    private static final Logger LOG = LoggerFactory.getLogger(OtpSetupPanel.class);

    public JLabel qrCodeLabel;
    public JLabel manualKeyLabel;
    public JLabel manualKeyValue;
    public JLabel otpLabel;
    public JTextField otpText;
    public JLabel deviceLabelLabel;
    public JTextField deviceLabelText;
    public JButton submit;

    @Autowired
    private OtpSetupPanelActionListener otpSetupPanelActionListener;

    public void configure(StartupStateContext sSC) {
        Container container = new Container();
        container.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(container);
        this.setBorder(new EmptyBorder(5, 5, 5, 5));

        qrCodeLabel = new JLabel();
        qrCodeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(5, 5, 5, 5);
        container.add(qrCodeLabel, gbc);

        gbc.gridwidth = 1;

        manualKeyLabel = new JLabel(ShUpConfig.resourceBundle.getString("shanoir.uploader.otp.setup.manual.key"));
        manualKeyLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        gbc.weightx = 0.2;
        gbc.gridx = 0;
        gbc.gridy = 1;
        container.add(manualKeyLabel, gbc);

        manualKeyValue = new JLabel("");
        manualKeyValue.setHorizontalAlignment(SwingConstants.LEFT);
        gbc.weightx = 0.7;
        gbc.gridx = 2;
        gbc.gridy = 1;
        container.add(manualKeyValue, gbc);

        otpLabel = new JLabel(ShUpConfig.resourceBundle.getString("shanoir.uploader.otp.code"));
        otpLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        gbc.weightx = 0.2;
        gbc.gridx = 0;
        gbc.gridy = 2;
        container.add(otpLabel, gbc);

        otpText = new JTextField("");
        otpText.setPreferredSize(new Dimension(200, 20));
        gbc.weightx = 0.7;
        gbc.gridx = 2;
        gbc.gridy = 2;
        container.add(otpText, gbc);

        deviceLabelLabel = new JLabel(ShUpConfig.resourceBundle.getString("shanoir.uploader.otp.setup.device.label"));
        deviceLabelLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        gbc.weightx = 0.2;
        gbc.gridx = 0;
        gbc.gridy = 3;
        container.add(deviceLabelLabel, gbc);

        deviceLabelText = new JTextField("");
        deviceLabelText.setPreferredSize(new Dimension(200, 20));
        gbc.weightx = 0.7;
        gbc.gridx = 2;
        gbc.gridy = 3;
        container.add(deviceLabelText, gbc);

        submit = new JButton(ShUpConfig.resourceBundle.getString("shanoir.uploader.otp.submit"));
        submit.setPreferredSize(new Dimension(200, 20));
        submit.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.weightx = 0.7;
        gbc.gridx = 2;
        gbc.gridy = 4;
        container.add(submit, gbc);

        otpSetupPanelActionListener.configure(this, sSC);
        submit.addActionListener(otpSetupPanelActionListener);
    }

    /**
     * Populates the QR code image and the manual fallback key before the panel
     * is shown. Called by {@link ShUpStartupDialog#showOtpSetupForm}.
     */
    public void populate(byte[] qrCodeBytes, String totpManualKey) {
        if (qrCodeBytes != null) {
            try {
                Image img = ImageIO.read(new ByteArrayInputStream(qrCodeBytes));
                // Scale to 150×150 to fit the startup dialog
                Image scaled = img.getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                qrCodeLabel.setIcon(new ImageIcon(scaled));
                qrCodeLabel.setText(null);
            } catch (Exception e) {
                LOG.error("Could not render OTP QR code image: {}", e.getMessage(), e);
                qrCodeLabel.setText(ShUpConfig.resourceBundle.getString("shanoir.uploader.otp.setup.qr.error"));
            }
        } else {
            qrCodeLabel.setText(ShUpConfig.resourceBundle.getString("shanoir.uploader.otp.setup.qr.unavailable"));
        }
        manualKeyValue.setText(totpManualKey != null ? totpManualKey : "");
    }
}
