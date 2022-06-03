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

package org.shanoir.ng.datasetacquisition;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessAuthorized;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessDenied;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.shanoir.ng.datasetacquisition.controler.DatasetAcquisitionApi;
import org.shanoir.ng.datasetacquisition.dto.DatasetAcquisitionDTO;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.examination.dto.ExaminationDTO;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.importer.dto.ImportJob;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.study.rights.StudyRightsService;
import org.shanoir.ng.study.rights.StudyUser;
import org.shanoir.ng.study.rights.StudyUserRightsRepository;
import org.shanoir.ng.utils.ModelsUtil;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

/**
 * User security service test.
 * 
 * @author jlouis
 * 
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class DatasetAcquisitionApiSecurityTest {
	
	private static final long LOGGED_USER_ID = 2L;
	private static final String LOGGED_USER_USERNAME = "logged";
	
	@Autowired
	private DatasetAcquisitionApi api;
	
	@MockBean
	private StudyUserRightsRepository rightsRepository;
	
	@MockBean
	private StudyRightsService commService;
	
	private BindingResult mockBindingResult;
	
	
	@Before
	public void setup() {
		mockBindingResult = new BeanPropertyBindingResult(mockDsAcq(1L), "datasetAcquisition");
		given(commService.hasRightOnStudy(Mockito.anyLong(), Mockito.anyString())).willReturn(false);
		given(commService.hasRightOnStudies(Mockito.any(), Mockito.anyString())).willReturn(new HashSet<Long>());
		given(rightsRepository.findByUserIdAndStudyId(Mockito.anyLong(), Mockito.anyLong())).willReturn( new StudyUser());
	}
	
	@Test
	@WithAnonymousUser
	public void testAsAnonymous() throws ShanoirException, RestServiceException {
		given(commService.hasRightOnStudy(Mockito.anyLong(), Mockito.anyString())).willReturn(true);
		Set<Long> ids = Mockito.anySet();
		given(commService.hasRightOnStudies(ids, Mockito.anyString())).willReturn(ids);
		
		assertAccessDenied(api::findDatasetAcquisitionById, 1L);
		assertAccessDenied(api::findByStudyCard, 1L);
		assertAccessDenied(t -> { api.findDatasetAcquisitions(t); }, PageRequest.of(0, 10));
		assertAccessDenied(t -> { api.createNewDatasetAcquisition(t); }, new ImportJob());
		assertAccessDenied((t, u, v) -> { api.updateDatasetAcquisition(t, u, v); }, 1L, mockDsAcqDTO(1L), mockBindingResult);
		assertAccessDenied(t -> { api.deleteDatasetAcquisition(t); }, 1L);
	}
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_USER" })
	public void testAsUser() throws ShanoirException, RestServiceException {
		given(commService.hasRightOnStudy(1L, "CAN_SEE_ALL")).willReturn(true);
		assertAccessAuthorized(api::findDatasetAcquisitionById, 1L);
		assertAccessDenied(api::findDatasetAcquisitionById, 3L);
		
		given(commService.hasRightOnStudies(Mockito.anySet(), Mockito.anyString())).willReturn(new HashSet<Long>());
		assertAccessAuthorized(t -> { api.findDatasetAcquisitions(t); }, PageRequest.of(0, 10));
		assertAccessAuthorized(api::findByStudyCard, 1L);
		assertNull(api.findDatasetAcquisitions(PageRequest.of(0, 10)).getBody());
		assertNull(api.findByStudyCard(new Long(1L)).getBody());
		Set<Long> ids = new HashSet<>(); ids.add(1L); ids.add(2L);
		given(commService.hasRightOnStudies(Mockito.anySet(), Mockito.anyString())).willReturn(ids);
		
		//assertEquals(2, api.findDatasetAcquisitions(PageRequest.of(0, 10)).getBody().getTotalElements());
		//assertEquals(2, api.findByStudyCard(new Long(1L)).getBody().size());

		ImportJob importJob = new ImportJob(); importJob.setExaminationId(1L);
		assertAccessDenied(t -> { api.createNewDatasetAcquisition(t); }, new ImportJob());
		assertAccessDenied((t, u, v) -> { api.updateDatasetAcquisition(t, u, v); }, 1L, mockDsAcqDTO(1L), mockBindingResult);
		assertAccessDenied(t -> { api.deleteDatasetAcquisition(t); }, 1L);

	}
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_EXPERT" })
	public void testAsExpert() throws ShanoirException, RestServiceException {
		given(commService.hasRightOnStudy(1L, "CAN_SEE_ALL")).willReturn(true);
		assertAccessAuthorized(api::findDatasetAcquisitionById, 1L);
		assertAccessDenied(api::findDatasetAcquisitionById, 3L);
		
		given(commService.hasRightOnStudies(Mockito.anySet(), Mockito.anyString())).willReturn(new HashSet<Long>());
		assertAccessAuthorized(t -> { api.findDatasetAcquisitions(t); }, PageRequest.of(0, 10));
		assertAccessAuthorized(api::findByStudyCard, 1L);
		assertNull(api.findDatasetAcquisitions(PageRequest.of(0, 10)).getBody());
		assertNull(api.findByStudyCard(new Long(1L)).getBody());
		Set<Long> ids = new HashSet<>(); ids.add(1L); ids.add(2L);
		given(commService.hasRightOnStudies(Mockito.anySet(), Mockito.anyString())).willReturn(ids);
		//assertEquals(2, api.findDatasetAcquisitions(PageRequest.of(0, 10)).getBody().getTotalElements());
		//assertEquals(2, api.findByStudyCard(new Long(1L)).getBody().size());
		
		given(commService.hasRightOnStudy(1L, "CAN_IMPORT")).willReturn(true);
		Examination exam = ModelsUtil.createExamination(); exam.setId(1L);
		ImportJob importJob = new ImportJob();
		importJob.setStudyId(2L);
		importJob.setExaminationId(3L);
		assertAccessDenied(t -> { api.createNewDatasetAcquisition(t); }, importJob);
		importJob.setStudyId(1L);
		importJob.setExaminationId(1L);
		assertAccessAuthorized(t -> { api.createNewDatasetAcquisition(t); }, importJob);
		
//		DatasetAcquisitionDTO dsAcqDB = api.findDatasetAcquisitionById(1L).getBody();
//		given(commService.hasRightOnStudy(1L, "CAN_ADMINISTRATE")).willReturn(true);
//		given(commService.hasRightOnStudy(3L, "CAN_ADMINISTRATE")).willReturn(false);
//		dsAcqDB.setRank(1000);
//		dsAcqDB.getExamination().setStudyId(3L);
//		assertAccessDenied((t, u, v) -> { api.updateDatasetAcquisition(t, u, v); }, 1L, dsAcqDB, mockBindingResult);
//		assertAccessDenied((t, u, v) -> { api.updateDatasetAcquisition(t, u, v); }, 3L, dsAcqDB, mockBindingResult);
//		dsAcqDB.getExamination().setStudyId(1L);
//		assertAccessAuthorized((t, u, v) -> { api.updateDatasetAcquisition(t, u, v); } catch (RestServiceException e1) { } }, 1L, dsAcqDB, mockBindingResult);
		
		assertAccessDenied(t -> { api.deleteDatasetAcquisition(t); }, 3L);
		assertAccessAuthorized(t -> { api.deleteDatasetAcquisition(t); }, 4L);
	}

	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_ADMIN" })
	public void testAsAdmin() throws ShanoirException, RestServiceException {
		given(commService.hasRightOnStudy(Mockito.anyLong(), Mockito.anyString())).willReturn(true);
		Set<Long> ids = Mockito.anySet();
		given(commService.hasRightOnStudies(ids, Mockito.anyString())).willReturn(ids);
		
		assertAccessAuthorized(api::findDatasetAcquisitionById, 1L);
		assertAccessAuthorized(api::findByStudyCard, 1L);
		assertAccessAuthorized(t -> { api.findDatasetAcquisitions(t); }, PageRequest.of(0, 10));
		//assertEquals(3, api.findDatasetAcquisitions(PageRequest.of(0, 10)).getBody().getTotalElements());

		assertAccessAuthorized(t -> { api.createNewDatasetAcquisition(t); }, new ImportJob());
		assertAccessAuthorized((t, u, v) -> { api.updateDatasetAcquisition(t, u, v); }, 1L, mockDsAcqDTO(1L), mockBindingResult);
		assertAccessAuthorized(t -> { api.deleteDatasetAcquisition(t); }, 4L);
	}


	private DatasetAcquisition mockDsAcq(Long id) {
		DatasetAcquisition dsA = ModelsUtil.createDatasetAcq();
		dsA.setId(id);
		return dsA;
	}
	
	private DatasetAcquisitionDTO mockDsAcqDTO(Long id) {
		DatasetAcquisitionDTO dto = new DatasetAcquisitionDTO();
		dto.setId(id);
		dto.setAcquisitionEquipmentId(1L);
		dto.setExamination(new ExaminationDTO());
		dto.setRank(1);
		dto.setSoftwareRelease("v1.0.0");
		dto.setSortingIndex(1);
		dto.setStudyCard(ModelsUtil.createStudyCard());
		dto.setStudyCardTimestamp(10000L);
		dto.setType("Mr");
		return dto;
	}
	
	private DatasetAcquisition mockDsAcq() {
		return mockDsAcq(null);
	}

}
