package org.shanoir.uploader.model;

import org.shanoir.uploader.ShUpConfig;

public class StudyCard implements Comparable<StudyCard> {

	private Long id;

	private String name;

	private Center center;

	private Boolean compatible;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Center getCenter() {
		return center;
	}

	public void setCenter(Center center) {
		this.center = center;
	}

	public Boolean getCompatible() {
		return compatible;
	}

	public void setCompatible(Boolean compatible) {
		this.compatible = compatible;
	}

	public String toString() {
		if (compatible) {
			return ShUpConfig.resourceBundle.getString("shanoir.uploader.import.compatible") + this.getName();
		} else {
			return this.getName();			
		}
	}

	public int compareTo(StudyCard o) {
		return Long.compare(this.getId(), o.getId());
	}

}
