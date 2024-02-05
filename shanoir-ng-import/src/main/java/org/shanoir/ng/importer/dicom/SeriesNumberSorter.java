package org.shanoir.ng.importer.dicom;

import java.util.Comparator;

import org.shanoir.ng.importer.model.Serie;

/**
 * Sorts a list of DICOM series according to their seriesNumber.
 * 
 * In case when querying the PACS does not return, as Telemis,
 * the seriesNumber in the response (is null in that case), we
 * catch the NumberFormatException and we return 0 as "equal".
 * This should keep the order returned by the PACS, that mostly
 * is according to the seriesNumber (normally). That is, why we
 * do not use another attribute/field to order the series.
 * 
 * @author mkain
 *
 */
public class SeriesNumberSorter implements Comparator<Serie> {

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
			return 0;
		}
	}

}