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
package org.shanoir.ng.tag.service;

import java.util.List;
import java.util.Map;

public interface TagService {

	/**
	 * Get the list of tags associated to this subject (independant from studies)
	 * @param subjectIds
	 * @return the liost of tags as a list of long
	 */
	Map<Long, List<Long>> getSubjectTag(List<String> subjectNames);
}
