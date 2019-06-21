/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.dataset;

import static org.mockito.BDDMockito.given;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.modality.MrDatasetRepository;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.utils.ModelsUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * Dataset service test.
 * 
 * @author msimon
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class DatasetServiceTest {

	private static final Long DATASET_ID = 1L;
	private static final Long UPDATED_STUDY_ID = 2L;

	@Mock
	private DatasetRepository datasetRepository;

	@InjectMocks
	private DatasetServiceImpl datasetService;

	@Mock
	private MrDatasetRepository mrDatasetRepository;

	@Mock
	private RabbitTemplate rabbitTemplate;

	@Before
	public void setup() {
		given(datasetRepository.findOne(DATASET_ID)).willReturn(ModelsUtil.createMrDataset());
		given(datasetRepository.save(Mockito.any(MrDataset.class))).willReturn(ModelsUtil.createMrDataset());
	}

	@Test
	public void deleteByIdTest() throws ShanoirException {
		datasetService.deleteById(DATASET_ID);

		Mockito.verify(datasetRepository, Mockito.times(1)).delete(Mockito.anyLong());
	}

	@Test
	public void findByIdTest() {
		final Dataset dataset = datasetService.findById(DATASET_ID);
		Assert.assertNotNull(dataset);
		Assert.assertTrue(ModelsUtil.DATASET_NAME.equals(dataset.getName()));

		Mockito.verify(datasetRepository, Mockito.times(1)).findOne(Mockito.anyLong());
	}

	@Test
	public void saveTest() throws ShanoirException {
		datasetService.save(createMrDataset());

		Mockito.verify(mrDatasetRepository, Mockito.times(1)).save(Mockito.any(MrDataset.class));
	}

	@Test
	public void updateTest() throws ShanoirException {
		final Dataset updatedTemplate = datasetService.update(createMrDataset());
		Assert.assertNotNull(updatedTemplate);
		Assert.assertTrue(UPDATED_STUDY_ID.equals(updatedTemplate.getStudyId()));

		Mockito.verify(mrDatasetRepository, Mockito.times(1)).save(Mockito.any(MrDataset.class));
	}

	@Test
	public void updateFromShanoirOldTest() throws ShanoirException {
		datasetService.updateFromShanoirOld(createMrDataset());

		Mockito.verify(datasetRepository, Mockito.times(1)).findOne(Mockito.anyLong());
		Mockito.verify(datasetRepository, Mockito.times(1)).save(Mockito.any(MrDataset.class));
	}

	private MrDataset createMrDataset() {
		final MrDataset dataset = new MrDataset();
		dataset.setId(DATASET_ID);
		dataset.setStudyId(UPDATED_STUDY_ID);
		return dataset;
	}

}
