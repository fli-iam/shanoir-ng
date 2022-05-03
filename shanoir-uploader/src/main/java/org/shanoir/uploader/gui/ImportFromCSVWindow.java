package org.shanoir.uploader.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.io.File;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.apache.log4j.Logger;
import org.shanoir.uploader.action.ImportFromCsvActionListener;
import org.shanoir.uploader.action.UploadFromCsvActionListener;
import org.shanoir.uploader.dicom.IDicomServerClient;
import org.shanoir.uploader.model.CsvImport;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClient;

public class ImportFromCSVWindow extends JFrame {

	public JButton uploadButton;
	public JButton openButton;
	public JProgressBar progressBar;

	private static Logger logger = Logger.getLogger(ImportFromCSVWindow.class);

	public File shanoirUploaderFolder;
	public ResourceBundle resourceBundle;
	public JFrame frame;
	public JLabel error = new JLabel();
	public JLabel csvDetail = new JLabel();

	final JPanel masterPanel;

	JTable table;
	

	UploadFromCsvActionListener uploadListener;
	ImportFromCsvActionListener importListener;
	IDicomServerClient dicomServerClient;
	ShanoirUploaderServiceClient shanoirUploaderServiceClient;
	public JScrollPane scrollPaneUpload;

	public ImportFromCSVWindow(File shanoirUploaderFolder, ResourceBundle resourceBundle, JScrollPane scrollPaneUpload, IDicomServerClient dicomServerClient, ShanoirUploaderServiceClient shanoirUploaderServiceClientNG) {
		this.shanoirUploaderFolder = shanoirUploaderFolder;
		this.resourceBundle = resourceBundle;
		this.dicomServerClient = dicomServerClient;
		this.shanoirUploaderServiceClient = shanoirUploaderServiceClientNG;
		this.scrollPaneUpload = scrollPaneUpload;

		// Create the frame.
		frame = new JFrame(resourceBundle.getString("shanoir.uploader.import.csv.title"));
		frame.setSize(1600, 700);
		this.setSize(1600, 700);

		// What happens when the frame closes?
		frame.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

		// Panel content
		masterPanel = new JPanel(new GridBagLayout());
		masterPanel.setLayout(new GridBagLayout());
		masterPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		frame.setContentPane(masterPanel);

		// CSV description here
		this.csvDetail.setText(resourceBundle.getString("shanoir.uploader.import.csv.detail"));
		GridBagConstraints gBCcsvDetail = new GridBagConstraints();
		gBCcsvDetail.anchor = GridBagConstraints.NORTHWEST;
		gBCcsvDetail.gridx = 0;
		gBCcsvDetail.gridy = 0;
		masterPanel.add(this.csvDetail, gBCcsvDetail);

		// Potential error here
		GridBagConstraints gBCError = new GridBagConstraints();
		gBCError.anchor = GridBagConstraints.NORTHWEST;
		gBCError.gridx = 0;
		gBCError.gridy = 1;
		this.error.setForeground(Color.RED);
		masterPanel.add(this.error, gBCError);

		// OPEN button here
		openButton = new JButton(resourceBundle.getString("shanoir.uploader.import.csv.button.open"));
		GridBagConstraints gBCOpenButton = new GridBagConstraints();
		gBCOpenButton.anchor = GridBagConstraints.NORTHWEST;
		gBCOpenButton.gridx = 0;
		gBCOpenButton.gridy = 2;
		openButton.setEnabled(true);
		masterPanel.add(openButton, gBCOpenButton);

		uploadListener = new UploadFromCsvActionListener(this, resourceBundle);

		openButton.addActionListener(uploadListener);

		// CSV display here
		//headers for the table
		String[] columns = new String[] {
				resourceBundle.getString("shanoir.uploader.import.csv.column.ipp"),
				resourceBundle.getString("shanoir.uploader.import.csv.column.name"),
				resourceBundle.getString("shanoir.uploader.import.csv.column.surname"),
				resourceBundle.getString("shanoir.uploader.import.csv.column.study.id"),
				resourceBundle.getString("shanoir.uploader.import.csv.column.studycard"),
				resourceBundle.getString("shanoir.uploader.import.csv.column.common.name"),
				resourceBundle.getString("shanoir.uploader.import.csv.column.study.filter"),
				resourceBundle.getString("shanoir.uploader.import.csv.column.acquisition.filter"),
				resourceBundle.getString("shanoir.uploader.import.csv.column.min.date"),
				resourceBundle.getString("shanoir.uploader.import.csv.column.comment"),
				resourceBundle.getString("shanoir.uploader.import.csv.column.error")
		};
		// Create table with data
		table = new JTable();
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		model.setColumnIdentifiers(columns);
		// 1100 to go
		table.getColumnModel().getColumn(0).setMinWidth(30);
		table.getColumnModel().getColumn(1).setMinWidth(90);
		table.getColumnModel().getColumn(2).setMinWidth(90);
		table.getColumnModel().getColumn(3).setMinWidth(5);
		table.getColumnModel().getColumn(4).setMinWidth(90);
		table.getColumnModel().getColumn(5).setMinWidth(100);
		table.getColumnModel().getColumn(6).setMinWidth(90);
		table.getColumnModel().getColumn(7).setMinWidth(90);
		table.getColumnModel().getColumn(8).setMinWidth(30);
		table.getColumnModel().getColumn(9).setMinWidth(90);
		table.getColumnModel().getColumn(10).setMinWidth(350);

		// Add the table to the frame
		JPanel tablePanel = new JPanel(new BorderLayout());
		GridBagConstraints gBCTableanchor = new GridBagConstraints();
		gBCTableanchor.anchor = GridBagConstraints.NORTHWEST;
		gBCTableanchor.gridx = 0;
		gBCTableanchor.gridy = 3;
		tablePanel.setSize(1500, 500);
		masterPanel.add(tablePanel , gBCTableanchor);

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setSize(1500, 500);
		scrollPane.setPreferredSize(new Dimension(1500, 500));
		table.getParent().setVisible(false);
		table.setSize(1500, 500);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tablePanel.add(scrollPane);

		// IMPORT button here when necessary
		uploadButton = new JButton(resourceBundle.getString("shanoir.uploader.import.csv.button.import"));
		GridBagConstraints gBCuploadButton = new GridBagConstraints();
		gBCuploadButton.anchor = GridBagConstraints.NORTHWEST;
		gBCuploadButton.gridx = 0;
		gBCuploadButton.gridy = 4;
		uploadButton.setEnabled(false);
		masterPanel.add(uploadButton, gBCuploadButton);
		
		progressBar = new JProgressBar(0);
		GridBagConstraints gBCProgressBar = new GridBagConstraints();
		gBCProgressBar.anchor = GridBagConstraints.NORTHWEST;
		gBCProgressBar.gridx = 0;
		gBCProgressBar.gridy = 5;
		progressBar.setVisible(false);
		masterPanel.add(progressBar, gBCProgressBar);
	
		importListener = new ImportFromCsvActionListener(this, resourceBundle, dicomServerClient, shanoirUploaderFolder, shanoirUploaderServiceClientNG);

		uploadButton.addActionListener(importListener);

		// center the frame
		// frame.setLocationRelativeTo( null );
		Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
		int windowWidth = 1600;
		int windowHeight = 700;
		// set position and size
		frame.setBounds(center.x - windowWidth / 2, center.y - windowHeight / 2, windowWidth, windowHeight);

		// Show it.
		frame.setVisible(true);
	}

	/**
	 * Displays an error in the  located error field
	 * @param string the generated error to display
	 */
	public void displayError(String string) {
		this.error.setText(string);
		this.error.setVisible(true);
	}

	public void displayCsv(List<CsvImport> imports) {
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		model.getDataVector().removeAllElements();

		boolean inError = false;
		for (CsvImport importRaw : imports) {
			model.addRow(importRaw.getRawData());
			if (importRaw.getErrorMessage() != null) {
				inError= true;
				this.error.setText(resourceBundle.getString("shanoir.uploader.import.csv.error.after.import"));
			}
		}
		this.error.setVisible(inError);

		model.fireTableDataChanged();
		table.getParent().setVisible(true);
		this.importListener.setCsvImports(imports);

		uploadButton.setEnabled(!inError);
	}
}
