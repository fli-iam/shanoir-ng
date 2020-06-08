package org.shanoir.uploader.gui;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.BorderLayout;
import java.awt.Insets;
import java.io.File;
import java.text.NumberFormat;
import java.util.ResourceBundle;
import org.apache.log4j.Logger;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.text.NumberFormatter;

import org.shanoir.downloader.ShanoirDownloader;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClientNG;


public class DownloaderPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    public DownloaderPanel(JFrame frame, GridBagLayout gBLPanel, ResourceBundle resourceBundle, Logger logger) {
        super(false);
        this.setLayout(gBLPanel);

		NumberFormat format = NumberFormat.getInstance();
		NumberFormatter formatter = new NumberFormatter(format);
		formatter.setValueClass(Long.class);
		formatter.setMinimum(Long.MIN_VALUE);
		formatter.setMaximum(Long.MAX_VALUE);
		formatter.setAllowsInvalid(true);

		int posY = 0;
		JLabel downloadDatasetIDLabel = new JLabel(resourceBundle.getString("shanoir.uploader.datasetIDLabel"));
		downloadDatasetIDLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_downloadDatasetIDLabel = new GridBagConstraints();
		gbc_downloadDatasetIDLabel.anchor = GridBagConstraints.EAST;
		gbc_downloadDatasetIDLabel.insets = new Insets(10, 10, 10, 10);
		gbc_downloadDatasetIDLabel.gridx = 0;
		gbc_downloadDatasetIDLabel.gridy = posY;
		this.add(downloadDatasetIDLabel, gbc_downloadDatasetIDLabel);

		JFormattedTextField downloadDatasetIDTF = new JFormattedTextField(formatter);
		downloadDatasetIDTF.setFocusLostBehavior(javax.swing.JFormattedTextField.COMMIT);
		GridBagConstraints gbc_downloadDatasetIDTF = new GridBagConstraints();
		gbc_downloadDatasetIDTF.insets = new Insets(10, 10, 10, 10);
		gbc_downloadDatasetIDTF.fill = GridBagConstraints.HORIZONTAL;
		gbc_downloadDatasetIDTF.gridx = 1;
		gbc_downloadDatasetIDTF.gridy = posY++;
		this.add(downloadDatasetIDTF, gbc_downloadDatasetIDTF);
		downloadDatasetIDTF.setColumns(15);
		downloadDatasetIDTF.setText("");

		JLabel downloadSubjectIDLabel = new JLabel(resourceBundle.getString("shanoir.uploader.subjectIDLabel"));
		downloadSubjectIDLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_downloadSubjectIDLabel = new GridBagConstraints();
		gbc_downloadSubjectIDLabel.anchor = GridBagConstraints.EAST;
		gbc_downloadSubjectIDLabel.insets = new Insets(10, 10, 10, 10);
		gbc_downloadSubjectIDLabel.gridx = 0;
		gbc_downloadSubjectIDLabel.gridy = posY;
		this.add(downloadSubjectIDLabel, gbc_downloadSubjectIDLabel);

		JFormattedTextField downloadSubjectIDTF = new JFormattedTextField(formatter);
		downloadSubjectIDTF.setFocusLostBehavior(javax.swing.JFormattedTextField.COMMIT);
		GridBagConstraints gbc_downloadSubjectIDTF = new GridBagConstraints();
		gbc_downloadSubjectIDTF.insets = new Insets(10, 10, 10, 10);
		gbc_downloadSubjectIDTF.fill = GridBagConstraints.HORIZONTAL;
		gbc_downloadSubjectIDTF.gridx = 1;
		gbc_downloadSubjectIDTF.gridy = posY++;
		this.add(downloadSubjectIDTF, gbc_downloadSubjectIDTF);
		downloadSubjectIDTF.setColumns(15);
		downloadSubjectIDTF.setText("");

		JLabel downloadStudyIDLabel = new JLabel(resourceBundle.getString("shanoir.uploader.studyIDLabel"));
		downloadStudyIDLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_downloadStudyIDLabel = new GridBagConstraints();
		gbc_downloadStudyIDLabel.anchor = GridBagConstraints.EAST;
		gbc_downloadStudyIDLabel.insets = new Insets(10, 10, 10, 10);
		gbc_downloadStudyIDLabel.gridx = 0;
		gbc_downloadStudyIDLabel.gridy = posY;
		this.add(downloadStudyIDLabel, gbc_downloadStudyIDLabel);

