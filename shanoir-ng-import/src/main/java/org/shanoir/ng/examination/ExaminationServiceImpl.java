package org.shanoir.ng.examination;

import java.util.List;
import java.util.Optional;

import org.shanoir.ng.configuration.amqp.RabbitMqConfiguration;
import org.shanoir.ng.shared.exception.ShanoirExaminationException;
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
 * Examination service implementation.
 * 
 * @author ifakhfakh
 *
 */
@Service
public class ExaminationServiceImpl implements ExaminationService {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(ExaminationServiceImpl.class);

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private ExaminationRepository examinationRepository;

	@Override
	public void deleteById(final Long id) throws ShanoirExaminationException {
		examinationRepository.delete(id);
	}

	@Override
	public List<Examination> findAll() {
		return Utils.toList(examinationRepository.findAll());
	}

	@Override
	public List<Examination> findBy(final String fieldName, final Object value) {
		return examinationRepository.findBy(fieldName, value);
	}

	/*@Override
	public Optional<Examination> findByData(final String data) {
		return examinationRepository.findByData(data);
	}*/

	@Override
	public Examination findById(final Long id) {
		return examinationRepository.findOne(id);
	}

	@Override
	public Examination save(final Examination examination) throws ShanoirExaminationException {
		Examination savedExamination = null;
		try {
			savedExamination = examinationRepository.save(examination);
		} catch (DataIntegrityViolationException dive) {
			ShanoirExaminationException.logAndThrow(LOG, "Error while creating examination: " + dive.getMessage());
		}
		updateShanoirOld(savedExamination);
		return savedExamination;
	}

	@Override
	public Examination update(final Examination examination) throws ShanoirExaminationException {
		final Examination examinationDb = examinationRepository.findOne(examination.getId());
		updateExaminationValues(examinationDb, examination);
		try {
			examinationRepository.save(examinationDb);
		} catch (Exception e) {
			ShanoirExaminationException.logAndThrow(LOG, "Error while updating examination: " + e.getMessage());
		}
		updateShanoirOld(examinationDb);
		return examinationDb;
	}

	@Override
	public void updateFromShanoirOld(final Examination examination) throws ShanoirExaminationException {
		if (examination.getId() == null) {
			throw new IllegalArgumentException("Examination id cannot be null");
		} else {
			final Examination examinationDb = examinationRepository.findOne(examination.getId());
			if (examinationDb != null) {
				try {
					examinationRepository.save(examinationDb);
				} catch (Exception e) {
					ShanoirExaminationException.logAndThrow(LOG,
							"Error while updating examination from Shanoir Old: " + e.getMessage());
				}
			}
		}
	}

	/*
	 * Update Shanoir Old.
	 * 
	 * @param Examination examination.
	 * 
	 * @return false if it fails, true if it succeed.
	 */
	private boolean updateShanoirOld(final Examination examination) {
		try {
			LOG.info("Send update to Shanoir Old");
			rabbitTemplate.convertAndSend(RabbitMqConfiguration.queueOut().getName(),
					new ObjectMapper().writeValueAsString(examination));
			return true;
		} catch (AmqpException e) {
			LOG.error("Cannot send examination " + examination.getId() + " save/update to Shanoir Old on queue : "
					+ RabbitMqConfiguration.queueOut().getName(), e);
		} catch (JsonProcessingException e) {
			LOG.error("Cannot send examination " + examination.getId() + " save/update because of an error while serializing examination.",
					e);
		}
		return false;
	}

	/*
	 * Update some values of examination to save them in database.
	 * 
	 * @param examinationDb examination found in database.
	 * 
	 * @param examination examination with new values.
	 * 
	 * @return database examination with new values.
	 */
	private Examination updateExaminationValues(final Examination examinationDb, final Examination examination) {
		
	   	 examinationDb.setCenterId(examination.getCenterId());		 
		 examinationDb.setComment(examination.getComment());
		 examinationDb.setExaminationDate(examination.getExaminationDate());
		 examinationDb.setExtraDataFilePathList(examination.getExtraDataFilePathList());
		 examinationDb.setInvestigatorExternal(examination.isInvestigatorExternal());
		 examinationDb.setInvestigatorCenterId(examination.getInvestigatorCenterId());
		 examinationDb.setNote(examination.getNote());
		 examinationDb.setSubjectWeight(examination.getSubjectWeight());
		 examinationDb.setTimepoint(examination.getTimepoint());
		 examinationDb.setWeightUnitOfMeasure(examination.getWeightUnitOfMeasure());
		 examinationDb.setDatasetAcquisitionList(examination.getDatasetAcquisitionList());
		 examinationDb.setSubjectId(examination.getSubjectId());
		 examinationDb.setStudyId(examination.getStudyId());
		 examinationDb.setInvestigatorId(examination.getInvestigatorId());
		 examinationDb.setInstrumentBasedAssessmentList(examination.getInstrumentBasedAssessmentList());
	 
		return examinationDb;
	}

}
