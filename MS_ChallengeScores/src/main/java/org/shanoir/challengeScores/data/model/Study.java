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

package org.shanoir.challengeScores.data.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import org.shanoir.challengeScores.utils.Utils;

/**
 * A metric is an evaluation method from the SegPerfAnalyser, to evaluate the segmentation quality.
 * This class holds a metric and its configuration, meaning what to do in some particular cases.
 *
 * @author jlouis
 */
@Entity
public class Study {

	@Id
	private Long id = null;

	private String name;

	@ManyToMany(mappedBy = "studies")
	private List<Metric> metrics;


	/**
	 * Constructor
	 */
	public Study() {

	}


	/**
	 * Constructor
	 *
	 * @param studyId
	 */
	public Study(Long studyId) {
		this.setId(studyId);
	}


	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof Study) {
			Study other = (Study) obj;
			if (this.getId() == null) {
				return other.getId() == null && Utils.equals(this.getName(), other.getName());
			} else {
				return this.getId().equals(other.getId());
			}
		} else {
			return false;
		}
	}


	@Override
	public int hashCode() {
		if (id != null) {
			return id.hashCode();
		} else {
			return name.hashCode();
		}
	}


	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}


	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}


	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}


	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}


	/**
	 * @return the metrics
	 */
	public List<Metric> getMetrics() {
		return metrics;
	}


	/**
	 * @param metrics the metrics to set
	 */
	public void setMetrics(List<Metric> metrics) {
		this.metrics = metrics;
	}
}
