package org.shanoir.ng.preclinical.references;

import java.util.List;
import java.util.Optional;

import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/**
 * Refs service implementation.
 * 
 * @author sloury
 *
 */
@Service
public class RefsServiceImpl implements RefsService {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(RefsServiceImpl.class);

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private RefsRepository refsRepository;

	@Override
	public void deleteById(final Long id) throws ShanoirException {
		refsRepository.delete(id);
	}

	@Override
	public List<Reference> findAll() {
		return Utils.toList(refsRepository.findAll());
	}

	@Override
	public List<Reference> findByCategory(String category) {
		return Utils.toList(refsRepository.findByCategory(category));
	}

	@Override
	public List<Reference> findByCategoryAndType(String category, String reftype) {
		return Utils.toList(refsRepository.findByCategoryAndType(category, reftype));
	}

	@Override
	public List<Reference> findBy(final String fieldName, final Object value) {
		return null;// refsRepository.findBy(fieldName, value);
	}

	@Override
	public Reference findByCategoryTypeAndValue(String category, String reftype, String value) {
		Optional<Reference> ref = refsRepository.findByCategoryTypeAndValue(category, reftype, value);
		if (!ref.isPresent())
			return null;
		return ref.get();
	}

	@Override
	public Optional<Reference> findByTypeAndValue(String reftype, String value) {
		return refsRepository.findByTypeAndValue(reftype, value);
	}

	@Override
	public List<String> findCategories() {
		return refsRepository.findCategories();
	}

	@Override
	public List<String> findTypesByCategory(String category) {
		return refsRepository.findTypesByCategory(category);
	}

	@Override
	public Reference findById(final Long id) {
		return refsRepository.findOne(id);
	}

	@Override
	public Reference save(final Reference reference) throws ShanoirException {
		Reference savedRef = null;
		try {
			savedRef = refsRepository.save(reference);
		} catch (DataIntegrityViolationException dive) {
			LOG.error("Error while creating  reference:  ", dive);
			throw new ShanoirException("Error while creating  reference:  ", dive);
		}
		return savedRef;
	}

	@Override
	public Reference update(final Reference ref) throws ShanoirException {
		final Reference refDb = refsRepository.findOne(ref.getId());
		updateReferenceValues(refDb, ref);
		try {
			refsRepository.save(refDb);
		} catch (Exception e) {
			LOG.error("Error while updating  reference:  ", e);
			throw new ShanoirException("Error while updating  reference:  ", e);
		}
		return refDb;
	}

	private Reference updateReferenceValues(final Reference refDb, final Reference ref) {
		refDb.setCategory(ref.getCategory());
		refDb.setReftype(ref.getReftype());
		refDb.setValue(ref.getValue());
		return refDb;
	}

}
