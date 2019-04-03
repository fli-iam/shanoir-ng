package org.shanoir.ng.shared.core.service;

import java.util.List;

import org.shanoir.ng.shared.core.model.AbstractEntity;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

/**
 * center service implementation.
 * 
 * @author msimon
 *
 */
public abstract class BasicEntityServiceImpl<T extends AbstractEntity> implements BasicEntityService<T> {

	@Autowired
	private CrudRepository<T, Long> repository;
		
	
	/*
	 * Update an entity with the values of another.
	 * 
	 * @param from the entity with the new values.
	 * @param to the instance to update with the new values.
	 * @return the updated instance.
	 */
	protected abstract T updateValues(final T from, final T to);

	@Override
	public T findById(final Long id) {
		return repository.findOne(id);
	}
	
	@Override
	public List<T> findAll() {
		return Utils.toList(repository.findAll());
	}
	
	@Override
	public T create(final T entity) {
		T savedEntity = repository.save(entity);
		return savedEntity;
	}
	
	@Override
	public T update(final T entity) throws EntityNotFoundException {
		final T entityDb = repository.findOne(entity.getId());
		if (entityDb == null) {
			throw new EntityNotFoundException(entity.getClass(), entity.getId());
		}
		updateValues(entity, entityDb);
		return repository.save(entityDb);
	}

	@Override
	public void deleteById(final Long id) throws EntityNotFoundException  {
		final T entity = repository.findOne(id);
		if (entity == null) {
			throw new EntityNotFoundException("Cannot find entity with id = " + id);
		}
		repository.delete(id);
	}
}
