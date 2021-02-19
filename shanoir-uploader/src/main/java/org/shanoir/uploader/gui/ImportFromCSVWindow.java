package org.shanoir.uploader.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Point;
import java.io.File;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

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
import org.shanoir.uploader.model.CsvImport;

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
	
	final JPanel importPanel;
	
	JTable table;
	
	UploadFromCsvActionListener uploadListener;
	ImportFromCsvActionListener importListener;

	public ImportFromCSVWindow(File shanoirUploaderFolder, ResourceBundle resourceBundle) {
		this.shanoirUploaderFolder = shanoirUploaderFolder;
		this.resourceBundle = resourceBundle;

		// Create the frame.
		frame = new JFrame(resourceBundle.getString("shanoir.uploader.import.csv.title"));

		// What happens when the frame closes?
		frame.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

		// Panel content
		JPanel masterPanel = new JPanel(new BorderLayout());
		frame.setContentPane(masterPanel);

		importPanel = new JPanel();
		importPanel.setBorder(BorderFactory.createLineBorder(Color.black));

		masterPanel.add(importPanel, BorderLayout.NORTH);
		
		// CSV description here
		this.csvDetail.setText(resourceBundle.getString("shanoir.uploader.import.csv.detail"));
		importPanel.add(this.csvDetail);
		
		// OPEN button here
		openButton = new JButton("Open");
		GridBagConstraints gBCOpenButton = new GridBagConstraints();
		gBCOpenButton.anchor = GridBagConstraints.EAST;
		gBCOpenButton.insets = new Insets(10, 10, 10, 10);
		gBCOpenButton.gridx = 3;
		gBCOpenButton.gridy = 2;
		openButton.setEnabled(true);
		importPanel.add(openButton, gBCOpenButton);
		
		uploadListener = new UploadFromCsvActionListener(this);
		
		openButton.addActionListener(uploadListener);
		
		// Potential error here
		importPanel.add(this.error);
		
		// CSV display here
        //headers for the table
        String[] columns = new String[] {
            "Name", "SurName", "ExamDate", "StudyId", "test1", "test2", "test3", "test4"
        };
        //create table with data
        table = new JTable();
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setColumnIdentifiers(columns);
         
        //add the table to the frame
        importPanel.add(new JScrollPane(table));
        table.getParent().setVisible(false);
		
		// IMPORT button here when necessary
        importButton = new JButton("Import");
		GridBagConstraints gBCImportButton = new GridBagConstraints();
		gBCImportButton.anchor = GridBagConstraints.EAST;
		gBCImportButton.insets = new Insets(10, 10, 10, 10);
		gBCImportButton.gridx = 3;
		gBCImportButton.gridy = 2;
		importButton.setEnabled(false);
		importPanel.add(importButton, gBCImportButton);

		importListener = new ImportFromCsvActionListener(this);
		
		importButton.addActionListener(importListener);

		// Size the frame.
		frame.pack();

		// center the frame
		// frame.setLocationRelativeTo( null );
		Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
		int windowWidth = 650;
		int windowHeight = 460;
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
	}

	public void displayCsv(List<CsvImport> imports) {
		this.error.setText("");
		String[][] data = imports.stream().map(element -> element.getRawData()).collect(Collectors.toList()).toArray(new String[0][]);
        DefaultTableModel model = (DefaultTableModel) table.getModel();

		for (int i = 0; i < data.length; i++) {
			// TODO: Check data consistency here ?
			model.addRow(data[i]);
		}
		model.fireTableDataChanged();
		table.getParent().setVisible(true);
		this.importListener.setCsvImports(imports);
		importButton.setEnabled(true);
	}
}
