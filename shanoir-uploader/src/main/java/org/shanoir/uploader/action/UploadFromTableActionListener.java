package org.shanoir.uploader.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.shanoir.uploader.gui.ImportFromTableWindow;
import org.shanoir.uploader.model.CsvImport;

/**
 * 
 */
public class UploadFromTableActionListener implements ActionListener {

	private static final Logger logger = LoggerFactory.getLogger(UploadFromTableActionListener.class);

	private JFileChooser fileChooser;
	private ImportFromTableWindow importFromCSVWindow;
	private ResourceBundle resourceBundle;

	public UploadFromTableActionListener(ImportFromTableWindow importFromCSVWindow, ResourceBundle resourceBundle) {
		this.importFromCSVWindow = importFromCSVWindow;
		this.fileChooser = new JFileChooser();
		// Create a file filter for .xlsx files
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Excel Files", "xlsx", "xls");
        fileChooser.setFileFilter(filter);
		this.resourceBundle = resourceBundle;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		int result = fileChooser.showOpenDialog(importFromCSVWindow);
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
					Iterator<Cell> cellIterator = row.cellIterator();
					while (cellIterator.hasNext()) {
						Cell cell = cellIterator.next();
						handleCell(cell);
					}
				}
			}
		} catch (InvalidFormatException | IOException | IllegalStateException e) {
			logger.error("Error while parsing the input file: ", e);
			this.importFromCSVWindow.displayError(resourceBundle.getString("shanoir.uploader.import.table.error.csv"));
			return;
		}

		// this.importFromCSVWindow.displayCsv(subjects);
	}

private void handleCell(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
				logger.info(cell.getStringCellValue());
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    logger.info("Date cell: " + cell.getDateCellValue());
                } else {
                    double numericValue = cell.getNumericCellValue();
	                String textValue = String.valueOf(numericValue);
					logger.info(textValue);
                }
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

}
