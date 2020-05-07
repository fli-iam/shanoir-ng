package org.shanoir.ng.subject.service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.subject.model.HemisphericDominance;
import org.shanoir.ng.subject.model.ImagedObjectCategory;
import org.shanoir.ng.subject.model.Sex;
import org.shanoir.ng.subject.model.Subject;
import org.shanoir.ng.subject.repository.SubjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;

@Service
public class RabbitMQSubjectService {

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

	private static final String CSV_SEPARATOR = "\t";

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

	private static final Logger LOG = LoggerFactory.getLogger(RabbitMQSubjectService.class);

	@Autowired
	SubjectRepository subjectRepository;
	
	/**
	 * This methods allows to get the particpants.tsv file from BIDS/SEF import and deserialize it into subjects
	 * Then the non existing ones are created
	 * We finally return the full list of subjects
	 * @param participantsFilePath the partcipants.tsv file given
	 * @return A list of subjects updated with their IDs.
	 * If an error occurs, a list of a single subject with no ID and only a name is sent back
	 * @throws JsonProcessingException
	 */
	@RabbitListener(bindings = @QueueBinding(
	        value = @Queue(value = RabbitMQConfiguration.SUBJECTS_QUEUE, durable = "true"),
	        exchange = @Exchange(value = RabbitMQConfiguration.SUBJECTS_EXCHANGE, ignoreDeclarationExceptions = "true",
	        	autoDelete = "false", durable = "true", type=ExchangeTypes.FANOUT)))
	public String manageParticpants(String participantsFilePath) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		try {
			File participantsFile = new File(participantsFilePath);
			if (!participantsFile.exists()) {
				LOG.error("Could not find the definition of participants.tsv, no subjects created: " + participantsFilePath);
				throw new ShanoirException("Error while loading participants.tsv file. Please contact an administrator.");
			}

			// Deserialize in a list of subjects
			List<Subject> participants = participantsDeserializer(participantsFile);

			// Get all existing subjects and the list of their names
			List<Subject> existingSubjects = StreamSupport.stream(subjectRepository.findAll().spliterator(), false).collect(Collectors.toList());
			List<String> existingNames = existingSubjects.stream().map(subject -> subject.getName()).collect(Collectors.toList());

			// Either create subjects or set their IDs
			List<IdName> participantsToReturn = new ArrayList<>();
			for (Subject subjToCreate : participants) {
				if (!existingNames.contains(subjToCreate.getName())) {
					// If not existing, create a new one
					Subject created = subjectRepository.save(subjToCreate);
					participantsToReturn.add(new IdName(created.getId(), created.getName()));
				} else {
					subjToCreate.setId(getSubjectIdByName(subjToCreate.getName(), existingSubjects));
					participantsToReturn.add(new IdName(subjToCreate.getId(), subjToCreate.getName()));
				}
			}
			// Return the list of subjects updated with their IDs.
			return mapper.writeValueAsString(participantsToReturn);
		} catch (Exception e) {
			LOG.error("Something went wrong deserializing the event. {}", e.getMessage());
			IdName subj = new IdName(null, "Something went wrong parsing participants.tsv: " + e.getMessage());
			List<IdName> errors = new ArrayList<>();
			errors.add(subj);
			return mapper.writeValueAsString(errors);
		}
	}

	/**
	 * Get the ID of a subject from its name and a list of subject
	 * @param name the name of the subject to find
	 * @param subjects the list of subjects to supply
	 * @return the ID of the subject corresponding to the name, null otherwise
	 */
	public Long getSubjectIdByName(String name, List<Subject> subjects) {
		for (Subject sub : subjects) {
			if (sub.getName().equals(name)) {
				return sub.getId();
			}
		}
		return null;
	}

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