		JFormattedTextField downloadStudyIDTF = new JFormattedTextField(formatter);
		downloadStudyIDTF.setFocusLostBehavior(javax.swing.JFormattedTextField.COMMIT);
		GridBagConstraints gbc_downloadStudyIDTF = new GridBagConstraints();
		gbc_downloadStudyIDTF.insets = new Insets(10, 10, 10, 10);
		gbc_downloadStudyIDTF.fill = GridBagConstraints.HORIZONTAL;
		gbc_downloadStudyIDTF.gridx = 1;
		gbc_downloadStudyIDTF.gridy = posY++;
		this.add(downloadStudyIDTF, gbc_downloadStudyIDTF);
		downloadStudyIDTF.setColumns(15);
		downloadStudyIDTF.setText("");

		JLabel formatLabel = new JLabel(resourceBundle.getString("shanoir.uploader.formatLabel"));
		formatLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gBCFormatLabel = new GridBagConstraints();
		gBCFormatLabel.anchor = GridBagConstraints.EAST;
		gBCFormatLabel.insets = new Insets(10, 10, 10, 10);
		gBCFormatLabel.gridx = 0;
		gBCFormatLabel.gridy = posY;
		this.add(formatLabel, gBCFormatLabel);

		ButtonGroup formatRG = new ButtonGroup();

		JRadioButton dicomformatR = new JRadioButton("Dicom");
		formatRG.add(dicomformatR);
		this.add(dicomformatR);

		JRadioButton niftiformatR = new JRadioButton("Nifti");
		formatRG.add(niftiformatR);
		this.add(niftiformatR);

		GridBagConstraints gBCFormatTF = new GridBagConstraints();
		gBCFormatTF.insets = new Insets(10, 10, 10, 10);
		gBCFormatTF.fill = GridBagConstraints.HORIZONTAL;
		gBCFormatTF.anchor = GridBagConstraints.WEST;
		gBCFormatTF.gridx = 1;
		gBCFormatTF.gridy = posY++;
		this.add(dicomformatR, gBCFormatTF);

		GridBagConstraints gBCFormatTF2 = new GridBagConstraints();
		gBCFormatTF2.insets = new Insets(10, 10, 10, 10);
		gBCFormatTF2.fill = GridBagConstraints.HORIZONTAL;
		gBCFormatTF2.anchor = GridBagConstraints.EAST;
		gBCFormatTF2.gridx = 1;
		gBCFormatTF2.gridy = posY++;
		this.add(niftiformatR, gBCFormatTF2);

		JLabel outputDirectoryLabel = new JLabel(resourceBundle.getString("shanoir.uploader.outputDirectoryLabel"));
		outputDirectoryLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gBCBOutputDirectoryLabel = new GridBagConstraints();
		gBCBOutputDirectoryLabel.anchor = GridBagConstraints.EAST;
		gBCBOutputDirectoryLabel.insets = new Insets(10, 10, 10, 10);
		gBCBOutputDirectoryLabel.gridx = 0;
		gBCBOutputDirectoryLabel.gridy = posY;
		this.add(outputDirectoryLabel, gBCBOutputDirectoryLabel);

		JTextField outputDirectoryTF = new JTextField();
		GridBagConstraints gBCOutputDirectoryTF = new GridBagConstraints();
		gBCOutputDirectoryTF.insets = new Insets(10, 10, 10, 10);
		gBCOutputDirectoryTF.fill = GridBagConstraints.HORIZONTAL;
		gBCOutputDirectoryTF.gridx = 1;
		gBCOutputDirectoryTF.gridy = posY;
		// gBCOutputDirectoryTF.gridwidth = 1;
		this.add(outputDirectoryTF, gBCOutputDirectoryTF);
		outputDirectoryTF.setColumns(15);
		outputDirectoryTF.setText(System.getProperty("user.home"));

