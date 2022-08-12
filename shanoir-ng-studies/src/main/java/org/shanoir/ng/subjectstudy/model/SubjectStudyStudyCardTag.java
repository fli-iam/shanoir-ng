package org.shanoir.ng.subjectstudy.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.shanoir.ng.tag.model.Tag;

@Entity
@Table
public class SubjectStudyStudyCardTag implements Serializable {
	
	private static final long serialVersionUID = -7148798110331644158L;
	
	@Id
    @Column(name = "subject_study_id")
    private Long id;
	
	@OneToOne
    @MapsId
    @JoinColumn(name = "subject_study_id")
    private SubjectStudy subjectStudy;
	
	private int type;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	public Tag getTag() {
		Tag tag = new Tag();
		if (type == 1) {
			tag.setName("VALID");
			tag.setColor("#26a269");
		} else if (type == 2) {
			tag.setName("WARNING");
			tag.setColor("#ff7800");
		} else {
			tag.setName("ERROR");
			tag.setColor("#f11722");		
		}
		return tag;
	}

}
