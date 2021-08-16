package org.shanoir.ng.importer.dicom;

import java.util.Comparator;

import org.shanoir.ng.importer.model.Serie;

public class SeriesNumberSorter implements Comparator<Serie> {

	@Override
	public int compare(Serie s1, Serie s2) {
		return s1.getSeriesNumber().compareTo(s2.getSeriesNumber());
	}

}