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
