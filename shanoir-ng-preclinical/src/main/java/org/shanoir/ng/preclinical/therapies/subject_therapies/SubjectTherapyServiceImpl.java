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

package org.shanoir.ng.preclinical.therapies.subject_therapies;

import java.util.List;

import org.shanoir.ng.preclinical.subjects.AnimalSubject;
import org.shanoir.ng.preclinical.therapies.Therapy;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/**
 * Subject therapy service implementation.
 * 
 * @author sloury
 *
 */
@Service
public class SubjectTherapyServiceImpl implements SubjectTherapyService {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(SubjectTherapyServiceImpl.class);

	@Autowired
	private SubjectTherapyRepository subtherapiesRepository;

	@Override
	public void deleteById(final Long id) throws ShanoirException {
		subtherapiesRepository.deleteById(id);
	}

	@Override
	public void deleteByAnimalSubject(AnimalSubject animalSubject) throws ShanoirException {
		List<SubjectTherapy> therapies = findAllByAnimalSubject(animalSubject);
		for (SubjectTherapy subjectTherapy : therapies) {
			deleteById(subjectTherapy.getId());
		}
	}

	@Override
	public List<SubjectTherapy> findAll() {
		return Utils.toList(subtherapiesRepository.findAll());
	}

	@Override
	public List<SubjectTherapy> findAllByAnimalSubject(AnimalSubject animalSubject) {
		return Utils.toList(subtherapiesRepository.findByAnimalSubject(animalSubject));
	}

	@Override
	public List<SubjectTherapy> findAllByTherapy(Therapy therapy) {
		return Utils.toList(subtherapiesRepository.findByTherapy(therapy));
	}

	@Override
	public SubjectTherapy findById(final Long id) {
		return subtherapiesRepository.findById(id).orElse(null);
	}

	@Override
	public SubjectTherapy save(final SubjectTherapy subtherapy) throws ShanoirException {
		SubjectTherapy savedTherapy = null;
		try {
			savedTherapy = subtherapiesRepository.save(subtherapy);
		} catch (DataIntegrityViolationException dive) {
			LOG.error("Error while creating subject therapy:  ", dive);
			throw new ShanoirException("Error while creating subject therapy:  ", dive);
		}
		return savedTherapy;
	}

	@Override
	public SubjectTherapy update(final SubjectTherapy subtherapy) throws ShanoirException {
		final SubjectTherapy subtherapyDb = subtherapiesRepository.findById(subtherapy.getId()).orElse(null);
		updateModelValues(subtherapyDb, subtherapy);
		try {
			subtherapiesRepository.save(subtherapyDb);
		} catch (Exception e) {
			LOG.error("Error while updating subject therapy:  ", e);
			throw new ShanoirException("Error while updating subject therapy:  ", e);
		}
		return subtherapyDb;
	}

	private SubjectTherapy updateModelValues(final SubjectTherapy subtherapyDb, final SubjectTherapy subtherapy) {
		subtherapyDb.setTherapy(subtherapy.getTherapy());
		subtherapyDb.setDose(subtherapy.getDose());
		subtherapyDb.setDoseUnit(subtherapy.getDoseUnit());
		subtherapyDb.setFrequency(subtherapy.getFrequency());
		subtherapyDb.setMolecule(subtherapy.getMolecule());
		subtherapyDb.setStartDate(subtherapy.getStartDate());
		subtherapyDb.setEndDate(subtherapy.getEndDate());
		return subtherapyDb;
	}

}
