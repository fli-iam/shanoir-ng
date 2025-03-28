package org.shanoir.ng.studycard.model;

public enum RuleApplicationScope {

    EXAMINATION(1),

    ACQUISITION(2),

    DATASET(3);

    private int id;
    
    private RuleApplicationScope(int id) {
        this.id = id;
    }
    
    public static RuleApplicationScope getEnum(int id) {
        for (RuleApplicationScope type : RuleApplicationScope.values()) {
            if (type.getId() == id) return type;
        }
        throw new IllegalArgumentException(id + " is not a valid study card rule type id");
    }
    
    public int getId() {
        return id;
    }

}
