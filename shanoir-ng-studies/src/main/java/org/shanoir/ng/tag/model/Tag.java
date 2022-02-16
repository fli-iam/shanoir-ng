package org.shanoir.ng.tag.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.study.model.Study;

@Entity
public class Tag extends HalEntity {

	private static final long serialVersionUID = 1L;

	private String name;

	private String color;

	@ManyToOne
	@JoinColumn(name = "study_id")
	private Study study;

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
	 * @return the color
	 */
	public String getColor() {
		return color;
	}

	/**
	 * @param color the color to set
	 */
	public void setColor(String color) {
		this.color = color;
	}

	/**
	 * @return the study
	 */
	public Study getStudy() {
		return study;
	}

	/**
	 * @param study the study to set
	 */
	public void setStudy(Study study) {
		this.study = study;
	}
}
