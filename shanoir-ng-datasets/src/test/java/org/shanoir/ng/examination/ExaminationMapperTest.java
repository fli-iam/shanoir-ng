package org.shanoir.ng.examination;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.utils.SecurityContextTestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Examination mapper test.
 * 
 * @author msimon
 * 
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ExaminationMapperTest {

	private static final Long EXAMINATION_ID = 1L;

	@Autowired
	private ExaminationMapper examinationMapper;

	// @Test
	// public void examinationsToExaminationDTOsTest() {
	// 	SecurityContextTestUtil.initAuthenticationContext();

	// 	final List<ExaminationDTO> examinationDTOs = examinationMapper
	// 			.examinationsToExaminationDTOs(Arrays.asList(createExamination()));
	// 	Assert.assertNotNull(examinationDTOs);
	// 	Assert.assertTrue(examinationDTOs.size() == 1);
	// 	Assert.assertTrue(EXAMINATION_ID.equals(examinationDTOs.get(0).getId()));
	// }

	@Test
	public void examinationsToSubjectExaminationDTOsTest() {
		final List<SubjectExaminationDTO> examinationDTOs = examinationMapper
				.examinationsToSubjectExaminationDTOs(Arrays.asList(createExamination()));
		Assert.assertNotNull(examinationDTOs);
		Assert.assertTrue(examinationDTOs.size() == 1);
		Assert.assertTrue(EXAMINATION_ID.equals(examinationDTOs.get(0).getId()));
	}

	@Test
	public void examinationToExaminationDTOTest() {
		SecurityContextTestUtil.initAuthenticationContext();

		final ExaminationDTO examinationDTO = examinationMapper.examinationToExaminationDTO(createExamination());
		Assert.assertNotNull(examinationDTO);
		Assert.assertTrue(EXAMINATION_ID.equals(examinationDTO.getId()));
	}

	@Test
	public void examinationToSubjectExaminationDTOTest() {
		final SubjectExaminationDTO examinationDTO = examinationMapper
				.examinationToSubjectExaminationDTO(createExamination());
		Assert.assertNotNull(examinationDTO);
		Assert.assertTrue(EXAMINATION_ID.equals(examinationDTO.getId()));
	}

	private Examination createExamination() {
		final Examination examination = new Examination();
		examination.setId(EXAMINATION_ID);
		return examination;
	}

}
