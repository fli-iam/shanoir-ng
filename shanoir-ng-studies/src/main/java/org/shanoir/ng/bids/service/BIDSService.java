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

package org.shanoir.ng.bids.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

public interface BIDSService {

    /**
     * Writes the participants.tsv of a study in its BIDS folder and returns its content.
     *
     * @param firstExaminationDates date of the first examination of each subject, by subject id: the ages are
     *                              computed at that date
     */
    String generateParticipantsTsvFile(Long studyId, Map<Long, LocalDate> firstExaminationDates) throws IOException;

    /**
     * Content of the participants.tsv of a study, without writing any file.
     *
     * @param firstExaminationDates date of the first examination of each subject, by subject id: the ages are
     *                              computed at that date
     */
    String participantsTsv(Long studyId, Map<Long, LocalDate> firstExaminationDates);

}
