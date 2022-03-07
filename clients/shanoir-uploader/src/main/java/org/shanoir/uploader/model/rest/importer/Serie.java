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

package org.shanoir.uploader.model.rest.importer;

import java.time.LocalDate;
import java.util.List;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.shanoir.ng.shared.dateTime.DateTimeUtils;
import org.shanoir.ng.shared.dateTime.LocalDateAnnotations;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents a serie based on Dicom as used in Shanoir.
 * 
 * @author mkain
 */
public class Serie {

	@JsonProperty("selected")
	private Boolean selected;

	@JsonProperty("seriesInstanceUID")
	private String seriesInstanceUID;

	@JsonProperty("modality")
	private String modality;

	@JsonProperty("protocolName")
	private String protocolName;

	@JsonProperty("seriesDescription")
	private String seriesDescription;

	@JsonProperty("sequenceName")
	private String sequenceName;

	@JsonProperty("seriesDate")
	@LocalDateAnnotations
	private LocalDate seriesDate;

	@JsonProperty("seriesNumber")
	private String seriesNumber;

	@JsonProperty("numberOfSeriesRelatedInstances")
	private Integer numberOfSeriesRelatedInstances;

	@JsonProperty("sopClassUID")
	private String sopClassUID;

	@JsonProperty("instances")
	private List<Instance> instances;

	@JsonProperty("imagesNumber")
	private Integer imagesNumber;

	// Keep this empty constructor to avoid Jackson deserialization exceptions
	public Serie() {}

	public Serie(Attributes attributes) {
		this.seriesInstanceUID = attributes.getString(Tag.SeriesInstanceUID);
		this.seriesDescription = attributes.getString(Tag.SeriesDescription);
		this.seriesDate = DateTimeUtils.dateToLocalDate(attributes.getDate(Tag.SeriesDate));
		this.seriesNumber = attributes.getString(Tag.SeriesNumber);
		this.numberOfSeriesRelatedInstances = attributes.getInt(Tag.NumberOfSeriesRelatedInstances, 0);
		this.modality = attributes.getString(Tag.Modality);
		this.protocolName = attributes.getString(Tag.ProtocolName);
	}

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

	public String getSeriesNumber() {
		return seriesNumber;
	}

	public void setSeriesNumber(String seriesNumber) {
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

	public Integer getImagesNumber() {
		return imagesNumber;
	}

	public void setImagesNumber(Integer imagesNumber) {
		this.imagesNumber = imagesNumber;
	}

	public List<Instance> getInstances() {
		return instances;
	}

	public void setInstances(List<Instance> instances) {
		this.instances = instances;
	}

	public String getSequenceName() {
		return sequenceName;
	}

	public void setSequenceName(String sequenceName) {
		this.sequenceName = sequenceName;
	}

}