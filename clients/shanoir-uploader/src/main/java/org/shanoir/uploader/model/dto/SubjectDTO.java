package org.shanoir.uploader.model.dto;

import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.shanoir.uploader.model.PseudonymusHashValues;

public class SubjectDTO {

	private Long id;

	private XMLGregorianCalendar birthDate;

	private String name;

	private String sex;

	private String imagedObjectCategory;

	private String languageHemisphericDominance;

	private String manualHemisphericDominance;

	private List<SubjectStudyDTO> subjectStudyList;

	private String identifier;

	private boolean isOfsep;

	private boolean exist;

	private PseudonymusHashValues pseudonymusHashValues;

	public SubjectDTO() {
		super();
	}

	public SubjectDTO(Long id, XMLGregorianCalendar birthDate, String name, String sex, String imagedObjectCategory,
			String languageHemisphericDominance, String manualHemisphericDominance,
			List<SubjectStudyDTO> subjectStudyList, String identifier) {
		super();
		this.id = id;
		this.birthDate = birthDate;
		this.name = name;
		this.sex = sex;
		this.imagedObjectCategory = imagedObjectCategory;
		this.languageHemisphericDominance = languageHemisphericDominance;
		this.manualHemisphericDominance = manualHemisphericDominance;
		this.subjectStudyList = subjectStudyList;
		this.identifier = identifier;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public XMLGregorianCalendar getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(XMLGregorianCalendar birthDate) {
		this.birthDate = birthDate;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getImagedObjectCategory() {
		return imagedObjectCategory;
	}

	public void setImagedObjectCategory(String imagedObjectCategory) {
		this.imagedObjectCategory = imagedObjectCategory;
	}

	public String getLanguageHemisphericDominance() {
		return languageHemisphericDominance;
	}

	public void setLanguageHemisphericDominance(String languageHemisphericDominance) {
		this.languageHemisphericDominance = languageHemisphericDominance;
	}

	public String getManualHemisphericDominance() {
		return manualHemisphericDominance;
	}

	public void setManualHemisphericDominance(String manualHemisphericDominance) {
		this.manualHemisphericDominance = manualHemisphericDominance;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public boolean isOfsep() {
		return isOfsep;
	}

	public void setOfsep(boolean isOfsep) {
		this.isOfsep = isOfsep;
	}

	public boolean isExist() {
		return exist;
	}

	public void setExist(boolean exist) {
		this.exist = exist;
	}

	public List<SubjectStudyDTO> getSubjectStudyList() {
		return subjectStudyList;
	}

	public void setSubjectStudyList(List<SubjectStudyDTO> subjectStudyList) {
		this.subjectStudyList = subjectStudyList;
	}

	public PseudonymusHashValues getPseudonymusHashValues() {
		return pseudonymusHashValues;
	}

	public void setPseudonymusHashValues(PseudonymusHashValues pseudonymusHashValues) {
		this.pseudonymusHashValues = pseudonymusHashValues;
	}

}
