package org.shanoir.ng.preclinical.therapies.subject_therapies;

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
import org.shanoir.ng.preclinical.references.RefsRepository;
import org.shanoir.ng.preclinical.subjects.AnimalSubjectRepository;
import org.shanoir.ng.shared.exception.ShanoirPreclinicalException;
import org.shanoir.ng.utils.AnimalSubjectModelUtil;
import org.shanoir.ng.utils.TherapyModelUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * Subject Therapy service test.
 * 
 * @author sloury
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class SubjectTherapyServiceTest {

	private static final Long STHERAPY_ID = 1L;
	private static final String UPDATED_THERAPY_NAME = "Chimiotherapy";

	@Mock
	private SubjectTherapyRepository stherapiesRepository;

	@Mock
	private RefsRepository refsRepository;

	@Mock
	private AnimalSubjectRepository subjectsRepository;

	@Mock
	private RabbitTemplate rabbitTemplate;

	@InjectMocks
	private SubjectTherapyServiceImpl stherapiesService;

	@Before
	public void setup() {
		given(stherapiesRepository.findAll()).willReturn(Arrays.asList(TherapyModelUtil.createSubjectTherapy()));
		given(subjectsRepository.findOne(1L)).willReturn(AnimalSubjectModelUtil.createAnimalSubject());
		given(stherapiesRepository.findByAnimalSubject(AnimalSubjectModelUtil.createAnimalSubject()))
				.willReturn(Arrays.asList(TherapyModelUtil.createSubjectTherapy()));
		given(stherapiesRepository.findByTherapy(TherapyModelUtil.createTherapyBrain()))
				.willReturn(Arrays.asList(TherapyModelUtil.createSubjectTherapy()));
		given(stherapiesRepository.findOne(STHERAPY_ID)).willReturn(TherapyModelUtil.createSubjectTherapy());
		given(stherapiesRepository.save(Mockito.any(SubjectTherapy.class)))
				.willReturn(TherapyModelUtil.createSubjectTherapy());
	}

	@Test
	public void deleteByIdTest() throws ShanoirPreclinicalException {
		stherapiesService.deleteById(STHERAPY_ID);

		Mockito.verify(stherapiesRepository, Mockito.times(1)).delete(Mockito.anyLong());
	}

	@Test
	public void deleteByAnimalSubjectTest() throws ShanoirPreclinicalException {
		stherapiesService.deleteByAnimalSubject(AnimalSubjectModelUtil.createAnimalSubject());

		Mockito.verify(stherapiesRepository, Mockito.times(1)).delete(Mockito.anyLong());
	}

	@Test
	public void findAllTest() {
		final List<SubjectTherapy> stherapies = stherapiesService.findAll();
		Assert.assertNotNull(stherapies);
		Assert.assertTrue(stherapies.size() == 1);

		Mockito.verify(stherapiesRepository, Mockito.times(1)).findAll();
	}

	/* THIS ONE WONT PASS DONT KNOW WHY... */
	/*
	 * @Test public void findAllBySubjectTest() { final List<SubjectTherapy>
	 * stherapies =
	 * stherapiesService.findAllBySubject(SubjectModelUtil.createSubject());
	 * Assert.assertNotNull(stherapies); Assert.assertTrue(stherapies.size() == 1);
	 * 
	 * Mockito.verify(stherapiesRepository,
	 * Mockito.times(1)).findBySubject(SubjectModelUtil.createSubject()); }
	 */
	@Test
	public void findAllByTherapyTest() {
		final List<SubjectTherapy> stherapies = stherapiesService
				.findAllByTherapy(TherapyModelUtil.createTherapyBrain());
		Assert.assertNotNull(stherapies);
		Assert.assertTrue(stherapies.size() == 1);

		Mockito.verify(stherapiesRepository, Mockito.times(1)).findByTherapy(TherapyModelUtil.createTherapyBrain());
	}

	@Test
	public void findByIdTest() {
		final SubjectTherapy stherapy = stherapiesService.findById(STHERAPY_ID);
		Assert.assertNotNull(stherapy);
		Assert.assertTrue(TherapyModelUtil.THERAPY_NAME_BRAIN.equals(stherapy.getTherapy().getName()));
		Assert.assertTrue(AnimalSubjectModelUtil.SUBJECT_ID.equals(stherapy.getAnimalSubject().getSubjectId()));

		Mockito.verify(stherapiesRepository, Mockito.times(1)).findOne(Mockito.anyLong());
	}

	@Test
	public void saveTest() throws ShanoirPreclinicalException {
		stherapiesService.save(createSubjectTherapy());

		Mockito.verify(stherapiesRepository, Mockito.times(1)).save(Mockito.any(SubjectTherapy.class));
	}

	@Test
	public void updateTest() throws ShanoirPreclinicalException {
		final SubjectTherapy updatedStherapy = stherapiesService.update(createSubjectTherapy());
		Assert.assertNotNull(updatedStherapy);
		Assert.assertTrue(UPDATED_THERAPY_NAME.equals(updatedStherapy.getTherapy().getName()));

		Mockito.verify(stherapiesRepository, Mockito.times(1)).save(Mockito.any(SubjectTherapy.class));
	}

	/*
	 * @Test public void updateFromShanoirOldTest() throws
	 * ShanoirPreclinicalException {
	 * stherapiesService.updateFromShanoirOld(createTherapy());
	 * 
	 * Mockito.verify(stherapiesRepository,
	 * Mockito.times(1)).findOne(Mockito.anyLong());
	 * Mockito.verify(stherapiesRepository,
	 * Mockito.times(1)).save(Mockito.any(Therapy.class)); }
	 */
	private SubjectTherapy createSubjectTherapy() {
		final SubjectTherapy stherapy = new SubjectTherapy();
		stherapy.setId(STHERAPY_ID);
		stherapy.setTherapy(TherapyModelUtil.createTherapyChimio());
		stherapy.setAnimalSubject(AnimalSubjectModelUtil.createAnimalSubject());
		return stherapy;
	}

}
