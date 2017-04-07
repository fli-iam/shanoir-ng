package org.shanoir.ng.subject;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.shanoir.ng.shared.hateoas.HalEntity;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@Entity
@Table(name="subject_type")
public class SubjectType{
	
	@Id
	private Long id;
	
	private String name;
	
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
	
	

}
