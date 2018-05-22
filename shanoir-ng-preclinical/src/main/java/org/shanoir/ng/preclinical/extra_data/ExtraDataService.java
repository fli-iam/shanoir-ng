package org.shanoir.ng.preclinical.extra_data;

import java.util.List;

import org.shanoir.ng.shared.exception.ShanoirPreclinicalException;
import org.shanoir.ng.shared.validation.UniqueCheckableService;

/**
 * ExtraData service.
 *
 * @author sloury
 *
 */
public interface ExtraDataService<T> extends UniqueCheckableService {

	/**
	 * Delete an examination extra data.
	 * 
	 * @param id
	 *            examination extra data id.
	 * @throws ShanoirPreclinicalException
	 */
	void deleteById(Long id) throws ShanoirPreclinicalException;

	/**
	 * Get all the examination extra datas.
	 * 
	 * @param id
	 *            examination id.
	 * @return a list of examination extra datas.
	 */
	List<T> findAllByExaminationId(Long id);

	/**
	 * Find examination extra data by its id.
	 *
	 * @param id
	 *            examination extra data id.
	 * @return an examination extra data or null.
	 */
	T findById(Long id);

	/**
	 * Save an examination extra data.
	 *
	 * @param examination
	 *            extra data examination extra data to create.
	 * @return created examination extra data.
	 * @throws ShanoirPreclinicalException
	 */
	T save(T extradata) throws ShanoirPreclinicalException;

	/**
	 * Update an examination extra data
	 *
	 * @param examination
	 *            extra data examination extra data to update.
	 * @return updated examination extra data.
	 * @throws ShanoirPreclinicalException
	 */
	T update(T extradata) throws ShanoirPreclinicalException;

}
