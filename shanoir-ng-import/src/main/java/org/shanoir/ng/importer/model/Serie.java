package org.shanoir.ng.importer.model;

import java.util.Date;
import java.util.List;


import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author atouboul
 *
 */

public class Serie   {
	  @JsonProperty("selected")
	  private Boolean selected = null;

	  @JsonProperty("seriesInstanceUID")
	  private String seriesInstanceUID = null;

	  @JsonProperty("modality")
	  private String modality = null;

	  @JsonProperty("protocolName")
	  private String protocolName = null;

	  @JsonProperty("seriesDescription")
	  private String seriesDescription = null;

	  @JsonProperty("seriesDate")
	  private String seriesDate = null;

	  @JsonProperty("seriesNumber")
	  private Integer seriesNumber = null;

	  @JsonProperty("numberOfSeriesRelatedInstances")
	  private Integer numberOfSeriesRelatedInstances = null;

	  @JsonProperty("sopClassUID")
	  private String sopClassUID = null;

	  @JsonProperty("equipment")
	  private EquipmentDicom equipment = null;

	  @JsonProperty("isCompressed")
	  private Boolean isCompressed = null;

	  @JsonProperty("isMultiFrame")
	  private Boolean isMultiFrame = null;
	  
	  @JsonProperty("nonImages")
	  private List<Object> nonImages = null;

	  @JsonProperty("nonImagesNumber")
	  private Integer nonImagesNumber = null;

	  @JsonProperty("images")
	  private List<Image> images = null;

	  @JsonProperty("imagesNumber")
	  private Integer imagesNumber = null;

	}
