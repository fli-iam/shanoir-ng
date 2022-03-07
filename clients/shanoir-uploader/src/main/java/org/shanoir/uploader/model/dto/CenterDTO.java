package org.shanoir.uploader.model.dto;

import java.util.List;

public class CenterDTO {

	private Long id;
	
	private String name;
	
	private List<InvestigatorDTO> investigators;
	
	public CenterDTO(Long id, String name, List<InvestigatorDTO> investigators) {
		super();
		this.id = id;
		this.name = name;
		this.investigators = investigators;
	}

	public CenterDTO(Long id, String name) {
		super();
		this.id = id;
		this.name = name;
	}
	
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

	public List<InvestigatorDTO> getInvestigators() {
		return investigators;
	}

	public void setInvestigators(List<InvestigatorDTO> investigators) {
		this.investigators = investigators;
	}
	
}
