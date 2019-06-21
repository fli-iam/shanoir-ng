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

package org.shanoir.ng.study;

import java.util.Comparator;

/**
 * Comparator used to sort members by ascending right.
 * 
 * @author msimon
 *
 */
public class MembersCategoryComparator implements Comparator<MembersCategoryDTO> {

	@Override
	public int compare(MembersCategoryDTO category1, MembersCategoryDTO category2) {
		return (category1.getStudyUserType().getId() > category2.getStudyUserType().getId()) ? 1 : -1;
	}

}
