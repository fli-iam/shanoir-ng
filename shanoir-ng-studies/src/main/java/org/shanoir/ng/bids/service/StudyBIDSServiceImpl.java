package org.shanoir.ng.bids.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.service.MicroserviceRequestsService;
import org.shanoir.ng.study.dto.DatasetDescription;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.model.StudyUser;
import org.shanoir.ng.study.service.StudyService;
import org.shanoir.ng.subject.model.HemisphericDominance;
import org.shanoir.ng.subject.model.ImagedObjectCategory;
import org.shanoir.ng.subject.model.Sex;
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

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;

@Service
public class StudyBIDSServiceImpl implements StudyBIDSService {

	private static final String NULL = "null";

	private static final String BIRTH_NAME_HASH = "birth_name_hash1";

	private static final String LAST_NAME_HASH = "last_name_hash1";

	private static final String FIRST_NAME_HASH = "first_name_hash1";

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

	private static final String[] CSV_PARTICIPANTS_HEADER_IMPORT = {
			PARTICIPANT_ID,
			SUBJECT_IDENTIFIER,
			SEX,
			BIRTH_DATE,
			MANUAL_HEMISPHERIC_DOMINANCE,
			LANGUAGE_HEMISPHERIC_DOMINANCE,
			IMAGED_OBJECT_CATEGORY,
			FIRST_NAME_HASH,
			LAST_NAME_HASH,
			BIRTH_NAME_HASH
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
	public void deleteBids(Long studyDeletedId) {
		Study studyDeleted = studyService.findById(studyDeletedId);
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

	@Override
	public void updateSubjectBids(Long subjectId, Subject subject) {
		Subject oldsub = subjectService.findById(subjectId);
		// Do nothing if we can't find the subject or if the name didn't change
		if (oldsub == null || oldsub.getName().equals(subject.getName())) {
			return;
		}
		// Change folder name to new name for all concerned studies
		for (SubjectStudy subjStud : oldsub.getSubjectStudyList()) {
			String studyFolderPath = bidsStorageDir + File.separator
					+ STUDY_PREFIX + subjStud.getStudy().getId() + "_" + subjStud.getStudy().getName()
					+ File.separator;
			File subjectFile = new File(studyFolderPath	+ SUBJECT_PREFIX + oldsub.getId() + "_" + oldsub.getName());
			// Rename using new name
			subjectFile.renameTo(new File(studyFolderPath +SUBJECT_PREFIX + oldsub.getId() + "_" + subject.getName()));
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

	@Override
	public List<Subject> participantsDeserializer(File participantsTsv) throws IOException, ShanoirException {
		if (participantsTsv == null || !participantsTsv.exists()) {
			return Collections.emptyList();
		}

		// Get the CSV as String[] lines
		CsvMapper mapper = new CsvMapper();
		mapper.enable(CsvParser.Feature.WRAP_AS_ARRAY);
		MappingIterator<String[]> it = mapper.readerFor(String[].class).readValues(participantsTsv);
		List<Subject> subjects = new ArrayList<>();

		// Check that the list of column is known
		List<String> columns = Arrays.asList(it.next()[0].split(CSV_SEPARATOR));
		for (String columnFound : columns) {
			if (Arrays.asList(CSV_PARTICIPANTS_HEADER_IMPORT).indexOf(columnFound) == -1) {
				throw new ShanoirException("Non existing column in participants.tsv file. Please refer to the .sef documentation: " + columnFound);
			}
		}

		// Iterate over the lines to create new subjects and store them
		while (it.hasNext()) {
			Subject su = new Subject();
			String[] row = it.next()[0].split(CSV_SEPARATOR);

			if (columns.contains(PARTICIPANT_ID)) {
				su.setName(row[columns.indexOf(PARTICIPANT_ID)]);
			} else {
				throw new ShanoirException("Error in participants.tsv: column participant_id is mandatory.");
			}
			if (columns.contains(SUBJECT_IDENTIFIER)) {
				su.setIdentifier(NULL.equals(row[columns.indexOf(SUBJECT_IDENTIFIER)]) ? NULL : row[columns.indexOf(SUBJECT_IDENTIFIER)]);
			}
			if (columns.contains(SEX)) {
				su.setSex(NULL.equals(row[columns.indexOf(SEX)])? null: Sex.valueOf(row[columns.indexOf(SEX)]));
			}
			if (columns.contains(BIRTH_DATE)) {
				su.setBirthDate(NULL.equals(row[columns.indexOf(BIRTH_DATE)])? null : LocalDate.parse(row[columns.indexOf(BIRTH_DATE)]));
			}
			if (columns.contains(MANUAL_HEMISPHERIC_DOMINANCE)) {
				su.setManualHemisphericDominance(NULL.equals(row[columns.indexOf(MANUAL_HEMISPHERIC_DOMINANCE)])? null : HemisphericDominance.valueOf(row[columns.indexOf(MANUAL_HEMISPHERIC_DOMINANCE)]));
			}
			if (columns.contains(LANGUAGE_HEMISPHERIC_DOMINANCE)) {
				su.setLanguageHemisphericDominance(NULL.equals(row[columns.indexOf(LANGUAGE_HEMISPHERIC_DOMINANCE)])? null : HemisphericDominance.valueOf(row[columns.indexOf(LANGUAGE_HEMISPHERIC_DOMINANCE)]));
			}
			if (columns.contains(IMAGED_OBJECT_CATEGORY)) {
				su.setImagedObjectCategory(ImagedObjectCategory.valueOf(row[columns.indexOf(IMAGED_OBJECT_CATEGORY)]));
			} else {
				throw new ShanoirException("Error in participants.tsv: column imaged_object_category is mandatory.");
			}
			if (columns.contains(FIRST_NAME_HASH)) {
				su.setName(NULL.equals(row[columns.indexOf(FIRST_NAME_HASH)])? null : row[columns.indexOf(FIRST_NAME_HASH)]);
			}
			if (columns.contains(LAST_NAME_HASH)) {
				su.setName(NULL.equals(row[columns.indexOf(LAST_NAME_HASH)])? null : row[columns.indexOf(LAST_NAME_HASH)]);
			}
			if (columns.contains(BIRTH_NAME_HASH)) {
				su.setName(NULL.equals(row[columns.indexOf(BIRTH_NAME_HASH)])? null : row[columns.indexOf(BIRTH_NAME_HASH)]);
			}
			subjects.add(su);
		}

		return subjects;
	}
}
