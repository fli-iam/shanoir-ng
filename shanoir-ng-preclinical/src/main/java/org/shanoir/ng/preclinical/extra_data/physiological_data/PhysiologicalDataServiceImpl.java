package org.shanoir.ng.preclinical.extra_data.physiological_data;

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
 * PhysiologicalData service implementation.
 * 
 * @author sloury
 *
 */
@Service
@Transactional
public class PhysiologicalDataServiceImpl implements ExtraDataService<PhysiologicalData> {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(PhysiologicalDataServiceImpl.class);

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private PhysiologicalDataRepository physioDataRepository;

	@Override
	public void deleteById(final Long id) throws ShanoirPreclinicalException {
		physioDataRepository.delete(id);
	}

	@Override
	public List<PhysiologicalData> findAllByExaminationId(Long id) {
		return Utils.toList(physioDataRepository.findAllByExaminationId(id));
	}

	@Override
	public List<PhysiologicalData> findBy(final String fieldName, final Object value) {
		return physioDataRepository.findBy(fieldName, value);
	}

	@Override
	public PhysiologicalData findById(final Long id) {
		return physioDataRepository.findOne(id);
	}

	@Override
	public PhysiologicalData save(final PhysiologicalData extradata) throws ShanoirPreclinicalException {
		PhysiologicalData savedPhysioData = null;
		try {
			savedPhysioData = physioDataRepository.save(extradata);
		} catch (DataIntegrityViolationException dive) {
			ShanoirPreclinicalException.logAndThrow(LOG,
					"Error while creating examination extra data: " + dive.getMessage());
		}
		return savedPhysioData;
	}

	@Override
	public PhysiologicalData update(final PhysiologicalData extradata) throws ShanoirPreclinicalException {
		final PhysiologicalData physiologicalDataDB = physioDataRepository.findOne(extradata.getId());
		updatePhysiologicalDataValues(physiologicalDataDB, extradata);
		try {
			physioDataRepository.save(physiologicalDataDB);
		} catch (Exception e) {
			ShanoirPreclinicalException.logAndThrow(LOG, "Error while updating extradata: " + e.getMessage());
		}
		return physiologicalDataDB;
	}

	private PhysiologicalData updatePhysiologicalDataValues(final PhysiologicalData physioDataDb,
			final PhysiologicalData physioData) {
		physioDataDb.setExaminationId(physioData.getExaminationId());
		physioDataDb.setExtradatatype(physioData.getExtradatatype());
		physioDataDb.setFilename(physioData.getFilename());
		physioDataDb.setFilepath(physioData.getFilepath());
		physioDataDb.setHas_heart_rate(physioData.getHas_heart_rate());
		physioDataDb.setHas_respiratory_rate(physioData.getHas_respiratory_rate());
		physioDataDb.setHas_sao2(physioData.getHas_sao2());
		physioDataDb.setHas_temperature(physioData.getHas_temperature());
		return physioDataDb;
	}

}
