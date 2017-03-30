package org.shanoir.ng.studyCards;

import java.util.List;
import java.util.Optional;

import org.shanoir.ng.configuration.amqp.RabbitMqConfiguration;
import org.shanoir.ng.shared.exception.ShanoirStudyCardsException;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Study Card service implementation.
 * 
 * @author msimon
 *
 */
@Service
public class StudyCardServiceImpl implements StudyCardService {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(StudyCardRepositoryImpl.class);

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private StudyCardRepository studyCardRepository;

	@Override
	public void deleteById(final Long id) throws ShanoirStudyCardsException {
		studyCardRepository.delete(id);
	}

	@Override
	public List<StudyCard> findAll() {
		return Utils.toList(studyCardRepository.findAll());
	}

	@Override
	public List<StudyCard> findBy(final String fieldName, final Object value) {
		return studyCardRepository.findBy(fieldName, value);
	}

	@Override
	public Optional<StudyCard> findByData(final String name) {
		return studyCardRepository.findByName(name);
	}

	@Override
	public StudyCard findById(final Long id) {
		return studyCardRepository.findOne(id);
	}

	@Override
	public StudyCard save(final StudyCard studyCard) throws ShanoirStudyCardsException {
		StudyCard savedStudyCard = null;
		try {
			savedStudyCard = studyCardRepository.save(studyCard);
		} catch (DataIntegrityViolationException dive) {
			ShanoirStudyCardsException.logAndThrow(LOG, "Error while creating template: " + dive.getMessage());
		}
		updateShanoirOld(savedStudyCard);
		return savedStudyCard;
	}

	@Override
	public StudyCard update(final StudyCard studyCard) throws ShanoirStudyCardsException {
		final StudyCard studyCardDb = studyCardRepository.findOne(studyCard.getId());
		updateStudyCardValues(studyCardDb, studyCard);
		try {
			studyCardRepository.save(studyCardDb);
		} catch (Exception e) {
			ShanoirStudyCardsException.logAndThrow(LOG, "Error while updating template: " + e.getMessage());
		}
		updateShanoirOld(studyCardDb);
		return studyCardDb;
	}

	@Override
	public void updateFromShanoirOld(final StudyCard studyCard) throws ShanoirStudyCardsException {
		if (studyCard.getId() == null) {
			throw new IllegalArgumentException("Study Card id cannot be null");
		} else {
			final StudyCard studyCardDb = studyCardRepository.findOne(studyCard.getId());
			if (studyCardDb != null) {
				try {
					studyCardDb.setName(studyCard.getName());
					studyCardDb.setDisabled(studyCard.isDisabled());
					studyCardRepository.save(studyCardDb);
				} catch (Exception e) {
					ShanoirStudyCardsException.logAndThrow(LOG,
							"Error while updating template from Shanoir Old: " + e.getMessage());
				}
			}
		}
	}

	/*
	 * Update Shanoir Old.
	 * 
	 * @param template template.
	 * 
	 * @return false if it fails, true if it succeed.
	 */
	private boolean updateShanoirOld(final StudyCard studyCard) {
		try {
			LOG.info("Send update to Shanoir Old");
			rabbitTemplate.convertAndSend(RabbitMqConfiguration.queueOut().getName(),
					new ObjectMapper().writeValueAsString(studyCard));
			return true;
		} catch (AmqpException e) {
			LOG.error("Cannot send template " + studyCard.getId() + " save/update to Shanoir Old on queue : "
					+ RabbitMqConfiguration.queueOut().getName(), e);
		} catch (JsonProcessingException e) {
			LOG.error("Cannot send template " + studyCard.getId() + " save/update because of an error while serializing template.",
					e);
		}
		return false;
	}

	/*
	 * Update some values of template to save them in database.
	 * 
	 * @param templateDb template found in database.
	 * 
	 * @param template template with new values.
	 * 
	 * @return database template with new values.
	 */
	private StudyCard updateStudyCardValues(final StudyCard studyCardDb, final StudyCard studyCard) {
		studyCardDb.setName(studyCard.getName());
		studyCardDb.setDisabled(studyCard.isDisabled());
		return studyCardDb;
	}

}
