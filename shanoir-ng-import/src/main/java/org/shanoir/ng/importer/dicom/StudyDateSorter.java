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

import java.time.LocalDate;
import java.util.Comparator;

import org.shanoir.ng.importer.model.Study;

/**
 * Sorts a list of DICOM studies according to their studyDate.
 *
 * @author mkain
 *
 */
public class StudyDateSorter implements Comparator<Study> {

    @Override
    public int compare(Study s1, Study s2) {
        LocalDate s1StudyDate = s1.getStudyDate();
        LocalDate s2StudyDate = s2.getStudyDate();
        if (s1StudyDate.isEqual(s2StudyDate)) {
            return 0;
        } else {
            if (s1StudyDate.isBefore(s2StudyDate)) {
                return -1;
            } else {
                return 1;
            }
        }
    }

}
