/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.shanoir.ng.shared.core.model.AbstractEntity;
import org.springframework.beans.BeanUtils;

public class ListDependencyUpdate {
	
	/**
	 * Updates oldValues by : deleting entities that don't exist in newValues,
	 * updating entities that exist in both and deleting entities that exist only in oldValues.
	 *
	 * @param <T> Class of your entities
	 * @param oldValues values to be updated
	 * @param newValues new values
	 */
	public static <T extends AbstractEntity> void updateWith(List<T> oldValues, List<T> newValues) {
		updateWith(oldValues, newValues, null);
		
	}

	/**
	 * Updates oldValues by : deleting entities that don't exist in newValues,
	 * updating entities that exist in both and deleting entities that exist only in oldValues.
	 *
	 * @param <T> Class of your entities
	 * @param oldValues values to be updated
	 * @param newValues new values
	 */
	public static <T extends AbstractEntity> void updateWith(List<T> oldValues, List<T> newValues, EqualCheckInterface<T> equalCheck) {
		if (newValues == null) throw new IllegalArgumentException("newValues cannot be null");
		if (oldValues == null) throw new IllegalArgumentException("oldValues cannot be null");
		
		// Find updated ids
		Map<Long, T> updatedMap = new HashMap<>();
		for (T entity : newValues) updatedMap.put(entity.getId(), entity);
		
		// remove deleted entities
		oldValues.removeIf(oldEntity -> {
			if (updatedMap.keySet().contains(oldEntity.getId())) {
				return false;
			} else if (equalCheck != null) {
				for (T newEntity : updatedMap.values()) {
					if (equalCheck.check(oldEntity, newEntity)) {
						return false;
					}
				}
			}
			return true;
		});
		
		// Index old values by their ids in a map
		Map<Long, T> oldValuesMap = new HashMap<>();
		for (T entity : oldValues) oldValuesMap.put(entity.getId(), entity);

		// Iterate over new values
		for (T newEntity : newValues) {
			T updated = null;
			if (equalCheck != null) {
				for (T old : oldValues) {
					if (equalCheck.check(old, newEntity)) {
						updated = old;
					}
				}
			}
			if (updated == null && newEntity.getId() != null) {
				updated = oldValuesMap.get(newEntity.getId());
			}
			if (updated != null) {
				Long id = updated.getId();
				BeanUtils.copyProperties(newEntity, updated, newEntity.getClass());
				updated.setId(id);
			} else {
				oldValues.add(newEntity);
			}
		}
		
	}
	
	/**
	 * Updates oldValues by : deleting entities that don't exist in newValues,
	 * updating entities that exist in both and deleting entities that exist only in oldValues.
	 *
	 * @param <T> Class of your entities
	 * @param oldValues values to be updated
	 * @param newValues new values
	 */
	public static <T extends AbstractEntity> void updateWithNoRemove(List<T> oldValues, List<T> newValues) {
		if (newValues == null) throw new IllegalArgumentException("newValues cannot be null");
		if (oldValues == null) throw new IllegalArgumentException("oldValues cannot be null");
		if (oldValues == newValues) throw new IllegalStateException("those cannot be the same object");
		
		// remove deleted entities
		//oldValues.removeIf(oldEntity -> !updatedIds.contains(oldEntity.getId()));
		
		// Index old values by their ids in a map
		Map<Long, T> oldValuesMap = new HashMap<>();
		for (T entity : oldValues) oldValuesMap.put(entity.getId(), entity);

		// Iterate over new values
		for (T newEntity : newValues) {
			if (newEntity.getId() != null) {
				T updated = oldValuesMap.get(newEntity.getId());
				if (updated != null) {
					Long id = updated.getId();
					BeanUtils.copyProperties(newEntity, updated, newEntity.getClass());					
					updated.setId(id);
				}
			} else {
				oldValues.add(newEntity);
			}
		}
		
	}
	
}
