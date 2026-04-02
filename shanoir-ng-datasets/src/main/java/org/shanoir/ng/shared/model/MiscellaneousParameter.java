package org.shanoir.ng.shared.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.shanoir.ng.shared.core.model.AbstractEntity;
import org.shanoir.ng.shared.core.model.IdName;

@Entity
@Table(name = "miscellaneous_parameter")
public class MiscellaneousParameter extends AbstractEntity {

    @Id
    private String name;

    private String value;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
