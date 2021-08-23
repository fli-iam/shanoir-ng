/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.importer.dto;

import java.time.LocalDate;
import java.util.List;

import org.shanoir.ng.shared.dateTime.LocalDateAnnotations;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author atouboul
 *
 */
public class Serie {

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
	@LocalDateAnnotations
	private LocalDate seriesDate = null;

	@JsonProperty("seriesNumber")
	private Integer seriesNumber = null;

	@JsonProperty("sequenceName")
	private String sequenceName;

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

	@JsonProperty("multiFrameCount")
	private Integer multiFrameCount;

	@JsonProperty("isEnhanced")
	private Boolean isEnhanced;
	
	@JsonProperty("isSpectroscopy")
	private Boolean isSpectroscopy;

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

	public LocalDate getSeriesDate() {
		return seriesDate;
	}

	public void setSeriesDate(LocalDate seriesDate) {
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

	public Integer getMultiFrameCount() {
		return multiFrameCount;
	}

	public void setMultiFrameCount(Integer multiFrameCount) {
		this.multiFrameCount = multiFrameCount;
	}

	public String getSequenceName() {
		return sequenceName;
	}

	public void setSequenceName(String sequenceName) {
		this.sequenceName = sequenceName;
	}

	// TODO ATO : make this nicer as soon as model is fully defined
	public DatasetFile getFirstDatasetFileForCurrentSerie() {
		return getDatasets().get(0).getExpressionFormats().get(0).getDatasetFiles().get(0);
	}

	public Boolean getIsEnhanced() {
		return isEnhanced;
	}

	public void setIsEnhanced(Boolean isEnhanced) {
		this.isEnhanced = isEnhanced;
	}

	public Boolean getIsSpectroscopy() {
		return isSpectroscopy;
	}

	public void setIsSpectroscopy(Boolean isSpectroscopy) {
		this.isSpectroscopy = isSpectroscopy;
	}
	
}
