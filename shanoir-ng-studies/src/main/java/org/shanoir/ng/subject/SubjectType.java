package org.shanoir.ng.subject;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.shanoir.ng.shared.hateoas.HalEntity;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@Entity
@Table(name="subject_type")
@JsonPropertyOrder({ "_links", "id", "name" })
@GenericGenerator(name = "IdOrGenerate", strategy="org.shanoir.ng.shared.model.UseIdOrGenerate")
public class SubjectType extends HalEntity{
	
	/*@Id
	private Long id;*/
	
	private String name;
	
	/*public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}*/

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	

}
