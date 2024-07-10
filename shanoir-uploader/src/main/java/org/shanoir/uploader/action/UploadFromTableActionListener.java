package org.shanoir.uploader.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.shanoir.ng.importer.dicom.query.DicomQuery;
import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.uploader.gui.ImportFromTableWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class UploadFromTableActionListener implements ActionListener {

	private static final Logger logger = LoggerFactory.getLogger(UploadFromTableActionListener.class);

	private JFileChooser fileChooser;
	private ImportFromTableWindow importFromTableWindow;
	private ResourceBundle resourceBundle;

	public UploadFromTableActionListener(ImportFromTableWindow importFromTableWindow, ResourceBundle resourceBundle) {
		this.importFromTableWindow = importFromTableWindow;
		this.fileChooser = new JFileChooser();
		// Create a file filter for .xlsx files
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Excel Files", "xlsx", "xls");
        fileChooser.setFileFilter(filter);
		this.resourceBundle = resourceBundle;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		int result = fileChooser.showOpenDialog(importFromTableWindow);
		if (result == JFileChooser.APPROVE_OPTION) {
			analyzeFile(fileChooser.getSelectedFile());
		}
	}

	/**
	 * Displays a table file, and checks its integrity.
	 * 
	 * @param selectedFile the selected table file
	 */
	private void analyzeFile(File selectedFile) {
		Map<String, ImportJob> importJobs = new HashMap<String, ImportJob>();;
		try (XSSFWorkbook myWorkBook = new XSSFWorkbook(selectedFile)) {
			XSSFSheet mySheet = myWorkBook.getSheetAt(0);
			Iterator<Row> rowIterator = mySheet.iterator();
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				int rowNumber = row.getRowNum();
				// rowNumber == 0 is the header line in bold
				if (rowNumber == 0) {
					// do nothing with header
				} else {
					addImportJob(importJobs, row);
				}
			}
		} catch (InvalidFormatException | IOException | IllegalStateException e) {
			logger.error("Error while parsing the input file: ", e);
			this.importFromTableWindow.displayError(resourceBundle.getString("shanoir.uploader.import.table.error.csv"));
			return;
		}

		// this.importFromCSVWindow.displayCsv(subjects);
	}

	private void addImportJob(Map<String, ImportJob> importJobs, Row row) {
		int rowNumber = row.getRowNum();
		ImportJob importJob = new ImportJob();
		importJobs.put(String.valueOf(rowNumber), importJob);
		createDicomQuery(row, importJob);
	}

	private void createDicomQuery(Row row, ImportJob importJob) {
		DicomQuery dicomQuery = new DicomQuery();
		Cell dicomQueryLevel = row.getCell(0);
		String value = handleCell(dicomQueryLevel);
		if ("STUDY".equals(value)) {
			dicomQuery.setStudyRootQuery(true);
		}
		Cell dicomPatientName = row.getCell(1);
		value = handleCell(dicomPatientName);
		dicomQuery.setPatientName(value);
		Cell dicomPatientID = row.getCell(2);
		value = handleCell(dicomPatientID);
		dicomQuery.setPatientID(value);
		Cell dicomPatientBirthDate = row.getCell(3);
		value = handleCell(dicomPatientBirthDate);
		dicomQuery.setPatientBirthDate(value);
		Cell dicomStudyDescription = row.getCell(4);
		value = handleCell(dicomStudyDescription);
		dicomQuery.setStudyDescription(value);
		Cell dicomStudyDate = row.getCell(5);
		value = handleCell(dicomStudyDate);
		dicomQuery.setStudyDate(value);
		Cell dicomModality = row.getCell(6);
		value = handleCell(dicomModality);
		dicomQuery.setModality(value);
		importJob.setDicomQuery(dicomQuery);
	}

	private String handleCell(Cell cell) {
		if (cell != null) {
			switch (cell.getCellType()) {
				case STRING:
					return cell.getStringCellValue();
				case NUMERIC:
					break;
				case BOOLEAN:
					break;
				case FORMULA:
					break;
				case BLANK:
					break;
				case _NONE:
					break;
				case ERROR:
					break;
				default:
					break;
			}
		}
		return "";
    }

}
