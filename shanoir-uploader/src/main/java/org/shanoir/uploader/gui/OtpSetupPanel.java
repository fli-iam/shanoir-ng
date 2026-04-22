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
import org.shanoir.uploader.action.init.OtpCancelActionListener;
import org.shanoir.uploader.action.init.OtpSetupSubmitActionListener;
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

    public JLabel instructionsLabel;
    public JLabel scanOrEnterLabel;
    public JLabel qrCodeLabel;
    public JTextField manualKeyValue;
    public JLabel toggleLink;
    public JLabel setupHintLabel;
    public JLabel otpLabel;
    public JTextField otpText;
    public JLabel deviceLabelLabel;
    public JTextField deviceLabelText;
    public JButton submit;

    private boolean showingQr = true;

    @Autowired
    private OtpCancelActionListener otpCancelActionListener;

    @Autowired
    private OtpSetupSubmitActionListener otpSetupSubmitActionListener;

    public void configure(StartupStateContext sSC) {
        this.removeAll();
        this.revalidate();
        this.repaint();

        Container container = new Container();
        container.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        this.add(container);
        this.setBorder(new EmptyBorder(5, 5, 5, 5));

        // Row 0 — instructions
        instructionsLabel = new JLabel(instructionsHtml());
        instructionsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        container.add(instructionsLabel, gbc);

        // Row 1 — contextual label (changes with toggle)
        scanOrEnterLabel = new JLabel(scanOrEnterText(true));
        scanOrEnterLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        container.add(scanOrEnterLabel, gbc);

        // Row 2 — QR code (visible by default)
        qrCodeLabel = new JLabel();
        qrCodeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        container.add(qrCodeLabel, gbc);

        // Row 3 — manual key field (hidden by default)
        manualKeyValue = new JTextField("");
        manualKeyValue.setPreferredSize(new Dimension(200, 20));
        manualKeyValue.setEditable(false);
        manualKeyValue.setVisible(false);
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        container.add(manualKeyValue, gbc);

        // Row 4 — toggle link
        toggleLink = new JLabel(linkText(true));
        toggleLink.setHorizontalAlignment(SwingConstants.CENTER);
        toggleLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggleLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showingQr = !showingQr;
                scanOrEnterLabel.setText(scanOrEnterText(showingQr));
                qrCodeLabel.setVisible(showingQr);
                manualKeyValue.setVisible(!showingQr);
                toggleLink.setText(linkText(showingQr));
                container.revalidate();
                container.repaint();
            }
        });
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        container.add(toggleLink, gbc);

        // Row 5 — setup hint
        setupHintLabel = new JLabel(setupHintHtml());
        setupHintLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 3;
        container.add(setupHintLabel, gbc);

        gbc.gridwidth = 1;

        // Row 6 — OTP code
        otpLabel = new JLabel(ShUpConfig.resourceBundle.getString("shanoir.uploader.otp.code"));
        otpLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        gbc.weightx = 0.2;
        gbc.gridx = 0;
        gbc.gridy = 6;
        container.add(otpLabel, gbc);

        otpText = new JTextField("");
        otpText.setPreferredSize(new Dimension(200, 20));
        gbc.weightx = 0.7;
        gbc.gridx = 2;
        gbc.gridy = 6;
        container.add(otpText, gbc);

        // Row 7 — device label
        deviceLabelLabel = new JLabel(ShUpConfig.resourceBundle.getString("shanoir.uploader.otp.setup.device.label"));
        deviceLabelLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        gbc.weightx = 0.2;
        gbc.gridx = 0;
        gbc.gridy = 7;
        container.add(deviceLabelLabel, gbc);

        deviceLabelText = new JTextField("");
        deviceLabelText.setPreferredSize(new Dimension(200, 20));
        gbc.weightx = 0.7;
        gbc.gridx = 2;
        gbc.gridy = 7;
        container.add(deviceLabelText, gbc);

        // Row 8 — cancel and submit
        JPanel buttonPanel = new JPanel(); // FlowLayout by default

        JButton cancel = new JButton(ShUpConfig.resourceBundle.getString("shanoir.uploader.otp.cancel"));
        cancel.setPreferredSize(new Dimension(100, 20));
        cancel.setHorizontalAlignment(SwingConstants.CENTER);
        otpCancelActionListener.configure(sSC);
        cancel.addActionListener(otpCancelActionListener);

        submit = new JButton(ShUpConfig.resourceBundle.getString("shanoir.uploader.otp.submit"));
        submit.setPreferredSize(new Dimension(100, 20));
        submit.setHorizontalAlignment(SwingConstants.CENTER);
        otpSetupSubmitActionListener.configure(this, sSC);
        submit.addActionListener(otpSetupSubmitActionListener);

        buttonPanel.add(cancel);
        buttonPanel.add(submit);

        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 3;

        container.add(buttonPanel, gbc);
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
        if (scanOrEnterLabel != null) {
            scanOrEnterLabel.setText(scanOrEnterText(true));
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

    private static String scanOrEnterText(boolean showingQr) {
        String key = showingQr
            ? "shanoir.uploader.otp.setup.open.scan"
            : "shanoir.uploader.otp.setup.open.enter";
        return ShUpConfig.resourceBundle.getString(key);
    }

    private static String linkText(boolean showingQr) {
        String key = showingQr
            ? "shanoir.uploader.otp.setup.unable.to.scan"
            : "shanoir.uploader.otp.setup.scan.qr";
        return "<html><a href='#'>" + ShUpConfig.resourceBundle.getString(key) + "</a></html>";
    }

    private static String setupHintHtml() {
        return "<html><div style='text-align:center'>"
            + ShUpConfig.resourceBundle.getString("shanoir.uploader.otp.setup.hint.enter.code") + "<br/><br/>"
            + ShUpConfig.resourceBundle.getString("shanoir.uploader.otp.setup.hint.device.name")
            + "</div></html>";
    }

    private static String instructionsHtml() {
        return "<html><div style='text-align:center'>"
            + "<h2>" + ShUpConfig.resourceBundle.getString("shanoir.uploader.otp.setup.title") + "</h2>"
            + ShUpConfig.resourceBundle.getString("shanoir.uploader.otp.setup.description") + "<br/>"
            + ShUpConfig.resourceBundle.getString("shanoir.uploader.otp.setup.install") + "<br/><br/>"
            + ShUpConfig.resourceBundle.getString("shanoir.uploader.otp.setup.app.microsoft") + "</li><br/>"
            + ShUpConfig.resourceBundle.getString("shanoir.uploader.otp.setup.app.google") + "</li><br/>"
            + ShUpConfig.resourceBundle.getString("shanoir.uploader.otp.setup.app.freeotp") + "</li><br/>"
            + "</div></html>";
    }
}
