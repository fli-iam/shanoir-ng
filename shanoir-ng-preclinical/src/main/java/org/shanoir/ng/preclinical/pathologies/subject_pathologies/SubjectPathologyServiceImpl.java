package org.shanoir.ng.preclinical.pathologies.subject_pathologies;

import java.util.List;

import org.shanoir.ng.preclinical.pathologies.Pathology;
import org.shanoir.ng.preclinical.pathologies.pathology_models.PathologyModel;
import org.shanoir.ng.preclinical.references.Reference;
import org.shanoir.ng.preclinical.subjects.AnimalSubject;
import org.shanoir.ng.shared.exception.ShanoirPreclinicalException;
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
	public void deleteById(final Long id) throws ShanoirPreclinicalException {
		pathosRepository.delete(id);
	}

	@Override
	public void deleteByAnimalSubject(AnimalSubject animalSubject) throws ShanoirPreclinicalException {
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
		List<SubjectPathology> subjectPathologies = Utils.toList(pathosRepository.findByAnimalSubject(animalSubject));
		return subjectPathologies;
	}

	@Override
	public List<SubjectPathology> findBy(final String fieldName, final Object value) {
		return pathosRepository.findBy(fieldName, value);
	}

	@Override
	public SubjectPathology findById(final Long id) {
		return pathosRepository.findOne(id);
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
	public SubjectPathology save(final SubjectPathology pathos) throws ShanoirPreclinicalException {
		SubjectPathology savedPathos = null;
		try {
			savedPathos = pathosRepository.save(pathos);
		} catch (DataIntegrityViolationException dive) {
			ShanoirPreclinicalException.logAndThrow(LOG,
					"Error while creating subbject pathology: " + dive.getMessage());
		}
		return savedPathos;
	}

	@Override
	public SubjectPathology update(final SubjectPathology pathos) throws ShanoirPreclinicalException {
		final SubjectPathology pathosDb = pathosRepository.findOne(pathos.getId());
		updateModelValues(pathosDb, pathos);
		try {
			pathosRepository.save(pathosDb);
		} catch (Exception e) {
			ShanoirPreclinicalException.logAndThrow(LOG, "Error while updating a subject pathology: " + e.getMessage());
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

}
