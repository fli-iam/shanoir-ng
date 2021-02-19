package org.shanoir.uploader.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;

import org.apache.log4j.Logger;
import org.shanoir.uploader.gui.ImportFromCSVWindow;
import org.shanoir.uploader.model.CsvImport;

public class UploadFromCsvActionListener implements ActionListener {

	JFileChooser fileChooser;
	ImportFromCSVWindow importFromCSVWindow;
	
	private static Logger logger = Logger.getLogger(UploadFromCsvActionListener.class);
	
	public UploadFromCsvActionListener(ImportFromCSVWindow importFromCSVWindow) {
		this.importFromCSVWindow = importFromCSVWindow;
		this.fileChooser = new JFileChooser();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		int result = fileChooser.showOpenDialog(importFromCSVWindow);
		if (result == JFileChooser.APPROVE_OPTION) {
			analyzeFile(fileChooser.getSelectedFile());
		}
	}

	/**
	 * Displays a CSV file, and checks its integrity
	 * @param selectedFile the selected CSV file
	 */
	private void analyzeFile(File selectedFile) {
		List<CsvImport> subjects = new ArrayList<>();
		try (BufferedReader csvReader = new BufferedReader(new FileReader(selectedFile))) {
			String row;
			while ((row = csvReader.readLine()) != null) {
			    subjects.add(new CsvImport(row.split(",")));
			}
		} catch (Exception e) {
			logger.error("Error while parsing the input file: ", e);
			this.importFromCSVWindow.displayError("CSV was not correct, please upload a correct CSV file comma separated");
			return;
		}
		
		this.importFromCSVWindow.displayCsv(subjects);
	}

}
