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

package org.shanoir.ng.preclinical.pathologies.subject_pathologies;

import java.util.List;

import org.shanoir.ng.preclinical.pathologies.Pathology;
import org.shanoir.ng.preclinical.pathologies.pathology_models.PathologyModel;
import org.shanoir.ng.preclinical.references.Reference;
import org.shanoir.ng.preclinical.subjects.AnimalSubject;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/**
 * Subject pathology service implementation.
 * 
 * @author sloury
 *
 */
@Service
public class SubjectPathologyServiceImpl implements SubjectPathologyService {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(SubjectPathologyServiceImpl.class);

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private SubjectPathologyRepository pathosRepository;

	@Override
	public void deleteById(final Long id) throws ShanoirException {
		pathosRepository.deleteById(id);
	}

	@Override
	public void deleteByAnimalSubject(AnimalSubject animalSubject) throws ShanoirException {
		List<SubjectPathology> pathologies = findByAnimalSubject(animalSubject);
		for (SubjectPathology subjectPathology : pathologies) {
			deleteById(subjectPathology.getId());
		}
	}

	@Override
	public List<SubjectPathology> findAll() {
		return Utils.toList(pathosRepository.findAll());
	}

	@Override
	public List<SubjectPathology> findByAnimalSubject(AnimalSubject animalSubject) {
		return Utils.toList(pathosRepository.findByAnimalSubject(animalSubject));
	}

	@Override
	public SubjectPathology findById(final Long id) {
		return pathosRepository.findById(id).orElse(null);
	}

	@Override
	public List<SubjectPathology> findAllByPathology(Pathology pathology) {
		return Utils.toList(pathosRepository.findAllByPathology(pathology));
	}

	@Override
	public List<SubjectPathology> findAllByPathologyModel(PathologyModel model) {
		return Utils.toList(pathosRepository.findAllByPathologyModel(model));
	}

	@Override
	public List<SubjectPathology> findAllByLocation(Reference location) {
		return Utils.toList(pathosRepository.findAllByLocation(location));
	}

	@Override
	public SubjectPathology save(final SubjectPathology pathos) throws ShanoirException {
		SubjectPathology savedPathos = null;
		try {
			savedPathos = pathosRepository.save(pathos);
		} catch (DataIntegrityViolationException dive) {
			LOG.error("Error while creating subbject pathology:  ", dive);
			throw new ShanoirException("Error while creating subbject pathology:  ", dive);
		}
		return savedPathos;
	}

	@Override
	public SubjectPathology update(final SubjectPathology pathos) throws ShanoirException {
		final SubjectPathology pathosDb = pathosRepository.findById(pathos.getId()).orElse(null);
		updateModelValues(pathosDb, pathos);
		try {
			pathosRepository.save(pathosDb);
		} catch (Exception e) {
			LOG.error("Error while updating subbject pathology:  ", e);
			throw new ShanoirException("Error while updating subbject pathology:  ", e);
		}
		return pathosDb;
	}

	private SubjectPathology updateModelValues(final SubjectPathology pathosDb, final SubjectPathology pathos) {
		pathosDb.setPathology(pathos.getPathology());
		pathosDb.setPathologyModel(pathos.getPathologyModel());
		pathosDb.setLocation(pathos.getLocation());
		pathosDb.setStartDate(pathos.getStartDate());
		pathosDb.setEndDate(pathos.getEndDate());
		return pathosDb;
	}

	@Override
	public List<SubjectPathology> findByPathologyModel(PathologyModel patMod) {
		return this.pathosRepository.findAllByPathologyModel(patMod);
	}

}
