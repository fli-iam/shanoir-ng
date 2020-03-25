package org.shanoir.ng.subject.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.shanoir.ng.bids.service.StudyBIDSService;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.exception.ShanoirException;
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
import com.fasterxml.jackson.databind.ObjectMapper;


@Service
public class RabbitMQSubjectService {

	private static final Logger LOG = LoggerFactory.getLogger(RabbitMQSubjectService.class);
	
	@Autowired
	StudyBIDSService bidsService;

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
			List<Subject> participants = bidsService.participantsDeserializer(participantsFile);

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
}
