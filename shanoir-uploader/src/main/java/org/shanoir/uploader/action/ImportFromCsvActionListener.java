package org.shanoir.uploader.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import org.apache.log4j.Logger;
import org.shanoir.uploader.gui.ImportFromCSVWindow;
import org.shanoir.uploader.model.CsvImport;

public class ImportFromCsvActionListener implements ActionListener {

	ImportFromCSVWindow importFromCSVWindow;
	
	List<CsvImport> csvImports;
	
	private static Logger logger = Logger.getLogger(ImportFromCsvActionListener.class);
	
	public ImportFromCsvActionListener(ImportFromCSVWindow importFromCSVWindow) {
		this.importFromCSVWindow = importFromCSVWindow;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		logger.error("coucocu" + csvImports);
		
		// Iterate over import to import them one by one
		for (CsvImport importTodo : this.csvImports) {
			importData(importTodo);
		}
	}

	/**
	 * Loads data to shanoir NG
	 * @param csvImport
	 * @return
	 */
	private void importData(CsvImport csvImport) {
		// Request PACS
		
		// Copy data from PACS
		
		// Check existence of study / study card
		
		// Create subject
		
		// Create examination
		
		// Import data
		
		// Manage error
	}

	/**
	 * @return the csvImports
	 */
	public List<CsvImport> getCsvImports() {
		return csvImports;
	}

	/**
	 * @param csvImports the csvImports to set
	 */
	public void setCsvImports(List<CsvImport> csvImports) {
		this.csvImports = csvImports;
	}

}
