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

package org.shanoir.ng.dicom.web;

import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.shanoir.ng.dataset.modality.GenericDataset;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.datasetacquisition.model.mr.MrDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.service.DatasetAcquisitionService;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.dicom.web.service.DICOMWebService;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.importer.service.DicomImporterService;
import org.shanoir.ng.importer.service.DicomSEGAndSRImporterService;
import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.processing.repository.DatasetProcessingRepository;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * Integration tests of the DICOMWeb facade consumed by the OHIF viewer:
 * the PACS (dcm4chee-arc) answers are simulated with Json fixtures captured
 * in the dcm4chee DICOM-Json format, the database access is mocked with a
 * small examination object graph and the requests run through the real
 * controller and UID handlers, as the viewer would trigger them.
 *
 * The invariant behind most assertions: the viewer only ever sees virtual
 * UIDs (examinationUID, acquisitionUID, datasetUID), never the real
 * StudyInstanceUID/SeriesInstanceUIDs of the PACS.
 */
@WebMvcTest(controllers = DICOMWebApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration()
@Import({ SeriesInstanceUIDHandler.class, StudyInstanceUIDAndSubjectNameHandler.class })
@TestPropertySource(properties = { "viewer.ohif.url.base=https://shanoir-viewer.irisa.fr/dicomweb" })
@ActiveProfiles("test")
public class DICOMWebApiControllerTest {

    private static final String DICOM_JSON = "application/dicom+json";

    private static final String STUDY_UID = "1.4.9.12.34.1.8527.1000000000000000000000000000000000000042";

    private static final String EXAMINATION_UID = "1.4.9.12.34.1.8527.42";

    // primary series of acquisition 100 and 101
    private static final String SERIES_UID_ACQ_100 = "1.4.9.12.34.1.8527.1111111111111111111111111111111111111111";

    private static final String SERIES_UID_ACQ_101 = "1.4.9.12.34.1.8527.2222222222222222222222222222222222222222";

    // series of the SEG dataset 500, stored by the viewer into acquisition 101
    private static final String SERIES_UID_SEG_DATASET_500 = "1.2.826.0.1.3680043.8.498.33333333333333333333333333333333";

    // series of the processing output dataset 600 (input: dataset 300 of acquisition 100)
    private static final String SERIES_UID_OUTPUT_DATASET_600 = "1.4.9.12.34.1.8527.4444444444444444444444444444444444444444";

    // series of the RT Structure Set dataset 700, contoured on acquisition 101
    private static final String SERIES_UID_RTSTRUCT_DATASET_700 = "1.2.826.0.1.3680043.8.498.77777777777777777777777777777777";

    // series in the same PACS study, that belongs to no acquisition of the examination
    private static final String SERIES_UID_INTRUDER = "1.4.9.12.34.1.8527.9999999999999999999999999999999999999999";

    private static final String SOP_UID_SEG = "1.2.826.0.1.3680043.8.498.55555555555555555555555555555555";

    private static final String SOP_UID_RTSTRUCT = "1.2.826.0.1.3680043.8.498.88888888888888888888888888888888";

    // image of acquisition 101 referenced in the ContourImageSequence of the RTSTRUCT
    private static final String SOP_UID_SOURCE_IMAGE = "1.4.9.12.34.1.8527.6666666666666666666666666666666666666666";

    private static final String ACQUISITION_UID_100 = "1.4.9.12.34.1.8527.100";

    private static final String ACQUISITION_UID_101 = "1.4.9.12.34.1.8527.101";

    private static final String DATASET_UID_500 = "1.4.9.12.34.1.8527.0.500";

    private static final String DATASET_UID_600 = "1.4.9.12.34.1.8527.0.600";

    private static final String DATASET_UID_700 = "1.4.9.12.34.1.8527.0.700";

    private static final String WADO_RS_PATH = "http://dcm4chee-arc:8081/dcm4chee-arc/aets/AS_RECEIVED/rs/studies/"
            + STUDY_UID + "/series/%s/instances/%s";

    /** the Accept header OHIF sends: any transfer syntax its decoders can read */
    private static final String VIEWER_ACCEPT = "multipart/related;type=\"application/octet-stream\";transfer-syntax=*";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private SeriesInstanceUIDHandler seriesInstanceUIDHandler;

    @Autowired
    private StudyInstanceUIDAndSubjectNameHandler studyInstanceUIDAndSubjectNameHandler;

    @MockBean
    private DICOMWebService dicomWebServiceMock;

    @MockBean
    private ExaminationService examinationServiceMock;

    @MockBean
    private DatasetAcquisitionService datasetAcquisitionServiceMock;

    @MockBean
    private DatasetService datasetServiceMock;

    @MockBean
    private DatasetProcessingRepository datasetProcessingRepositoryMock;

    // required by the STOWRSMultipartRequestFilter, that is part of the web slice
    @MockBean
    private DicomSEGAndSRImporterService dicomSEGAndSRImporterServiceMock;

    @MockBean
    private DicomImporterService dicomImporterServiceMock;

    @BeforeEach
    public void setup() throws IOException {
        // the handlers are real beans shared over all tests of this class: clear
        // their caches to keep each test independent of the execution order
        seriesInstanceUIDHandler.clearVirtualUIDCaches();
        studyInstanceUIDAndSubjectNameHandler.clearCaches();

        Examination examination = new Examination();
        examination.setId(42L);
        examination.setStudyInstanceUID(STUDY_UID);
        given(examinationServiceMock.findById(42L)).willReturn(examination);

        MrDataset dataset300 = createDataset(new MrDataset(), 300L, SERIES_UID_ACQ_100, "301");
        MrDatasetAcquisition acquisition100 = new MrDatasetAcquisition();
        acquisition100.setId(100L);
        acquisition100.setSeriesInstanceUID(SERIES_UID_ACQ_100);
        acquisition100.setDatasets(List.of(dataset300));

        MrDataset dataset400 = createDataset(new MrDataset(), 400L, SERIES_UID_ACQ_101, "401");
        GenericDataset dataset500 = createDataset(new GenericDataset(), 500L, SERIES_UID_SEG_DATASET_500, SOP_UID_SEG);
        GenericDataset dataset700 = createDataset(new GenericDataset(), 700L, SERIES_UID_RTSTRUCT_DATASET_700,
                SOP_UID_RTSTRUCT);
        MrDatasetAcquisition acquisition101 = new MrDatasetAcquisition();
        acquisition101.setId(101L);
        acquisition101.setSeriesInstanceUID(SERIES_UID_ACQ_101);
        acquisition101.setDatasets(List.of(dataset400, dataset500, dataset700));

        given(datasetAcquisitionServiceMock.findByExamination(42L))
                .willReturn(List.of(acquisition100, acquisition101));
        given(datasetAcquisitionServiceMock.findById(100L)).willReturn(acquisition100);
        given(datasetAcquisitionServiceMock.findById(101L)).willReturn(acquisition101);
        given(datasetServiceMock.findById(500L)).willReturn(dataset500);
        given(datasetServiceMock.findById(700L)).willReturn(dataset700);

        GenericDataset dataset600 = createDataset(new GenericDataset(), 600L, SERIES_UID_OUTPUT_DATASET_600, "601");
        given(datasetServiceMock.findById(600L)).willReturn(dataset600);
        DatasetProcessing processing = new DatasetProcessing();
        processing.setOutputDatasets(List.of(dataset600));
        given(datasetProcessingRepositoryMock
                .findAllByInputDatasets_IdIn(argThat(datasetIds -> datasetIds != null && datasetIds.contains(300L))))
                .willReturn(List.of(processing));

        given(dicomWebServiceMock.findSeriesOfStudy(STUDY_UID, "", ""))
                .willReturn(readFixture("dicom/seriesOfStudy.json"));
        given(dicomWebServiceMock.findSerieMetadataOfStudy(STUDY_UID, SERIES_UID_SEG_DATASET_500))
                .willReturn(readFixture("dicom/segSerieMetadata.json"));
        given(dicomWebServiceMock.findSerieMetadataOfStudy(STUDY_UID, SERIES_UID_RTSTRUCT_DATASET_700))
                .willReturn(readFixture("dicom/rtStructSerieMetadata.json"));
    }

    private <T extends Dataset> T createDataset(T dataset, Long id, String seriesInstanceUID, String sopInstanceUID) {
        DatasetFile file = new DatasetFile();
        file.setPacs(true);
        file.setPath(String.format(WADO_RS_PATH, seriesInstanceUID, sopInstanceUID));
        DatasetExpression expression = new DatasetExpression();
        expression.setDatasetExpressionFormat(DatasetExpressionFormat.DICOM);
        expression.setDatasetFiles(List.of(file));
        dataset.setId(id);
        dataset.setDatasetExpressions(List.of(expression));
        return dataset;
    }

    private String readFixture(String path) throws IOException {
        return new ClassPathResource(path).getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    @WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
    public void findSeriesOfStudyReturnsOnlyVirtualUIDsFilteredAndSorted() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/dicomweb/studies/{examinationUID}/series", EXAMINATION_UID)
                .accept(DICOM_JSON))
                .andExpect(status().isOk())
                // the intruder series of the PACS study is filtered out, the SEG
                // series of acquisition 101 and the processing output are appended
                .andExpect(jsonPath("$.length()").value(4))
                // sorted by SeriesNumber (1, 2, 3, SEG without number last)
                .andExpect(jsonPath("$[0]['0020000E'].Value[0]").value(ACQUISITION_UID_100))
                .andExpect(jsonPath("$[1]['0020000E'].Value[0]").value(ACQUISITION_UID_101))
                .andExpect(jsonPath("$[2]['0020000E'].Value[0]").value(DATASET_UID_600))
                .andExpect(jsonPath("$[3]['0020000E'].Value[0]").value(DATASET_UID_500))
                // the real StudyInstanceUID is replaced with the examinationUID
                .andExpect(jsonPath("$[0]['0020000D'].Value[0]").value(EXAMINATION_UID))
                // RetrieveURLs reference virtual UIDs only
                .andExpect(jsonPath("$[3]['00081190'].Value[0]")
                        .value(Matchers.endsWith("/studies/" + EXAMINATION_UID + "/series/" + DATASET_UID_500)))
                // the viewer never sees a real UID
                .andExpect(content().string(not(Matchers.containsString(STUDY_UID))))
                .andExpect(content().string(not(Matchers.containsString(SERIES_UID_ACQ_100))))
                .andExpect(content().string(not(Matchers.containsString(SERIES_UID_ACQ_101))))
                .andExpect(content().string(not(Matchers.containsString(SERIES_UID_SEG_DATASET_500))))
                .andExpect(content().string(not(Matchers.containsString(SERIES_UID_OUTPUT_DATASET_600))))
                .andExpect(content().string(not(Matchers.containsString(SERIES_UID_INTRUDER))));
    }

    @Test
    @WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
    public void findSeriesOfStudyWithAcquisitionFilterReturnsAllSeriesOfAcquisition() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/dicomweb/studies/{examinationUID}/series", EXAMINATION_UID)
                .queryParam("SeriesInstanceUID", ACQUISITION_UID_101)
                .accept(DICOM_JSON))
                .andExpect(status().isOk())
                // the primary series and the SEG series of acquisition 101, but
                // neither the series of acquisition 100 nor the processing output
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0]['0020000E'].Value[0]").value(ACQUISITION_UID_101))
                .andExpect(jsonPath("$[1]['0020000E'].Value[0]").value(DATASET_UID_500))
                .andExpect(content().string(not(Matchers.containsString(STUDY_UID))))
                .andExpect(content().string(not(Matchers.containsString(SERIES_UID_ACQ_101))))
                .andExpect(content().string(not(Matchers.containsString(SERIES_UID_SEG_DATASET_500))));
    }

    @Test
    @WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
    public void findSerieMetadataOfStudyRewritesUIDsAndBulkDataURIs() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                .get("/dicomweb/studies/{examinationUID}/series/{serieInstanceUID}/metadata",
                        EXAMINATION_UID, DATASET_UID_500)
                .accept(DICOM_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]['0020000D'].Value[0]").value(EXAMINATION_UID))
                .andExpect(jsonPath("$[0]['0020000E'].Value[0]").value(DATASET_UID_500))
                // the source series referenced by the SEG object in the
                // ReferencedSeriesSequence is mapped to its acquisitionUID
                .andExpect(jsonPath("$[0]['00081115'].Value[0]['0020000E'].Value[0]").value(ACQUISITION_UID_101))
                // the BulkDataURI of the PixelData points to the public facade
                // with virtual UIDs, not to the internal PACS with real UIDs
                .andExpect(jsonPath("$[0]['7FE00010'].BulkDataURI")
                        .value("https://shanoir-viewer.irisa.fr/dicomweb/studies/" + EXAMINATION_UID + "/series/"
                                + DATASET_UID_500 + "/instances/" + SOP_UID_SEG + "/bulkdata/7FE00010"))
                .andExpect(content().string(not(Matchers.containsString(STUDY_UID))))
                .andExpect(content().string(not(Matchers.containsString(SERIES_UID_ACQ_101))))
                .andExpect(content().string(not(Matchers.containsString(SERIES_UID_SEG_DATASET_500))));
    }

    @Test
    @WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
    public void findSerieMetadataOfStudyRewritesStudyReferencesOfRTStructNestedInSequences() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                .get("/dicomweb/studies/{examinationUID}/series/{serieInstanceUID}/metadata",
                        EXAMINATION_UID, DATASET_UID_700)
                .accept(DICOM_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]['0020000D'].Value[0]").value(EXAMINATION_UID))
                .andExpect(jsonPath("$[0]['0020000E'].Value[0]").value(DATASET_UID_700))
                // ReferencedFrameOfReferenceSequence -> RTReferencedStudySequence:
                // the ReferencedSOPInstanceUID carries the StudyInstanceUID of the
                // contoured study, that the viewer only knows as examinationUID
                .andExpect(jsonPath("$[0]['30060010'].Value[0]['30060012'].Value[0]['00081155'].Value[0]")
                        .value(EXAMINATION_UID))
                // RTReferencedSeriesSequence: the contoured series of acquisition 101
                .andExpect(jsonPath("$[0]['30060010'].Value[0]['30060012'].Value[0]"
                        + "['30060014'].Value[0]['0020000E'].Value[0]").value(ACQUISITION_UID_101))
                // ContourImageSequence: the same tag 0008,1155 carries SOP Instance
                // UIDs here, that are not virtualised and stay untouched
                .andExpect(jsonPath("$[0]['30060010'].Value[0]['30060012'].Value[0]['30060014'].Value[0]"
                        + "['30060016'].Value[0]['00081155'].Value[0]").value(SOP_UID_SOURCE_IMAGE))
                .andExpect(content().string(not(Matchers.containsString(STUDY_UID))))
                .andExpect(content().string(not(Matchers.containsString(SERIES_UID_ACQ_101))))
                .andExpect(content().string(not(Matchers.containsString(SERIES_UID_RTSTRUCT_DATASET_700))));
    }

    @Test
    @WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
    public void findSerieMetadataOfStudyWithUnknownAcquisitionUIDReturnsNotFound() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                .get("/dicomweb/studies/{examinationUID}/series/{serieInstanceUID}/metadata",
                        EXAMINATION_UID, "1.4.9.12.34.1.8527.999")
                .accept(DICOM_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
    public void findInstanceResolvesDatasetUIDToRealSeriesInstanceUID() throws Exception {
        given(dicomWebServiceMock.findInstance(STUDY_UID, SERIES_UID_SEG_DATASET_500, SOP_UID_SEG, ""))
                .willReturn(ResponseEntity.ok().build());
        mvc.perform(MockMvcRequestBuilders
                .get("/dicomweb/studies/{examinationUID}/series/{serieInstanceUID}/instances/{sopInstanceUID}",
                        EXAMINATION_UID, DATASET_UID_500, SOP_UID_SEG))
                .andExpect(status().isOk());
        verify(dicomWebServiceMock).findInstance(STUDY_UID, SERIES_UID_SEG_DATASET_500, SOP_UID_SEG, "");
    }

    @Test
    @WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
    public void findFrameForwardsViewerAcceptHeaderAndResolvesRealUIDs() throws Exception {
        given(dicomWebServiceMock.findFrameOfStudyOfSerieOfInstance(STUDY_UID, SERIES_UID_SEG_DATASET_500,
                SOP_UID_SEG, "1", VIEWER_ACCEPT)).willReturn(ResponseEntity.ok().build());
        mvc.perform(MockMvcRequestBuilders
                .get("/dicomweb/studies/{examinationUID}/series/{serieInstanceUID}/instances/{sopInstanceUID}"
                        + "/frames/{frame}",
                        EXAMINATION_UID, DATASET_UID_500, SOP_UID_SEG, "1")
                .header(HttpHeaders.ACCEPT, VIEWER_ACCEPT))
                .andExpect(status().isOk());
        // the PACS has to learn, which transfer syntaxes the viewer can decode
        verify(dicomWebServiceMock).findFrameOfStudyOfSerieOfInstance(STUDY_UID, SERIES_UID_SEG_DATASET_500,
                SOP_UID_SEG, "1", VIEWER_ACCEPT);
    }

    @Test
    @WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
    public void findFrameDropsAnAcceptHeaderThatIsNoMediaTypeList() throws Exception {
        given(dicomWebServiceMock.findFrameOfStudyOfSerieOfInstance(STUDY_UID, SERIES_UID_SEG_DATASET_500,
                SOP_UID_SEG, "1", null)).willReturn(ResponseEntity.ok().build());
        mvc.perform(MockMvcRequestBuilders
                .get("/dicomweb/studies/{examinationUID}/series/{serieInstanceUID}/instances/{sopInstanceUID}"
                        + "/frames/{frame}",
                        EXAMINATION_UID, DATASET_UID_500, SOP_UID_SEG, "1")
                .header(HttpHeaders.ACCEPT, "multipart/related\r\nX-Injected: shanoir"))
                .andExpect(status().isOk());
        // nothing of the viewer ends up unchecked in the request to the PACS
        verify(dicomWebServiceMock).findFrameOfStudyOfSerieOfInstance(STUDY_UID, SERIES_UID_SEG_DATASET_500,
                SOP_UID_SEG, "1", null);
    }

    @Test
    @WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
    public void findBulkDataProxiesToPACSWithRealUIDs() throws Exception {
        given(dicomWebServiceMock.findBulkDataOfStudyOfSerieOfInstance(STUDY_UID, SERIES_UID_SEG_DATASET_500,
                SOP_UID_SEG, "7FE00010?offset=0&length=100")).willReturn(ResponseEntity.ok().build());
        mvc.perform(MockMvcRequestBuilders
                .get("/dicomweb/studies/{examinationUID}/series/{serieInstanceUID}/instances/{sopInstanceUID}"
                        + "/bulkdata/7FE00010?offset=0&length=100",
                        EXAMINATION_UID, DATASET_UID_500, SOP_UID_SEG))
                .andExpect(status().isOk());
        verify(dicomWebServiceMock).findBulkDataOfStudyOfSerieOfInstance(STUDY_UID, SERIES_UID_SEG_DATASET_500,
                SOP_UID_SEG, "7FE00010?offset=0&length=100");
    }

    @Test
    @WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
    public void findBulkDataRejectsNonHexBulkDataPath() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                .get("/dicomweb/studies/{examinationUID}/series/{serieInstanceUID}/instances/{sopInstanceUID}"
                        + "/bulkdata/no-hex-tag-path",
                        EXAMINATION_UID, DATASET_UID_500, SOP_UID_SEG))
                .andExpect(status().isNotFound());
        verify(dicomWebServiceMock, never()).findBulkDataOfStudyOfSerieOfInstance(anyString(), anyString(),
                anyString(), anyString());
    }

    @Test
    @WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
    public void findBulkDataRejectsUnexpectedQueryParameters() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                .get("/dicomweb/studies/{examinationUID}/series/{serieInstanceUID}/instances/{sopInstanceUID}"
                        + "/bulkdata/7FE00010?offset=0&unexpected=parameter",
                        EXAMINATION_UID, DATASET_UID_500, SOP_UID_SEG))
                .andExpect(status().isNotFound());
        verify(dicomWebServiceMock, never()).findBulkDataOfStudyOfSerieOfInstance(anyString(), anyString(),
                anyString(), anyString());
    }

}
