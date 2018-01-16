package org.shanoir.ng.importer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author atouboul
 *
 */

public class Subject {

    @JsonProperty("id")
    private long id;

    @JsonProperty("name")
    private String name;

}
