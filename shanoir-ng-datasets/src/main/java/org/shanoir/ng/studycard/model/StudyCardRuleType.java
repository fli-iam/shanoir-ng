package org.shanoir.ng.studycard.model;

public enum StudyCardRuleType {

    EXAMINATION(1),

    ACQUISITION(2),

    DATASET(3);

    private int id;
	
	private StudyCardRuleType(int id) {
		this.id = id;
	}
	
	public static StudyCardRuleType getEnum(int id) {
		for (StudyCardRuleType type : StudyCardRuleType.values()) {
			if (type.getId() == id) return type;
		}
		throw new IllegalArgumentException(id + " is not a valid study card rule type id");
	}
	
	public int getId() {
		return id;
	}

}
