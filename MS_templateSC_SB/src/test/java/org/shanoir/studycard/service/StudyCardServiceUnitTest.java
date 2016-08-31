package org.shanoir.studycard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.shanoir.studycard.model.StudyCard;
import org.shanoir.studycard.repositories.StudyCardRepository;
import org.shanoir.studycard.service.impl.StudyCardServiceImpl;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author msimon
 *
 */
@RunWith(SpringRunner.class)
public class StudyCardServiceUnitTest {

	@Mock
	private StudyCardRepository studyCardRepository;
	
	@InjectMocks
	private StudyCardServiceImpl studyCardService;

	private final StudyCard studyCard = new StudyCard(1, "test1Bis", false, 1L, 1L, 1L);
	private final StudyCard updatedStudyCard = new StudyCard(1, "test1Ter", false, 1L, 1L, 1L);

	@Test
	public void testFindAll() throws Exception {
		given(studyCardRepository.findAll()).willReturn(Arrays.asList(studyCard));
		assertThat(studyCardService.findAll()).size().isEqualTo(1);
	}

	@Test
	public void testFindById() throws Exception {
		given(studyCardRepository.findById(1000)).willReturn(studyCard);
		
		StudyCard sc = studyCardService.findById(1000);
		assertThat(sc).isNotNull();
		assertThat(sc.getName()).isEqualTo("test1Bis");
	}

	@Test
	public void testSave() throws Exception {
		given(studyCardRepository.save(studyCard)).willReturn(studyCard);
		
		StudyCard sc = studyCardService.save(studyCard);
		assertThat(sc).isNotNull();
		assertThat(sc.getName()).isEqualTo("test1Bis");
	}

	@Test
	public void testUpdate() throws Exception {
		given(studyCardRepository.findOne(1L)).willReturn(studyCard);
		given(studyCardRepository.save(studyCard)).willReturn(updatedStudyCard);
		
		StudyCard sc = studyCardService.update(1L, studyCard);
		assertThat(sc).isNotNull();
		assertThat(sc.getName()).isEqualTo("test1Ter");
	}

	@Test
	public void testBadUpdate() throws Exception {
		given(studyCardRepository.findOne(1L)).willReturn(studyCard);
		given(studyCardRepository.save(studyCard)).willReturn(updatedStudyCard);
		
		StudyCard sc = studyCardService.update(2L, studyCard);
		assertThat(sc).isNull();
	}

}
