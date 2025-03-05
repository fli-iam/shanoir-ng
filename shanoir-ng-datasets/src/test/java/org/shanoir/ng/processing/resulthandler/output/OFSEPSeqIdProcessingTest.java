package org.shanoir.ng.processing.resulthandler.output;


import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dicom.web.StudyInstanceUIDHandler;
import org.shanoir.ng.dicom.web.service.DICOMWebService;
import org.shanoir.ng.download.WADODownloaderService;
import org.shanoir.ng.vip.monitoring.model.ExecutionMonitoring;
import org.shanoir.ng.vip.resulthandler.OFSEPSeqIdHandler;
import org.shanoir.ng.vip.resulthandler.ResultHandlerException;
import org.shanoir.ng.shared.exception.PacsException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@ActiveProfiles("test")
public class OFSEPSeqIdProcessingTest {

    @Mock
    private WADODownloaderService wadoDownloaderService;
    
    @InjectMocks
    private OFSEPSeqIdHandler outputProcessing;

    @MockBean
    private StudyInstanceUIDHandler studyInstanceUIDHandler;

    @Test
    public void canProcessTest() throws ResultHandlerException {
        ExecutionMonitoring processing = new ExecutionMonitoring();
        processing.setPipelineIdentifier("ofsep_sequences_identification/0.1");
        assertTrue(outputProcessing.canProcess(processing));
        processing.setPipelineIdentifier("ofsep_sequences_identification/1.0");
        assertTrue(outputProcessing.canProcess(processing));
        processing.setPipelineIdentifier("ct-tiqua/2.2");
        assertFalse(outputProcessing.canProcess(processing));
    }

    @Test
    public void areArraysEqualsTest() throws JSONException {

        assertTrue(outputProcessing.areOrientationsEquals(this.getDsOrientation(), this.getMatchingVolumeOrientation()));

        assertFalse(outputProcessing.areOrientationsEquals(this.getDsOrientation(), this.getNonMatchingVolumeOrientation()));

    }

    @Test
    public void getMatchingVolumeTest() throws JSONException, PacsException {

        Dataset ds = new MrDataset();
        ds.setId(1L);

        Attributes attr = new Attributes();
        attr.setDouble(Tag.ImageOrientationPatient, VR.DS, this.getDsOrientation());

        JSONObject vol1 = new JSONObject()
            .put("orientation", this.getMatchingVolumeOrientation())
            .put("id", "volume_1");

        JSONObject vol2 = new JSONObject()
            .put("orientation", this.getMatchingVolumeOrientation())
            .put("id", "volume_2");

        JSONObject serie = new JSONObject()
                .put("id", 1L)
                .put("volumes", new JSONArray().put(vol1).put(vol2));

        JSONObject volume = outputProcessing.getMatchingVolume(ds, serie, attr);

        assertEquals("volume_1", volume.get("id"));


    }

    private double[] getDsOrientation(){
        return new double[]{1.0,0.0,6.12303176911e-17,6.12303176911e-17,0.0,-1.0};
    }

    private JSONArray getMatchingVolumeOrientation() throws JSONException {
        return new JSONArray()
                .put(1.0)
                .put(0.0)
                .put(6.12303176911e-17)
                .put(6.12303176911e-17)
                .put(0.0)
                .put(-1.0);
    }

    private JSONArray getNonMatchingVolumeOrientation() throws JSONException {
        return new JSONArray()
                .put(-0.0331592650151)
                .put(-0.0331592650151)
                .put(-6.8217453e-010)
                .put(-0.0156900742116)
                .put(-0.0005205582762)
                .put(-0.9998767677021);
    }
}