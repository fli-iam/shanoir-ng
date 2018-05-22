package org.shanoir.ng.preclinical.examination_extradata;

import static org.mockito.BDDMockito.given;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.shanoir.ng.preclinical.extra_data.examination_extra_data.ExaminationExtraData;
import org.shanoir.ng.preclinical.extra_data.examination_extra_data.ExaminationExtraDataRepository;
import org.shanoir.ng.preclinical.extra_data.examination_extra_data.ExaminationExtraDataServiceImpl;
import org.shanoir.ng.shared.exception.ShanoirPreclinicalException;
import org.shanoir.ng.utils.AnestheticModelUtil;
import org.shanoir.ng.utils.ExtraDataModelUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * Examination anesthetics service test.
 * 
 * @author sloury
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class ExtraDataServiceTest {

	public static final Long EXAMINATION_ID = 1L;
	public static final Long EXTRADATA_ID = 1L;
	public static final String EXTRADATA_TYPE = "Extra data";
	public static final Long PHYSIOLOGICALDATA_ID = 2L;
	public static final String PHYSIOLOGICALDATA_TYPE = "Physiological data";
	public static final Long BLOODGASDATA_ID = 3L;
	public static final String BLOODGASDATA_TYPE = "Blood gas data";
	public static final String EXTRADATA_FILEPATH = "/home/sloury/Documents/FLI-IAM/SHANOIR_NG/upload/";
	public static final String EXTRADATA_FILENAME = "extradata.txt";

	@Mock
	private ExaminationExtraDataRepository extraDataRepository;

	@Mock
	private RabbitTemplate rabbitTemplate;

	@InjectMocks
	private ExaminationExtraDataServiceImpl extraDataService;

	@Before
	public void setup() {
		given(extraDataRepository.findAll()).willReturn(Arrays.asList(ExtraDataModelUtil.createExaminationExtraData()));
		given(extraDataRepository.findAllByExaminationId(1L))
				.willReturn(Arrays.asList(ExtraDataModelUtil.createExaminationExtraData()));
		given(extraDataRepository.findOne(EXTRADATA_ID)).willReturn(ExtraDataModelUtil.createExaminationExtraData());
		given(extraDataRepository.save(Mockito.any(ExaminationExtraData.class)))
				.willReturn(ExtraDataModelUtil.createExaminationExtraData());
	}

	@Test
	public void deleteByIdTest() throws ShanoirPreclinicalException {
		extraDataService.deleteById(EXTRADATA_ID);

		Mockito.verify(extraDataRepository, Mockito.times(1)).delete(Mockito.anyLong());
	}

	@Test
	public void findByIdTest() {
		final ExaminationExtraData extradata = extraDataService.findById(EXTRADATA_ID);
		Assert.assertNotNull(extradata);
		Assert.assertTrue(EXTRADATA_FILENAME.equals(extradata.getFilename()));
		Assert.assertTrue(AnestheticModelUtil.EXAM_ID.equals(extradata.getExaminationId()));

		Mockito.verify(extraDataRepository, Mockito.times(1)).findOne(Mockito.anyLong());
	}

	@Test
	public void findByExaminationIdTest() {
		final List<ExaminationExtraData> extradata = extraDataService.findAllByExaminationId(EXAMINATION_ID);
		Assert.assertNotNull(extradata);
		Assert.assertTrue(extradata.size() == 1);

		Mockito.verify(extraDataRepository, Mockito.times(1)).findAllByExaminationId(EXAMINATION_ID);
	}

	@Test
	public void saveTest() throws ShanoirPreclinicalException {
		extraDataService.save(createExtraData());

		Mockito.verify(extraDataRepository, Mockito.times(1)).save(Mockito.any(ExaminationExtraData.class));
	}

	@Test
	public void updateTest() throws ShanoirPreclinicalException {
		final ExaminationExtraData updatedExtraData = extraDataService.update(createExtraData());
		Assert.assertNotNull(updatedExtraData);
		Assert.assertTrue(EXTRADATA_FILENAME.equals(updatedExtraData.getFilename()));

		Mockito.verify(extraDataRepository, Mockito.times(1)).save(Mockito.any(ExaminationExtraData.class));
	}

	/*
	 * @Test public void updateFromShanoirOldTest() throws
	 * ShanoirPreclinicalException {
	 * pathologiesService.updateFromShanoirOld(createPathology());
	 * 
	 * Mockito.verify(pathologiesRepository,
	 * Mockito.times(1)).findOne(Mockito.anyLong());
	 * Mockito.verify(pathologiesRepository,
	 * Mockito.times(1)).save(Mockito.any(Pathology.class)); }
	 */
	private ExaminationExtraData createExtraData() {
		final ExaminationExtraData extradata = new ExaminationExtraData();
		extradata.setId(EXTRADATA_ID);
		extradata.setExaminationId(EXAMINATION_ID);
		extradata.setExtradatatype(EXTRADATA_TYPE);
		extradata.setFilename(EXTRADATA_FILENAME);
		extradata.setFilepath(EXTRADATA_FILEPATH);
		return extradata;
	}

}
