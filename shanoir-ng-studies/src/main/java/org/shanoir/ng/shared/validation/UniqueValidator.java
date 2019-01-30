package org.shanoir.ng.shared.validation;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.shanoir.ng.acquisitionequipment.AcquisitionEquipment;
import org.shanoir.ng.acquisitionequipment.AcquisitionEquipmentService;
import org.shanoir.ng.shared.error.FieldError;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.model.AbstractGenericItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Validator for unicity.
 * 
 * @author msimon
 *
 * @param <T>
 */
public class UniqueValidator <T extends AbstractGenericItem> {

	private static final Logger LOG = LoggerFactory.getLogger(UniqueValidator.class);

	private UniqueCheckableService<T> service;

	/**
	 * @param service
	 */
	public UniqueValidator(UniqueCheckableService<T> service) {
		super();
		this.service = service;
	}

	/**
	 * Validates what can't be done by Spring/Hibernate validation, in particular unique constraints
	 * Calls database !!!
	 * Check equals implementation for your entity !!!
	 *
	 * @param entity
	 * @return
	 */
	public FieldErrorMap validate(T entity) {
		FieldErrorMap errorMap = new FieldErrorMap();
		try {
			for (Field field : entity.getClass().getDeclaredFields()) {
				// check @unique
				if (field.isAnnotationPresent(Unique.class)) {
					String getterName = "get"+StringUtils.capitalize(field.getName());
					try {
						Method getter = entity.getClass().getMethod(getterName);
						Object value = getter.invoke(entity);
						List<T> foundedList = service.findBy(field.getName(), value);
						// If found entities and it is not the same current entity
						if (!foundedList.isEmpty() && !(foundedList.size() == 1 && foundedList.get(0).getId().equals(entity.getId()))) {
							List<FieldError> errors = new ArrayList<FieldError>();
							errors.add(new FieldError("unique", "The given value is already taken for this field, choose another value", value));
							errorMap.put(field.getName(), errors);
						}
					} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
						LOG.error("Error while checking @Unique custom annotation", e);
					} catch (NoSuchMethodException e) {
						LOG.error("Error while checking @Unique custom annotation, you must implement a method named "
								+ getterName + "() for accessing " + entity.getClass().getName() + "." + field.getName());
					}
				}
			}
		} catch (SecurityException e) {
			LOG.error("Error while checking @Unique custom annotation", e);
		}
		return errorMap;
	}

	/**
	 * Validates what can't be done by Spring/Hibernate validation, 
	 * in particular @UniqueConstraint annotation defined for the table
	 * 
	 * @param entity
	 * @return
	 */
	public FieldErrorMap validateFromTable(T entity) {
		FieldErrorMap errorMap = new FieldErrorMap();
		try {
			final Table table = entity.getClass().getAnnotation(Table.class);
			if (null != table) {			
				for (UniqueConstraint uc : table.uniqueConstraints()) {
					final List<Field> uniqueConstraintFields = new ArrayList<>();
					for (String columnName : uc.columnNames()) {
						if (columnName.contains("_")) {
							final Field[] fields= entity.getClass().getDeclaredFields();
							for (final Field f : fields) {
								JoinColumn[] cols = f.getAnnotationsByType(JoinColumn.class);
								if (cols != null) {
									for (JoinColumn col : cols) {
										if (columnName.equals(col.name())) {
											uniqueConstraintFields.add(f);
											break;
										}
									}
								}
							}
						} else {
							try {
								final Field f = entity.getClass().getDeclaredField(columnName);
								if (null != f) {
									uniqueConstraintFields.add(f);
								}
							} catch (NoSuchFieldException e) {
								LOG.error("No Such Field Exception while checking @UniqueConstraint custom annotation", e);
							}
						}
					}
					if (!uniqueConstraintFields.isEmpty()) {
						List<String> names = new ArrayList<String>();
						List<Object> values = new ArrayList<Object>();

						for (final Field f : uniqueConstraintFields) {
							String getterName = "get"+StringUtils.capitalize(f.getName());
							Method getter = entity.getClass().getMethod(getterName);
							Object value = null;
							try {
								value = getter.invoke(entity);
							} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
								LOG.error("Error while checking @UniqueConstraint custom annotation", e);
							}
							names.add(f.getName());
							values.add(value);

						}
						// If found entities and it is not the same current entity
						List<AcquisitionEquipment> foundedList = ((AcquisitionEquipmentService) service).findByCoupleOfFieldValue(names.get(0), values.get(0), names.get(1), values.get(1));

						// If found entities and it is not the same current entity
						if (!foundedList.isEmpty() && !(foundedList.size() == 1 && foundedList.get(0).getId().equals(entity.getId()))) {
							List<FieldError> errors = new ArrayList<FieldError>();
							String joinedValues = String.join(" - ", values.toString());
							errors.add(new FieldError("unique", "The given value is already taken for this field, choose another", joinedValues));
							String joinedNames = String.join(" - ", names);
							errorMap.put(joinedNames, errors);
						}
					}

				}
			}
		} catch (SecurityException e) {
			LOG.error("Error while checking @Unique custom annotation", e);
		} catch (NoSuchMethodException e) {
			LOG.error("Error no such method exception");
		}
		return errorMap;
	}
}