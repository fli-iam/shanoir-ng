package org.shanoir.ng.study;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.study.dto.StudyDTO;
import org.shanoir.ng.study.dto.mapper.StudyMapper;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.timepoint.TimepointMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Study mapper test.
 * 
 * @author msimon
 * 
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class StudyMapperTest {

	private static final Long STUDY_ID = 1L;
	private static final String STUDY_NAME = "test";

	@Autowired
	private StudyMapper studyMapper;

	@MockBean
	private TimepointMapper timepointMapperMock;

	@Test
	public void studiesToStudyDTOsTest() {
		final List<StudyDTO> studyDTOs = studyMapper.studiesToStudyDTOs(Arrays.asList(createStudy()));
		Assert.assertNotNull(studyDTOs);
		Assert.assertTrue(studyDTOs.size() == 1);
		Assert.assertTrue(studyDTOs.get(0).getId().equals(STUDY_ID));
	}

	@Test
	public void studyToStudyDTOTest() {
		final StudyDTO studyDTO = studyMapper.studyToStudyDTO(createStudy());
		Assert.assertNotNull(studyDTO);
		Assert.assertTrue(studyDTO.getId().equals(STUDY_ID));
	}

	private Study createStudy() {
		final Study study = new Study();
		study.setId(STUDY_ID);
		study.setExperimentalGroupsOfSubjects(new ArrayList<>());
		study.setName(STUDY_NAME);
		study.setStudyCenterList(new ArrayList<>());
		study.setStudyUserList(new ArrayList<>());
		study.setSubjectStudyList(new ArrayList<>());
		return study;
	}

}
