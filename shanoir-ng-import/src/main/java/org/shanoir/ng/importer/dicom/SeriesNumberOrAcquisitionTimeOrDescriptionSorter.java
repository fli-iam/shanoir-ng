package org.shanoir.ng.importer.dicom;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

import org.shanoir.ng.importer.model.Serie;

/**
 * Sorts a list of DICOM series according to their seriesNumber
 * or the acquisitionTime or the seriesDescription (in this order).
 * 
 * In case when the queried PACS does not return, as Telemis,
 * the seriesNumber in the response (is null in every case), we
 * catch the NumberFormatException and we use the seriesDescription
 * to at least establish an alphabetical order in the tree to help
 * the users to find their series.
 * 
 * @author mkain
 *
 */
public class SeriesNumberOrAcquisitionTimeOrDescriptionSorter implements Comparator<Serie> {

	@Override
	public int compare(Serie s1, Serie s2) {
		String s1SeriesNumber = s1.getSeriesNumber();
		String s2SeriesNumber = s2.getSeriesNumber();
		try {
			int s1SeriesNumberInt = Integer.parseInt(s1SeriesNumber);
			int s2SeriesNumberInt = Integer.parseInt(s2SeriesNumber);
			if (s1SeriesNumberInt == 0 && s2SeriesNumberInt == 0) {
				return orderByAcquisitionTime(s1, s2);
			}
			if (s1SeriesNumberInt == s2SeriesNumberInt) {
				return 0;
			} else {
				if (s1SeriesNumberInt < s2SeriesNumberInt) {
					return -1;
				} else {
					return 1;
				}
			}
		} catch(NumberFormatException e) {
			return orderBySeriesDescription(s1, s2);
		}
	}

	private int orderByAcquisitionTime(Serie s1, Serie s2) {
		String s1AcquisitionTime = s1.getAcquisitionTime();
		String s2AcquisitionTime = s2.getAcquisitionTime();
		if (s1AcquisitionTime == null || s2AcquisitionTime == null) {
			return 0;
		}
		LocalTime t1 = parseDicomTime(s1AcquisitionTime);
		LocalTime t2 = parseDicomTime(s2AcquisitionTime);
		return t1.compareTo(t2);
	}

	private LocalTime parseDicomTime(String dicomTime) {
		String padded = String.format("%-6s", dicomTime).replace(' ', '0');
		if (padded.contains(".")) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HHmmss.SSSSSS");
			return LocalTime.parse(padded, formatter);
		} else {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HHmmss");
			return LocalTime.parse(padded, formatter);
		}
	}

	private int orderBySeriesDescription(Serie s1, Serie s2) {
		String s1SeriesDescription = s1.getSeriesDescription();
		String s2SeriesDescription = s2.getSeriesDescription();
		if (s1SeriesDescription == null || s2SeriesDescription == null) {
			return 0;
		}
		return s1SeriesDescription.compareToIgnoreCase(s2SeriesDescription);
	}

}