package org.shanoir.ng.processing.resulthandler.output;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.shanoir.ng.dicom.web.StudyInstanceUIDHandler;
import org.shanoir.ng.vip.monitoring.model.ExecutionMonitoring;
import org.shanoir.ng.vip.resulthandler.DefaultHandler;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class DefaultHandlerTest {

    @InjectMocks
    private DefaultHandler outputProcessing;

    @MockBean
    private StudyInstanceUIDHandler studyInstanceUIDHandler;
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
