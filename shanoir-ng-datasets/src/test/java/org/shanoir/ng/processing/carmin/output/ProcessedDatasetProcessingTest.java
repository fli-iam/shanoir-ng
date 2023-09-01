package org.shanoir.ng.processing.carmin.output;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.shanoir.ng.processing.carmin.model.CarminDatasetProcessing;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class ProcessedDatasetProcessingTest {

    @InjectMocks
    private ProcessedDatasetProcessing outputProcessing;

    @Test
    public void canProcessTest() {
        CarminDatasetProcessing processing = new CarminDatasetProcessing();
        processing.setPipelineIdentifier("ofsep_sequences_identification/0.1");
        Assertions.assertTrue(outputProcessing.canProcess(processing));
        processing.setPipelineIdentifier("ofsep_sequences_identification/1.0");
        Assertions.assertTrue(outputProcessing.canProcess(processing));
        processing.setPipelineIdentifier("ct-tiqua/2.2");
        Assertions.assertTrue(outputProcessing.canProcess(processing));
    }

}
