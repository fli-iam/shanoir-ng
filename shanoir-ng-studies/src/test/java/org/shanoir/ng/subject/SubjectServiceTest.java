package org.shanoir.ng.subject;

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
import org.shanoir.ng.shared.exception.ShanoirSubjectException;
import org.shanoir.ng.utils.ModelsUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * Subject service test.
 * 
 * @author msimon
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class SubjectServiceTest {

	private static final Long SUBJECT_ID = 1L;
	private static final String UPDATED_SUBJECT_DATA = "subject1";

	@Mock
	private SubjectRepository subjectRepository;

	@Mock
	private RabbitTemplate rabbitTemplate;

	@InjectMocks
	private SubjectServiceImpl subjectService;

	@Before
	public void setup() {
		given(subjectRepository.findAll()).willReturn(Arrays.asList(ModelsUtil.createSubject()));
		given(subjectRepository.findOne(SUBJECT_ID)).willReturn(ModelsUtil.createSubject());
		given(subjectRepository.save(Mockito.any(Subject.class))).willReturn(ModelsUtil.createSubject());
	}

	@Test
	public void deleteByIdTest() throws ShanoirSubjectException {
		subjectService.deleteById(SUBJECT_ID);

		Mockito.verify(subjectRepository, Mockito.times(1)).delete(Mockito.anyLong());
	}

	@Test
	public void findAllTest() {
		final List<Subject> subjects = subjectService.findAll();
		Assert.assertNotNull(subjects);
		Assert.assertTrue(subjects.size() == 1);

		Mockito.verify(subjectRepository, Mockito.times(1)).findAll();
	}

	@Test
	public void findByIdTest() {
		final Subject subject = subjectService.findById(SUBJECT_ID);
		Assert.assertNotNull(subject);
		Assert.assertTrue(ModelsUtil.SUBJECT_NAME.equals(subject.getName()));

		Mockito.verify(subjectRepository, Mockito.times(1)).findOne(Mockito.anyLong());
	}

	@Test
	public void saveTest() throws ShanoirSubjectException {
		subjectService.save(createSubject());

		Mockito.verify(subjectRepository, Mockito.times(1)).save(Mockito.any(Subject.class));
	}

	@Test
	public void updateTest() throws ShanoirSubjectException {
		final Subject updatedSubject = subjectService.update(createSubject());
		Assert.assertNotNull(updatedSubject);
		Assert.assertTrue(UPDATED_SUBJECT_DATA.equals(updatedSubject.getName()));

		Mockito.verify(subjectRepository, Mockito.times(1)).save(Mockito.any(Subject.class));
	}

	@Test
	public void updateFromShanoirOldTest() throws ShanoirSubjectException {
		subjectService.updateFromShanoirOld(createSubject());

		Mockito.verify(subjectRepository, Mockito.times(1)).findOne(Mockito.anyLong());
		Mockito.verify(subjectRepository, Mockito.times(1)).save(Mockito.any(Subject.class));
	}

	private Subject createSubject() {
		final Subject subject = new Subject();
		subject.setId(SUBJECT_ID);
		subject.setName(UPDATED_SUBJECT_DATA);
		return subject;
	}

}
