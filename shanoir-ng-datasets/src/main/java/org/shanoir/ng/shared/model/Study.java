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
package org.shanoir.ng.shared.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.shanoir.ng.dataset.model.Dataset;

/**
 * @author yyao
 *
 */
@Entity
@Table(name = "study")
public class Study {
	
	@Id
	private long id;
	 
	private String name;

	@ManyToMany
	@JoinTable(name = "related_datasets", joinColumns = @JoinColumn(name = "study_id"), inverseJoinColumns = @JoinColumn(name = "dataset_id"))
	private List<Dataset> relatedDatasets;

	/**
	 * @return the relatedDatasets
	 */
	public List<Dataset> getRelatedDatasets() {
		return relatedDatasets;
	}

	/**
	 * @param relatedDatasets the relatedDatasets to set
	 */
	public void setRelatedDatasets(List<Dataset> relatedDatasets) {
		this.relatedDatasets = relatedDatasets;
	}

	public Study() {}

	/**
	 * @param id
	 * @param name
	 */
	public Study(Long id, String name) {
		this.id = id;
		this.name = name;
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
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

}
