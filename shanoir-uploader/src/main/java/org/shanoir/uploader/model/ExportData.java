package org.shanoir.uploader.model;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;

import javax.xml.datatype.XMLGregorianCalendar;

import org.shanoir.uploader.model.dto.ExaminationDTO;
import org.shanoir.uploader.model.dto.SubjectDTO;
import org.shanoir.uploader.utils.Util;



/**
 * TO DELETE!!!!
 * 
 * This is the model class for the Study, StudyCard, Subject and MR Examination 
 * selection in order to import the data automatically under Shanoir Webapp. 
 * 
 * Part of the MVC Pattern :  Model
 * Controller is available under package org.shanoir.uploader.action as importDetailsListener.java
 * View is available under package org.shanoir.uploader.gui as importDetailsDialog.java
 * 
 * @author atouboul
 *
 */
public class ExportData extends Observable {

		// OUTPUT ATTRIBUTES
		private Study study;
		private StudyCard studyCard;
		private boolean isPhysicallyInvolved;
		private String subjectType;
		private SubjectDTO subject;
		private ExaminationDTO existingExamination;
		private Center centerOfNewExamination;
		private Investigator executiveOfNewExamination;
		private XMLGregorianCalendar dateOfNewExamination;
		
		private String commentOfNewExamination;
		private Long examinationId;
		private String mriCenter;
		private String mriCenterAddress;

		// INPUT ATTRIBUTES
		private List<Study> studyWithStudyCards;
		private List<ExaminationDTO> examinationList;		
		
		// INPUT AND OUTPUT ATTRIBUTE
		private Long subjectStudyId;
		
		// MAPPING OF INPUT ATTRIBUTES
		private List<Investigator> investigatorListForCenter;
		HashMap<String, Study> hashStudy = null; 
		HashMap<String, StudyCard> hashStudyCard = null ;
		HashMap<String, Center> hashCenter = null; 
		HashMap<String, Investigator> hashExternalInvestigator = null ;
		HashMap<String, ExaminationDTO> hashExamination = null ; 
		
		// OTHER
		private static final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		private boolean resetInProgress = false;
		
		public Study getStudy() {
			return study;
		}

		public void setStudy(Study study) {
			this.study = study;
			setChanged();
			notifyObservers("study");
		}

		public StudyCard getStudyCard() {
			return studyCard;
		}

		public void setStudyCard(StudyCard studyCard) {
			this.studyCard = studyCard;
			setChanged();
			notifyObservers("studycard");
		}

		public SubjectDTO getSubject() {
			return subject;
		}

		public void setSubject(SubjectDTO subject) {
			this.subject = subject;
			setChanged();
			notifyObservers("subject");
		}
		
		public void setSubjectName(String name) {
			if (name != null){
				if (this.subject == null) this.subject = new SubjectDTO(); 
				this.subject.setName(name);
				setChanged();
				notifyObservers("subjectmanual");
			}
		}

		public List<Study> getStudyWithStudyCards() {
			return studyWithStudyCards;
		}

		public void setStudyWithStudyCards(List<Study> studyWithStudyCards) {
			if (studyWithStudyCards != null) {
				hashStudy = new HashMap<String, Study>();
				hashStudyCard = new HashMap<String, StudyCard>();
				hashCenter = new HashMap<String, Center>();
				hashExternalInvestigator = new HashMap<String, Investigator>();  
				for (Study s : studyWithStudyCards) {
					hashStudy.put(s.getName(), s);
					if (s.getStudyCards() != null) {
						for (StudyCard sc : s.getStudyCards()) {
							hashStudyCard.put(sc.getName(), sc);
						}
					}
					if (s.getCenters() != null && s.getCenters().size() > 0) {
						for (Center c : s.getCenters()) {
							hashCenter.put(c.getName(), c);
							if (c.getInvestigatorList() != null  && c.getInvestigatorList().size() > 0) {
								for (Investigator i : c.getInvestigatorList()) {
									hashExternalInvestigator.put(i.getName(), i);
								}
							}
						}
					}
				}
				this.studyWithStudyCards = studyWithStudyCards;
				setChanged();
				notifyObservers("studywithstudycard");
			}
		}
		
		public HashMap<String, Study> getHashStudy() {
			return hashStudy;
		}

		public void setHashStudy(HashMap<String, Study> hashStudy) {
			this.hashStudy = hashStudy;
		}

		public HashMap<String, StudyCard> getHashStudyCard() {
			return hashStudyCard;
		}

		public void setHashStudyCard(HashMap<String, StudyCard> hashStudyCard) {
			this.hashStudyCard = hashStudyCard;
		}

		public void forceSubject(SubjectDTO subject) {
			this.subject = subject;
		}
		
		public void setImagedObjectCategory(String imageObjectCategory) {
			if (this.subject != null) {
				subject.setImagedObjectCategory(imageObjectCategory);
				setChanged();
				notifyObservers("imagedObjectCategory");
			}
		}

		public String getSubjectType() {
			return subjectType;
		}

		public void setSubjectType(String subjectType) {
			this.subjectType = subjectType;
			setChanged();
			notifyObservers("subjecttype");
		}
		
		public boolean isPhysicallyInvolved() {
			return isPhysicallyInvolved;
		}

		public void setPhysicallyInvolved(boolean isPhysicallyInvolved,boolean notify) {
			this.isPhysicallyInvolved = isPhysicallyInvolved;
			if (notify){
				setChanged();
				notifyObservers("subjectphysicallyinvolved");
			}
		}
		
		public Long getSubjectStudyId() {
			return subjectStudyId;
		}

		public void setSubjectStudyId(Long subjectStudyId) {
			this.subjectStudyId = subjectStudyId;
		}
		
