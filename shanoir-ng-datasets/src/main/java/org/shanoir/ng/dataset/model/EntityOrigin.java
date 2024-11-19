package org.shanoir.ng.dataset.model;

import org.shanoir.ng.examination.model.InstrumentType;

public enum EntityOrigin {


    // Is a copy
    COPY(1),

    // Has been copied
    SOURCE(2);

    private int id;

    private EntityOrigin(final int id) {
        this.id = id;
    }

    public static EntityOrigin getType(final Integer id) {
        if (id == null) {
            return null;
        }
        for (EntityOrigin entityOrigin : EntityOrigin.values()) {
            if (id.equals(entityOrigin.getId())) {
                return entityOrigin;
            }
        }
        throw new IllegalArgumentException("No matching entity origin for id " + id);
    }

    public int getId() {
        return id;
    }
}
