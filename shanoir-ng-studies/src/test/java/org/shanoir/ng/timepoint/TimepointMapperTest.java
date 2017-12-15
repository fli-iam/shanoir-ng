package org.shanoir.ng.timepoint;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
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
public class TimepointMapperTest {

	private static final Long TIMEPOINT_ID = 1L;

	@Autowired
	private TimepointMapper timepointMapper;

	@Test
	public void timepointsToTimepointDTOsTest() {
		final List<TimepointDTO> subjectStudyDTOs = timepointMapper
				.timepointsToTimepointDTOs(Arrays.asList(createTimepoint()));
		Assert.assertNotNull(subjectStudyDTOs);
		Assert.assertTrue(subjectStudyDTOs.size() == 1);
		Assert.assertTrue(subjectStudyDTOs.get(0).getId().equals(TIMEPOINT_ID));
	}

	@Test
	public void timepointToTimepointDTOTest() {
		final TimepointDTO timepointDTO = timepointMapper.timepointToTimepointDTO(createTimepoint());
		Assert.assertNotNull(timepointDTO);
		Assert.assertTrue(timepointDTO.getId().equals(TIMEPOINT_ID));
	}

	private Timepoint createTimepoint() {
		final Timepoint timepoint = new Timepoint();
		timepoint.setId(TIMEPOINT_ID);
		return timepoint;
	}

}
