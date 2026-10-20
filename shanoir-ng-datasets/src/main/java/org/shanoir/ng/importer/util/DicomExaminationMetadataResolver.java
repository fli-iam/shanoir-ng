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

package org.shanoir.ng.importer.util;

import java.time.LocalDate;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.shanoir.ng.shared.dateTime.DateTimeUtils;

/**
 * Shared rules for examination metadata taken from DICOM instance attributes.
 */
public final class DicomExaminationMetadataResolver {

    private DicomExaminationMetadataResolver() {
    }

    /**
     * Study Date (0008,0020), then Series Date (0008,0021), Acquisition Date (0008,0022)
     * and Content Date (0008,0023): a de-identification profile may remove the first ones.
     */
    public static LocalDate resolveExaminationDate(Attributes attributes) {
        if (attributes == null) {
            return null;
        }
        for (int tag : new int[] {Tag.StudyDate, Tag.SeriesDate, Tag.AcquisitionDate, Tag.ContentDate}) {
            LocalDate date = DateTimeUtils.dateToLocalDate(attributes.getDate(tag));
            if (date != null) {
                return date;
            }
        }
        return null;
    }

    /**
     * Study Description (0008,1030); may be null if absent in PACS.
     */
    public static String resolveComment(Attributes attributes) {
        if (attributes == null) {
            return null;
        }
        return attributes.getString(Tag.StudyDescription);
    }
}
