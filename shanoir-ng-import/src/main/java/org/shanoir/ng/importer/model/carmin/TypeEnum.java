package org.shanoir.ng.importer.model.carmin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TypeEnum {
    FILE("File"),

    ARCHIVE("Archive");

    private String value;

    TypeEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static TypeEnum fromValue(String text) {
        for (TypeEnum b : TypeEnum.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