		JButton outputDirectoryButton;
		outputDirectoryButton = new JButton("...");
		GridBagConstraints gbc_outputDirectoryButton = new GridBagConstraints();
		gbc_outputDirectoryButton.insets = new Insets(10, 10, 10, 10);
		gbc_outputDirectoryButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_outputDirectoryButton.anchor = GridBagConstraints.EAST;
		gbc_outputDirectoryButton.gridx = 2;
		gbc_outputDirectoryButton.gridy = posY++;
		this.add(outputDirectoryButton, gbc_outputDirectoryButton);
		
		outputDirectoryButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {

				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setDialogTitle(resourceBundle.getString("shanoir.uploader.chooseDirectory"));   
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

				int userSelection = fileChooser.showSaveDialog(frame);
				
				if (userSelection == JFileChooser.APPROVE_OPTION) {

					File outputDirectory = fileChooser.getSelectedFile();
					outputDirectoryTF.setText(outputDirectory.getPath());
				}
				
			}
		});

		JButton downloadButton = new JButton(resourceBundle.getString("shanoir.uploader.downloadButton"));
		GridBagConstraints gbc_downloadButton = new GridBagConstraints();
		gbc_downloadButton.insets = new Insets(10, 10, 10, 10);
		gbc_downloadButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_downloadButton.anchor = GridBagConstraints.EAST;
		gbc_downloadButton.gridx = 1;
		gbc_downloadButton.gridy = posY++;
		this.add(downloadButton, gbc_downloadButton);

		downloadButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {

				JDialog progressDialog = new JDialog(frame, resourceBundle.getString("shanoir.uploader.downloading"), true);
				
				Thread downloadThread = new Thread(() -> {

					try {

						String format = dicomformatR.isSelected() ? "dcm" : "nii";
						
						Long datasetId = null;
						Long studyId = null;
						Long subjectId = null;

						if (!downloadDatasetIDTF.getText().isEmpty()) {
							datasetId = Long.parseLong(downloadDatasetIDTF.getText());
						}
						if (!downloadStudyIDTF.getText().isEmpty()) {
							studyId = Long.parseLong(downloadStudyIDTF.getText());
						}
						if (!downloadSubjectIDTF.getText().isEmpty()) {
							subjectId = Long.parseLong(downloadSubjectIDTF.getText());
						}

						if(datasetId == null && studyId == null && subjectId == null) {
							String message = resourceBundle.getString("shanoir.uploader.invalidId");
							throw new Exception(message);
						}

						// File outputDirectory = new File(outputDirectoryTF.getText());
						File outputDirectory = new File(System.getProperty("user.home"));

						ShanoirUploaderServiceClientNG shng = ShUpOnloadConfig.getShanoirUploaderServiceClientNG();
						
						String message = "";

						if (datasetId != null) {
							message = ShanoirDownloader.downloadDataset(outputDirectory, datasetId, format, shng);
						}
						else {

							if (studyId != null && subjectId == null) {
								message = ShanoirDownloader.downloadDatasetByStudy(outputDirectory, studyId, format, shng);
							}
	
							if (studyId == null && subjectId != null) {
								message = ShanoirDownloader.downloadDatasetBySubject(outputDirectory, subjectId, format, shng);
							}
	
							if (studyId != null && subjectId != null) {
								message = ShanoirDownloader.downloadDatasetBySubjectIdStudyId(outputDirectory, subjectId, studyId, format, shng);
							}
							
						}
						
						progressDialog.dispose();

						if(message == "") {
							message = resourceBundle.getString("shanoir.uploader.downloadComplete");
						}
						JOptionPane.showMessageDialog(frame, message, "Info", JOptionPane.INFORMATION_MESSAGE);

					} catch (Exception e) {
						
						progressDialog.dispose();

						logger.error(e.getMessage());
						String message = resourceBundle.getString("shanoir.uploader.downloadError") + " \n\n" + e.getMessage();
						JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
					}
				});
			
				ImageIcon downloadIcon = new ImageIcon(getClass().getClassLoader().getResource("images/spinner.gif"));
				JLabel downloadAnimationLabel = new JLabel(downloadIcon, SwingConstants.CENTER);
				downloadIcon.setImageObserver(downloadAnimationLabel);
				
			    progressDialog.add(BorderLayout.CENTER, downloadAnimationLabel);
			    progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			    progressDialog.setSize(300, 75);
				progressDialog.setLocationRelativeTo(frame);
				
				downloadThread.start();
				progressDialog.setVisible(true);

			}
		});

	}
}
