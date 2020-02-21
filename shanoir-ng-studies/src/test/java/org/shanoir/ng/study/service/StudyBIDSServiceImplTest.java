package org.shanoir.ng.study.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.shanoir.ng.bids.service.StudyBIDSServiceImpl;
import org.shanoir.ng.shared.service.MicroserviceRequestsService;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.model.StudyUser;
import org.shanoir.ng.subject.model.HemisphericDominance;
import org.shanoir.ng.subject.model.Sex;
import org.shanoir.ng.subject.model.Subject;
import org.shanoir.ng.subject.service.SubjectService;
import org.shanoir.ng.subjectstudy.model.SubjectStudy;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

/**
 * Test for StudyBIDSServiceImpl class.
 * @author JComeD
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class StudyBIDSServiceImplTest {
	
	private static final String CSV_SEPARATOR = ",";
	
	private static final String CSV_SPLITTER = "\n";
	
	@Mock
	private MicroserviceRequestsService microservicesRequestsService;

	@Mock
	private RestTemplate restTemplate;
	
	@Mock
	private StudyService studyService;
	
	@Mock
	private SubjectService subjectService;

	@InjectMocks
	@Spy
	private StudyBIDSServiceImpl service = new StudyBIDSServiceImpl();

	private Study studyToCreate = new Study();

	private Subject subject = new Subject();
	
	@Before
	public void setUp() {
		ReflectionTestUtils.setField(service, "bidsStorageDir", "/tmp");
		studyToCreate.setName("Test-study");
		studyToCreate.setId(Long.valueOf("452154785412"));
		
		StudyUser user = new StudyUser();
		user.setStudy(studyToCreate);
		user.setUserId(Long.valueOf("123"));
		user.setUserName("UserName");
		
		subject.setId(Long.valueOf("1231"));
		subject.setName("subjectName");
		subject.setIdentifier("iudentifier");
		subject.setLanguageHemisphericDominance(HemisphericDominance.Left);
		subject.setManualHemisphericDominance(HemisphericDominance.Right);
		subject.setSex(Sex.F);
		
		SubjectStudy subjstud = new SubjectStudy();
		subjstud.setStudy(studyToCreate);
		subjstud.setSubject(subject);
		
		subject.setSubjectStudyList(Collections.singletonList(subjstud));
		studyToCreate.setSubjectStudyList(Collections.singletonList(subjstud));
		studyToCreate.setStudyUserList(Collections.singletonList(user ));
	}

	@Test
	public void testCreateBidsFolder() {
		// GIVEN a new study to be created

		// WHEN the study is created
		service.createBidsFolder(studyToCreate);

		// THEN a bids folder is created too
		File studyFolder = service.getStudyFolder(studyToCreate);
		assertTrue(studyFolder.exists());
		File readme = new File(studyFolder.getAbsolutePath() + File.separator + "README");
		assertTrue(readme.exists());
	}
	
	@Test
	public void testUpdateBidsFolder() {
		// GIVEN a study BIDS folder with a given name
		service.createBidsFolder(studyToCreate);
		Study oldStudy = new Study();
		oldStudy.setName("Test-study");
		oldStudy.setId(Long.valueOf("452154785412"));
		File oldStudyFolder = service.getStudyFolder(oldStudy);

		given(studyService.findById(studyToCreate.getId())).willReturn(oldStudy);
		
		// WHEN the name changes
		studyToCreate.setName("otherName");
		service.updateBidsFolder(studyToCreate);
		
		// THEN The bids folder name changes (old is delete, new exists)
		File studyFolder = service.getStudyFolder(studyToCreate);
		System.out.println(studyFolder);
		assertTrue(studyFolder.exists());
		assertTrue(studyFolder.getName().endsWith("otherName"));
		assertFalse(oldStudyFolder.exists());
		File readme = new File(studyFolder.getAbsolutePath() + File.separator + "README");
		assertTrue(readme.exists());
	}

	@Test
	public void testExportAsBids() {
		// GIVEN a study with a bids folder
		service.createBidsFolder(studyToCreate);
		File studyFolder = service.getStudyFolder(studyToCreate);
		
		// WHEN we export it as bids
		File fileExported = service.exportAsBids(studyToCreate);
		
		// Then we get the same folder
		assertEquals(fileExported.getAbsolutePath(), studyFolder.getAbsolutePath());
	}

	@Test
	public void testDeleteBids() {
		// GIVEN a study with an existing BIDS folder
		service.createBidsFolder(studyToCreate);
		File studyFolder = service.getStudyFolder(studyToCreate);
		assertTrue(studyFolder.exists());
		
		given(studyService.findById(studyToCreate.getId())).willReturn(studyToCreate);

		// WHEN the study is deleted
		service.deleteBids(studyToCreate.getId());
		
		// THEN the associated bids folder is deleted too
		assertFalse(studyFolder.exists());
	}

	@Test
	public void testDeleteSubjectBIDS() {
		// GIVEN a study with a subject
		service.createBidsFolder(studyToCreate);
		File studyFolder = service.getStudyFolder(studyToCreate);
		File subjectFile = new File(studyFolder
				+ File.separator
				+ "sub-" + subject.getId()
				+ "_" + subject.getName());

		subjectFile.mkdirs();
		assertTrue(subjectFile.exists());
		
		given(subjectService.findById(subject.getId())).willReturn(subject);

		// WHEN the subject is deleted
		service.deleteSubjectBids(subject.getId());
		
		// THEN it is also deleted from BIDS folder
		assertFalse(subjectFile.exists());
	}

	@Test
	public void testUpdateSubjectBids() {
		// GIVEN a study with a subject
		Subject oldSubject =  new Subject();
		oldSubject.setName("otherName");
		oldSubject.setId(subject.getId());
		SubjectStudy subjstud = new SubjectStudy();
		subjstud.setStudy(studyToCreate);
		subjstud.setSubject(oldSubject);
		
		oldSubject.setSubjectStudyList(Collections.singletonList(subjstud));
		studyToCreate.setSubjectStudyList(Collections.singletonList(subjstud));

		service.createBidsFolder(studyToCreate);
		File studyFolder = service.getStudyFolder(studyToCreate);
		File subjectFile = new File(studyFolder
				+ File.separator
				+ "sub-" + subject.getId()
				+ "_otherName");
		subjectFile.mkdirs();
		assertTrue(subjectFile.exists());
		
		given(subjectService.findById(subject.getId())).willReturn(oldSubject);

		// WHEN the subject name changes
		service.updateSubjectBids(subject.getId(), subject);

		// THEN the subject folder name also changes
		assertFalse(subjectFile.exists());
		subjectFile = new File(studyFolder
				+ File.separator
				+ "sub-" + subject.getId()
				+ "_" + subject.getName());
		assertTrue(subjectFile.exists());
	}

	@Test
	public void testCreateParticipantsFile() throws IOException {
		// GIVEN a study with a list of subjects
		File studyFolder = service.getStudyFolder(studyToCreate);
		if (!studyFolder.exists()) {
			studyFolder.mkdirs();
		}

		// WHEN we create the subject participants.tsv file
		service.createParticipantsFiles(studyFolder, studyToCreate);
		
		// THEN the file is created with some content
		File subjectFile = new File(studyFolder + File.separator + "participants.tsv");
		assertTrue(subjectFile.exists());
		// Check content
		List<String> lines = Files.readAllLines(Paths.get(subjectFile.getPath()));
		StringBuilder columnLine = new StringBuilder();
		columnLine.append("participant_id").append(CSV_SEPARATOR)
		  	  .append("common_name").append(CSV_SEPARATOR)
		  	  .append("sex").append(CSV_SEPARATOR)
		  	  .append("birth_date").append(CSV_SEPARATOR)
		  	  .append("manualHemisphericDominance").append(CSV_SEPARATOR)
		  	  .append("languageHemisphericDominance").append(CSV_SEPARATOR)
		  	  .append("imagedObjectCategory").append(CSV_SEPARATOR);
		assertEquals(lines.get(0), columnLine.toString());
		StringBuilder dataLine = new StringBuilder();
		dataLine.append(subject.getIdentifier()).append(CSV_SEPARATOR)
		 	  .append(subject.getName()).append(CSV_SEPARATOR)
		 	  .append(subject.getSex()).append(CSV_SEPARATOR)
		 	  .append(subject.getBirthDate()).append(CSV_SEPARATOR)
		 	  .append(subject.getManualHemisphericDominance()).append(CSV_SEPARATOR)
		 	  .append(subject.getLanguageHemisphericDominance()).append(CSV_SEPARATOR)
		 	  .append(subject.getImagedObjectCategory()).append(CSV_SEPARATOR);
		assertEquals(lines.get(1), dataLine.toString());
	}

	@Test
	public void testDeserializeParticipantTsv() throws IOException {
		// GIVEN a participants.tsv file
		File studyFolder = service.getStudyFolder(studyToCreate);
		if (!studyFolder.exists()) {
			studyFolder.mkdirs();
		}
		// Create the file
		service.createParticipantsFiles(studyFolder, studyToCreate);
	
		File subjectFile = new File(studyFolder + File.separator + "participants.tsv");
		assertTrue(subjectFile.exists());

		// WHEN we deserialize it into subjects
		List<Subject> subjects = service.participantsDeserializer(subjectFile);
		
		// THEN we get a list of subjects
		assertNotNull(subjects);
		assertEquals(1, subjects.size());
		Subject subj = subjects.get(0);
		assertNotNull(subj);
		assertEquals(subject.getIdentifier(), subj.getIdentifier());
		assertEquals(subject.getName(), subj.getName());
		assertEquals(subject.getSex(), subj.getSex());
		assertEquals(subject.getBirthDate(), subj.getBirthDate());
		assertEquals(subject.getManualHemisphericDominance(), subj.getManualHemisphericDominance());
		assertEquals(subject.getLanguageHemisphericDominance(), subj.getLanguageHemisphericDominance());
		assertEquals(subject.getImagedObjectCategory(), subj.getImagedObjectCategory());
	}

	@After
	public void tearDown() {
	    File dir = service.getStudyFolder(studyToCreate);
	    if (dir.exists() && dir.listFiles() != null) {
	    	for (File file:dir.listFiles()) {
	    		file.delete();
	    	}
	    }
	    dir.delete();
	}
}
