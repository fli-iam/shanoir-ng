package org.shanoir.ng.shared.error;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.shanoir.ng.shared.core.model.AbstractEntity;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.security.EditableOnlyByValidator;
import org.shanoir.ng.shared.validation.UniqueCheckableService;
import org.shanoir.ng.shared.validation.UniqueValidator;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

/**
 * Field error map.
 * 
 * @author msimon
 *
 */
public class FieldErrorMap extends HashMap<String, List<FieldError>> {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 */
	public FieldErrorMap() {
		super();
	}


	/**
	 * Constructor
	 *
	 * @param {@link
	 * 			BindingResult}
	 * @return {@link FieldErrorMap}
	 */
	public FieldErrorMap(BindingResult result) {
		if (result.hasErrors()) {
			for (ObjectError objectError : result.getAllErrors()) {
				org.springframework.validation.FieldError fieldError = (org.springframework.validation.FieldError) objectError;
				if (!this.containsKey(fieldError.getField())) {
					this.put(fieldError.getField(), new ArrayList<org.shanoir.ng.shared.error.FieldError>());
				}
				this.get(fieldError.getField()).add(new org.shanoir.ng.shared.error.FieldError(fieldError.getCode(),
						fieldError.getDefaultMessage(), fieldError.getRejectedValue()));
			}
		}
	}

	/**
	 * Merge errors properly
	 *
	 * @param maps
	 */
	public FieldErrorMap merge(FieldErrorMap map) {
		for (String fieldName : map.keySet()) {
			List<FieldError> error = map.get(fieldName);
			if (!this.containsKey(fieldName)) {
				this.put(fieldName, error);
			} else {
				this.get(fieldName).addAll(error);
			}
		}
		return this;
	}

	public FieldErrorMap checkFieldAccess(AbstractEntity entity) {
		FieldErrorMap newErrors = EditableOnlyByValidator.validate(entity);
		return this.merge(newErrors);
	}
	
	public <T extends AbstractEntity> FieldErrorMap checkFieldAccess(T entity, UniqueCheckableService<T> service) throws EntityNotFoundException {
		if (entity.getId() == null) throw new IllegalArgumentException("An updated entity must have an id. For a creation use [public FieldErrorMap checkFieldAccess(AbstractEntity entity)].");
		T dbEntity = service.findById(entity.getId());
		if (dbEntity == null) throw new EntityNotFoundException(entity.getClass(), entity.getId());
		FieldErrorMap newErrors = EditableOnlyByValidator.validate(entity, dbEntity);
		return this.merge(newErrors);
	}

	public FieldErrorMap checkBindingContraints(BindingResult result) {
		return this.merge(new FieldErrorMap(result));
	}

	public <T extends AbstractEntity> FieldErrorMap checkUniqueConstraints(T entity, UniqueCheckableService<T> service) {
		final UniqueValidator<T> uniqueValidator = new UniqueValidator<T>(service);
		return this.merge(uniqueValidator.validate(entity));
	}

}
