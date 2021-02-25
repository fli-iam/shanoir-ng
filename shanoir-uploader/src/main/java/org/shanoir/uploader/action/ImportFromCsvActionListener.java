package org.shanoir.uploader.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JTabbedPane;

import org.apache.log4j.Logger;
import org.shanoir.dicom.importer.Serie;
import org.shanoir.dicom.importer.UploadJob;
import org.shanoir.dicom.importer.UploadJobManager;
import org.shanoir.dicom.importer.UploadState;
import org.shanoir.dicom.model.DicomTreeNode;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.dicom.IDicomServerClient;
import org.shanoir.uploader.dicom.query.Media;
import org.shanoir.uploader.dicom.query.Patient;
import org.shanoir.uploader.dicom.query.Study;
import org.shanoir.uploader.gui.ImportFromCSVWindow;
import org.shanoir.uploader.model.CsvImport;
import org.shanoir.uploader.model.rest.AcquisitionEquipment;
import org.shanoir.uploader.model.rest.Examination;
import org.shanoir.uploader.model.rest.IdList;
import org.shanoir.uploader.model.rest.ImagedObjectCategory;
import org.shanoir.uploader.model.rest.StudyCard;
import org.shanoir.uploader.model.rest.Subject;
import org.shanoir.uploader.model.rest.SubjectType;
import org.shanoir.uploader.model.rest.importer.ImportJob;
import org.shanoir.uploader.nominativeData.NominativeDataUploadJob;
import org.shanoir.uploader.nominativeData.NominativeDataUploadJobManager;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClientNG;
import org.shanoir.uploader.utils.ImportUtils;
import org.shanoir.uploader.utils.Util;

/**
 * Thsi class is used after 'import' button from CSV importer.
 * It loads the list of imports to do, then imports them one by one
 * Managing errors and displays
 * @author fli
 *
 */
public class ImportFromCsvActionListener implements ActionListener {

	ImportFromCSVWindow importFromCSVWindow;
	IDicomServerClient dicomServerClient;
	File shanoirUploaderFolder;

	List<CsvImport> csvImports;
	ShanoirUploaderServiceClientNG shanoirUploaderServiceClientNG;

	private static Logger logger = Logger.getLogger(ImportFromCsvActionListener.class);

	public ImportFromCsvActionListener(ImportFromCSVWindow importFromCSVWindow, IDicomServerClient dicomServerClient, File shanoirUploaderFolder, ShanoirUploaderServiceClientNG shanoirUploaderServiceClientNG) {
		this.importFromCSVWindow = importFromCSVWindow;
		this.dicomServerClient = dicomServerClient;
		this.shanoirUploaderFolder = shanoirUploaderFolder;
		this.shanoirUploaderServiceClientNG = shanoirUploaderServiceClientNG;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// Iterate over import to import them one by one
		Set<Long> idList = new HashSet<>();
		Map<String, ArrayList<StudyCard>> studyCardsByStudy = new HashMap<>();


		// Get the list of studies, study card, center, equipements to check their existence
		for (CsvImport importTodo : this.csvImports) {
			idList.add(Long.valueOf(importTodo.getStudyId()));
			studyCardsByStudy.put(importTodo.getStudyId(), new ArrayList<StudyCard>());
		}
		List<org.shanoir.uploader.model.rest.Study> studies = new ArrayList<>();
		try {
			studies = shanoirUploaderServiceClientNG.findStudiesNamesAndCenters();

			IdList idealist = new IdList();
			idealist.setIdList(new ArrayList<>(idList));
			List<StudyCard> studyCards = shanoirUploaderServiceClientNG.findStudyCardsByStudyIds(idealist);
			
			if (studyCards == null) {
				throw new ShanoirException("Could not retrieve study cards linked to given studies, please check data consistency");
			}
			
			// Iterate over study cards to get equipement + fille study => SC map
			for (StudyCard studyCard : studyCards) {
				AcquisitionEquipment acquisitionEquipment;
				acquisitionEquipment = shanoirUploaderServiceClientNG.findAcquisitionEquipmentById(studyCard.getAcquisitionEquipmentId());
				if (acquisitionEquipment == null) {
					throw new ShanoirException("Could not retrieve acquisition equipement linked to study card " + studyCard.getName() + ", please check data consistency");
				}
				studyCard.setAcquisitionEquipment(acquisitionEquipment);
				studyCard.setCenterId(acquisitionEquipment.getCenter().getId());
				studyCardsByStudy.get(studyCard.getStudyId().toString()).add(studyCard);
			}
		} catch (Exception e1) {
			this.importFromCSVWindow.error.setText(e1.getMessage());
			return;
		}

		boolean success = true;

		for (CsvImport importTodo : this.csvImports) {
			org.shanoir.uploader.model.rest.Study study = studies.stream().filter(element -> element.getId().toString().equals(importTodo.getStudyId())).findFirst().get();
			success = success && importData(importTodo, studyCardsByStudy.get(importTodo.getStudyId()), study );
		}

		if (success) {
			// Open current import tab and close csv import panel
			((JTabbedPane) this.importFromCSVWindow.scrollPaneUpload.getParent().getParent()).setSelectedComponent(this.importFromCSVWindow.scrollPaneUpload.getParent());
	
			this.importFromCSVWindow.frame.setVisible(false);
			this.importFromCSVWindow.frame.dispose();
		} else {
			this.importFromCSVWindow.error.setText("An import line failed, please check data in CSV  list.");
			this.importFromCSVWindow.error.setVisible(true);
		}
		
	}

