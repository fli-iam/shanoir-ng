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

package org.shanoir.ng.importer.dicom;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.shanoir.ng.importer.model.Serie;

public class SeriesNumberOrAcquisitionTimeOrDescriptionSorterTest {

    private final SeriesNumberOrAcquisitionTimeOrDescriptionSorter sorter =
            new SeriesNumberOrAcquisitionTimeOrDescriptionSorter();

    @Test
    public void testSortWithMalformedAcquisitionTimeDoesNotThrow() {
        List<Serie> series = new ArrayList<>();
        Serie first = new Serie();
        first.setSeriesNumber("3");
        first.setAcquisitionTime("not-a-dicom-time");
        first.setSeriesDescription("Fl_CaSc");
        Serie second = new Serie();
        second.setSeriesNumber("4");
        second.setAcquisitionTime("145910.252000");
        second.setSeriesDescription("TestBolus");
        series.add(second);
        series.add(first);

        assertDoesNotThrow(() -> series.sort(sorter));
    }

    @Test
    public void testCompareWithNullSeriesNumberUsesAcquisitionOrDescription() {
        Serie first = new Serie();
        first.setSeriesNumber(null);
        first.setAcquisitionTime("175309.077500");
        first.setSeriesDescription("trufi_2-chamber");
        Serie second = new Serie();
        second.setSeriesNumber(null);
        second.setAcquisitionTime("175349.127500");
        second.setSeriesDescription("trufi_4-chamber");

        assertDoesNotThrow(() -> sorter.compare(first, second));
    }

}
