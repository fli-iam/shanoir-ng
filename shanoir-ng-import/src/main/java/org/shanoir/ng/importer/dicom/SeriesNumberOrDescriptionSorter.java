package org.shanoir.ng.importer.dicom;

import java.util.Comparator;

import org.shanoir.ng.importer.model.Serie;

/**
 * Sorts a list of DICOM series according to their seriesNumber
 * or seriesDescription.
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
public class SeriesNumberOrDescriptionSorter implements Comparator<Serie> {

	@Override
	public int compare(Serie s1, Serie s2) {
		String s1SeriesNumber = s1.getSeriesNumber();
		String s2SeriesNumber = s2.getSeriesNumber();
		try {
			int s1SeriesNumberInt = Integer.parseInt(s1SeriesNumber);
			int s2SeriesNumberInt = Integer.parseInt(s2SeriesNumber);
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
			String s1SeriesDescription = s1.getSeriesDescription();
			String s2SeriesDescription = s2.getSeriesDescription();
			return s1SeriesDescription.compareToIgnoreCase(s2SeriesDescription);
		}
	}

}