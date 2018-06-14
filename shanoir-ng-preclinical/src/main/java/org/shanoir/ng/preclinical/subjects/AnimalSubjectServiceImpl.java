package org.shanoir.ng.preclinical.subjects;

import java.util.List;

import org.shanoir.ng.preclinical.references.Reference;
import org.shanoir.ng.shared.exception.ShanoirPreclinicalException;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/**
 * AnimalSubjects service implementation.
 * 
 * @author sloury
 *
 */
@Service
public class AnimalSubjectServiceImpl implements AnimalSubjectService {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(AnimalSubjectServiceImpl.class);

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private AnimalSubjectRepository subjectsRepository;

	@Override
	public void deleteById(final Long id) throws ShanoirPreclinicalException {
		subjectsRepository.delete(id);
	}

	@Override
	public List<AnimalSubject> findAll() {
		return Utils.toList(subjectsRepository.findAll());
	}

	@Override
	public AnimalSubject findById(final Long id) {
		return subjectsRepository.findOne(id);
	}

	@Override
	public AnimalSubject save(final AnimalSubject subject) throws ShanoirPreclinicalException {
		AnimalSubject savedSubject = null;
		try {
			savedSubject = subjectsRepository.save(subject);
		} catch (DataIntegrityViolationException dive) {
			ShanoirPreclinicalException.logAndThrow(LOG, "Error while creating AnimalSubject: " + dive.getMessage());
		}
		return savedSubject;
	}

	@Override
	public AnimalSubject update(final AnimalSubject subject) throws ShanoirPreclinicalException {
		final AnimalSubject subjectDB = subjectsRepository.findOne(subject.getId());
		updateSubjectValues(subjectDB, subject);
		try {
			subjectsRepository.save(subjectDB);
		} catch (Exception e) {
			ShanoirPreclinicalException.logAndThrow(LOG, "Error while updating subject: " + e.getMessage());
		}
		return subjectDB;
	}

	private AnimalSubject updateSubjectValues(final AnimalSubject subjectDb, final AnimalSubject subject) {
		subjectDb.setSubjectId(subject.getSubjectId());
		subjectDb.setBiotype(subject.getBiotype());
		subjectDb.setProvider(subject.getProvider());
		subjectDb.setSpecie(subject.getSpecie());
		subjectDb.setStabulation(subject.getStabulation());
		subjectDb.setStrain(subject.getStrain());
		return subjectDb;
	}

	@Override
	public List<AnimalSubject> findByReference(Reference reference) {
		return Utils.toList(subjectsRepository.findByReference(reference));
	}

	@Override
	public List<AnimalSubject> findBy(String fieldName, Object value) {
		return subjectsRepository.findBy(fieldName, value);
	}

}
