package org.shanoir.ng.examination;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.shanoir.ng.examination.service.ExaminationServiceImpl;
import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.paging.PageImpl;
import org.shanoir.ng.shared.service.MicroserviceRequestsService;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.utils.ModelsUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * Examination service test.
 * 
 * @author ifakhfakh
 * 
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(KeycloakUtil.class)
public class ExaminationServiceTest {

	private static final Long EXAMINATION_ID = 1L;
	private static final Long STUDY_ID = 1L;
	private static final String UPDATED_EXAMINATION_COMMENT = "examination 2";

	@Mock
	private ExaminationRepository examinationRepository;

	@Mock
	private KeycloakUtil keycloakUtil;

	@Mock
	private MicroserviceRequestsService microservicesRequestsService;

	@Mock
	private RabbitTemplate rabbitTemplate;

	@Mock
	private RestTemplate restTemplate;

	@InjectMocks
	private ExaminationServiceImpl examinationService;

	@Before
	public void setup() throws ShanoirException {
		// given(examinationRepository.findByStudyIdIn(Mockito.anyListOf(Long.class), Mockito.any(Pageable.class)))
		// 		.willReturn(Arrays.asList(ModelsUtil.createExamination()));
		given(examinationRepository.findOne(EXAMINATION_ID)).willReturn(ModelsUtil.createExamination());
		given(examinationRepository.save(Mockito.any(Examination.class))).willReturn(ModelsUtil.createExamination());
		given(examinationRepository.findByStudyIdIn(Mockito.anyListOf(Long.class), Mockito.any(Pageable.class))).willReturn(new PageImpl<Examination>(Arrays.asList(ModelsUtil.createExamination())));

		PowerMockito.mockStatic(KeycloakUtil.class);
		when(KeycloakUtil.getKeycloakHeader()).thenReturn(null);
	}

	@Test
	public void deleteByIdTest() throws ShanoirException {
		examinationService.deleteById(EXAMINATION_ID);

		Mockito.verify(examinationRepository, Mockito.times(1)).delete(Mockito.anyLong());
	}

	@Test
	public void findAllTest() throws ShanoirException {
		IdNameDTO idNameDTO = new IdNameDTO();
		idNameDTO.setId(STUDY_ID);
		IdNameDTO[] tab = { idNameDTO };
		given(restTemplate.exchange(Mockito.anyString(), Mockito.any(), Mockito.any(HttpEntity.class),
		Matchers.<Class<IdNameDTO[]>>any())).willReturn(new ResponseEntity<>(tab, HttpStatus.OK));
		final Page<Examination> examinations = examinationService.findPage(new PageRequest(0, 10));
		Assert.assertNotNull(examinations);
		Assert.assertTrue(examinations.getContent().size() == 1);

		Mockito.verify(examinationRepository, Mockito.times(1)).findByStudyIdIn(Arrays.asList(STUDY_ID), new PageRequest(0, 10));
	}

	@Test
	public void findByIdTest() throws ShanoirException {
		final Examination examination = examinationService.findById(EXAMINATION_ID);
		Assert.assertNotNull(examination);
		Assert.assertTrue(ModelsUtil.EXAMINATION_NOTE.equals(examination.getNote()));
	}

	@Test
	public void saveTest() throws ShanoirException {
		examinationService.save(createExamination());

		Mockito.verify(examinationRepository, Mockito.times(1)).save(Mockito.any(Examination.class));
	}

	@Test
	public void updateTest() throws ShanoirException {
		final Examination updatedExamination = examinationService.update(createExamination());
		Assert.assertNotNull(updatedExamination);
		Assert.assertTrue(UPDATED_EXAMINATION_COMMENT.equals(updatedExamination.getComment()));

		Mockito.verify(examinationRepository, Mockito.times(1)).save(Mockito.any(Examination.class));
	}

	private Examination createExamination() {
		final Examination examination = new Examination();
		examination.setId(EXAMINATION_ID);
		examination.setComment(UPDATED_EXAMINATION_COMMENT);
		return examination;
	}

}
