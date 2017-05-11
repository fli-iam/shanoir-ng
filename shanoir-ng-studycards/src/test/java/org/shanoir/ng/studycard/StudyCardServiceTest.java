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
import org.shanoir.ng.shared.exception.ShanoirStudyCardsException;
import org.shanoir.ng.studycard.StudyCard;
import org.shanoir.ng.studycard.StudyCardRepository;
import org.shanoir.ng.studycard.StudyCardServiceImpl;
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
		given(studyCardRepository.findOne(TEMPLATE_ID)).willReturn(ModelsUtil.createStudyCard());
		given(studyCardRepository.save(Mockito.any(StudyCard.class))).willReturn(ModelsUtil.createStudyCard());
	}

	@Test
	public void deleteByIdTest() throws ShanoirStudyCardsException {
		studyCardService.deleteById(TEMPLATE_ID);

		Mockito.verify(studyCardRepository, Mockito.times(1)).delete(Mockito.anyLong());
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
		Assert.assertTrue(ModelsUtil.TEMPLATE_Name.equals(studyCard.getName()));

		Mockito.verify(studyCardRepository, Mockito.times(1)).findOne(Mockito.anyLong());
	}

	@Test
	public void saveTest() throws ShanoirStudyCardsException {
		studyCardService.save(createStudyCard());

		Mockito.verify(studyCardRepository, Mockito.times(1)).save(Mockito.any(StudyCard.class));
	}

	@Test
	public void updateTest() throws ShanoirStudyCardsException {
		final StudyCard updatedStudyCard = studyCardService.update(createStudyCard());
		Assert.assertNotNull(updatedStudyCard);
		Assert.assertTrue(UPDATED_STUDYCARD_DATA.equals(updatedStudyCard.getName()));

		Mockito.verify(studyCardRepository, Mockito.times(1)).save(Mockito.any(StudyCard.class));
	}

	@Test
	public void updateFromShanoirOldTest() throws ShanoirStudyCardsException {
		studyCardService.updateFromShanoirOld(createStudyCard());

		Mockito.verify(studyCardRepository, Mockito.times(1)).findOne(Mockito.anyLong());
		Mockito.verify(studyCardRepository, Mockito.times(1)).save(Mockito.any(StudyCard.class));
	}

	private StudyCard createStudyCard() {
		final StudyCard studyCard = new StudyCard();
		studyCard.setId(TEMPLATE_ID);
		studyCard.setName(UPDATED_STUDYCARD_DATA);
		return studyCard;
	}

}
