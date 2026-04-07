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

package org.shanoir.uploader.test.datasets.studycard;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.Test;
import org.shanoir.uploader.test.AbstractTest;

public class StudyCardTest extends AbstractTest {

    private static final Logger LOG = LoggerFactory.getLogger(StudyCardTest.class);

    @Test
    public void applyStudyCardOnStudy() throws Exception {
        // @todo here: create SC example, import and verify apply after
//        List<StudyCardOnStudyResultDTO> results = shUpClient.applyStudyCardOnStudy(Long.valueOf(2));
//        LOG.info(results);
    }

}
