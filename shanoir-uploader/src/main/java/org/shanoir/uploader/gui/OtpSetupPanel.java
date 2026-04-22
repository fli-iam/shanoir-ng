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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
 * not yet configured on the user account).
 *
 * <p>By default the QR code is shown. A toggle link below it ("Unable to scan?")
 * switches to a read-only text field containing the base32 manual key; clicking
 * the link again ("Scan QR code") restores the QR code view.
 */
@Component
public class OtpSetupPanel extends JPanel {

    private static final Logger LOG = LoggerFactory.getLogger(OtpSetupPanel.class);

    public JLabel qrCodeLabel;
    public JTextField manualKeyValue;
    public JLabel toggleLink;
    public JLabel otpLabel;
    public JTextField otpText;
    public JLabel deviceLabelLabel;
    public JTextField deviceLabelText;
    public JButton submit;

    private boolean showingQr = true;

    @Autowired
    private OtpSetupPanelActionListener otpSetupPanelActionListener;

    public void configure(StartupStateContext sSC) {
        Container container = new Container();
        container.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        this.add(container);
        this.setBorder(new EmptyBorder(5, 5, 5, 5));

        // Row 0 — QR code (visible by default)
        qrCodeLabel = new JLabel();
        qrCodeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        container.add(qrCodeLabel, gbc);

        // Row 1 — manual key field (hidden by default)
        manualKeyValue = new JTextField("");
        manualKeyValue.setPreferredSize(new Dimension(200, 20));
        manualKeyValue.setEditable(false);
        manualKeyValue.setVisible(false);
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        container.add(manualKeyValue, gbc);

        // Row 2 — toggle link
        toggleLink = new JLabel(linkText(true));
        toggleLink.setHorizontalAlignment(SwingConstants.CENTER);
        toggleLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggleLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showingQr = !showingQr;
                qrCodeLabel.setVisible(showingQr);
                manualKeyValue.setVisible(!showingQr);
                toggleLink.setText(linkText(showingQr));
                container.revalidate();
                container.repaint();
            }
        });
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        container.add(toggleLink, gbc);

        gbc.gridwidth = 1;

        // Row 3 — OTP code
        otpLabel = new JLabel(ShUpConfig.resourceBundle.getString("shanoir.uploader.otp.code"));
        otpLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        gbc.weightx = 0.2;
        gbc.gridx = 0;
        gbc.gridy = 3;
        container.add(otpLabel, gbc);

        otpText = new JTextField("");
        otpText.setPreferredSize(new Dimension(200, 20));
        gbc.weightx = 0.7;
        gbc.gridx = 2;
        gbc.gridy = 3;
        container.add(otpText, gbc);

        // Row 4 — device label
        deviceLabelLabel = new JLabel(ShUpConfig.resourceBundle.getString("shanoir.uploader.otp.setup.device.label"));
        deviceLabelLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        gbc.weightx = 0.2;
        gbc.gridx = 0;
        gbc.gridy = 4;
        container.add(deviceLabelLabel, gbc);

        deviceLabelText = new JTextField("");
        deviceLabelText.setPreferredSize(new Dimension(200, 20));
        gbc.weightx = 0.7;
        gbc.gridx = 2;
        gbc.gridy = 4;
        container.add(deviceLabelText, gbc);

        // Row 5 — submit
        submit = new JButton(ShUpConfig.resourceBundle.getString("shanoir.uploader.otp.submit"));
        submit.setPreferredSize(new Dimension(200, 20));
        submit.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.weightx = 0.7;
        gbc.gridx = 2;
        gbc.gridy = 5;
        container.add(submit, gbc);

        otpSetupPanelActionListener.configure(this, sSC);
        submit.addActionListener(otpSetupPanelActionListener);
    }

    /**
     * Populates the QR code and manual key, and resets the view to QR mode.
     * Called by {@link ShUpStartupDialog#showOtpSetupForm} before displaying the panel.
     */
    public void populate(byte[] qrCodeBytes, String totpManualKey) {
        // Reset to QR view each time the panel is freshly shown
        showingQr = true;
        if (qrCodeLabel != null) {
            qrCodeLabel.setVisible(true);
        }
        if (manualKeyValue != null) {
            manualKeyValue.setVisible(false);
        }
        if (toggleLink != null) {
            toggleLink.setText(linkText(true));
        }

        if (qrCodeBytes != null) {
            try {
                Image img = ImageIO.read(new ByteArrayInputStream(qrCodeBytes));
                Image scaled = img.getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                qrCodeLabel.setIcon(new ImageIcon(scaled));
                qrCodeLabel.setText(null);
            } catch (Exception e) {
                LOG.error("Could not render OTP QR code image: {}", e.getMessage(), e);
                qrCodeLabel.setIcon(null);
                qrCodeLabel.setText(ShUpConfig.resourceBundle.getString("shanoir.uploader.otp.setup.qr.error"));
            }
        } else {
            qrCodeLabel.setIcon(null);
            qrCodeLabel.setText(ShUpConfig.resourceBundle.getString("shanoir.uploader.otp.setup.qr.unavailable"));
        }

        if (manualKeyValue != null) {
            manualKeyValue.setText(totpManualKey != null ? totpManualKey : "");
        }
    }

    private static String linkText(boolean showingQr) {
        String key = showingQr
            ? "shanoir.uploader.otp.setup.unable.to.scan"
            : "shanoir.uploader.otp.setup.scan.qr";
        return "<html><a href='#'>" + ShUpConfig.resourceBundle.getString(key) + "</a></html>";
    }
}
