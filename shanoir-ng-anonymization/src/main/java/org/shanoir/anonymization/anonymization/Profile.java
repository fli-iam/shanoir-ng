package org.shanoir.anonymization.anonymization;

import java.util.HashMap;
import java.util.Map;

public class Profile {

	private Integer profileColumn;
	
	private Map<String, String> anonymizationMap;

	public Profile(Integer profileColumn) {
		super();
		this.profileColumn = profileColumn;
		anonymizationMap = new HashMap<String, String>();
	}

	public Integer getProfileColumn() {
		return profileColumn;
	}

	public Map<String, String> getAnonymizationMap() {
		return anonymizationMap;
	}

	public void setAnonymizationMap(Map<String, String> anonymizationMap) {
		this.anonymizationMap = anonymizationMap;
	}

}