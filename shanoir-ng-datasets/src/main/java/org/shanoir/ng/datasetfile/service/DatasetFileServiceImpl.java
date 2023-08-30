package org.shanoir.ng.datasetfile.service;

import java.util.List;
import java.util.Optional;

import org.shanoir.ng.shared.exception.EntityNotFoundException;

import org.shanoir.ng.utils.Utils;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DatasetFileServiceImpl implements DatasetFileService {

	@Autowired
	private DatasetFileRepository repository;

	@Override
	public DatasetFile updateValues(DatasetFile from, DatasetFile to) {
		to.setDatasetExpression(from.getDatasetExpression());
		to.setPacs(from.isPacs());
		to.setPath(from.getPath());
		return to;
	}

	@Override
	public Optional<DatasetFile> findById(final Long id) {
		return repository.findById(id);
	}

	@Override
	public List<DatasetFile> findAll() {
		return Utils.toList(repository.findAll());
	}

	@Override
	public DatasetFile create(final DatasetFile entity) {
		DatasetFile savedEntity = repository.save(entity);
		return savedEntity;
	}

	@Override
	public DatasetFile update(final DatasetFile entity) throws EntityNotFoundException {
		final Optional<DatasetFile> entityDbOpt = repository.findById(entity.getId());
		final DatasetFile entityDb = entityDbOpt.orElseThrow(
				() -> new EntityNotFoundException(DatasetFile.class, entity.getId()));
		updateValues(entity, entityDb);
		return repository.save(entityDb);
	} 

	@Override
	public void deleteById(final Long id) throws EntityNotFoundException  {
		final Optional<DatasetFile> entity = repository.findById(id);
		entity.orElseThrow(() -> new EntityNotFoundException("Cannot find entity with id = " + id));
		repository.deleteById(id);
	}

}