		public Center getCenterOfNewExamination() {
			return centerOfNewExamination;
		}

		public void setCenterOfNewExamination(String centerOfNewExamination) {
			if (centerOfNewExamination != null) {
				this.centerOfNewExamination = (this.hashCenter.get(centerOfNewExamination));
				this.setInvestigatorListForCenter(this.getHashCenter().get(centerOfNewExamination).getInvestigatorList());
				Collections.sort(this.getInvestigatorListForCenter());
				setChanged();
				notifyObservers("examinationCenter");
			}
		}

		public Investigator getExecutiveOfNewExamination() {
			return executiveOfNewExamination;
		}

		public void setExecutiveOfNewExamination(String executiveOfNewExamination) {
			if (executiveOfNewExamination != null && !executiveOfNewExamination.equals("")) {
				this.executiveOfNewExamination = (getHashExternalInvestigator().get(executiveOfNewExamination));
			} else {
				this.executiveOfNewExamination = null;
			}
			setChanged();
			notifyObservers("examinationExecutive");
		}

		public XMLGregorianCalendar getDateOfNewExamination() {
			return dateOfNewExamination;
		}

		public void setDateOfNewExamination(XMLGregorianCalendar dateOfNewExamination) {
			this.dateOfNewExamination = dateOfNewExamination;
			setChanged();
			notifyObservers("examinationDate");
		}

		public String getCommentOfNewExamination() {
			return commentOfNewExamination;
		}

		public void setCommentOfNewExamination(String commentOfNewExamination) {
			if ((commentOfNewExamination == null) || (commentOfNewExamination.equals(""))) {
				this.commentOfNewExamination = null;
			} else {
				this.commentOfNewExamination = commentOfNewExamination;
			}
			setChanged();
			notifyObservers("updateMRComment");
		}

		public void removeCommentOfNewExamination() {
			this.commentOfNewExamination = null;
			setChanged();
			notifyObservers("updateMRComment");
		}
		
		public void setAutoCommentOfNewExamination(String commentOfNewExamination) {
			if ((commentOfNewExamination == null) || (commentOfNewExamination.equals(""))) {
				this.commentOfNewExamination = null;
			} else {
				this.commentOfNewExamination = commentOfNewExamination;
			}
			setChanged();
			notifyObservers("examinationComment");
		}
		
		public List<ExaminationDTO> getExaminationList() {
			return examinationList;
		}

		public void setExaminationList(List<ExaminationDTO> examinationList) {
			hashExamination = new HashMap<String, ExaminationDTO>();
			if (examinationList != null) {
				this.examinationList = examinationList;
				for (ExaminationDTO e : examinationList) {
					hashExamination.put("["+formatter.format(Util.toDate(e.getExaminationDate()))+"] " + e.getComment() + " (id="+e.getId()+")", e);
				}
				setChanged();
				notifyObservers("examinationlist");
			}
		}

		public void setPhysicallyInvolved(boolean isPhysicallyInvolved) {
			this.isPhysicallyInvolved = isPhysicallyInvolved;
		}
		

		public HashMap<String, Investigator> getHashExternalInvestigator() {
			return hashExternalInvestigator;
		}
		
		public ExaminationDTO getExistingExamination() {
			return existingExamination;
		}

		public void setExistingExamination(String existingExamination) {
			if (existingExamination != null) {
				this.existingExamination = getHashExamination().get(existingExamination);
			} else {
				this.existingExamination = null;
			}
			setChanged();
			notifyObservers("existingExamination");
		}

		public HashMap<String, Center> getHashCenter() {
			return hashCenter;
		}
		
		public HashMap<String, ExaminationDTO> getHashExamination() {
			return hashExamination;
		}

		public void setHashExamination(HashMap<String, ExaminationDTO> hashExamination) {
			this.hashExamination = hashExamination;
		}

		public List<Investigator> getInvestigatorListForCenter() {
			return investigatorListForCenter;
		}

		public void setInvestigatorListForCenter(List<Investigator> investigatorListForCenter) {
			this.investigatorListForCenter = investigatorListForCenter;
		}

		public boolean isResetInProgress() {
			return resetInProgress;
		}

		public void setResetInProgress(boolean resetInProgress) {
			this.resetInProgress = resetInProgress;
		}
		
		public Long getExaminationId() {
			return examinationId;
		}

		public void setExaminationId(Long examinationId) {
			this.examinationId = examinationId;
		}

		public String getMriCenter() {
			return mriCenter;
		}

		public void setMriCenter(String mriCenter) {
			this.mriCenter = mriCenter;
			setChanged();
			notifyObservers("mriCenter");
		}

		public String getMriCenterAddress() {
			return mriCenterAddress;
		}

		public void setMriCenterAddress(String mriCenterAddress) {
			this.mriCenterAddress = mriCenterAddress;
			setChanged();
			notifyObservers("mriCenterAddress");
		}

		public void reset() {
			resetInProgress = true;
			study = null;
			studyCard = null;
			isPhysicallyInvolved = true;
			subjectType = null;
			subject = null;
			existingExamination=null;
			examinationId = null;
			executiveOfNewExamination = null;
			centerOfNewExamination = null;
			dateOfNewExamination = null;
			commentOfNewExamination=null;
			studyWithStudyCards = null;
			examinationList = null;
			subjectStudyId = null;
			investigatorListForCenter = null;
			mriCenter = null;
			mriCenterAddress = null;
			hashStudy = null; 
			hashStudyCard = null;
			hashCenter = null;
			hashExternalInvestigator = null;
			hashExamination = null;
			setChanged();
			notifyObservers("reset");
		}

}
