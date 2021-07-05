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
import org.shanoir.ng.utils.ModelsUtil;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
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
@SpringBootTest
@EnableGlobalMethodSecurity(prePostEnabled = true)
@ActiveProfiles("test")
public class DatasetAcquisitionApiSecurityTest {
	
	private static final long LOGGED_USER_ID = 2L;
	private static final String LOGGED_USER_USERNAME = "logged";
	
	@Autowired
	private DatasetAcquisitionApi api;
	
	@MockBean
	private StudyRightsService commService;
	
	private BindingResult mockBindingResult;
	
	
	@Before
	public void setup() {
		mockBindingResult = new BeanPropertyBindingResult(mockDsAcq(1L), "datasetAcquisition");
		given(commService.hasRightOnStudy(Mockito.anyLong(), Mockito.anyString())).willReturn(false);
		given(commService.hasRightOnStudies(Mockito.any(), Mockito.anyString())).willReturn(new HashSet<Long>());
	}
	
	@Test
	@WithAnonymousUser
	public void testAsAnonymous() throws ShanoirException, RestServiceException {
		given(commService.hasRightOnStudy(Mockito.anyLong(), Mockito.anyString())).willReturn(true);
		Set<Long> ids = Mockito.anySetOf(Long.class);
		given(commService.hasRightOnStudies(ids, Mockito.anyString())).willReturn(ids);
		
		assertAccessDenied(api::findDatasetAcquisitionById, 1L);
		assertAccessDenied(api::findByStudyCard, 1L);
		assertAccessDenied(t -> { try { api.findDatasetAcquisitions(t); } catch (RestServiceException e2) { fail(e2.toString());}}, PageRequest.of(0, 10));
		assertAccessDenied(t -> { try { api.createNewDatasetAcquisition(t); } catch (RestServiceException e1) { fail(e1.toString()); } }, new ImportJob());
		assertAccessDenied((t, u, v) -> { try { api.updateDatasetAcquisition(t, u, v); } catch (RestServiceException e) { fail(e.toString()); }}, 1L, mockDsAcqDTO(1L), mockBindingResult);
		assertAccessDenied(t -> { try { api.deleteDatasetAcquisition(t); } catch (RestServiceException e) { fail(e.toString()); }}, 1L);
	}
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_USER" })
	public void testAsUser() throws ShanoirException, RestServiceException {
		given(commService.hasRightOnStudy(1L, "CAN_SEE_ALL")).willReturn(true);
		assertAccessAuthorized(api::findDatasetAcquisitionById, 1L);
		assertAccessDenied(api::findDatasetAcquisitionById, 3L);
		
		given(commService.hasRightOnStudies(Mockito.anySetOf(Long.class), Mockito.anyString())).willReturn(new HashSet<Long>());
		assertAccessAuthorized(t -> { try { api.findDatasetAcquisitions(t); } catch (RestServiceException e2) { }}, PageRequest.of(0, 10));
		assertAccessAuthorized(api::findByStudyCard, 1L);
		assertNull(api.findDatasetAcquisitions(PageRequest.of(0, 10)).getBody());
		assertNull(api.findByStudyCard(new Long(1L)).getBody());
		Set<Long> ids = new HashSet<>(); ids.add(1L); ids.add(2L);
		given(commService.hasRightOnStudies(Mockito.anySetOf(Long.class), Mockito.anyString())).willReturn(ids);
		
		//assertEquals(2, api.findDatasetAcquisitions(PageRequest.of(0, 10)).getBody().getTotalElements());
		//assertEquals(2, api.findByStudyCard(new Long(1L)).getBody().size());

		ImportJob importJob = new ImportJob(); importJob.setExaminationId(1L);
		assertAccessDenied(t -> { try { api.createNewDatasetAcquisition(t); } catch (RestServiceException e1) { fail(e1.toString()); } }, new ImportJob());
		assertAccessDenied((t, u, v) -> { try { api.updateDatasetAcquisition(t, u, v); } catch (RestServiceException e) { fail(e.toString()); }}, 1L, mockDsAcqDTO(1L), mockBindingResult);
		assertAccessDenied(t -> { try { api.deleteDatasetAcquisition(t); } catch (RestServiceException e) { fail(e.toString()); }}, 1L);

	}
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_EXPERT" })
	public void testAsExpert() throws ShanoirException, RestServiceException {
		given(commService.hasRightOnStudy(1L, "CAN_SEE_ALL")).willReturn(true);
		assertAccessAuthorized(api::findDatasetAcquisitionById, 1L);
		assertAccessDenied(api::findDatasetAcquisitionById, 3L);
		
		given(commService.hasRightOnStudies(Mockito.anySetOf(Long.class), Mockito.anyString())).willReturn(new HashSet<Long>());
		assertAccessAuthorized(t -> { try { api.findDatasetAcquisitions(t); } catch (RestServiceException e2) { }}, PageRequest.of(0, 10));
		assertAccessAuthorized(api::findByStudyCard, 1L);
		assertNull(api.findDatasetAcquisitions(PageRequest.of(0, 10)).getBody());
		assertNull(api.findByStudyCard(new Long(1L)).getBody());
		Set<Long> ids = new HashSet<>(); ids.add(1L); ids.add(2L);
		given(commService.hasRightOnStudies(Mockito.anySetOf(Long.class), Mockito.anyString())).willReturn(ids);
		//assertEquals(2, api.findDatasetAcquisitions(PageRequest.of(0, 10)).getBody().getTotalElements());
		//assertEquals(2, api.findByStudyCard(new Long(1L)).getBody().size());
		
		given(commService.hasRightOnStudy(1L, "CAN_IMPORT")).willReturn(true);
		Examination exam = ModelsUtil.createExamination(); exam.setId(1L);
		ImportJob importJob = new ImportJob();
		importJob.setStudyId(2L);
		importJob.setExaminationId(3L);
		assertAccessDenied(t -> { try { api.createNewDatasetAcquisition(t); } catch (RestServiceException e1) { fail(e1.toString()); } }, importJob);
		importJob.setStudyId(1L);
		importJob.setExaminationId(1L);
		assertAccessAuthorized(t -> { try { api.createNewDatasetAcquisition(t); } catch (RestServiceException e1) {} }, importJob);
		
//		DatasetAcquisitionDTO dsAcqDB = api.findDatasetAcquisitionById(1L).getBody();
//		given(commService.hasRightOnStudy(1L, "CAN_ADMINISTRATE")).willReturn(true);
//		given(commService.hasRightOnStudy(3L, "CAN_ADMINISTRATE")).willReturn(false);
//		dsAcqDB.setRank(1000);
//		dsAcqDB.getExamination().setStudyId(3L);
//		assertAccessDenied((t, u, v) -> { try { api.updateDatasetAcquisition(t, u, v); } catch (RestServiceException e1) { fail(e1.toString()); } }, 1L, dsAcqDB, mockBindingResult);
//		assertAccessDenied((t, u, v) -> { try { api.updateDatasetAcquisition(t, u, v); } catch (RestServiceException e1) { fail(e1.toString()); } }, 3L, dsAcqDB, mockBindingResult);
//		dsAcqDB.getExamination().setStudyId(1L);
//		assertAccessAuthorized((t, u, v) -> { try { api.updateDatasetAcquisition(t, u, v); } catch (RestServiceException e1) { } }, 1L, dsAcqDB, mockBindingResult);
		
		assertAccessDenied(t -> { try { api.deleteDatasetAcquisition(t); } catch (RestServiceException e) { fail(e.toString()); }}, 3L);
		assertAccessAuthorized(t -> { try { api.deleteDatasetAcquisition(t); } catch (RestServiceException e) { }}, 4L);
	}

	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_ADMIN" })
	public void testAsAdmin() throws ShanoirException, RestServiceException {
		given(commService.hasRightOnStudy(Mockito.anyLong(), Mockito.anyString())).willReturn(true);
		Set<Long> ids = Mockito.anySetOf(Long.class);
		given(commService.hasRightOnStudies(ids, Mockito.anyString())).willReturn(ids);
		
		assertAccessAuthorized(api::findDatasetAcquisitionById, 1L);
		assertAccessAuthorized(api::findByStudyCard, 1L);
		assertAccessAuthorized(t -> { try { api.findDatasetAcquisitions(t); } catch (RestServiceException e2) { }}, PageRequest.of(0, 10));
		//assertEquals(3, api.findDatasetAcquisitions(PageRequest.of(0, 10)).getBody().getTotalElements());

		assertAccessAuthorized(t -> { try { api.createNewDatasetAcquisition(t); } catch (RestServiceException e1) {} }, new ImportJob());
		assertAccessAuthorized((t, u, v) -> { try { api.updateDatasetAcquisition(t, u, v); } catch (RestServiceException e) { }}, 1L, mockDsAcqDTO(1L), mockBindingResult);
		assertAccessAuthorized(t -> { try { api.deleteDatasetAcquisition(t); } catch (RestServiceException e) { }}, 4L);
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
