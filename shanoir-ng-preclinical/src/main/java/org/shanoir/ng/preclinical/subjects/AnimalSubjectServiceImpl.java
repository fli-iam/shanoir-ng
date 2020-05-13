/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.preclinical.subjects;

import java.util.List;

import org.shanoir.ng.preclinical.references.Reference;
import org.shanoir.ng.shared.exception.ShanoirException;
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
	public void deleteById(final Long id) throws ShanoirException {
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
	public AnimalSubject save(final AnimalSubject subject) throws ShanoirException {
		AnimalSubject savedSubject = null;
		try {
			savedSubject = subjectsRepository.save(subject);
		} catch (DataIntegrityViolationException dive) {
			LOG.error("Error while creating  AnimalSubject:  ", dive);
			throw new ShanoirException("Error while creating  AnimalSubject:  ", dive);
		}
		return savedSubject;
	}

	@Override
	public AnimalSubject update(final AnimalSubject subject) throws ShanoirException {
		final AnimalSubject subjectDB = subjectsRepository.findOne(subject.getId());
		updateSubjectValues(subjectDB, subject);
		try {
			subjectsRepository.save(subjectDB);
		} catch (Exception e) {
			LOG.error("Error while updating  AnimalSubject:  ", e);
			throw new ShanoirException("Error while updating  AnimalSubject:  ", e);
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
