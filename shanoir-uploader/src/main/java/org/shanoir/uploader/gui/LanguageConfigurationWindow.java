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
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.io.File;
import java.util.ResourceBundle;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.shanoir.uploader.action.LanguageConfigurationListener;

public class LanguageConfigurationWindow extends JFrame {

    private static final Logger LOG = LoggerFactory.getLogger(LanguageConfigurationWindow.class);
    public File shanoirUploaderFolder;
    public ResourceBundle resourceBundle;
    public JRadioButton rbEnglish;
    public JRadioButton rbFrench;

    LanguageConfigurationWindow(File shanoirUploaderFolder, ResourceBundle resourceBundle) {
        this.shanoirUploaderFolder = shanoirUploaderFolder;
        this.resourceBundle = resourceBundle;

        // Create the frame.
        JFrame frame = new JFrame(resourceBundle.getString("shanoir.uploader.configurationMenu.language.title"));

        // What happens when the frame closes?
        frame.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        // Panel content
        JPanel masterPanel = new JPanel(new BorderLayout());
        frame.setContentPane(masterPanel);

        final JPanel configurationPanel = new JPanel();
        // configurationPanel.setBorder(BorderFactory.createLineBorder(Color.black));

        masterPanel.add(configurationPanel, BorderLayout.NORTH);

        GridBagLayout gBLPanel = new GridBagLayout();
        gBLPanel.columnWidths = new int[] {0, 0, 0};
        gBLPanel.rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0};
        gBLPanel.columnWeights = new double[] {1.0, 1.0, Double.MIN_VALUE};
        gBLPanel.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        configurationPanel.setLayout(gBLPanel);

        JLabel configurationLabel = new JLabel(
                resourceBundle.getString("shanoir.uploader.configurationMenu.language.label"));
        Font newLabelFont = new Font(configurationLabel.getFont().getName(), Font.BOLD,
                configurationLabel.getFont().getSize());
        configurationLabel.setFont(newLabelFont);
        addItem(configurationPanel, configurationLabel, 0, 0, 3, GridBagConstraints.WEST);

        rbEnglish = new JRadioButton(
                resourceBundle.getString("shanoir.uploader.configurationMenu.language.radioButton.english"));
        rbFrench = new JRadioButton(
                resourceBundle.getString("shanoir.uploader.configurationMenu.language.radioButton.french"));
        ButtonGroup bg1 = new ButtonGroup();
        bg1.add(rbEnglish);
        bg1.add(rbFrench);
        addItem(configurationPanel, rbEnglish, 0, 1, 1, GridBagConstraints.CENTER);
        addItem(configurationPanel, rbFrench, 0, 2, 1, GridBagConstraints.CENTER);

        JButton configureButton = new JButton(
                resourceBundle.getString("shanoir.uploader.configurationMenu.language.configureButton"));
        addItem(configurationPanel, configureButton, 0, 3, 1, GridBagConstraints.CENTER);

        LanguageConfigurationListener lCL = new LanguageConfigurationListener(this);
        configureButton.addActionListener(lCL);

        // Size the frame.
        frame.pack();

        // center the frame
        // frame.setLocationRelativeTo(null );
        Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        int windowWidth = 300;
        int windowHeight = 200;
        // set position and size
        frame.setBounds(center.x - windowWidth / 2, center.y - windowHeight / 2, windowWidth, windowHeight);

        // Show it.
        frame.setVisible(true);
    }

    private void addItem(JPanel p, JComponent c, int x, int y, int width, int align) {

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = x;
        gc.gridy = y;
        gc.gridwidth = width;
        gc.anchor = align;
        gc.insets = new Insets(10, 10, 10, 10);
        p.add(c, gc);
    }

}
