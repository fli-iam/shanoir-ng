package org.shanoir.ng.subjectstudy;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.acquisitionequipment.dto.mapper.AcquisitionEquipmentMapper;
import org.shanoir.ng.subject.model.Subject;
import org.shanoir.ng.subjectstudy.dto.SubjectStudyDTO;
import org.shanoir.ng.subjectstudy.dto.mapper.SubjectStudyMapper;
import org.shanoir.ng.subjectstudy.model.SubjectStudy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
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
public class SubjectStudyMapperTest {

	private static final Long SUBJECT_ID = 1L;

	@MockBean
	private AcquisitionEquipmentMapper acquisitionEquipmentMapperMock;

	@Autowired
	private SubjectStudyMapper subjectStudyMapper;

	@Test
	public void subjectStudyListToSubjectStudyDTOListTest() {
		final List<SubjectStudyDTO> subjectStudyDTOs = subjectStudyMapper
				.subjectStudyListToSubjectStudyDTOList(Arrays.asList(createSubjectStudy()));
		Assert.assertNotNull(subjectStudyDTOs);
		Assert.assertTrue(subjectStudyDTOs.size() == 1);
		Assert.assertTrue(subjectStudyDTOs.get(0).getSubject().getId().equals(SUBJECT_ID));
	}

	@Test
	public void subjectStudyToSubjectStudyDTOTest() {
		final SubjectStudyDTO subjectStudyDTO = subjectStudyMapper.subjectStudyToSubjectStudyDTO(createSubjectStudy());
		Assert.assertNotNull(subjectStudyDTO);
		Assert.assertTrue(subjectStudyDTO.getSubject().getId().equals(SUBJECT_ID));
	}

	private SubjectStudy createSubjectStudy() {
		final SubjectStudy center = new SubjectStudy();
		final Subject subject = new Subject();
		subject.setId(SUBJECT_ID);
		center.setSubject(subject);
		return center;
	}

}
