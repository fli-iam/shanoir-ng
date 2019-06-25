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

package org.shanoir.ng.study.rights;

import java.util.List;

import org.shanoir.ng.shared.security.rights.StudyUserRight;

public interface StudyUserInterface {

	/**
	 * @return the receiveAnonymizationReport
	 */
	public boolean isReceiveAnonymizationReport();

	/**
	 * @param receiveAnonymizationReport the receiveAnonymizationReport to set
	 */
	public void setReceiveAnonymizationReport(boolean receiveAnonymizationReport);

	/**
	 * @return the receiveNewImportReport
	 */
	public boolean isReceiveNewImportReport();

	/**
	 * @param receiveNewImportReport the receiveNewImportReport to set
	 */
	public void setReceiveNewImportReport(boolean receiveNewImportReport);

	/**
	 * @return the studyId
	 */
	public Long getStudyId();

	/**
	 * @return the studyUserRight
	 */
	public List<StudyUserRight> getStudyUserRights();

	/**
	 * @param studyUserRight the studyUserRight to set
	 */
	public void setStudyUserRights(List<StudyUserRight> studyUserRights);

	/**
	 * @return the userId
	 */
	public Long getUserId();

	/**
	 * @param userId the userId to set
	 */
	public void setUserId(Long userId);

	public String getUserName();

	public void setUserName(String userName);

	public Long getId();
	
	public void setId(Long id);

}
