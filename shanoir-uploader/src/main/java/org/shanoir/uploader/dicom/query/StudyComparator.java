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

package org.shanoir.uploader.dicom.query;

import java.util.Comparator;
import java.util.Map.Entry;

import org.shanoir.uploader.dicom.DicomTreeNode;
import org.shanoir.uploader.utils.Util;

/**
 * Dicom Study comparator based on their study date.
 *
 * @author grenard
 * @author mkain
 *
 */
public class StudyComparator implements Comparator<Entry<String, DicomTreeNode>> {

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Entry<String, DicomTreeNode> study1, Entry<String, DicomTreeNode> study2) {
		final String date1 = ((Study) study1.getValue()).getDescriptionMap().get("date");
		final String date2 = ((Study) study2.getValue()).getDescriptionMap().get("date");
		if (date1 != null && !date1.equals("")) {
			if (date2 != null && !date2.equals("")) {
				return Util.convertStringDicomDateToDate(date1).compareTo(Util.convertStringDicomDateToDate(date2));
			} else {
				return -1;
			}
		} else {
			if (date2 != null && !date2.equals("")) {
				return 1;
			} else {
				return 0;
			}
		}
	}

}
