package org.shanoir.ng.processing.carmin.output;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.shanoir.ng.processing.carmin.model.CarminDatasetProcessing;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class ProcessedDatasetProcessingTest {

    @InjectMocks
    private ProcessedDatasetProcessing outputProcessing;

    @Test
    public void canProcessTest() {
        CarminDatasetProcessing processing = new CarminDatasetProcessing();
        processing.setPipelineIdentifier("ofsep_sequences_identification/0.1");
        assertTrue(outputProcessing.canProcess(processing));
        processing.setPipelineIdentifier("ofsep_sequences_identification/1.0");
        assertTrue(outputProcessing.canProcess(processing));
        processing.setPipelineIdentifier("ct-tiqua/2.2");
        assertTrue(outputProcessing.canProcess(processing));
    }

    @Test
    public void getDatasetIdFromFilenameTest(){

        String[] names = {
                "id+[dataset id]+whatever.nii",""
        };

        assertEquals(Long.valueOf(89546), outputProcessing.getDatasetIdsFromFilename("id+89546+whatever.nii"));

        assertEquals(Long.valueOf(123), outputProcessing.getDatasetIdsFromFilename("id+123+[something].tar.gz"));

        assertNull(outputProcessing.getDatasetIdsFromFilename("prefix+id+89546+nothing.dcm"));

        assertNull(outputProcessing.getDatasetIdsFromFilename("id+89546.dcm"));


    }

}
