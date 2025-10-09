package org.shanoir.ng.execution.controller;

import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessAuthorized;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessDenied;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.shanoir.ng.dataset.repository.DatasetRepository;
import org.shanoir.ng.processing.dto.GroupByEnum;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.shanoir.ng.vip.execution.controler.ExecutionApi;
import org.shanoir.ng.vip.execution.dto.ExecutionCandidateDTO;
import org.shanoir.ng.vip.execution.service.ExecutionServiceImpl;
import org.shanoir.ng.vip.shared.dto.DatasetParameterDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Test for ExecutionApiController class
 */

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ExecutionApiControllerTest {

	private static final String REQUEST_PATH = "/vip/execution";


	@MockitoBean
	private DatasetRepository datasetRepository;

	@Autowired
	private ExecutionApi api;

	@MockitoBean
	private ExecutionServiceImpl executionService;


	@BeforeEach
	public void setup() {
	}

	@Test
	@WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
	public void testCreateExecutionAsAdmin() throws Exception {

		// DatasetForRightsProjection ds1 = new DatasetForRights(1L, 1L, 1L, null);
		// DatasetForRightsProjection ds2 = new DatasetForRights(2L, 1L, 1L, new HashSet<>(List.of(2L, 3L)));
		// given(datasetRepository.findDatasetsForRights(List.of(1L, 2L))).willReturn(List.of(ds1, ds2));

		ExecutionCandidateDTO candidate = createExecutionCandidateDTO();

		assertAccessAuthorized(api::createExecution, candidate);
	}

	@Test
	@WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_EXPERT" })
	public void testCreateExecutionAsExpert() throws Exception {

		// DatasetForRightsProjection ds1 = new DatasetForRights(1L, 1L, 1L, null);
		// DatasetForRightsProjection ds2 = new DatasetForRights(2L, 1L, 1L, new HashSet<>(List.of(2L, 3L)));
		// given(datasetRepository.findDatasetsForRights(List.of(1L, 2L))).willReturn(List.of(ds1, ds2));

		ExecutionCandidateDTO candidate = createExecutionCandidateDTO();

		assertAccessDenied(api::createExecution, candidate);
	}

	private ExecutionCandidateDTO createExecutionCandidateDTO() {
		ExecutionCandidateDTO candidate = new ExecutionCandidateDTO();
		candidate.setName("Test execution");
		candidate.setPipelineIdentifier("pipeline1");
		candidate.setInputParameters(Map.of("param1", List.of("value1"), "param2", List.of("value2a", "value2b")));
		candidate.setDatasetParameters(createDatasetParameterDTOList());
		candidate.setStudyIdentifier(1L);
		candidate.setOutputProcessing("ZIP");
		candidate.setProcessingType("DICOM");
		candidate.setRefreshToken("refreshToken");
		candidate.setClient("clientId");
		candidate.setConverterId(1L);
		return candidate;
	}

	private List<DatasetParameterDTO> createDatasetParameterDTOList() {
		List<DatasetParameterDTO> datasetParameters = new ArrayList<>();
		datasetParameters.add(createDatasetParameterDTO("ds1"));
		datasetParameters.add(createDatasetParameterDTO("ds2"));
		return datasetParameters;
	}

	private DatasetParameterDTO createDatasetParameterDTO(String name) {
		DatasetParameterDTO dto = new DatasetParameterDTO();
		dto.setName(name);
		dto.setGroupBy(GroupByEnum.SUBJECT);
		dto.setExportFormat("DICOM");
		dto.setDatasetIds(List.of(1L, 2L));
		dto.setConverterId(1L);
		return dto;
	}
}
