package org.shanoir.uploader.model.rest;

public enum StudyCardPolicy {

    /**
     * Study card is mandatory during import
     */
    MANDATORY(1),

    /**
     * Study card is disabled during import
     */
    DISABLED(2);

    private int id;

    /**
     * Constructor.
     *
     * @param id
     *           id
     */
    private StudyCardPolicy(final int id) {
        this.id = id;
    }

    public String getIdString() {
        return Integer.toString(id);
    }

}
