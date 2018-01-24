package org.shanoir.ng.importer.dto;

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
