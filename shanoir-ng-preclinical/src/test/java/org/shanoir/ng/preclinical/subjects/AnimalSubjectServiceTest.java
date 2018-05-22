package org.shanoir.ng.preclinical.subjects;

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
import org.shanoir.ng.preclinical.references.Reference;
import org.shanoir.ng.preclinical.references.RefsRepository;
import org.shanoir.ng.shared.exception.ShanoirPreclinicalException;
import org.shanoir.ng.utils.AnimalSubjectModelUtil;
import org.shanoir.ng.utils.ReferenceModelUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * Subjects service test.
 * 
 * @author sloury
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class AnimalSubjectServiceTest {

	private static final Long SUBJECT_ID = 1L;
	private static final String UPDATED_SUBJECT_DATA = "subject73";
	private static final Long REF_ID = 1L;

	@Mock
	private AnimalSubjectRepository subjectsRepository;

	@Mock
	private RabbitTemplate rabbitTemplate;

	@InjectMocks
	private AnimalSubjectServiceImpl subjectsService;

	@Mock
	private RefsRepository refsRepository;

	@Before
	public void setup() {
		given(subjectsRepository.findAll()).willReturn(Arrays.asList(AnimalSubjectModelUtil.createAnimalSubject()));
		given(subjectsRepository.findByReference(ReferenceModelUtil.createReferenceSpecie()))
				.willReturn(Arrays.asList(AnimalSubjectModelUtil.createAnimalSubject()));
		given(subjectsRepository.findOne(SUBJECT_ID)).willReturn(AnimalSubjectModelUtil.createAnimalSubject());
		given(refsRepository.save(Mockito.any(Reference.class))).willReturn(AnimalSubjectModelUtil.createSpecie());
		given(subjectsRepository.save(Mockito.any(AnimalSubject.class)))
				.willReturn(AnimalSubjectModelUtil.createAnimalSubject());
	}

	@Test
	public void deleteByIdTest() throws ShanoirPreclinicalException {
		subjectsService.deleteById(SUBJECT_ID);

		Mockito.verify(subjectsRepository, Mockito.times(1)).delete(Mockito.anyLong());
	}

	@Test
	public void findAllTest() {
		final List<AnimalSubject> subjects = subjectsService.findAll();
		Assert.assertNotNull(subjects);
		Assert.assertTrue(subjects.size() == 1);

		Mockito.verify(subjectsRepository, Mockito.times(1)).findAll();
	}

	@Test
	public void findByIdTest() {
		final AnimalSubject subject = subjectsService.findById(SUBJECT_ID);
		Assert.assertNotNull(subject);
		Assert.assertTrue(AnimalSubjectModelUtil.SUBJECT_ID.equals(subject.getId()));

		Mockito.verify(subjectsRepository, Mockito.times(1)).findOne(Mockito.anyLong());
	}

	@Test
	public void findByReferenceTest() {
		final List<AnimalSubject> subjects = subjectsService.findByReference(AnimalSubjectModelUtil.createSpecie());
		Assert.assertNotNull(subjects);
		Assert.assertTrue(subjects.size() == 1);

		Mockito.verify(subjectsRepository, Mockito.times(1)).findByReference(AnimalSubjectModelUtil.createSpecie());
	}

	@Test
	public void saveTest() throws ShanoirPreclinicalException {
		subjectsService.save(createAnimalSubject());

		Mockito.verify(subjectsRepository, Mockito.times(1)).save(Mockito.any(AnimalSubject.class));
	}

	@Test
	public void updateTest() throws ShanoirPreclinicalException {
		final AnimalSubject updatedSubject = subjectsService.update(createAnimalSubject());
		Assert.assertNotNull(updatedSubject);
		Assert.assertTrue(SUBJECT_ID.equals(updatedSubject.getId()));

		Mockito.verify(subjectsRepository, Mockito.times(1)).save(Mockito.any(AnimalSubject.class));
	}

	/*
	 * @Test public void updateFromShanoirOldTest() throws
	 * ShanoirPreclinicalException {
	 * subjectsService.updateFromShanoirOld(createSubject());
	 * 
	 * Mockito.verify(subjectsRepository,
	 * Mockito.times(1)).findOne(Mockito.anyLong());
	 * Mockito.verify(subjectsRepository,
	 * Mockito.times(1)).save(Mockito.any(Subject.class)); }
	 */
	private AnimalSubject createAnimalSubject() {
		final AnimalSubject animalSubject = new AnimalSubject();
		animalSubject.setId(SUBJECT_ID);
		return animalSubject;
	}

}
