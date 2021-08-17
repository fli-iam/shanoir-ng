package org.shanoir.ng.importer.dicom;

import java.util.Comparator;

import org.shanoir.ng.importer.model.Serie;

public class SeriesNumberSorter implements Comparator<Serie> {

	@Override
	public int compare(Serie s1, Serie s2) {
		String serie1SeriesNumberStr = s1.getSeriesNumber();
		String serie2SeriesNumberStr = s2.getSeriesNumber();
		int serie1SeriesNumberInt = Integer.parseInt(serie1SeriesNumberStr);
		int serie2SeriesNumberInt = Integer.parseInt(serie2SeriesNumberStr);
		if (serie1SeriesNumberInt == serie2SeriesNumberInt) {
			return 0;
		} else {
			if (serie1SeriesNumberInt < serie2SeriesNumberInt) {
				return -1;
			} else {
				return 1;
			}
		}
	}

}