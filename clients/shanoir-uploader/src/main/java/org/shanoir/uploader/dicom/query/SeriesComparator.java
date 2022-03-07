package org.shanoir.uploader.dicom.query;

import java.util.Comparator;
import java.util.Map.Entry;

import org.shanoir.dicom.importer.Serie;
import org.shanoir.dicom.model.DicomTreeNode;

/**
 * Dicom Serie comparator based on their serie number.
 *
 * @author grenard
 * @author mkain
 *
 */
public class SeriesComparator implements Comparator<Entry<String, DicomTreeNode>> {

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Entry<String, DicomTreeNode> serie1, Entry<String, DicomTreeNode> serie2) {
		final String seriesNumber1 = ((Serie) serie1.getValue()).getDescriptionMap().get("seriesNumber");
		final String seriesNumber2 = ((Serie) serie2.getValue()).getDescriptionMap().get("seriesNumber");
		if (seriesNumber1 != null && !seriesNumber1.equals("")) {
			if (seriesNumber2 != null && !seriesNumber2.equals("")) {
				return Integer.decode(seriesNumber1).compareTo(Integer.decode(seriesNumber2));
			} else {
				return -1;
			}
		} else {
			if (seriesNumber2 != null && !seriesNumber2.equals("")) {
				return 1;
			} else {
				return 0;
			}
		}
	}

}
