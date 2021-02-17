package org.shanoir.ng.bids.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.shanoir.ng.shared.service.MicroserviceRequestsService;
import org.shanoir.ng.study.dto.DatasetDescription;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.model.StudyUser;
import org.shanoir.ng.study.service.StudyService;
import org.shanoir.ng.subject.model.Subject;
import org.shanoir.ng.subject.service.SubjectService;
import org.shanoir.ng.subjectstudy.model.SubjectStudy;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class StudyBIDSServiceImpl implements StudyBIDSService {

	private static final String NULL = "null";

	private static final String IMAGED_OBJECT_CATEGORY = "imaged_object_category";

	private static final String LANGUAGE_HEMISPHERIC_DOMINANCE = "language_hemispheric_dominance";

	private static final String MANUAL_HEMISPHERIC_DOMINANCE = "manual_hemispheric_dominance";

	private static final String BIRTH_DATE = "birth_date";

	private static final String SEX = "sex";

	private static final String SUBJECT_IDENTIFIER = "subject_identifier";

	private static final String PARTICIPANT_ID = "participant_id";

	private static final String STUDY_PREFIX = "stud-";

	private static final String SUBJECT_PREFIX = "sub-";

	private static final String CSV_SEPARATOR = "\t";

	private static final String CSV_SPLITTER = "\n";

	private static final String[] CSV_PARTICIPANTS_HEADER = {
			PARTICIPANT_ID,
			SUBJECT_IDENTIFIER,
			SEX,
			BIRTH_DATE,
			MANUAL_HEMISPHERIC_DOMINANCE,
			LANGUAGE_HEMISPHERIC_DOMINANCE,
			IMAGED_OBJECT_CATEGORY
	};


	/** Logger. */
	private static final Logger LOG = LoggerFactory.getLogger(StudyBIDSServiceImpl.class);

	private static final String DATASET_DESCRIPTION_FILE = "dataset_description.json";

	private static final String README_FILE = "README";

	private static final String DEFAULT_README = "This BIDS dataset was automatically created by Shanoir-NG.";

	@Value("${bids-data-folder}")
	private String bidsStorageDir;

	@Autowired
	private MicroserviceRequestsService microservicesRequestsService;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	StudyService studyService;

	@Autowired
	SubjectService subjectService;

	@Override
	/**
	 * This method creates the BIDS folder for the study.
	 */
	public void createBidsFolder(Study study) {
		// 1. Create base folder /var/datasets-data/bids_data if it does not exists
		File bidsFolder = new File(bidsStorageDir);
		if (!bidsFolder.exists()) {
			bidsFolder.mkdirs();
		}
		File studyBidsFolder = getStudyFolder(study);
		if (studyBidsFolder.exists()) {
			// If the folder already exist for any reason, don't recreate the bids folder
			return;
		}
		// Create folders
		studyBidsFolder.mkdirs();

		// 2. Create dataset_description.json and README
		DatasetDescription datasetDescription = new DatasetDescription();
		datasetDescription.setName(study.getName());
		datasetDescription.setDatasetDOI(study.getId().toString());
		datasetDescription.setAuthors(study.getStudyUserList().stream().map(StudyUser::getUserName).collect(Collectors.toList()));
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			objectMapper.writeValue(new File(studyBidsFolder.getAbsolutePath() + File.separator + DATASET_DESCRIPTION_FILE), datasetDescription);
			objectMapper.writeValue(new File(studyBidsFolder.getAbsolutePath() + File.separator + README_FILE), DEFAULT_README);
		} catch (IOException e) {
			LOG.error("Error when creating BIDS folder: {}", e.getMessage());
		}
	}

	@Override
	/**
	 * This method updates the BIDS folder for a study when it is updated.
	 */
	public void updateBidsFolder(Study studyToChange) {
		// Get the study to update
		Study baseStudy = studyService.findById(studyToChange.getId());

		// If the name didn't change, don't change anything.
		if (baseStudy == null || studyToChange.getName().equals(baseStudy.getName())) {
			return;
		}
		// Otherwise, rename the folder to the new name.
		File bidsDir = getStudyFolder(baseStudy);
		if (bidsDir.exists()) {
			bidsDir.renameTo(new File(bidsStorageDir + File.separator + STUDY_PREFIX + studyToChange.getId() + '_' + studyToChange.getName()));
		} else {
			createBidsFolderFromScratch(studyToChange);
		}
	}

	@Override
	public File exportAsBids(Study studyToExport) {
		// Get the study to update
		File bidsDir = getStudyFolder(studyToExport);
		if (!bidsDir.exists()) {
			// Create it from scratch
			bidsDir = createBidsFolderFromScratch(studyToExport);
		}
		participantsSerializer(bidsDir, studyToExport);
		return bidsDir;
	}

	@Override
	public void deleteBids(Study studyDeleted) {
		// Try to delete the BIDS folder recursively if possible
		File bidsDir = getStudyFolder(studyDeleted);
		if (bidsDir.exists()) {
			try {
				FileUtils.deleteDirectory(bidsDir);
			} catch (IOException e) {
				LOG.error("ERROR when deleting BIDS folder: please delete it manually: {}", bidsDir.getAbsolutePath(), e);
			}
		}
	}

	/**
	 * Get the study bids base folder for a study
	 * @param study the study to get
	 * @return the folder associated to the study
	 */
	@Override
	public File getStudyFolder(Study study) {
		return new File(bidsStorageDir + File.separator + STUDY_PREFIX + study.getId() + '_' + study.getName());
	}

	@Override
	public File createBidsFolderFromScratch(Study studyToGenerate) {
		// Call datasets MS to create data
		HttpEntity<Object> entity = new HttpEntity<>(KeycloakUtil.getKeycloakHeader());
		try {
			restTemplate.exchange(microservicesRequestsService.getBidsMsUrl()
					+ MicroserviceRequestsService.STUDY_ID
					+ studyToGenerate.getId()
					+ MicroserviceRequestsService.STUDY_NAME
					+ studyToGenerate.getName()
					, HttpMethod.GET, entity, Void.class);
		} catch (RestClientException e) {
			LOG.error("Error on study microservice request - {}", e.getMessage());
		}
		// BidsDir should now exists, reload it
		return getStudyFolder(studyToGenerate);
	}

	@Override
	public void deleteSubjectBids(Long subjectId) {
		Subject oldsub = subjectService.findById(subjectId);

		// Do nothing if we can't find the subject
		if (oldsub == null) {
			return;
		}
		// Delete folders for all concerned studies
		for (SubjectStudy subjStud : oldsub.getSubjectStudyList()) {
			File subjectFile = new File(bidsStorageDir + File.separator
					+ STUDY_PREFIX + subjStud.getStudy().getId() + "_" + subjStud.getStudy().getName()
					+ File.separator
					+ SUBJECT_PREFIX + oldsub.getId() + "_" + oldsub.getName());
			//Delete file
			try {
				FileUtils.deleteDirectory(subjectFile);
			} catch (IOException e) {
				LOG.error("Failed to delete file:{} : {}", subjectFile, e);
			}
		}
	}

	/**
	 * Creates the participants.tsv and participants.json file from the study
	 */
	public void participantsSerializer(File parentFolder, Study study) {
		File csvFile = new File(parentFolder.getAbsolutePath() + File.separator + "participants.tsv");
		if (csvFile.exists()) {
			// Recreate it everytime
			FileUtils.deleteQuietly(csvFile);
		}
		StringBuilder buffer =  new StringBuilder();

		// Headers
		for (String columnHeader : CSV_PARTICIPANTS_HEADER) {
			buffer.append(columnHeader).append(CSV_SEPARATOR);
		}
		buffer.append(CSV_SPLITTER);

		for (SubjectStudy stubject : study.getSubjectStudyList()) {
			Subject u = stubject.getSubject();

			// Write in the file the values
			buffer.append(u.getName()).append(CSV_SEPARATOR)
			.append(u.getIdentifier() == null ? NULL : u.getIdentifier()).append(CSV_SEPARATOR)
			.append(u.getSex() == null ? NULL : u.getSex()).append(CSV_SEPARATOR)
			.append(u.getBirthDate()).append(CSV_SEPARATOR)
			.append(u.getManualHemisphericDominance() == null ? NULL : u.getManualHemisphericDominance()).append(CSV_SEPARATOR)
			.append(u.getLanguageHemisphericDominance() == null ? NULL : u.getLanguageHemisphericDominance()).append(CSV_SEPARATOR)
			.append(u.getImagedObjectCategory()).append(CSV_SEPARATOR)
			.append(CSV_SPLITTER);
		}

		try {
			Files.write(Paths.get(csvFile.getAbsolutePath()), buffer.toString().getBytes());
		} catch (IOException e) {
			LOG.error("Error while creating particpants.tsv file: {}", e);
		}
	}

}
