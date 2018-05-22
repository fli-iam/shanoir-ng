package org.shanoir.ng.preclinical.pathologies.pathology_models;

import java.util.List;

import org.shanoir.ng.preclinical.pathologies.Pathology;
import org.shanoir.ng.shared.exception.ShanoirPreclinicalException;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;


/**
 * Pathology models service implementation.
 * 
 * @author sloury
 *
 */
@Service
public class PathologyModelServiceImpl implements PathologyModelService {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(PathologyModelServiceImpl.class);

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private PathologyModelRepository modelsRepository;

	@Override
	public void deleteById(final Long id) throws ShanoirPreclinicalException {
		modelsRepository.delete(id);
	}

	@Override
	public List<PathologyModel> findAll() {
		return Utils.toList(modelsRepository.findAll());
	}

	@Override
	public List<PathologyModel> findBy(final String fieldName, final Object value) {
		return modelsRepository.findBy(fieldName, value);
	}
	
	@Override
	public PathologyModel findById(final Long id) {
		return modelsRepository.findOne(id);
	}
	
	@Override
	public List<PathologyModel> findByPathology(Pathology pathology) {
		return Utils.toList(modelsRepository.findByPathology(pathology));
	}

	@Override
	public PathologyModel save(final PathologyModel model) throws ShanoirPreclinicalException {
		PathologyModel savedModel = null;
		try {
			savedModel = modelsRepository.save(model);
		} catch (DataIntegrityViolationException dive) {
			ShanoirPreclinicalException.logAndThrow(LOG, "Error while creating pathology model: " + dive.getMessage());
		}
		return savedModel;
	}

	@Override
	public PathologyModel update(final PathologyModel model) throws ShanoirPreclinicalException {
		final PathologyModel modelDb = modelsRepository.findOne(model.getId());
		updateModelValues(modelDb, model);
		try {
			modelsRepository.save(modelDb);
		} catch (Exception e) {
			ShanoirPreclinicalException.logAndThrow(LOG, "Error while updating pathology model: " + e.getMessage());
		}
		return modelDb;
	}

	

	private PathologyModel updateModelValues(final PathologyModel modelDb, final PathologyModel model) {
		modelDb.setName(model.getName());
		modelDb.setComment(model.getComment());
		modelDb.setFilename(model.getFilename());
		modelDb.setFilepath(model.getFilepath());
		modelDb.setPathology(model.getPathology());
		return modelDb;
	}

}
