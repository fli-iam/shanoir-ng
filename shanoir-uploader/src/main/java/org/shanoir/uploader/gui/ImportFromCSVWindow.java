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
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.apache.log4j.Logger;
import org.shanoir.uploader.action.ImportFromCsvActionListener;
import org.shanoir.uploader.action.UploadFromCsvActionListener;
import org.shanoir.uploader.dicom.IDicomServerClient;
import org.shanoir.uploader.model.CsvImport;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClientNG;

public class ImportFromCSVWindow extends JFrame {

	public JButton uploadButton;
	public JButton openButton;

	private static Logger logger = Logger.getLogger(ImportFromCSVWindow.class);

	public File shanoirUploaderFolder;
	public ResourceBundle resourceBundle;
	public JFrame frame;
	public JLabel error = new JLabel();
	public JLabel csvDetail = new JLabel();
	JButton importButton;
	
	final JPanel masterPanel;
	
	JTable table;
	
	UploadFromCsvActionListener uploadListener;
	ImportFromCsvActionListener importListener;
	IDicomServerClient dicomServerClient;
	ShanoirUploaderServiceClientNG shanoirUploaderServiceClientNG;
	public JScrollPane scrollPaneUpload;

	public ImportFromCSVWindow(File shanoirUploaderFolder, ResourceBundle resourceBundle, JScrollPane scrollPaneUpload, IDicomServerClient dicomServerClient, ShanoirUploaderServiceClientNG shanoirUploaderServiceClientNG) {
		this.shanoirUploaderFolder = shanoirUploaderFolder;
		this.resourceBundle = resourceBundle;
		this.dicomServerClient = dicomServerClient;
		this.shanoirUploaderServiceClientNG = shanoirUploaderServiceClientNG;
		this.scrollPaneUpload = scrollPaneUpload;

		// Create the frame.
		frame = new JFrame(resourceBundle.getString("shanoir.uploader.import.csv.title"));
		frame.setSize(1200, 600);
		this.setSize(1200, 600);

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
		masterPanel.add(this.error, gBCError);
		
		// OPEN button here
		openButton = new JButton("Open");
		GridBagConstraints gBCOpenButton = new GridBagConstraints();
		gBCOpenButton.anchor = GridBagConstraints.NORTHWEST;
		gBCOpenButton.gridx = 0;
		gBCOpenButton.gridy = 2;
		openButton.setEnabled(true);
		masterPanel.add(openButton, gBCOpenButton);
		
		uploadListener = new UploadFromCsvActionListener(this);
		
		openButton.addActionListener(uploadListener);
		
		// CSV display here
        //headers for the table
        String[] columns = new String[] {
            "Name", "SurName", "Study Id", "StudyCard name", "Common name", "Sex", "Birth date", "Comment", "Error"
        };
        // Create table with data
        table = new JTable();
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setColumnIdentifiers(columns);
        // 1100 to go
        table.getColumnModel().getColumn(0).setMinWidth(100);
        table.getColumnModel().getColumn(1).setMinWidth(100);
        table.getColumnModel().getColumn(2).setMinWidth(5);
        table.getColumnModel().getColumn(3).setMinWidth(100);
        table.getColumnModel().getColumn(4).setMinWidth(100);
        table.getColumnModel().getColumn(5).setMinWidth(5);
        table.getColumnModel().getColumn(6).setMinWidth(90);
        table.getColumnModel().getColumn(7).setMinWidth(100);
        table.getColumnModel().getColumn(8).setMinWidth(350);
        
        // Add the table to the frame
		JPanel tablePanel = new JPanel(new BorderLayout());
		GridBagConstraints gBCTableanchor = new GridBagConstraints();
		gBCTableanchor.anchor = GridBagConstraints.NORTHWEST;
		gBCTableanchor.gridx = 0;
		gBCTableanchor.gridy = 3;
		tablePanel.setSize(1100, 500);
		masterPanel.add(tablePanel , gBCTableanchor);

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setSize(1100, 500);
		scrollPane.setPreferredSize(new Dimension(1100, 500));
        table.getParent().setVisible(false);
        table.setSize(1100, 500);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tablePanel.add(scrollPane);
		
		// IMPORT button here when necessary
        importButton = new JButton("Import");
		GridBagConstraints gBCImportButton = new GridBagConstraints();
		gBCImportButton.anchor = GridBagConstraints.NORTHWEST;
		gBCImportButton.gridx = 0;
		gBCImportButton.gridy = 4;
		importButton.setEnabled(false);
		masterPanel.add(importButton, gBCImportButton);

		importListener = new ImportFromCsvActionListener(this, dicomServerClient, shanoirUploaderFolder, shanoirUploaderServiceClientNG);
		
		importButton.addActionListener(importListener);

		// center the frame
		// frame.setLocationRelativeTo( null );
		Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
		int windowWidth = 1200;
		int windowHeight = 600;
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
				this.error.setText("An error in the CSV keeps you from uploading data, please correct CSV input.");
				this.error.setVisible(true);
			}
		}

		model.fireTableDataChanged();
		table.getParent().setVisible(true);
		this.importListener.setCsvImports(imports);
		
		importButton.setEnabled(!inError);
	}
}
