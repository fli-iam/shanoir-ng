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

package org.shanoir.ng.studycard;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.shanoir.ng.dataset.security.DatasetSecurityService;
import org.shanoir.ng.datasetacquisition.service.DatasetAcquisitionService;
import org.shanoir.ng.download.WADODownloaderService;
import org.shanoir.ng.importer.service.DicomSEGAndSRImporterService;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.shared.validation.FindByRepository;
import org.shanoir.ng.solr.service.SolrService;
import org.shanoir.ng.studycard.controler.StudyCardApiController;
import org.shanoir.ng.studycard.model.StudyCard;
import org.shanoir.ng.studycard.service.CardsProcessingService;
import org.shanoir.ng.studycard.service.QualityCardService;
import org.shanoir.ng.studycard.service.StudyCardService;
import org.shanoir.ng.studycard.service.StudyCardUniqueConstraintManager;
import org.shanoir.ng.utils.ModelsUtil;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for study card controller.
 *
 * @author msimon
 *
 */

@WebMvcTest(controllers = {StudyCardApiController.class, StudyCardUniqueConstraintManager.class})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class StudyCardApiControllerTest {

	private static final String REQUEST_PATH = "/studycards";
	private static final String REQUEST_PATH_WITH_ID = REQUEST_PATH + "/1";

	private Gson gson;

	@Autowired
	private MockMvc mvc;

	@MockBean
	private StudyCardService studyCardServiceMock;
	
	@MockBean
    private QualityCardService qualityCardServiceMock;
	
	@MockBean
	private CardsProcessingService studyCardProcessingServiceMock;
	
	@MockBean
	private DatasetAcquisitionService datasetAcquisitionServiceMock;
	
	@MockBean
	private WADODownloaderService downloaderMock;
	
	@MockBean
	private FindByRepository<StudyCard> findByRepositoryMock;
	
	@MockBean(name = "datasetSecurityService")
	private DatasetSecurityService datasetSecurityService;

	@MockBean
	private DicomSEGAndSRImporterService dicomSRImporterService;
	
	@MockBean
	private SolrService solrService;

	@BeforeEach
	public void setup() throws EntityNotFoundException, MicroServiceCommunicationException {
		gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();
		StudyCard studyCardMock = new StudyCard();
		studyCardMock.setId(1L);
		doNothing().when(studyCardServiceMock).deleteById(1L);
		given(studyCardServiceMock.findAll()).willReturn(Arrays.asList(studyCardMock));
		given(studyCardServiceMock.findById(1L)).willReturn(studyCardMock);
		given(studyCardServiceMock.save(Mockito.mock(StudyCard.class))).willReturn(new StudyCard());
		given(findByRepositoryMock.findBy(Mockito.anyString(), Mockito.any(), Mockito.any())).willReturn(new ArrayList<StudyCard>());
		given(datasetSecurityService.filterCardList(Mockito.any(), Mockito.anyString())).willReturn(true);
		given(datasetSecurityService.hasRightOnStudy(Mockito.any(), Mockito.anyString())).willReturn(true);
	}

	@Test
	@WithMockKeycloakUser(id = 1, username = "test", authorities = { "ROLE_ADMIN" })
	public void deleteStudyCardTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.delete(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNoContent());
	}

	@Test
	@WithMockKeycloakUser(id = 1, username = "test", authorities = { "ROLE_USER" })
	public void findStudyCardByIdTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockKeycloakUser(id = 1, username = "test", authorities = { "ROLE_USER" })
	public void findStudyCardsTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockKeycloakUser(id = 1, username = "test", authorities = { "ROLE_ADMIN" })
	public void saveNewStudyCardTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post(REQUEST_PATH).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(gson.toJson(ModelsUtil.createStudyCard())))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockKeycloakUser(id = 1, username = "test", authorities = { "ROLE_ADMIN" })
	public void updateTemplateTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.put(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(gson.toJson(ModelsUtil.createStudyCard())))
				.andExpect(status().isNoContent());
	}

}
