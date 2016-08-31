package org.shanoir.studycard.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.studycard.model.StudyCard;
import org.shanoir.studycard.service.impl.StudyCardServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author msimon
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class StudyCardServiceIntegrationTest {

	private static final Long bigLongId = 1000000L;
	
	@Autowired
	private StudyCardServiceImpl studyCardService;

	private final StudyCard newStudyCard = new StudyCard(0, "test0", false, 1L, 1L, 1L);
	private final StudyCard studyCard = new StudyCard(1, "test0Bis", false, 1L, 1L, 1L);

	@Test
	public void testFindAll() throws Exception {
		StudyCard sc = studyCardService.save(newStudyCard);
		assertThat(studyCardService.findAll()).size().isGreaterThan(0);
		
		studyCardService.deleteById(sc.getId());
	}

	@Test
	public void testFindById() throws Exception {
		StudyCard sc = studyCardService.save(newStudyCard);
		
		StudyCard dbStudyCard = studyCardService.findById(sc.getId());
		assertThat(dbStudyCard).isNotNull();
		assertThat(sc.getName()).isEqualTo("test0");
		
		studyCardService.deleteById(sc.getId());
	}

	@Test
	public void testSave() throws Exception {
		StudyCard sc = studyCardService.save(newStudyCard);
		assertThat(sc).isNotNull();
		assertThat(sc.getName()).isEqualTo("test0");
		
		studyCardService.deleteById(sc.getId());
	}

	@Test
	public void testUpdate() throws Exception {
		StudyCard sc = studyCardService.save(newStudyCard);
		
		StudyCard updatedStudyCard = studyCardService.update(sc.getId(), studyCard);
		assertThat(updatedStudyCard).isNotNull();
		assertThat(updatedStudyCard.getName()).isEqualTo("test0Bis");
		
		studyCardService.deleteById(sc.getId());
	}

	@Test
	public void testBadUpdate() throws Exception {
		StudyCard updatedStudyCard = studyCardService.update(bigLongId, studyCard);
		assertThat(updatedStudyCard).isNull();
	}

	@Test
	public void testDelete() throws Exception {
		StudyCard sc = studyCardService.save(newStudyCard);
		StudyCard dbStudyCard = studyCardService.findById(sc.getId());
		assertThat(dbStudyCard).isNotNull();
		assertThat(sc.getName()).isEqualTo("test0");
		
		studyCardService.deleteById(sc.getId());
		
		dbStudyCard = studyCardService.findById(sc.getId());
		assertThat(dbStudyCard).isNull();
	}

	@Test(expected = EmptyResultDataAccessException.class)
	public void testDeleteBadId() throws Exception {
		studyCardService.deleteById(bigLongId);
	}

}
