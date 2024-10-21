package org.shanoir.ng.study.model;

public enum StudyCardPolicy {

    /**
     * Study card is mandatory during import
     */
    MANDATORY(1),

    /**
     * Study card is optional during import
     */
    OPTIONAL(2),

    /**
     * Study card is disabled during import
     */
    DISABLED(3);

    private int id;

    /**
     * Constructor.
     *
     * @param id
     *            id
     */
    private StudyCardPolicy(final int id) {
        this.id = id;
    }

    /**
     * Get the study card policy type by its id.
     *
     * @param id
     *            type id.
     * @return dataset modality type.
     */
    public static StudyCardPolicy getType(final Integer id) {
        if (id == null) {
            return null;
        }
        for (StudyCardPolicy type : StudyCardPolicy.values()) {
            if (id.equals(type.getId())) {
                return type;
            }
        }
        throw new IllegalArgumentException("No matching study card policy type for id " + id);
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

}
