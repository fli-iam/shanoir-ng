package org.shanoir.ng.subject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;
import org.shanoir.ng.manufacturermodel.DatasetModalityType;
import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.hateoas.Links;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;


@Entity
@Table(name="subject")
@JsonPropertyOrder({ "_links", "id", "name", "identifier", "sex", "birthDate" , "imagedObjectCategory"," pseudonymusHashValues", "subjectStudyList", "languageHemisphericDominance", "manualHemisphericDominance", "userPersonalCommentList" })
@GenericGenerator(name = "IdOrGenerate", strategy = "increment")
public class Subject extends HalEntity {

	private static final long serialVersionUID = 6844259659282875507L;
	
	private Date birthDate;

	private String name;
	
	@NotNull
	@Column(nullable = false, insertable = false, updatable = false)
	@Enumerated(EnumType.STRING)
	private Sex sex;

	
	/** Relations between the subjects and the studies. */
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "subject", fetch = FetchType.LAZY)
	@Cascade( { org.hibernate.annotations.CascadeType.SAVE_UPDATE})
	private List<SubjectStudy>  subjectStudyList = new ArrayList<SubjectStudy>(0);

	private String identifier;

	
	@OneToOne 
	private PseudonymusHashValues pseudonymusHashValues;
	
	
	@Column(insertable = false, updatable = false)
	@Enumerated(EnumType.STRING)
	private HemisphericDominance languageHemisphericDominance;

	/** Manual Hemispheric dominance. */
	@Column(insertable = false, updatable = false)
	@Enumerated(EnumType.STRING)
	private HemisphericDominance manualHemisphericDominance;
	
	/** The category of the subject (phantom, human alive, human cadaver, etc.). */	
	@Column(insertable = false, updatable = false)
	@Enumerated(EnumType.STRING)
	private ImagedObjectCategory imagedObjectCategory;
	
	/** Personal Comments on this subject. */
	@OneToMany(mappedBy = "subject", cascade = CascadeType.ALL)
	//@Cascade( { org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
	private List<UserPersonalCommentSubject> userPersonalCommentList = new ArrayList<UserPersonalCommentSubject>(0);
	
	/**
	 * Init HATEOAS links
	 */
	@PostLoad
	public void initLinks() {
		this.addLink(Links.REL_SELF, "subject/" + getId());
	}
	
//	@Override
//	@GeneratedValue(strategy = GenerationType.IDENTITY, generator = "IdOrGenerate")
//	@GenericGenerator(name = "IdOrGenerate", strategy="increment")
//	public Long getId() {
//		return super.getId();
//	}

	public Date getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public List<SubjectStudy> getSubjectStudyList() {
		return subjectStudyList;
	}

	public void setSubjectStudyList(List<SubjectStudy> subjectStudyList) {
		this.subjectStudyList = subjectStudyList;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public PseudonymusHashValues getPseudonymusHashValues() {
		return pseudonymusHashValues;
	}

	public void setPseudonymusHashValues(PseudonymusHashValues pseudonymusHashValues) {
		this.pseudonymusHashValues = pseudonymusHashValues;
	}


	public List<UserPersonalCommentSubject> getUserPersonalCommentList() {
		return userPersonalCommentList;
	}

	public void setUserPersonalCommentList(List<UserPersonalCommentSubject> userPersonalCommentList) {
		this.userPersonalCommentList = userPersonalCommentList;
	}

	public Sex getSex() {
		return sex;
	}

	public void setSex(Sex sex) {
		this.sex = sex;
	}

	public HemisphericDominance getLanguageHemisphericDominance() {
		return languageHemisphericDominance;
	}

	public void setLanguageHemisphericDominance(HemisphericDominance languageHemisphericDominance) {
		this.languageHemisphericDominance = languageHemisphericDominance;
	}

	public HemisphericDominance getManualHemisphericDominance() {
		return manualHemisphericDominance;
	}

	public void setManualHemisphericDominance(HemisphericDominance manualHemisphericDominance) {
		this.manualHemisphericDominance = manualHemisphericDominance;
	}

	public ImagedObjectCategory getImagedObjectCategory() {
		return imagedObjectCategory;
	}

	public void setImagedObjectCategory(ImagedObjectCategory imagedObjectCategory) {
		this.imagedObjectCategory = imagedObjectCategory;
	}

	
	

	
}
