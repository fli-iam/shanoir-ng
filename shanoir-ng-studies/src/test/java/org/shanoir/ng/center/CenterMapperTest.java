package org.shanoir.ng.center;

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
 * Center mapper test.
 * 
 * @author msimon
 * 
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CenterMapperTest {

	private static final Long CENTER_ID = 1L;
	private static final String CENTER_NAME = "test";

	@Autowired
	private CenterMapper centerMapper;

	@Test
	public void centersToCenterDTOsTest() {
		final List<CenterDTO> centerDTOs = centerMapper.centersToCenterDTOs(Arrays.asList(createCenter()));
		Assert.assertNotNull(centerDTOs);
		Assert.assertTrue(centerDTOs.size() == 1);
		Assert.assertTrue(centerDTOs.get(0).getId().equals(CENTER_ID));
	}

	@Test
	public void centersToSimpleCenterDTOsTest() {
		final List<SimpleCenterDTO> centerDTOs = centerMapper.centersToSimpleCenterDTOs(Arrays.asList(createCenter()));
		Assert.assertNotNull(centerDTOs);
		Assert.assertTrue(centerDTOs.size() == 1);
		Assert.assertTrue(centerDTOs.get(0).getId().equals(CENTER_ID));
	}

	@Test
	public void centerToCenterDTOTest() {
		final CenterDTO centerDTO = centerMapper.centerToCenterDTO(createCenter());
		Assert.assertNotNull(centerDTO);
		Assert.assertTrue(centerDTO.getId().equals(CENTER_ID));
	}

	@Test
	public void centerToSimpleCenterDTOTest() {
		final SimpleCenterDTO centerDTO = centerMapper.centerToSimpleCenterDTO(createCenter());
		Assert.assertNotNull(centerDTO);
		Assert.assertTrue(centerDTO.getId().equals(CENTER_ID));
	}

	private Center createCenter() {
		final Center center = new Center();
		center.setId(CENTER_ID);
		center.setName(CENTER_NAME);
		return center;
	}

}
