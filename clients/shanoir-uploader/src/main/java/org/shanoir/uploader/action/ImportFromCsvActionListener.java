package org.shanoir.uploader.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.uploader.dicom.IDicomServerClient;
import org.shanoir.uploader.gui.ImportFromCSVWindow;
import org.shanoir.uploader.model.CsvImport;
import org.shanoir.uploader.model.rest.AcquisitionEquipment;
import org.shanoir.uploader.model.rest.IdList;
import org.shanoir.uploader.model.rest.StudyCard;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClient;

/**
 * This class is used after 'import' button from CSV importer.
 * It loads the list of imports to do, then imports them one by one
 * Managing errors and displays
 * @author fli
 *
 */
public class ImportFromCsvActionListener implements ActionListener {

	ImportFromCSVWindow importFromCSVWindow;
	IDicomServerClient dicomServerClient;
	File shanoirUploaderFolder;

	List<CsvImport> csvImports;
	ShanoirUploaderServiceClient shanoirUploaderServiceClientNG;
	private ResourceBundle resourceBundle;

	public ImportFromCsvActionListener(ImportFromCSVWindow importFromCSVWindow, ResourceBundle resourceBundle, IDicomServerClient dicomServerClient, File shanoirUploaderFolder, ShanoirUploaderServiceClient shanoirUploaderServiceClientNG) {
		this.importFromCSVWindow = importFromCSVWindow;
		this.dicomServerClient = dicomServerClient;
		this.shanoirUploaderFolder = shanoirUploaderFolder;
		this.shanoirUploaderServiceClientNG = shanoirUploaderServiceClientNG;
		this.resourceBundle = resourceBundle;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		ImportFromCsvRunner importer = new ImportFromCsvRunner(csvImports, resourceBundle, importFromCSVWindow, dicomServerClient, shanoirUploaderServiceClientNG);
		importer.execute();
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
