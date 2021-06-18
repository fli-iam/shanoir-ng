package org.shanoir.ng.shared.migration;

import java.util.Map;

import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.event.ShanoirEvent;

/**
 * This class holds all the elements necessary for the migration of a study to another Shanoir instance.
 * @author fli
 *
 */
public class MigrationJob {

	private IdName study;
	
	private Long oldStudyId;

	private Map<Long, Long> subjectsMap;

	private Map<Long, Long> studyCardsMap;

	private Map<Long, Long> centersMap;

	private Map<Long, Long> equipmentMap;

	private ShanoirEvent event;

	private String refreshToken;
	
	private String accessToken;

	private String shanoirUrl;

	/**
	 * @return the study
	 */
	public IdName getStudy() {
		return study;
	}

	/**
	 * @param study the study to set
	 */
	public void setStudy(IdName study) {
		this.study = study;
	}

	/**
	 * @return the subjectsMap
	 */
	public Map<Long, Long> getSubjectsMap() {
		return subjectsMap;
	}

	/**
	 * @param subjectsMap the subjectsMap to set
	 */
	public void setSubjectsMap(Map<Long, Long> subjectsMap) {
		this.subjectsMap = subjectsMap;
	}

	/**
	 * @return the studyCardsMap
	 */
	public Map<Long, Long> getStudyCardsMap() {
		return studyCardsMap;
	}

	/**
	 * @param studyCardsMap the studyCardsMap to set
	 */
	public void setStudyCardsMap(Map<Long, Long> studyCardsMap) {
		this.studyCardsMap = studyCardsMap;
	}

	/**
	 * @return the centersMap
	 */
	public Map<Long, Long> getCentersMap() {
		return centersMap;
	}

	/**
	 * @param centersMap the centersMap to set
	 */
	public void setCentersMap(Map<Long, Long> centersMap) {
		this.centersMap = centersMap;
	}


	public Long getOldStudyId() {
		return oldStudyId;
	}

	public void setOldStudyId(Long oldStudyId) {
		this.oldStudyId = oldStudyId;
	}

	public Map<Long, Long> getEquipmentMap() {
		return equipmentMap;
	}

	public void setEquipmentMap(Map<Long, Long> equipmentMap) {
		this.equipmentMap = equipmentMap;
	}

	public ShanoirEvent getEvent() {
		return event;
	}

	public void setEvent(ShanoirEvent event) {
		this.event = event;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public String getShanoirUrl() {
		return shanoirUrl;
	}

	public void setShanoirUrl(String shanoirUrl) {
		this.shanoirUrl = shanoirUrl;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	
}
