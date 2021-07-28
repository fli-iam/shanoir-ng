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

package org.shanoir.ng.preclinical.subjects;

import static org.mockito.BDDMockito.given;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
import org.shanoir.ng.shared.exception.ShanoirException;
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
		given(subjectsRepository.findById(SUBJECT_ID)).willReturn(Optional.of(AnimalSubjectModelUtil.createAnimalSubject()));
		given(refsRepository.save(Mockito.any(Reference.class))).willReturn(AnimalSubjectModelUtil.createSpecie());
		given(subjectsRepository.save(Mockito.any(AnimalSubject.class)))
				.willReturn(AnimalSubjectModelUtil.createAnimalSubject());
	}

	@Test
	public void deleteByIdTest() throws ShanoirException {
		subjectsService.deleteById(SUBJECT_ID);

		Mockito.verify(subjectsRepository, Mockito.times(1)).deleteById(Mockito.anyLong());
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

		Mockito.verify(subjectsRepository, Mockito.times(1)).findById(Mockito.anyLong()).orElse(null);
	}

	@Test
	public void findByReferenceTest() {
		final List<AnimalSubject> subjects = subjectsService.findByReference(AnimalSubjectModelUtil.createSpecie());
		Assert.assertNotNull(subjects);
		Assert.assertTrue(subjects.size() == 1);

		Mockito.verify(subjectsRepository, Mockito.times(1)).findByReference(AnimalSubjectModelUtil.createSpecie());
	}

	@Test
	public void saveTest() throws ShanoirException {
		subjectsService.save(createAnimalSubject());

		Mockito.verify(subjectsRepository, Mockito.times(1)).save(Mockito.any(AnimalSubject.class));
	}

	@Test
	public void updateTest() throws ShanoirException {
		final AnimalSubject updatedSubject = subjectsService.update(createAnimalSubject());
		Assert.assertNotNull(updatedSubject);
		Assert.assertTrue(SUBJECT_ID.equals(updatedSubject.getId()));

		Mockito.verify(subjectsRepository, Mockito.times(1)).save(Mockito.any(AnimalSubject.class));
	}

	/*
	 * @Test public void updateFromShanoirOldTest() throws
	 * ShanoirException {
	 * subjectsService.updateFromShanoirOld(createSubject());
	 * 
	 * Mockito.verify(subjectsRepository,
	 * Mockito.times(1)).findById(Mockito.anyLong()).orElse(null);
	 * Mockito.verify(subjectsRepository,
	 * Mockito.times(1)).save(Mockito.any(Subject.class)); }
	 */
	private AnimalSubject createAnimalSubject() {
		final AnimalSubject animalSubject = new AnimalSubject();
		animalSubject.setId(SUBJECT_ID);
		return animalSubject;
	}

}
