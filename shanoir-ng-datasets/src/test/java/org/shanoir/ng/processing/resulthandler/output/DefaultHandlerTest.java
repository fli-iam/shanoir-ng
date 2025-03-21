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
