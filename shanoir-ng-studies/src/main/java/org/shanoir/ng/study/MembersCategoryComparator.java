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
