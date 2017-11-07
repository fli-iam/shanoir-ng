/**
 * 
 */
package org.shanoir.ng.importer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author yyao
 *
 */
public class EquipmentDicom   {
	  @JsonProperty("manufacturer")
	  private String manufacturer = null;

	  @JsonProperty("manufacturerModelName")
	  private String manufacturerModelName = null;

	  @JsonProperty("deviceSerialNumber")
	  private String deviceSerialNumber = null;

	}
