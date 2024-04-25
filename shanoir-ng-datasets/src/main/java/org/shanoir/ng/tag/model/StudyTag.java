package org.shanoir.ng.tag.model;

import jakarta.persistence.Entity;
import org.shanoir.ng.shared.hateoas.HalEntity;

@Entity
public class StudyTag extends HalEntity {

	private static final long serialVersionUID = 1L;

	private String name;

	private String color;

	private long studyId;

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
	public long getStudyId() {
		return studyId;
	}

	/**
	 * @param study the study to set
	 */
	public void setStudyId(long study) {
		this.studyId = study;
	}
}
