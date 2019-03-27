package org.shanoir.ng.manufacturermodel;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.manufacturermodel.dto.ManufacturerModelDTO;
import org.shanoir.ng.manufacturermodel.dto.mapper.ManufacturerModelMapper;
import org.shanoir.ng.manufacturermodel.model.ManufacturerModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Manufacturer model mapper test.
 * 
 * @author msimon
 * 
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ManufacturerModelMapperTest {

	private static final String MANUFACTURER_MODEL_NAME = "test";

	@Autowired
	private ManufacturerModelMapper manufacturerModelMapper;

	@Test
	public void centersToCenterDTOsTest() {
		final ManufacturerModelDTO manufacturerModelDTO = manufacturerModelMapper.manufacturerModelToManufacturerModelDTO(createManufacturerModel());
		Assert.assertNotNull(manufacturerModelDTO);
		Assert.assertTrue(manufacturerModelDTO.getName().equals(MANUFACTURER_MODEL_NAME));
	}

	private ManufacturerModel createManufacturerModel() {
		final ManufacturerModel center = new ManufacturerModel();
		center.setName(MANUFACTURER_MODEL_NAME);
		return center;
	}

}
