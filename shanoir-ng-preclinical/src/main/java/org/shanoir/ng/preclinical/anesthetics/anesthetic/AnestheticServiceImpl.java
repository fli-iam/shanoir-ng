package org.shanoir.ng.preclinical.anesthetics.anesthetic;

import java.util.List;

import org.shanoir.ng.shared.exception.ShanoirPreclinicalException;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;


/**
 * Anesthetic service implementation.
 * 
 * @author sloury
 *
 */
@Service
public class AnestheticServiceImpl implements AnestheticService {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(AnestheticServiceImpl.class);

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private AnestheticRepository anestheticsRepository;

	@Override
	public void deleteById(final Long id) throws ShanoirPreclinicalException {
		anestheticsRepository.delete(id);
	}

	@Override
	public List<Anesthetic> findAll() {
		return Utils.toList(anestheticsRepository.findAll());
	}
	
	@Override
	public List<Anesthetic> findAllByAnestheticType(AnestheticType type) {
		return Utils.toList(anestheticsRepository.findAllByAnestheticType(type));
	}

	@Override
	public List<Anesthetic> findBy(final String fieldName, final Object value) {
		return anestheticsRepository.findBy(fieldName, value);
	}
	
	@Override
	public Anesthetic findById(final Long id) {
		return anestheticsRepository.findOne(id);
	}
	
	@Override
	public Anesthetic save(final Anesthetic anesthetic) throws ShanoirPreclinicalException {
		Anesthetic savedAnesthetic = null;
		try {
			savedAnesthetic = anestheticsRepository.save(anesthetic);
		} catch (DataIntegrityViolationException dive) {
			ShanoirPreclinicalException.logAndThrow(LOG, "Error while creating anesthetic: " + dive.getMessage());
		}
		return savedAnesthetic;
	}

	@Override
	public Anesthetic update(final Anesthetic anesthetic) throws ShanoirPreclinicalException {
		final Anesthetic anestheticDb = anestheticsRepository.findOne(anesthetic.getId());
		updateModelValues(anestheticDb, anesthetic);
		try {
			anestheticsRepository.save(anestheticDb);
		} catch (Exception e) {
			ShanoirPreclinicalException.logAndThrow(LOG, "Error while updating anesthetic: " + e.getMessage());
		}
		return anestheticDb;
	}

	

	private Anesthetic updateModelValues(final Anesthetic anestheticDb, final Anesthetic anesthetic) {
		anestheticDb.setName(anesthetic.getName());
		anestheticDb.setComment(anesthetic.getComment());
		anestheticDb.setAnestheticType(anesthetic.getAnestheticType());
		//anestheticDb.setIngredients(anesthetic.getIngredients());
		return anestheticDb;
	}

}
