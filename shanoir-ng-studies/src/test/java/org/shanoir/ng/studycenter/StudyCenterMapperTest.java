package org.shanoir.ng.studycenter;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.acquisitionequipment.AcquisitionEquipmentMapper;
import org.shanoir.ng.center.CenterMapper;
import org.shanoir.ng.study.model.Study;
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
public class StudyCenterMapperTest {

	private static final Long STUDY_ID = 1L;

	@MockBean
	private AcquisitionEquipmentMapper acquisitionEquipmentMapperMock;

	@MockBean
	private CenterMapper centerMapperMock;

	@Autowired
	private StudyCenterMapper studyCenterMapper;

	@Test
	public void studyCenterListToStudyCenterDTOListTest() {
		final List<StudyCenterDTO> subjectStudyDTOs = studyCenterMapper
				.studyCenterListToStudyCenterDTOList(Arrays.asList(createStudyCenter()));
		Assert.assertNotNull(subjectStudyDTOs);
		Assert.assertTrue(subjectStudyDTOs.size() == 1);
		Assert.assertTrue(subjectStudyDTOs.get(0).getId().equals(STUDY_ID));
	}

	@Test
	public void studyCenterToStudyCenterDTOTest() {
		final StudyCenterDTO subjectStudyDTO = studyCenterMapper.studyCenterToStudyCenterDTO(createStudyCenter());
		Assert.assertNotNull(subjectStudyDTO);
		Assert.assertTrue(subjectStudyDTO.getId().equals(STUDY_ID));
	}

	private StudyCenter createStudyCenter() {
		final StudyCenter studyCenter = new StudyCenter();
		final Study study = new Study();
		study.setId(STUDY_ID);
		studyCenter.setStudy(study);
		return studyCenter;
	}

}
