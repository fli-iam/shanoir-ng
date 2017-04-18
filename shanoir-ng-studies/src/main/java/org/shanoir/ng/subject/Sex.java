package org.shanoir.ng.subject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PostLoad;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.hateoas.Links;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@Entity
@Table(name="sex")
@JsonPropertyOrder({ "_links", "id", "name"})
@GenericGenerator(name = "IdOrGenerate", strategy="org.shanoir.ng.shared.model.UseIdOrGenerate")
public class Sex extends HalEntity {
	
	/*@Id
	private Long id;*/
	
	@Column(nullable = false, unique = true)
	private String name;
	
	/**
	 * Init HATEOAS links
	 */
	/*@PostLoad
	public void initLinks() {
		this.addLink(Links.REL_SELF, "subject/" + getId());
	}*/

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
