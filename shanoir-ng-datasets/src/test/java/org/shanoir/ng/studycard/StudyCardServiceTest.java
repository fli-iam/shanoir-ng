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
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.studycard.model.StudyCard;
import org.shanoir.ng.studycard.repository.StudyCardRepository;
import org.shanoir.ng.studycard.service.StudyCardServiceImpl;
import org.shanoir.ng.utils.ModelsUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * Study card service test.
 * 
 * @author msimon
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class StudyCardServiceTest {

	private static final Long TEMPLATE_ID = 1L;
	private static final String UPDATED_STUDYCARD_DATA = "StudyCard1";

	@Mock
	private StudyCardRepository studyCardRepository;

	@Mock
	private RabbitTemplate rabbitTemplate;

	@InjectMocks
	private StudyCardServiceImpl studyCardService;

	@Before
	public void setup() {
		given(studyCardRepository.findAll()).willReturn(Arrays.asList(ModelsUtil.createStudyCard()));
		given(studyCardRepository.findById(TEMPLATE_ID).orElse(null)).willReturn(ModelsUtil.createStudyCard());
		given(studyCardRepository.save(Mockito.any(StudyCard.class))).willReturn(ModelsUtil.createStudyCard());
	}

	@Test
	public void deleteByIdTest() throws EntityNotFoundException, MicroServiceCommunicationException {
		studyCardService.deleteById(TEMPLATE_ID);
		Mockito.verify(studyCardRepository, Mockito.times(1)).deleteById(Mockito.anyLong());
	}

	@Test
	public void findAllTest() {
		final List<StudyCard> studyCards = studyCardService.findAll();
		Assert.assertNotNull(studyCards);
		Assert.assertTrue(studyCards.size() == 1);

		Mockito.verify(studyCardRepository, Mockito.times(1)).findAll();
	}

	@Test
	public void findByIdTest() {
		final StudyCard studyCard = studyCardService.findById(TEMPLATE_ID);
		Assert.assertNotNull(studyCard);

		Mockito.verify(studyCardRepository, Mockito.times(1)).findById(Mockito.anyLong());
	}

	@Test
	public void saveTest() throws MicroServiceCommunicationException {
		studyCardService.save(createStudyCard());

		Mockito.verify(studyCardRepository, Mockito.times(1)).save(Mockito.any(StudyCard.class));
	}

	@Test
	public void updateTest() throws EntityNotFoundException, MicroServiceCommunicationException {
		final StudyCard updatedStudyCard = studyCardService.update(createStudyCard());
		Assert.assertNotNull(updatedStudyCard);
		Assert.assertTrue(UPDATED_STUDYCARD_DATA.equals(updatedStudyCard.getName()));

		Mockito.verify(studyCardRepository, Mockito.times(1)).save(Mockito.any(StudyCard.class));
	}

	private StudyCard createStudyCard() {
		final StudyCard studyCard = new StudyCard();
		studyCard.setId(TEMPLATE_ID);
		studyCard.setName(UPDATED_STUDYCARD_DATA);
		return studyCard;
	}

}
