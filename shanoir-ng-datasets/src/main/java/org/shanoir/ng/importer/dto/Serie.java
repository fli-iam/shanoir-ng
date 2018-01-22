package org.shanoir.ng.importer.dto;

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
	  
	  @JsonProperty("datasets")
	  private List<Dataset> datasets = null;

	public Boolean getSelected() {
		return selected;
	}

	public void setSelected(Boolean selected) {
		this.selected = selected;
	}

	public String getSeriesInstanceUID() {
		return seriesInstanceUID;
	}

	public void setSeriesInstanceUID(String seriesInstanceUID) {
		this.seriesInstanceUID = seriesInstanceUID;
	}

	public String getModality() {
		return modality;
	}

	public void setModality(String modality) {
		this.modality = modality;
	}

	public String getProtocolName() {
		return protocolName;
	}

	public void setProtocolName(String protocolName) {
		this.protocolName = protocolName;
	}

	public String getSeriesDescription() {
		return seriesDescription;
	}

	public void setSeriesDescription(String seriesDescription) {
		this.seriesDescription = seriesDescription;
	}

	public String getSeriesDate() {
		return seriesDate;
	}

	public void setSeriesDate(String seriesDate) {
		this.seriesDate = seriesDate;
	}

	public Integer getSeriesNumber() {
		return seriesNumber;
	}

	public void setSeriesNumber(Integer seriesNumber) {
		this.seriesNumber = seriesNumber;
	}

	public Integer getNumberOfSeriesRelatedInstances() {
		return numberOfSeriesRelatedInstances;
	}

	public void setNumberOfSeriesRelatedInstances(Integer numberOfSeriesRelatedInstances) {
		this.numberOfSeriesRelatedInstances = numberOfSeriesRelatedInstances;
	}

	public String getSopClassUID() {
		return sopClassUID;
	}

	public void setSopClassUID(String sopClassUID) {
		this.sopClassUID = sopClassUID;
	}

	public EquipmentDicom getEquipment() {
		return equipment;
	}

	public void setEquipment(EquipmentDicom equipment) {
		this.equipment = equipment;
	}

	public Boolean getIsCompressed() {
		return isCompressed;
	}

	public void setIsCompressed(Boolean isCompressed) {
		this.isCompressed = isCompressed;
	}

	public Boolean getIsMultiFrame() {
		return isMultiFrame;
	}

	public void setIsMultiFrame(Boolean isMultiFrame) {
		this.isMultiFrame = isMultiFrame;
	}

	public List<Object> getNonImages() {
		return nonImages;
	}

	public void setNonImages(List<Object> nonImages) {
		this.nonImages = nonImages;
	}

	public Integer getNonImagesNumber() {
		return nonImagesNumber;
	}

	public void setNonImagesNumber(Integer nonImagesNumber) {
		this.nonImagesNumber = nonImagesNumber;
	}

	public List<Image> getImages() {
		return images;
	}

	public void setImages(List<Image> images) {
		this.images = images;
	}

	public Integer getImagesNumber() {
		return imagesNumber;
	}

	public void setImagesNumber(Integer imagesNumber) {
		this.imagesNumber = imagesNumber;
	}

	public List<Dataset> getDatasets() {
		return datasets;
	}

	public void setDatasets(List<Dataset> datasets) {
		this.datasets = datasets;
	}
	  
	  

}
