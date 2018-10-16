package org.shanoir.ng.preclinical.extra_data.examination_extra_data;

import java.util.List;

import org.shanoir.ng.preclinical.extra_data.ExtraDataService;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/**
 * ExaminationExtraData service implementation.
 * 
 * @author sloury
 *
 */
@Service
public class ExaminationExtraDataServiceImpl implements ExtraDataService<ExaminationExtraData> {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(ExaminationExtraDataServiceImpl.class);

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private ExaminationExtraDataRepository extraDataRepository;

	@Override
	public void deleteById(final Long id) throws ShanoirException {
		extraDataRepository.delete(id);
	}

	@Override
	public List<ExaminationExtraData> findAllByExaminationId(Long id) {
		return Utils.toList(extraDataRepository.findAllByExaminationId(id));
	}

	@Override
	public List<ExaminationExtraData> findBy(final String fieldName, final Object value) {
		return extraDataRepository.findBy(fieldName, value);
	}

	@Override
	public ExaminationExtraData findById(final Long id) {
		return extraDataRepository.findOne(id);
	}

	@Override
	public ExaminationExtraData save(final ExaminationExtraData extradata) throws ShanoirException {
		ExaminationExtraData savedExtraData = null;
		try {
			savedExtraData = extraDataRepository.save(extradata);
		} catch (DataIntegrityViolationException dive) {
			LOG.error("Error while creating examination extra data:  ", dive);
			throw new ShanoirException("Error while creating examination extra data:  ", dive);
		}
		return savedExtraData;
	}

	@Override
	public ExaminationExtraData update(final ExaminationExtraData extradata) throws ShanoirException {
		final ExaminationExtraData extraDataDB = extraDataRepository.findOne(extradata.getId());
		updateExtraDataValues(extraDataDB, extradata);
		try {
			extraDataRepository.save(extraDataDB);
		} catch (Exception e) {
			LOG.error("Error while updating examination extra data:  ", e);
			throw new ShanoirException("Error while updating examination extra data:  ", e);
		}
		return extraDataDB;
	}

	private ExaminationExtraData updateExtraDataValues(final ExaminationExtraData extraDataDb,
			final ExaminationExtraData extraData) {
		extraDataDb.setExaminationId(extraData.getExaminationId());
		extraDataDb.setExtradatatype(extraData.getExtradatatype());
		extraDataDb.setFilename(extraData.getFilename());
		extraDataDb.setFilepath(extraData.getFilepath());
		return extraDataDb;
	}

}
