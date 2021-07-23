package org.shanoir.ng.exporter.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import org.shanoir.ng.dataset.modality.EegDataset;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.eeg.EegDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.MrDatasetAcquisition;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.importer.dto.Subject;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.utils.ModelsUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
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

	@Autowired
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
		File bidsDataFile = new File(examFile.getAbsolutePath() + "/anat/test.test");
		assertTrue(bidsDataFile.exists());
	}

	@Test
	public void testAddDataset() throws IOException {
		// GIVEN a study with existing BIDS folder
		// Mock on rest template to get the list of subjects
		Subject[] subjects = {subject};
		ObjectMapper mapper = new ObjectMapper();
		String value = mapper.writeValueAsString(subjects);
		given(rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.DATASET_SUBJECT_QUEUE, exam.getStudyId())).willReturn(value);

		
		// Mock on examination service to get the list of subject
		given(examService.findBySubjectId(subject.getId())).willReturn(Collections.singletonList(exam));

		// Create bids folder
		service.exportAsBids(exam.getStudyId(), studyName);

		// WHEN we add a dataset to the study

		Examination exam2 = ModelsUtil.createExamination();
		exam2.setId(Long.valueOf("6584169874"));
		exam2.setSubjectId(subject.getId());

		EegDataset ds2 = new EegDataset();
		//ds2.setStudyId(exam2.getStudyId());
		ds2.setSubjectId(exam2.getSubjectId());
		ds2.setId(Long.valueOf("16843"));
		ds2.setChannels(Collections.emptyList());
		ds2.setEvents(Collections.emptyList());

		EegDatasetAcquisition dsa2 = new EegDatasetAcquisition();
		dsa2.setExamination(exam2);
		dsa2.setDatasets(Collections.singletonList(ds2));

		ds2.setDatasetAcquisition(dsa2);

		List<DatasetAcquisition> datasetAcqs = new ArrayList<>();
		datasetAcqs.add(dsa2);
		exam2.setDatasetAcquisitions(datasetAcqs);

		// Create some dataFile and register it to be copied
		File dataFile2 = new File(tempFolderPath + "test.test");
		if (!dataFile2.exists()) {
			dataFile2.createNewFile();
		}

		DatasetExpression dsExpr2 = new DatasetExpression();
		DatasetFile dsFile2 = new DatasetFile();
		dsFile2.setDatasetExpression(dsExpr2);
		dsFile2.setPacs(false);
		dsFile2.setPath("file://" + dataFile2.getAbsolutePath());
		dsExpr2.setDatasetFiles(Collections.singletonList(dsFile2));

		ds2.setDatasetExpressions(Collections.singletonList(dsExpr2));
		
		//HERE
		service.addDataset(exam2, subject.getName(), studyName);
		
		// THEN the data is added too
		File studyFile = new File(tempFolderPath + "stud-" + exam2.getStudyId() + "_" + studyName);
		assertTrue(studyFile.exists());
		File subjectFile = new File(studyFile.getAbsolutePath() + "/sub-" + subject.getId() + "_" + subject.getName());
		assertTrue(subjectFile.exists());
		File examFile = new File(subjectFile.getAbsolutePath() + "/ses-" + exam2.getId());
		assertTrue(examFile.exists());
		File bidsDataFile = new File(examFile.getAbsolutePath() + "/eeg/test.test");
		assertTrue(bidsDataFile.exists());
		File scansFile = new File(subjectFile.getAbsolutePath() + File.separator + subjectFile.getName() + "_scans.tsv");
		assertTrue(scansFile.exists());

		// WHEN we delete a dataset
		service.deleteDataset(ds2);
		
		// THEN it is also deleted in the BIDS folder
		studyFile = new File(tempFolderPath + "stud-" + exam2.getStudyId() + "_" + studyName);
		assertTrue(studyFile.exists());
		subjectFile = new File(studyFile.getAbsolutePath() + "/sub-" + subject.getId() + "_" + subject.getName());
		assertTrue(subjectFile.exists());
		examFile = new File(subjectFile.getAbsolutePath() + "/ses-" + exam2.getId());
		assertTrue(examFile.exists());
		bidsDataFile = new File(examFile.getAbsolutePath() + "/eeg/test.test");
		assertFalse(bidsDataFile.exists());

		// WHEN we delete an examination
		given(examService.findById(exam2.getId())).willReturn(exam2);
	
		service.deleteExam(exam2.getId());
		
		// THEN it is also deleted in the BIDS folder
		studyFile = new File(tempFolderPath + "stud-" + exam2.getStudyId() + "_" + studyName);
		assertTrue(studyFile.exists());
		subjectFile = new File(studyFile.getAbsolutePath() + "/sub-" + subject.getId() + "_" + subject.getName());
		assertTrue(subjectFile.exists());
		examFile = new File(subjectFile.getAbsolutePath() + "/ses-" + exam2.getId());
		assertFalse(examFile.exists());
	}

	@Test
	public void testDeleteDataset() {
		// See testAddDataset
	}

	@Test
	public void testDeleteExamination() {
		// See testAddDataset
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

