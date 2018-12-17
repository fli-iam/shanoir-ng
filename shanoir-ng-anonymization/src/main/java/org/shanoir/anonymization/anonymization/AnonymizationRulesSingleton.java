package org.shanoir.anonymization.anonymization;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnonymizationRulesSingleton {
	
	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(AnonymizationRulesSingleton.class);
	
	private static final AnonymizationRulesSingleton instance = new AnonymizationRulesSingleton();
	
	private static final String ANONYMIZATION_FILE_PATH = "anonymization.xlsx";

	private static final String xTagsColumn = "0xTag";
	
	private Map<String, String> anonymizationMAP;
	private List<String> tagsToKeep = new ArrayList< String>();
	
	private AnonymizationRulesSingleton() {
		Map<String, String> anonymizationMAP = new HashMap<String, String>();
		List<String> tagsToKeep = new ArrayList< String>();
		Integer xtagColumn = null;
		Integer profileColumn = null;

		try {
			ClassLoader classLoader = getClass().getClassLoader();
			InputStream in = classLoader.getResourceAsStream(ANONYMIZATION_FILE_PATH);

			XSSFWorkbook myWorkBook = new XSSFWorkbook(in);

			// Return first sheet from the XLSX workbook
			XSSFSheet mySheet = myWorkBook.getSheetAt(0);

			// Get iterator to all the rows in current sheet
			Iterator<Row> rowIterator = mySheet.iterator();

			// Traversing over each row of XLSX file
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				int rowNumber = row.getRowNum();
				if (rowNumber == 0) {
					Iterator<Cell> cellIterator = row.cellIterator();
					while (cellIterator.hasNext() && (xtagColumn == null || profileColumn == null)) {
						Cell cell = cellIterator.next();
						if (cell.getStringCellValue().equals(xTagsColumn)) {
							xtagColumn = cell.getColumnIndex();
							LOG.debug("Tags column : " + xtagColumn);
						} else if (cell.getStringCellValue().equals("Shanoir Profile")) {
							profileColumn = cell.getColumnIndex();
						}
					}
				}
				if (xtagColumn != null && profileColumn != null) {
					Cell xtagCell = row.getCell(xtagColumn);
					if (xtagCell != null) {
						String tagString = xtagCell.getStringCellValue();
						if (tagString != null && tagString.length() != 0 && !tagString.equals("0xTag")) {
							Cell basicProfileCell = row.getCell(profileColumn);
							LOG.debug("The basic profile of tag " + tagString + " = "
									+ basicProfileCell.getStringCellValue());
							anonymizationMAP.put(tagString, basicProfileCell.getStringCellValue());
						}
					}
				} else {
					LOG.error("Unable to read anonymization tags or/and anonymization profile ");
				}
			}
			
			// Return second sheet from the XLSX workbook
			XSSFSheet mySheet2 = myWorkBook.getSheetAt(1);

			// Get iterator to all the rows in current sheet
			Iterator<Row> rowIterator2 = mySheet2.iterator();

			// Traversing over each row of XLSX file
			while (rowIterator2.hasNext()) {
				Row row = rowIterator2.next();
				Cell cell = row.getCell(0);
				tagsToKeep.add(cell.getStringCellValue());
			}
			this.tagsToKeep = tagsToKeep;
			
		} catch (IOException e) {
			LOG.error("Unable to read anonymization file: " + e);
		}

		this.anonymizationMAP = anonymizationMAP;
	}
	
	public static AnonymizationRulesSingleton getInstance() {
		return instance;
	}

	public Map<String, String> getAnonymizationMAP() {
		return anonymizationMAP;
	}

	public List<String> getTagsToKeep() {
		return tagsToKeep;
	}

}
