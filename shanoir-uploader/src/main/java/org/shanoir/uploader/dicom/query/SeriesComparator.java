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
        final String seriesNumber1 = ((SerieTreeNode) serie1.getValue()).getSeriesNumber();
        final String seriesNumber2 = ((SerieTreeNode) serie2.getValue()).getSeriesNumber();
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
