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

package org.shanoir.ng.processing.resulthandler.output;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.vip.executionMonitoring.model.ExecutionMonitoring;
import org.shanoir.ng.vip.output.handler.DefaultHandler;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
public class DefaultHandlerTest {

    @InjectMocks
    private DefaultHandler outputProcessing;

    @MockitoBean
    private ExaminationService examinationService; //Needed for spring management, even if not used. Do not remove

    @Test
    public void canProcessTest() {
        ExecutionMonitoring processing = new ExecutionMonitoring();
        processing.setPipelineIdentifier("ofsep_sequences_identification/0.1");
        Assertions.assertTrue(outputProcessing.canProcess(processing));
        processing.setPipelineIdentifier("ofsep_sequences_identification/1.0");
        Assertions.assertTrue(outputProcessing.canProcess(processing));
        processing.setPipelineIdentifier("ct-tiqua/2.2");
        Assertions.assertTrue(outputProcessing.canProcess(processing));
    }

}
