package org.shanoir.uploader.action;

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.gui.ImportDialog;
import org.shanoir.uploader.gui.MainWindow;
import org.shanoir.uploader.gui.customcomponent.JComboBoxMandatory;
import org.shanoir.uploader.model.rest.AcquisitionEquipment;
import org.shanoir.uploader.model.rest.Examination;
import org.shanoir.uploader.model.rest.IdName;
import org.shanoir.uploader.model.rest.Study;
import org.shanoir.uploader.model.rest.StudyCard;
import org.shanoir.uploader.model.rest.Subject;
import org.shanoir.uploader.model.rest.SubjectStudy;
import org.shanoir.uploader.model.rest.SubjectType;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClient;
import org.shanoir.uploader.utils.ImportUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportStudyAndStudyCardCBItemListener implements ItemListener {
	
	private static final Logger logger = LoggerFactory.getLogger(ImportStudyAndStudyCardCBItemListener.class);

	private MainWindow mainWindow;

	private ImportJob importJob;
	
	private Subject subject;
	
	private SubjectStudy subjectStudy;

	private List<AcquisitionEquipment> acquisitionEquipments;
	
	private List<Examination> examinationsOfSubject;
	
	private Date studyDate;

	private ImportStudyCardFilterDocumentListener importStudyCardDocumentListener;
	
	private ShanoirUploaderServiceClient serviceClient;

	public ImportStudyAndStudyCardCBItemListener(MainWindow mainWindow, ImportJob importJob, List<AcquisitionEquipment> acquisitionEquipments, Subject subject, Date studyDate, ImportStudyCardFilterDocumentListener importStudyCardDocumentListener, ShanoirUploaderServiceClient serviceClient) {
		this.mainWindow = mainWindow;
		this.importJob = importJob;
		this.acquisitionEquipments = acquisitionEquipments;
		this.subject = subject;
		this.studyDate = studyDate;
		this.importStudyCardDocumentListener = importStudyCardDocumentListener;
		this.serviceClient = serviceClient;
	}

	public void itemStateChanged(ItemEvent e) {
		int state = e.getStateChange();
		if (state == ItemEvent.SELECTED) {
			if (e.getSource().equals(mainWindow.importDialog.studyCB)) {
				Study study = (Study) e.getItem();
				if (study.isWithStudyCards()) {
					updateStudyCards(study);
					showOrHideStudyCardComponents(true);
				} else {
					AcquisitionEquipment equipment = ImportUtils.createEquipmentAndIfStudyCard(importJob, study, null, null, acquisitionEquipments);
	 				mainWindow.importDialog.mrExaminationCenterCB.removeAllItems();
					if (equipment != null) {
						IdName center = equipment.getCenter();
						mainWindow.importDialog.mrExaminationCenterCB.addItem(center);
					}
					showOrHideStudyCardComponents(false);
				}
				// Profile Neurinfo
				if (ShUpConfig.isModeSubjectNameManual()) {
					updateExistingSubjects(study);
					this.subject = (Subject) mainWindow.importDialog.existingSubjectsCB.getSelectedItem();	
					// for OFSEP this is done in ImportDialogOpener as subject found before, if
					updateImportDialogForExistingSubject(this.subject, mainWindow.importDialog);
				}
				updateSubjectStudy(study, subject);
				examinationsOfSubject = updateExaminations(subject);
				filterExistingExamsForSelectedStudy(study);
			}
			// the selection of the StudyCard and its center defines
			// the center for new created examinations
			if (e.getSource().equals(mainWindow.importDialog.studyCardCB)) {
				JComboBoxMandatory comboBox = (JComboBoxMandatory) e.getSource();
				StudyCard studyCard = (StudyCard) comboBox.getSelectedItem();
 				// put center into exam using study card and acquisition equipment
 				mainWindow.importDialog.mrExaminationCenterCB.removeAllItems();
 				AcquisitionEquipment equipment = studyCard.getAcquisitionEquipment();
 				if (equipment != null) {
 					IdName center = equipment.getCenter();
 					mainWindow.importDialog.mrExaminationCenterCB.addItem(center);
				}
			}
			// the selection of an existing subject defines the list of existing exams
			if (e.getSource().equals(mainWindow.importDialog.existingSubjectsCB)) {
				Study study = (Study) mainWindow.importDialog.studyCB.getSelectedItem();
				this.subject = (Subject) mainWindow.importDialog.existingSubjectsCB.getSelectedItem();
				updateImportDialogForExistingSubject(this.subject, mainWindow.importDialog);
				updateSubjectStudy(study, subject);
				examinationsOfSubject = updateExaminations(subject);
				filterExistingExamsForSelectedStudy(study);			
			}		
		} // ignore otherwise
	}

	private void showOrHideStudyCardComponents(boolean show) {
		mainWindow.importDialog.studyCardLabel.setVisible(show);
		mainWindow.importDialog.studyCardCB.setVisible(show);
		mainWindow.importDialog.studyCardFilterLabel.setVisible(show);
		mainWindow.importDialog.studyCardFilterTextField.setVisible(show);
	}

	public static void updateImportDialogForExistingSubject(Subject subject, ImportDialog importDialog) {
		if (subject != null) {
			importDialog.subjectImageObjectCategoryCB.setEnabled(false);
			if (subject.getImagedObjectCategory() != null) {
				importDialog.subjectImageObjectCategoryCB.setSelectedItem(subject.getImagedObjectCategory());
			}
			importDialog.subjectLanguageHemisphericDominanceCB.setEnabled(false);
			if (subject.getLanguageHemisphericDominance() != null) {
				importDialog.subjectLanguageHemisphericDominanceCB.setSelectedItem(subject.getLanguageHemisphericDominance().getName());
			} else {
				importDialog.subjectLanguageHemisphericDominanceCB.setSelectedItem("");
			}
			importDialog.subjectManualHemisphericDominanceCB.setEnabled(false);		
			if (subject.getManualHemisphericDominance() != null) {
				importDialog.subjectManualHemisphericDominanceCB.setSelectedItem(subject.getManualHemisphericDominance().getName());
			} else {
				importDialog.subjectManualHemisphericDominanceCB.setSelectedItem("");
			}
			// not used anymore on server: remove later
			importDialog.subjectPersonalCommentTextArea.setBackground(Color.LIGHT_GRAY);
			importDialog.subjectPersonalCommentTextArea.setEditable(false);
		}
	}

	private void updateExistingSubjects(Study study) {
		try {
			mainWindow.importDialog.existingSubjectsCB.removeAllItems();
			List<Subject> subjects = serviceClient.findSubjectsByStudy(study.getId());
			if (subjects != null) {
				for (Subject subject : subjects) {
					mainWindow.importDialog.existingSubjectsCB.addItem(subject);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private List<Examination> updateExaminations(Subject subject) {
		try {
			if (subject != null) {
				List<Examination> examinationList = serviceClient.findExaminationsBySubjectId(subject.getId());
				return examinationList;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * Examinations in Shanoir are related to study.
	 * @param study
	 * @throws ParseException 
	 */
	private void filterExistingExamsForSelectedStudy(Study study) {
		// manage list of existing exams, and check if study date matches
		mainWindow.importDialog.mrExaminationExistingExamCB.removeAllItems();
		mainWindow.importDialog.mrExaminationExistingExamCB.setEnabled(false);
		mainWindow.importDialog.mrExaminationNewExamCB.setEnabled(true);
		mainWindow.importDialog.mrExaminationNewExamCB.setSelected(true);
		// Exams exist, but maybe not for the study selected
		if (examinationsOfSubject != null && !examinationsOfSubject.isEmpty()) {
			List<Examination> examinationsFilteredByStudy = examinationsOfSubject.parallelStream()
				.filter(e -> e.getStudyId().equals(study.getId()))
				.collect(Collectors.toList());
			for (Iterator iterator = examinationsFilteredByStudy.iterator(); iterator.hasNext();) {
				Examination examination = (Examination) iterator.next();
				mainWindow.importDialog.mrExaminationExistingExamCB.addItem(examination); // I did not achieve to call this from within Lambda
				// Existing exam found with the same study date: preselect and do not propose new exam per default
				if (examination.getExaminationDate().compareTo(studyDate) == 0) {
					mainWindow.importDialog.mrExaminationExistingExamCB.setEnabled(true);
					mainWindow.importDialog.mrExaminationExistingExamCB.setSelectedItem(examination);
					mainWindow.importDialog.mrExaminationNewExamCB.setSelected(false);
				}
			}
			// here we know, that for this study at least one exam exists (but not with the same study date)
			if (mainWindow.importDialog.mrExaminationExistingExamCB.getItemCount() > 0) {
				if (!mainWindow.importDialog.mrExaminationNewExamCB.isSelected()) {
					mainWindow.importDialog.mrExaminationExistingExamCB.setEnabled(true);
				}
			// No exams exist already for this subject, so user has to create a new exam
			} else {
				mainWindow.importDialog.mrExaminationNewExamCB.setEnabled(false);
			}
		} 
	}

	private void updateStudyCards(Study study) {
		mainWindow.importDialog.studyCardCB.removeAllItems();
		this.importStudyCardDocumentListener.cleanDefaultStudyCards();
		if (study.getStudyCards() != null) {
			for (StudyCard studyCard : study.getStudyCards()) {
				mainWindow.importDialog.studyCardCB.addItem(studyCard);
				this.importStudyCardDocumentListener.addDefaultStudyCard(studyCard);
			}
		}
	}

	private void updateSubjectStudy(Study study, Subject subject) {
		// Check if RelSubjectStudy exists for selected study
		if (subject != null) {
			// Profile Neurinfo: findSubjectsByStudyId returns single subject-study
			if (ShUpConfig.isModeSubjectNameManual()) {
				SubjectStudy subjectStudy = subject.getSubjectStudy();
				if (subjectStudy != null) {
					logger.info("Existing subjectStudy found with ID: " + subjectStudy.getId());
					updateSubjectStudyInImportDialog(subjectStudy, mainWindow.importDialog);
					this.subjectStudy = subjectStudy;
					return;
				} else {
					logger.error("subjectStudy empty for existing subject in study.");
				}
			// Profile OFSEP: findByIdentifier returns list of subject-study
			} else {
				List<SubjectStudy> subjectStudyList = subject.getSubjectStudyList();
				if (subjectStudyList != null) {
					for (Iterator iterator = subjectStudyList.iterator(); iterator.hasNext();) {
						SubjectStudy subjectStudy = (SubjectStudy) iterator.next();
						// subject is already in study: display values in GUI and stop editing
						if (subjectStudy.getStudy().getId().equals(study.getId())) {
							logger.info("Existing subjectStudy found with ID: " + subjectStudy.getId());
							updateSubjectStudyInImportDialog(subjectStudy, mainWindow.importDialog);
							this.subjectStudy = subjectStudy;
							return;
						}
					}
				} else {
					logger.error("subjectStudy list empty for existing subject in study.");
				}
			}
		}
		this.subjectStudy = null;
	}

	public static void updateSubjectStudyInImportDialog(SubjectStudy subjectStudy, ImportDialog importDialog) {
		if (subjectStudy != null) {
			importDialog.subjectStudyIdentifierTF.setText(subjectStudy.getSubjectStudyIdentifier());
			importDialog.subjectStudyIdentifierTF.setBackground(Color.LIGHT_GRAY);
			importDialog.subjectStudyIdentifierTF.setEnabled(false);
			importDialog.subjectStudyIdentifierTF.setEditable(false);
			importDialog.subjectIsPhysicallyInvolvedCB.setSelected(subjectStudy.isPhysicallyInvolved());
			importDialog.subjectIsPhysicallyInvolvedCB.setEnabled(false);
			importDialog.subjectTypeCB.setSelectedItem(subjectStudy.getSubjectType());
			importDialog.subjectTypeCB.setEnabled(false);	
		} else {
			// subject is not in study, enable editing and display defaults
			if (ShUpConfig.isModeSubjectStudyIdentifier()) {
				importDialog.subjectStudyIdentifierTF.setEnabled(true);
				importDialog.subjectStudyIdentifierTF.setEditable(true);
				importDialog.subjectStudyIdentifierTF.setBackground(Color.WHITE);
			}
			importDialog.subjectStudyIdentifierTF.setText("");
			importDialog.subjectIsPhysicallyInvolvedCB.setEnabled(true);
			importDialog.subjectIsPhysicallyInvolvedCB.setSelected(true);
			importDialog.subjectTypeCB.setEnabled(true);
			importDialog.subjectTypeCB.setSelectedItem(SubjectType.values()[1]);
		}
	}

	public SubjectStudy getSubjectStudy() {
		return subjectStudy;
	}

	public void setSubjectStudy(SubjectStudy subjectStudy) {
		this.subjectStudy = subjectStudy;
	}
	
}
