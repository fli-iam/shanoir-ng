package org.shanoir.ng.examination;

import static org.mockito.BDDMockito.given;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.shanoir.ng.shared.exception.ShanoirDatasetException;
import org.shanoir.ng.utils.ModelsUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * Examination service test.
 * 
 * @author ifakhfakh
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class ExaminationServiceTest {

	private static final Long EXAMINATION_ID = 1L;
	private static final String UPDATED_EXAMINATION_COMMENT = "examination 2";

	@Mock
	private ExaminationRepository examinationRepository;

	@Mock
	private RabbitTemplate rabbitTemplate;

	@InjectMocks
	private ExaminationServiceImpl examinationService;

	@Before
	public void setup() {
		given(examinationRepository.findAll()).willReturn(Arrays.asList(ModelsUtil.createExamination()));
		given(examinationRepository.findOne(EXAMINATION_ID)).willReturn(ModelsUtil.createExamination());
		given(examinationRepository.save(Mockito.any(Examination.class))).willReturn(ModelsUtil.createExamination());
	}

	@Test
	public void deleteByIdTest() throws ShanoirDatasetException {
		examinationService.deleteById(EXAMINATION_ID);

		Mockito.verify(examinationRepository, Mockito.times(1)).delete(Mockito.anyLong());
	}

	@Test
	public void findAllTest() throws ShanoirDatasetException {
		final List<ExaminationDTO> examination = examinationService.findAll();
		Assert.assertNotNull(examination);
		Assert.assertTrue(examination.size() == 1);

		Mockito.verify(examinationRepository, Mockito.times(1)).findAll();
	}

	@Test
	public void findByIdTest() throws ShanoirDatasetException {
		final ExaminationDTO examination = examinationService.findById(EXAMINATION_ID);
		Assert.assertNotNull(examination);
		Assert.assertTrue(ModelsUtil.EXAMINATION_NOTE.equals(examination.getNote()));

		Mockito.verify(examinationRepository, Mockito.times(1)).findOne(Mockito.anyLong());
	}

	@Test
	public void saveTest() throws ShanoirDatasetException {
		examinationService.save(createExamination());

		Mockito.verify(examinationRepository, Mockito.times(1)).save(Mockito.any(Examination.class));
	}

	@Test
	public void updateTest() throws ShanoirDatasetException {
		final Examination updatedExamination = examinationService.update(createExamination());
		Assert.assertNotNull(updatedExamination);
		Assert.assertTrue(UPDATED_EXAMINATION_COMMENT.equals(updatedExamination.getComment()));

		Mockito.verify(examinationRepository, Mockito.times(1)).save(Mockito.any(Examination.class));
	}

	@Test
	public void updateFromShanoirOldTest() throws ShanoirDatasetException {
		examinationService.updateFromShanoirOld(createExamination());

		Mockito.verify(examinationRepository, Mockito.times(1)).findOne(Mockito.anyLong());
		Mockito.verify(examinationRepository, Mockito.times(1)).save(Mockito.any(Examination.class));
	}

	private Examination createExamination() {
		final Examination examination = new Examination();
		examination.setId(EXAMINATION_ID);
		examination.setComment(UPDATED_EXAMINATION_COMMENT);
		return examination;
	}

}
