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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.shanoir.ng.shared.core.model.AbstractEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class
 *
 * @author jlouis
 */
public class Utils {
	
	private static final Logger LOG = LoggerFactory.getLogger(Utils.class);

	/**
	 * Convert Iterable to List
	 *
	 * @param iterable
	 * @return a list
	 */
	public static <E> List<E> toList(Iterable<E> iterable) {
		if (iterable instanceof List) {
			return (List<E>) iterable;
		}
		ArrayList<E> list = new ArrayList<E>();
		if (iterable != null) {
			for (E e : iterable) {
				list.add(e);
			}
		}
		return list;
	}

	public static boolean equalsIgnoreNull(Object o1, Object o2) {
		if (o1 == null)
			return o2 == null;
		if (o2 == null)
			return o1 == null;
		if (o1 instanceof AbstractEntity && o2 instanceof AbstractEntity) {
			return ((AbstractEntity) o1).getId().equals(((AbstractEntity) o2).getId());
		}
		return o1.equals(o2) || o2.equals(o1);
		// o1.equals(o2) is not equivalent to o2.equals(o1) ! For instance with
		// java.sql.Timestamp and java.util.Date
	}
	

	/**
	 * Deletes all files and subdirectories under dir. Returns true if all
	 * deletions were successful. If a deletion fails, the method stops
	 * attempting to delete and returns false.
	 *
	 * @param tempFolder the temp folder
	 *
	 * @return true, if delete folder
	 */
	public static boolean deleteFolder(final File tempFolder) {
		if (tempFolder.isDirectory()) {
			String[] children = tempFolder.list();

			for (int i = 0; i < children.length; i++) {
				boolean success = deleteFolder(new File(tempFolder, children[i]));

				if (!success) {
					LOG.error("deleteFolder : the removing of " + tempFolder.getAbsolutePath() + " failed");
					return false;
				}
			}
		}

		// The directory is now empty so delete it
		return tempFolder.delete();
	}
		
	public static <T> List<T> copyList(List<T> list) {
    	List<T> copy = new ArrayList<T>();
    	for (T item : list) copy.add(item);
    	return copy;
    }

	
	public static void removeIdsFromList(Iterable<Long> ids, List<? extends AbstractEntity> list) {
		for (Long id : ids) {
			int deletedIndex = -1;
			int i = 0;
			for (AbstractEntity entity : list) {
				if (id.equals(entity.getId())) {
					deletedIndex = i;
					break;
				}
				i++;
			}
			if (deletedIndex > -1) list.remove(deletedIndex);
		}
	}
	
	
	public static boolean haveOneInCommon(final Iterable<String> roles, final Iterable<String> authorities) {
		for (final String role : roles) {
			for (final String authority : authorities) {
				if (role != null && role.equals(authority)) {
					return true;
				}
			}
		}
		return false;
	}
}
