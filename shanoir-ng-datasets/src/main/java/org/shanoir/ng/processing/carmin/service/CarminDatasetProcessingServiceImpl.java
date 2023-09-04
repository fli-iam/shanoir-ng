package org.shanoir.ng.processing.carmin.service;

import java.util.List;
import java.util.Optional;

import org.shanoir.ng.processing.carmin.model.CarminDatasetProcessing;
import org.shanoir.ng.processing.carmin.repository.CarminDatasetProcessingRepository;
import org.shanoir.ng.processing.carmin.security.CarminDatasetProcessingSecurityService;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author KhalilKes
 */
@Service
public class CarminDatasetProcessingServiceImpl implements CarminDatasetProcessingService {

	@Autowired
	private CarminDatasetProcessingRepository repository;

	@Autowired
	private CarminDatasetProcessingSecurityService carminDatasetProcessingSecurityService;

	private final String RIGHT_STR = "CAN_SEE_ALL";

	protected CarminDatasetProcessing updateValues(CarminDatasetProcessing from, CarminDatasetProcessing to) {
		to.setIdentifier(from.getIdentifier());
		to.setStatus(from.getStatus());
		to.setName(from.getName());
		to.setPipelineIdentifier(from.getPipelineIdentifier());
		to.setStartDate(from.getStartDate());
		to.setEndDate(from.getEndDate());
		to.setTimeout(from.getTimeout());
		to.setResultsLocation(from.getResultsLocation());
		to.setDatasetProcessingType(from.getDatasetProcessingType());
		to.setComment(from.getComment());
		to.setInputDatasets(from.getInputDatasets());
		to.setOutputDatasets(from.getOutputDatasets());
		to.setProcessingDate(from.getProcessingDate());
		to.setStudyId(from.getStudyId());
		return to;
	}

	@Override
	public CarminDatasetProcessing createCarminDatasetProcessing(
			final CarminDatasetProcessing carminDatasetProcessing) {
		CarminDatasetProcessing savedEntity = repository.save(carminDatasetProcessing);
		return savedEntity;
	}

	@Override
	public Optional<CarminDatasetProcessing> findByIdentifier(String identifier) {
		return repository.findByIdentifier(identifier);
	}

	@Override
	public List<CarminDatasetProcessing> findAllAllowed() {
		return carminDatasetProcessingSecurityService.filterCarminDatasetList(findAll(), RIGHT_STR);
	}

	@Override
	public CarminDatasetProcessing updateCarminDatasetProcessing(final CarminDatasetProcessing carminDatasetProcessing)
			throws EntityNotFoundException {
		final Optional<CarminDatasetProcessing> entityDbOpt = repository
				.findById(carminDatasetProcessing.getId());
		final CarminDatasetProcessing entityDb = entityDbOpt.orElseThrow(
				() -> new EntityNotFoundException(carminDatasetProcessing.getClass(), carminDatasetProcessing.getId()));

		updateValues(carminDatasetProcessing, (CarminDatasetProcessing) entityDb);
		return (CarminDatasetProcessing) repository.save(entityDb);

	}

	public Optional<CarminDatasetProcessing> findById(final Long id) {
		return repository.findById(id);
	}
	
	public List<CarminDatasetProcessing> findAll() {
		return Utils.toList(repository.findAll());
	}
	
	public CarminDatasetProcessing create(final CarminDatasetProcessing entity) {
		CarminDatasetProcessing savedEntity = repository.save(entity);
		return savedEntity;
	}
	
	public CarminDatasetProcessing update(final CarminDatasetProcessing entity) throws EntityNotFoundException {
		final Optional<CarminDatasetProcessing> entityDbOpt = repository.findById(entity.getId());
		final CarminDatasetProcessing entityDb = entityDbOpt.orElseThrow(
				() -> new EntityNotFoundException(entity.getClass(), entity.getId()));
		updateValues(entity, entityDb);
		return repository.save(entityDb);
	}

	public void deleteById(final Long id) throws EntityNotFoundException  {
		final Optional<CarminDatasetProcessing> entity = repository.findById(id);
		entity.orElseThrow(() -> new EntityNotFoundException("Cannot find entity with id = " + id));
		repository.deleteById(id);
	}

}