	/**
	 * Loads data to shanoir NG
	 * @param csvImport the import
	 * @param studyCardsByStudy the list of study
	 * @param study2
	 * @return
	 */
	private boolean importData(CsvImport csvImport, List<StudyCard> studyCardsByStudy, org.shanoir.uploader.model.rest.Study study2) {

		// 1. Check existence of study / study card
		StudyCard sc = studyCardsByStudy.stream().filter(element -> element.getName().equals(csvImport.getStudyCardName())).findFirst().get();
		if (sc == null) {
			csvImport.setErrorMessage("The study card " +  csvImport.getStudyCardName() + " is not contained in the study " + study2.getId());
			return false;
		}

		// 2. Request PACS to check the presence of data
		Media media;
		try {
			media = dicomServerClient.queryDicomServer("DIR OL", "", "", "", null, null);
		} catch (Exception e) {
			csvImport.setErrorMessage("The PACS data does not contain any data with such criteria.");
			return false;
		}
		
		// 3. Select all series
		
		//TODO here: Faire le tri des données selon un critère ?
		Set<Serie> selectedSeries = new HashSet<>();
		Patient pat = null;
		Study stud = null;
		for (DicomTreeNode item : media.getTreeNodes().values()) {
			if (item instanceof Patient) {
				Patient patient = (Patient) item;
				pat = patient;
				Collection<DicomTreeNode> studies = patient.getTreeNodes().values();
				for (Iterator<DicomTreeNode> studiesIt = studies.iterator(); studiesIt.hasNext();) {
					Study study = (Study) studiesIt.next();
					stud = study;
					Collection<DicomTreeNode> series = study.getTreeNodes().values();
					for (Iterator<DicomTreeNode> seriesIt = series.iterator(); seriesIt.hasNext();) {
						Serie serie = (Serie) seriesIt.next();
						selectedSeries.add(serie);
					}
				}
			}
		}
		if (selectedSeries.isEmpty()) {
			csvImport.setErrorMessage("The PACS data does not contain any data with such criteria.");
			return false;
		}

		// 4. Create DicomDataTransferObject
		DicomDataTransferObject dicomData = null;
		try {
			dicomData = new DicomDataTransferObject(null, pat, stud);
			dicomData.setSubjectIdentifier(csvImport.getCommonName());
		} catch (Exception e) {
			csvImport.setErrorMessage("An unexpected error happend, please contact an administrator");
			return false;
		}

		File uploadFolder = ImportUtils.createUploadFolder(dicomServerClient.getWorkFolder(), dicomData);
		List<String> allFileNames = ImportUtils.downloadOrCopyFilesIntoUploadFolder(true, selectedSeries, uploadFolder, this.dicomServerClient, null);

		/**
		 * 5. Fill MRI information into serie from first DICOM file of each serie
		 * This has already been done for CD/DVD import, but not yet here for PACS
		 */
		for (Iterator<Serie> iterator = selectedSeries.iterator(); iterator.hasNext();) {
			Serie serie = iterator.next();
			Util.processSerieMriInfo(uploadFolder, serie);
		}

		/**
		 * 6. Write the UploadJob and schedule upload
		 */
		UploadJob uploadJob = new UploadJob();
		ImportUtils.initUploadJob(selectedSeries, dicomData, uploadJob);

		if (allFileNames == null) {
			uploadJob.setUploadState(UploadState.ERROR);
			csvImport.setErrorMessage("Copy from PACS failed, please contact the PACS administrator.");
			return false;
		}
		UploadJobManager uploadJobManager = new UploadJobManager(uploadFolder.getAbsolutePath());
		uploadJobManager.writeUploadJob(uploadJob);

		/**
		 * 7. Write the NominativeDataUploadJobManager for displaying the download state
		 */
		NominativeDataUploadJob dataJob = new NominativeDataUploadJob();
		ImportUtils.initDataUploadJob(selectedSeries, dicomData, dataJob);

		NominativeDataUploadJobManager uploadDataJobManager = new NominativeDataUploadJobManager(
				uploadFolder.getAbsolutePath());
		uploadDataJobManager.writeUploadDataJob(dataJob);
		ShUpOnloadConfig.getCurrentNominativeDataController().addNewNominativeData(uploadFolder, dataJob);

		logger.info(uploadFolder.getName() + ": finished: " + toString());

		// 10.  Create subject
		Subject subject = new Subject();
		subject.setName(csvImport.getCommonName());
		subject.setSex(csvImport.getSex());

		subject.setImagedObjectCategory(ImagedObjectCategory.LIVING_HUMAN_BEING);

		subject.setBirthDate(csvImport.getBirthDate());

		ImportUtils.addSubjectStudy(study2, subject, SubjectType.PATIENT, true, null);

		// Get center ID from study card
		Long centerId = sc.getCenterId();
		Subject createdSubjet = shanoirUploaderServiceClientNG.createSubject(subject, true, centerId);
		
		if (createdSubjet == null) {
			uploadJob.setUploadState(UploadState.ERROR);
			csvImport.setErrorMessage("Subject creation failed, please check common name uniqueness and your rights on the study.");
			return false;
		}

		// 10. Create examination
		Examination examDTO = new Examination();
		examDTO.setCenterId(centerId);
		examDTO.setComment(csvImport.getComment());
		examDTO.setExaminationDate(new Date());
		examDTO.setPreclinical(false);
		examDTO.setStudyId(Long.valueOf(csvImport.getStudyId()));
		examDTO.setSubjectId(createdSubjet.getId());
		Examination createdExam = shanoirUploaderServiceClientNG.createExamination(examDTO);

		if (createdExam == null) {
			uploadJob.setUploadState(UploadState.ERROR);
			csvImport.setErrorMessage("Examination creation failed, please check data consistency and your rights to import on the study.");
			return false;
		}

		/**
		 * 11. Fill import-job.json to prepare the import
		 */
		ImportJob importJob = ImportUtils.prepareImportJob(uploadJob, createdSubjet.getName(), createdSubjet.getId(), createdExam.getId(), study2, sc);
		Runnable runnable = new ImportFinishRunnableNG(uploadJob, uploadFolder, importJob, subject.getName());
		Thread thread = new Thread(runnable);
		thread.start();
		
		return true;
	}

	/**
	 * @return the csvImports
	 */
	public List<CsvImport> getCsvImports() {
		return csvImports;
	}

	/**
	 * @param csvImports the csvImports to set
	 */
	public void setCsvImports(List<CsvImport> csvImports) {
		this.csvImports = csvImports;
	}


}
