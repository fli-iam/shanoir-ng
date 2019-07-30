package org.shanoir.uploader.model.dto.rest.importmodel;

public class NIfTIConverter {
	
	private Long id;
	
	private String name;
	
	private Integer niftiConverterType;
	
	private Boolean isActive;
	
	private String comment;

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

	public Integer getNiftiConverterType() {
		return niftiConverterType;
	}

	public void setNiftiConverterType(Integer niftiConverterType) {
		this.niftiConverterType = niftiConverterType;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
	
	

}
