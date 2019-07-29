/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.anonymization.anonymization;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
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

	private static final String PROFILE = "Profile ";

	private Map<String, Profile> profiles;

	private Map<String, List<String>> tagsToDeleteForManufacturer;

	private AnonymizationRulesSingleton() {
		this.profiles = new HashMap<String, Profile>();
		this.tagsToDeleteForManufacturer = new HashMap<String, List<String>>();
		Integer xtagColumn = null;
		try {
			ClassLoader classLoader = getClass().getClassLoader();
			InputStream in = classLoader.getResourceAsStream(ANONYMIZATION_FILE_PATH);
			XSSFWorkbook myWorkBook = new XSSFWorkbook(in);
			
			/**
			 * Read profiles from the sheet "Profiles"
			 */
			XSSFSheet mySheet = myWorkBook.getSheetAt(0);
			// Get iterator to all the rows in current sheet
			Iterator<Row> rowIterator = mySheet.iterator();
			// Traversing over each row of XLSX file (line by line)
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				int rowNumber = row.getRowNum();
				// rowNumber == 0 is the header line in bold
				if (rowNumber == 0) {
					Iterator<Cell> cellIterator = row.cellIterator();
					while (cellIterator.hasNext()) {
						Cell cell = cellIterator.next();
						if (cell.getStringCellValue().equals(xTagsColumn)) {
							xtagColumn = cell.getColumnIndex();
						} else if (cell.getStringCellValue().startsWith(PROFILE)) {
							// init map of profiles here
							Profile profile = new Profile(cell.getColumnIndex());
							profiles.put(cell.getStringCellValue(), profile);
						}
					}
				}
				if (xtagColumn != null && !profiles.isEmpty()) {
					Cell xtagCell = row.getCell(xtagColumn);
					if (xtagCell != null && xtagCell.getStringCellValue().length() == 10) {
						String tagString = xtagCell.getStringCellValue();
						if (tagString != null && tagString.length() != 0 && !tagString.equals("0xTag")) {
							Collection<Profile> profilesColl = profiles.values();
							for (Iterator<Profile> iterator = profilesColl.iterator(); iterator.hasNext();) {
								Profile profile = (Profile) iterator.next();
								Cell actionCell = row.getCell(profile.getProfileColumn());
								profile.getAnonymizationMap().put(tagString, actionCell.getStringCellValue());	
							}
						}
					}
				} else {
					LOG.error("Unable to read anonymization tags or/and anonymization profile ");
				}
			}

			/**
			 * Read tagsToDeleteForManufacturer from the sheet "TagsToDeleteForManufacturer"
			 */
			XSSFSheet mySheet2 = myWorkBook.getSheetAt(1);
			// Get iterator to all the rows in current sheet
			Iterator<Row> rowIterator2 = mySheet2.iterator();
			// Traversing over each row of XLSX file
			while (rowIterator2.hasNext()) {
				Row row = rowIterator2.next();
				Cell cellManufacturer = row.getCell(0);
				if (cellManufacturer != null) {
					String manufacturer = cellManufacturer.getStringCellValue();
					Cell cellTag = row.getCell(1);
					if (cellTag != null) {
						String tag = cellTag.getStringCellValue();
						List<String> tagsForManufacturer = tagsToDeleteForManufacturer.get(manufacturer);
						if (tagsForManufacturer == null) {
							tagsForManufacturer = new ArrayList<>();
							tagsForManufacturer.add(tag);
							tagsToDeleteForManufacturer.put(manufacturer, tagsForManufacturer);
						} else {
							tagsForManufacturer.add(tag);
						}
					}
				}
			}

			myWorkBook.close();

		} catch (IOException e) {
			LOG.error("Unable to read anonymization file: " + e);
		}

	}

	public static AnonymizationRulesSingleton getInstance() {
		return instance;
	}

	public Map<String, Profile> getProfiles() {
		return profiles;
	}

	public Map<String, List<String>> getTagsToDeleteForManufacturer() {
		return tagsToDeleteForManufacturer;
	}

}
