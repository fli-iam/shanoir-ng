package org.shanoir.uploader.model.dto.rest;

import java.util.Date;

import org.shanoir.uploader.model.ExportData;
import org.shanoir.uploader.model.PseudonymusHashValues;
import org.shanoir.uploader.utils.Util;

public class SubjectFromShupDTO {

    private Long id;
    
    private String name;
    
    private String identifier;

    private Date birthDate;

    private Integer languageHemisphericDominance;

    private Integer manualHemisphericDominance;

    private Integer imagedObjectCategory;

    private Integer sex;
    
    private Long studyId;
    
    private Long studyCardId;
    
    private Integer subjectType;
    
    private Boolean physicallyInvolved;
    
    private String subjectStudyIdentifier;
    
    private boolean preclinical;
    
    private PseudonymusHashValues pseudonymusHashValues;

    
	public SubjectFromShupDTO(Long id, String name, String identifier, Date birthDate,
			Integer languageHemisphericDominance, Integer manualHemisphericDominance, Integer imagedObjectCategory,
			Integer sex, Long studyId, Long studyCardId, Integer subjectType, Boolean physicallyInvolved,
			String subjectStudyIdentifier, boolean preclinical, PseudonymusHashValues pseudonymusHashValues) {
		super();
		this.id = id;
		this.name = name;
		this.identifier = identifier;
		this.birthDate = birthDate;
		this.languageHemisphericDominance = languageHemisphericDominance;
		this.manualHemisphericDominance = manualHemisphericDominance;
		this.imagedObjectCategory = imagedObjectCategory;
		this.sex = sex;
		this.studyId = studyId;
		this.studyCardId = studyCardId;
		this.subjectType = subjectType;
		this.physicallyInvolved = physicallyInvolved;
		this.subjectStudyIdentifier = subjectStudyIdentifier;
		this.preclinical = preclinical;
		this.pseudonymusHashValues = pseudonymusHashValues;
	}
	
	public SubjectFromShupDTO(ExportData exportData) {
		super();
		this.id = exportData.getSubject().getId();
		this.name = exportData.getSubject().getName();
		this.identifier = exportData.getSubject().getIdentifier();
		this.birthDate = Util.toDate(exportData.getSubject().getBirthDate());
		this.languageHemisphericDominance = hemisphericDominanceMapperForShanoirNG(exportData.getSubject().getLanguageHemisphericDominance());
		this.manualHemisphericDominance = hemisphericDominanceMapperForShanoirNG(exportData.getSubject().getManualHemisphericDominance());
		this.imagedObjectCategory = imageObjectCategoriesMapperForShanoirNG(exportData.getSubject().getImagedObjectCategory());
		this.sex = sexMapperForShanoirNG(exportData.getSubject().getSex());
		this.studyId = Long.valueOf(exportData.getStudy().getId());
		this.studyCardId = Long.valueOf(exportData.getStudyCard().getId());
		this.subjectType = subjectTypeMapperForShanoirNG(exportData.getSubjectType());
		this.physicallyInvolved = exportData.isPhysicallyInvolved();
		this.subjectStudyIdentifier = exportData.getSubject().getIdentifier();
		this.pseudonymusHashValues = exportData.getSubject().getPseudonymusHashValues();

	}

	public PseudonymusHashValues getPseudonymusHashValues() {
		return pseudonymusHashValues;
	}

	public void setPseudonymusHashValues(PseudonymusHashValues pseudonymusHashValues) {
		this.pseudonymusHashValues = pseudonymusHashValues;
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

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public Date getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}

	public Integer getLanguageHemisphericDominance() {
		return languageHemisphericDominance;
	}

	public void setLanguageHemisphericDominance(Integer languageHemisphericDominance) {
		this.languageHemisphericDominance = languageHemisphericDominance;
	}

	public Integer getManualHemisphericDominance() {
		return manualHemisphericDominance;
	}

	public void setManualHemisphericDominance(Integer manualHemisphericDominance) {
		this.manualHemisphericDominance = manualHemisphericDominance;
	}

	public Integer getImagedObjectCategory() {
		return imagedObjectCategory;
	}

	public void setImagedObjectCategory(Integer imagedObjectCategory) {
		this.imagedObjectCategory = imagedObjectCategory;
	}

	public Integer getSex() {
		return sex;
	}

	public void setSex(Integer sex) {
		this.sex = sex;
	}

	public Long getStudyId() {
		return studyId;
	}

	public void setStudyId(Long studyId) {
		this.studyId = studyId;
	}

	public Long getStudyCardId() {
		return studyCardId;
	}

	public void setStudyCardId(Long studyCardId) {
		this.studyCardId = studyCardId;
	}

	public Integer getSubjectType() {
		return subjectType;
	}

	public void setSubjectType(Integer subjectType) {
		this.subjectType = subjectType;
	}

	public Boolean getPhysicallyInvolved() {
		return physicallyInvolved;
	}

	public void setPhysicallyInvolved(Boolean physicallyInvolved) {
		this.physicallyInvolved = physicallyInvolved;
	}

	public String getSubjectStudyIdentifier() {
		return subjectStudyIdentifier;
	}

	public void setSubjectStudyIdentifier(String subjectStudyIdentifier) {
		this.subjectStudyIdentifier = subjectStudyIdentifier;
	}

	public boolean isPreclinical() {
		return preclinical;
	}

	public void setPreclinical(boolean preclinical) {
		this.preclinical = preclinical;
	}
	
	
	public Integer subjectTypeMapperForShanoirNG(String subjectType) {
		if (subjectType == null) {
			return  null;
		}
		
		if (subjectType.equals("Healthy volunteer")) {
			return  1;
		}
		if (subjectType.equals("Patient")) {
			return  2;
		}
		if (subjectType.equals("Phantom")) {
			return  3;
		}

		return  null;

	}
	
	public Integer hemisphericDominanceMapperForShanoirNG(String hemisphericDominance) {
		if (hemisphericDominance == null) {
			return  null;
		}
		
		if (hemisphericDominance.equals("Left")) {
			return  1;
		}
		if (hemisphericDominance.equals("Right")) {
			return  2;
		}

		return  null;

	}
	
	public Integer imageObjectCategoriesMapperForShanoirNG(String imageObjectCategorie) {
		if (imageObjectCategorie == null) {
			return  null;
		}
		if (imageObjectCategorie.equals("Phantom")) {
			return  1;
		}
		if (imageObjectCategorie.equals("Living human being")) {
			return  2;
		}
		if (imageObjectCategorie.equals("Human cadaver")) {
			return  3;
		}
		if (imageObjectCategorie.equals("Anatomical piece")) {
			return  4;
		}
	
		return  null;

	}

	public Integer sexMapperForShanoirNG(String sex) {
		if (sex == null) {
			return  null;
		}
		if (sex.equals("M")) {
			return  1;
		}
		if (sex.equals("F")) {
			return  2;
		}

		return  null;

	}
    
    
}
