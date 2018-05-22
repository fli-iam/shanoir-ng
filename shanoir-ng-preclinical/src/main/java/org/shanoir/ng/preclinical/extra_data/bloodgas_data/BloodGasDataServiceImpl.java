package org.shanoir.ng.preclinical.extra_data.bloodgas_data;

import java.util.List;

import org.shanoir.ng.preclinical.extra_data.ExtraDataService;
import org.shanoir.ng.shared.exception.ShanoirPreclinicalException;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * BloodGasData service implementation.
 * 
 * @author sloury
 *
 */
@Service
@Transactional
public class BloodGasDataServiceImpl implements ExtraDataService<BloodGasData> {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(BloodGasDataServiceImpl.class);

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private BloodGasDataRepository bloodGasRepository;

	@Override
	public void deleteById(final Long id) throws ShanoirPreclinicalException {
		bloodGasRepository.delete(id);
	}

	@Override
	public List<BloodGasData> findAllByExaminationId(Long id) {
		return Utils.toList(bloodGasRepository.findAllByExaminationId(id));
	}

	@Override
	public List<BloodGasData> findBy(final String fieldName, final Object value) {
		return bloodGasRepository.findBy(fieldName, value);
	}

	@Override
	public BloodGasData findById(final Long id) {
		return bloodGasRepository.findOne(id);
	}

	@Override
	public BloodGasData save(final BloodGasData extradata) throws ShanoirPreclinicalException {
		BloodGasData savedPhysioData = null;
		try {
			savedPhysioData = bloodGasRepository.save(extradata);
		} catch (DataIntegrityViolationException dive) {
			ShanoirPreclinicalException.logAndThrow(LOG, "Error while creating blood gas data: " + dive.getMessage());
		}
		return savedPhysioData;
	}

	@Override
	public BloodGasData update(final BloodGasData extradata) throws ShanoirPreclinicalException {
		final BloodGasData bloodgasDataDB = bloodGasRepository.findOne(extradata.getId());
		updateBloodGasDataValues(bloodgasDataDB, extradata);
		try {
			bloodGasRepository.save(bloodgasDataDB);
		} catch (Exception e) {
			ShanoirPreclinicalException.logAndThrow(LOG, "Error while updating extradata: " + e.getMessage());
		}
		return bloodgasDataDB;
	}

	private BloodGasData updateBloodGasDataValues(final BloodGasData bloodGasDataDb, final BloodGasData bloodGasData) {
		bloodGasDataDb.setExaminationId(bloodGasData.getExaminationId());
		bloodGasDataDb.setExtradatatype(bloodGasData.getExtradatatype());
		bloodGasDataDb.setFilename(bloodGasData.getFilename());
		bloodGasDataDb.setFilepath(bloodGasData.getFilepath());
		return bloodGasDataDb;
	}

}
