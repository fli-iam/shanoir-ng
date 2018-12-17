package org.shanoir.ng.subject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.SqlResultSetMapping;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.GenericGenerator;
import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.hateoas.Links;
import org.shanoir.ng.shared.validation.Unique;
import org.shanoir.ng.subjectstudy.SubjectStudy;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;


@GenericGenerator(name = "IdOrGenerate", strategy = "increment")
@Entity
@JsonPropertyOrder({ "_links", "id", "name", "identifier", "sex", "birthDate", "imagedObjectCategory",
	"preclinical", "pseudonymusHashValues", "subjectStudyList", "languageHemisphericDominance", "manualHemisphericDominance",
"userPersonalCommentList" })
@SqlResultSetMapping(name = "subjectNameResult", classes = { @ConstructorResult(targetClass = IdNameDTO.class, columns = {
		@ColumnResult(name = "id", type = Long.class), @ColumnResult(name = "name") }) })
public class Subject extends HalEntity {

	private static final long serialVersionUID = 6844259659282875507L;

	private Date birthDate;

	@Unique
	private String name;

	/** Sex. */
	private Integer sex;

	/** Relations between the subjects and the studies. */
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "subject", fetch = FetchType.LAZY)
	private List<SubjectStudy> subjectStudyList;

	private String identifier;

	@OneToOne(cascade = CascadeType.ALL)
	private PseudonymusHashValues pseudonymusHashValues;

	/** Language Hemispheric dominance. */
	private Integer languageHemisphericDominance;

	/** Manual Hemispheric dominance. */
	private Integer manualHemisphericDominance;

	/** Flag to set the subject as pre-clinical subject */ 
	//@Column(nullable=false, columnDefinition="BOOLEAN DEFAULT false")
	@Column(nullable=false)
	@ColumnDefault("false")
	private boolean preclinical;
	
	/**
	 * The category of the subject (phantom, human alive, human cadaver, etc.).
	 */
	private Integer imagedObjectCategory;

	/** Personal Comments on this subject. */
	@OneToMany(mappedBy = "subject", cascade = CascadeType.ALL)
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
	@GenericGenerator(name = "IdOrGenerate", strategy = "increment") // strategy="org.shanoir.ng.shared.model.UseIdOrGenerate")
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
		return Sex.getSex(sex);
	}

	public void setSex(Sex sex) {
		if (sex == null) {
			this.sex = null;
		} else {
			this.sex = sex.getId();
		}
	}

	public HemisphericDominance getLanguageHemisphericDominance() {
		return HemisphericDominance.getDominance(languageHemisphericDominance);
	}

	public void setLanguageHemisphericDominance(HemisphericDominance languageHemisphericDominance) {
		if (languageHemisphericDominance == null) {
			this.languageHemisphericDominance = null;
		} else {
			this.languageHemisphericDominance = languageHemisphericDominance.getId();
		}
	}

	public HemisphericDominance getManualHemisphericDominance() {
		return HemisphericDominance.getDominance(manualHemisphericDominance);
	}

	public void setManualHemisphericDominance(HemisphericDominance manualHemisphericDominance) {
		if (manualHemisphericDominance == null) {
			this.manualHemisphericDominance = null;
		} else {
			this.manualHemisphericDominance = manualHemisphericDominance.getId();
		}
	}

	public ImagedObjectCategory getImagedObjectCategory() {
		return ImagedObjectCategory.getCategory(imagedObjectCategory);
	}

	public void setImagedObjectCategory(ImagedObjectCategory imagedObjectCategory) {
		if (imagedObjectCategory == null) {
			this.imagedObjectCategory = null;
		} else {
			this.imagedObjectCategory = imagedObjectCategory.getId();
		}
	}

	public boolean isPreclinical() {
		return preclinical;
	}

	public void setPreclinical(boolean preclinical) {
		this.preclinical = preclinical;
	}

}
