package org.shanoir.ng.subject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;
import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.hateoas.Links;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;


@Entity
@Table(name="subject")
@JsonPropertyOrder({ "_links", "id", "name", "identifier", "sex", "birthDate" , "imagedObjectCategory"})
@GenericGenerator(name = "IdOrGenerate", strategy="org.shanoir.ng.shared.model.UseIdOrGenerate")
public class Subject extends HalEntity {

	private Date birthDate;

	private String name;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sex")
	private Sex sex;

	
	/** Relations between the subjects and the studies. */
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "subject", fetch = FetchType.LAZY)
	@Cascade( { org.hibernate.annotations.CascadeType.SAVE_UPDATE})
	private List<SubjectStudy>  subjectStudyList = new ArrayList<SubjectStudy>(0);

	private String identifier;

	
	@OneToOne 
	private PseudonymusHashValues pseudonymusHashValues;
	
	
	/** Language Hemispheric dominance. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn( nullable = true, updatable = true)
	private HemisphericDominance languageHemisphericDominance;

	/** Manual Hemispheric dominance. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = true, updatable = true)
	private HemisphericDominance manualHemisphericDominance;
	
	/** The category of the subject (phantom, human alive, human cadaver, etc.). */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = true, updatable = true)
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
	
	@Override
	@GeneratedValue(strategy = GenerationType.IDENTITY, generator = "IdOrGenerate")
	@GenericGenerator(name = "IdOrGenerate", strategy="org.shanoir.ng.shared.model.UseIdOrGenerate")
	public Long getId() {
		return super.getId();
	}

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

	public Sex getSex() {
		return sex;
	}

	public void setSex(Sex sex) {
		this.sex = sex;
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

	public HemisphericDominance getLanguageHemisphericDominance() {
		return languageHemisphericDominance;
	}

	public void setLanguageHemisphericDominance(HemisphericDominance languageHemisphericDominance) {
		this.languageHemisphericDominance = languageHemisphericDominance;
	}

	public HemisphericDominance getManualRefHemisphericDominance() {
		return manualHemisphericDominance;
	}

	public void setManualRefHemisphericDominance(HemisphericDominance manualRefHemisphericDominance) {
		this.manualHemisphericDominance = manualRefHemisphericDominance;
	}


	public ImagedObjectCategory getImagedObjectCategory() {
		return imagedObjectCategory;
	}

	public void setImagedObjectCategory(ImagedObjectCategory imagedObjectCategory) {
		this.imagedObjectCategory = imagedObjectCategory;
	}

	public List<UserPersonalCommentSubject> getUserPersonalCommentList() {
		return userPersonalCommentList;
	}

	public void setUserPersonalCommentList(List<UserPersonalCommentSubject> userPersonalCommentList) {
		this.userPersonalCommentList = userPersonalCommentList;
	}
	
	

	
}
