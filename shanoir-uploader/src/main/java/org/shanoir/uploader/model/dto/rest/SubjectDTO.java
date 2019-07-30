package org.shanoir.uploader.model.dto.rest;

import java.util.Date;
import java.util.List;

public class SubjectDTO {

	   private Long id;
	    
	    private String name;
	    
	    private String identifier;

	    private Date birthDate;

	    private HemisphericDominance languageHemisphericDominance;

	    private HemisphericDominance manualHemisphericDominance;

	    private ImagedObjectCategory imagedObjectCategory;

	    private Sex sex;
	    
	    private List<SubjectStudyDTO> subjectStudyList;
	    
	    private boolean preclinical;

		/**
		 * @return the id
		 */
		public Long getId() {
			return id;
		}

		/**
		 * @param id the id to set
		 */
		public void setId(Long id) {
			this.id = id;
		}

		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}

		/**
		 * @param name the name to set
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * @return the identifier
		 */
		public String getIdentifier() {
			return identifier;
		}

		/**
		 * @param identifier the identifier to set
		 */
		public void setIdentifier(String identifier) {
			this.identifier = identifier;
		}

		/**
		 * @return the birthDate
		 */
		public Date getBirthDate() {
			return birthDate;
		}

		/**
		 * @param birthDate the birthDate to set
		 */
		public void setBirthDate(Date birthDate) {
			this.birthDate = birthDate;
		}

		/**
		 * @return the languageHemisphericDominance
		 */
		public HemisphericDominance getLanguageHemisphericDominance() {
			return languageHemisphericDominance;
		}

		/**
		 * @param languageHemisphericDominance the languageHemisphericDominance to set
		 */
		public void setLanguageHemisphericDominance(HemisphericDominance languageHemisphericDominance) {
			this.languageHemisphericDominance = languageHemisphericDominance;
		}

		/**
		 * @return the manualHemisphericDominance
		 */
		public HemisphericDominance getManualHemisphericDominance() {
			return manualHemisphericDominance;
		}

		/**
		 * @param manualHemisphericDominance the manualHemisphericDominance to set
		 */
		public void setManualHemisphericDominance(HemisphericDominance manualHemisphericDominance) {
			this.manualHemisphericDominance = manualHemisphericDominance;
		}

		/**
		 * @return the imagedObjectCategory
		 */
		public ImagedObjectCategory getImagedObjectCategory() {
			return imagedObjectCategory;
		}

		/**
		 * @param imagedObjectCategory the imagedObjectCategory to set
		 */
		public void setImagedObjectCategory(ImagedObjectCategory imagedObjectCategory) {
			this.imagedObjectCategory = imagedObjectCategory;
		}

		/**
		 * @return the sex
		 */
		public Sex getSex() {
			return sex;
		}

		/**
		 * @param sex the sex to set
		 */
		public void setSex(Sex sex) {
			this.sex = sex;
		}

		/**
		 * @return the subjectStudyList
		 */
		public List<SubjectStudyDTO> getSubjectStudyList() {
			return subjectStudyList;
		}

		/**
		 * @param subjectStudyList the subjectStudyList to set
		 */
		public void setSubjectStudyList(List<SubjectStudyDTO> subjectStudyList) {
			this.subjectStudyList = subjectStudyList;
		}
		
		public boolean isPreclinical() {
			return preclinical;
		}

		public void setPreclinical(boolean preclinical) {
			this.preclinical = preclinical;
		}
	
}
