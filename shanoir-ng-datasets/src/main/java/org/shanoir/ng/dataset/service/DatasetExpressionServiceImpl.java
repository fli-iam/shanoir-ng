package org.shanoir.ng.dataset.service;

import java.util.List;
import java.util.Optional;

import org.shanoir.ng.shared.exception.EntityNotFoundException;

import org.shanoir.ng.utils.Utils;

import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.repository.DatasetExpressionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DatasetExpressionServiceImpl implements DatasetExpressionService {

	@Autowired
	private DatasetExpressionRepository repository;

	@Override
	public DatasetExpression updateValues(DatasetExpression from, DatasetExpression to) {
		to.setComingFromDatasetExpressions(from.getComingFromDatasetExpressions());
		to.setDataset(from.getDataset());
		to.setDatasetExpressionFormat(from.getDatasetExpressionFormat());
		to.setDatasetFiles(from.getDatasetFiles());
		to.setDatasetProcessingType(from.getDatasetProcessingType());
		to.setFirstImageAcquisitionTime(from.getFirstImageAcquisitionTime());
		to.setFrameCount(from.getFrameCount());
		to.setLastImageAcquisitionTime(from.getLastImageAcquisitionTime());
		to.setMultiFrame(from.isMultiFrame());
		to.setNiftiConverterId(from.getNiftiConverterId());
		to.setNiftiConverterVersion(from.getNiftiConverterVersion());
		to.setOriginalDatasetExpression(from.getOriginalDatasetExpression());
		to.setOriginalNiftiConversion(from.getOriginalNiftiConversion());
		return to;
	}

	@Override
	public Optional<DatasetExpression> findById(final Long id) {
		return repository.findById(id);
	}

	@Override
	public List<DatasetExpression> findAll() {
		return Utils.toList(repository.findAll());
	}

	@Override
	public DatasetExpression create(final DatasetExpression entity) {
		DatasetExpression savedEntity = repository.save(entity);
		return savedEntity;
	}

	@Override
	public DatasetExpression update(final DatasetExpression entity) throws EntityNotFoundException {
		final Optional<DatasetExpression> entityDbOpt = repository.findById(entity.getId());
		final DatasetExpression entityDb = entityDbOpt.orElseThrow(
				() -> new EntityNotFoundException(DatasetExpression.class, entity.getId()));
		updateValues(entity, entityDb);
		return repository.save(entityDb);
	}

	@Override
	public void deleteById(final Long id) throws EntityNotFoundException  {
		final Optional<DatasetExpression> entity = repository.findById(id);
		entity.orElseThrow(() -> new EntityNotFoundException("Cannot find entity with id = " + id));
		repository.deleteById(id);
	}
}
