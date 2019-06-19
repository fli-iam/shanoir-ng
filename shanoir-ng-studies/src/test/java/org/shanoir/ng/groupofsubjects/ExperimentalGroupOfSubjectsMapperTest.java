package org.shanoir.ng.groupofsubjects;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.shared.core.model.IdName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Subject - study mapper test.
 * 
 * @author msimon
 * 
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ExperimentalGroupOfSubjectsMapperTest {

	private static final Long GROUP_OD_SUBJECTS_ID = 1L;

	@Autowired
	private ExperimentalGroupOfSubjectsMapper experimentalGroupOfSubjectsMapper;

	@Test
	public void experimentalGroupOfSubjectsToIdNameDTOsTest() {
		final List<IdName> subjectStudyDTOs = experimentalGroupOfSubjectsMapper
				.experimentalGroupOfSubjectsToIdNameDTOs(Arrays.asList(createExperimentalGroupOfSubjects()));
		Assert.assertNotNull(subjectStudyDTOs);
		Assert.assertTrue(subjectStudyDTOs.size() == 1);
		Assert.assertTrue(subjectStudyDTOs.get(0).getId().equals(GROUP_OD_SUBJECTS_ID));
	}

	@Test
	public void experimentalGroupOfSubjectsToIdNameDTOTest() {
		final IdName subjectStudyDTO = experimentalGroupOfSubjectsMapper
				.experimentalGroupOfSubjectsToIdNameDTO(createExperimentalGroupOfSubjects());
		Assert.assertNotNull(subjectStudyDTO);
		Assert.assertTrue(subjectStudyDTO.getId().equals(GROUP_OD_SUBJECTS_ID));
	}

	private ExperimentalGroupOfSubjects createExperimentalGroupOfSubjects() {
		final ExperimentalGroupOfSubjects groupOfSubjects = new ExperimentalGroupOfSubjects();
		groupOfSubjects.setId(GROUP_OD_SUBJECTS_ID);
		return groupOfSubjects;
	}

}
