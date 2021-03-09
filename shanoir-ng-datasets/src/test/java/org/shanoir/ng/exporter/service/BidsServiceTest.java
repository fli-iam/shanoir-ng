package org.shanoir.ng.exporter.service;

import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.MrDatasetAcquisition;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.importer.dto.Subject;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.utils.ModelsUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Test class for BIDS service class.
 * @author JCome
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(KeycloakUtil.class)
public class BidsServiceTest {

	@Mock
	private ExaminationService examService;

	@Mock
	private RabbitTemplate rabbitTemplate;

	@InjectMocks
	@Spy
	private BIDSServiceImpl service = new BIDSServiceImpl();
	
	String studyName = "STUDY";

	Examination exam = ModelsUtil.createExamination();
	Subject subject = new Subject();

	
	public static String tempFolderPath;

	@Before
	public void setUp() throws IOException {
        PowerMockito.mockStatic(KeycloakUtil.class);
        given(KeycloakUtil.getKeycloakHeader()).willReturn(null);

        String property = "java.io.tmpdir";
        tempFolderPath = System.getProperty(property) + "/tmpTest/";
        File tempFile = new File(tempFolderPath);
        tempFile.mkdirs();

        File file = new File(tempFolderPath);
		file.mkdirs();
	    System.setProperty("bidsStorageDir", tempFolderPath);
		ReflectionTestUtils.setField(service, "bidsStorageDir", tempFolderPath);

		exam.setId(Long.valueOf("13851681"));
		// Create a full study with some data and everything
		subject.setId(Long.valueOf("123"));
		subject.setName("name");

		Dataset ds = new MrDataset();
		ds.setId(Long.valueOf("1684"));

		DatasetAcquisition dsa = new MrDatasetAcquisition();
		dsa.setExamination(exam);
		dsa.setDatasets(Collections.singletonList(ds));

		ds.setDatasetAcquisition(dsa);

		exam.setDatasetAcquisitions(Collections.singletonList(dsa));
		
		// Create some dataFile and register it to be copied
		File dataFile = new File(tempFolderPath + "test.test");
		dataFile.createNewFile();

		DatasetExpression dsExpr = new DatasetExpression();
		DatasetFile dsFile = new DatasetFile();
		dsFile.setDatasetExpression(dsExpr);
		dsFile.setPacs(false);
		dsFile.setPath("file://" + dataFile.getAbsolutePath());
		dsExpr.setDatasetFiles(Collections.singletonList(dsFile));

		ds.setDatasetExpressions(Collections.singletonList(dsExpr));
	}

	@Test
	public void testExportAsBids() throws IOException {
		//GIVEN a study full of data

		// Mock on rest template to get the list of subjects
		Subject[] subjects = {subject};
		ObjectMapper mapper = new ObjectMapper();
		String value = mapper.writeValueAsString(subjects);
		given(rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.DATASET_SUBJECT_QUEUE, exam.getStudyId())).willReturn(value);
		
		// Mock on examination service to get the list of subject
		given(examService.findBySubjectId(subject.getId())).willReturn(Collections.singletonList(exam));

		// WHEN we export the data
		service.exportAsBids(exam.getStudyId(), studyName);
		
		// THEN the bids folder is generated with study - subject - exam - data
		File studyFile = new File(tempFolderPath + "stud-" + exam.getStudyId() + "_" + studyName);
		assertTrue(studyFile.exists());
		File subjectFile = new File(studyFile.getAbsolutePath() + "/sub-" + subject.getId() + "_" + subject.getName());
		assertTrue(subjectFile.exists());
		File examFile = new File(subjectFile.getAbsolutePath() + "/ses-" + exam.getId());
		assertTrue(examFile.exists());
		File bidsDataFile = new File(examFile.getAbsolutePath() + "/undefined/test.test");
		assertTrue(bidsDataFile.exists());
	}

	@After
	public void tearDown() {
		// delete files
        File tempFile = new File(tempFolderPath);
        if (tempFile.exists()) {
        	FileUtils.deleteQuietly(tempFile);
        }
	}
}

