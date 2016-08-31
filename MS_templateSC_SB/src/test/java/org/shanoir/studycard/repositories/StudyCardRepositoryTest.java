package org.shanoir.studycard.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.studycard.model.StudyCard;
import org.shanoir.studycard.repositories.StudyCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author msimon
 *
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {RepositoryConfiguration.class})
public class StudyCardRepositoryTest {

	@Autowired
	private StudyCardRepository studyCardRepository;
	
	private final StudyCard newStudyCard = new StudyCard(0, "test0", false, 1L, 1L, 1L);

	@Test
	public void testFindById() throws Exception {
		Long newSCId = studyCardRepository.save(newStudyCard).getId();
		assertThat(newSCId).isNotNull();
		
		StudyCard studyCard = studyCardRepository.findById(newSCId);
		assertThat(studyCard).isNotNull();
		assertThat(studyCard.getName()).isEqualTo("test0");
		
		studyCardRepository.delete(newSCId);
	}

	@Test
	public void testFindFirstByName() throws Exception {
		StudyCard studyCard = studyCardRepository.findFirstByName("test0");
		assertThat(studyCard).isNull();
		
		studyCardRepository.save(newStudyCard);
		
		studyCard = studyCardRepository.findFirstByName("test0");
		assertThat(studyCard).isNotNull();
		assertThat(studyCard.getId()).isEqualTo(1);
		
		studyCardRepository.delete(1L);
	}

}
