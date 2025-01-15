package org.shanoir.uploader.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import javax.swing.JProgressBar;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.shanoir.ng.importer.dicom.ImagesCreatorAndDicomFileAnalyzerService;
import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.importer.model.Instance;
import org.shanoir.ng.importer.model.Patient;
import org.shanoir.ng.importer.model.PseudonymusHashValues;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.ng.importer.model.Subject;
import org.shanoir.ng.shared.dicom.EquipmentDicom;
import org.shanoir.ng.shared.dicom.InstitutionDicom;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.action.ImportFinishRunnable;
import org.shanoir.uploader.dicom.IDicomServerClient;
import org.shanoir.uploader.dicom.MRI;
import org.shanoir.uploader.dicom.retrieve.DcmRcvManager;
import org.shanoir.uploader.model.rest.AcquisitionEquipment;
import org.shanoir.uploader.model.rest.Center;
import org.shanoir.uploader.model.rest.Examination;
import org.shanoir.uploader.model.rest.HemisphericDominance;
import org.shanoir.uploader.model.rest.IdList;
import org.shanoir.uploader.model.rest.IdName;
import org.shanoir.uploader.model.rest.ImagedObjectCategory;
import org.shanoir.uploader.model.rest.Manufacturer;
import org.shanoir.uploader.model.rest.ManufacturerModel;
import org.shanoir.uploader.model.rest.Sex;
import org.shanoir.uploader.model.rest.Study;
import org.shanoir.uploader.model.rest.StudyCard;
import org.shanoir.uploader.model.rest.StudyCenter;
import org.shanoir.uploader.model.rest.SubjectStudy;
import org.shanoir.uploader.model.rest.SubjectType;
import org.shanoir.uploader.nominativeData.NominativeDataUploadJob;
import org.shanoir.uploader.upload.UploadJob;
import org.shanoir.uploader.upload.UploadState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This class contains useful methods for data upload that are used multiple times in the application.
 * @author Jcome
 * @author mkain
 *
 */
public class ImportUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(ImportUtils.class);
	
	private static ObjectMapper objectMapper = new ObjectMapper();

	static {
		objectMapper.registerModule(new JavaTimeModule())
			.registerModule(new Jdk8Module())
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}
	
	/**
	 * Adds a subjectStudy to a given subject with the given study
	 * and in case subject is already in study, nothing is made.
	 * 
	 * @param study the added study
	 * @param subject the subject we add a subjectStudy on
	 * @param subjectType the type of suject
	 * @param physicallyInvolved is the subject physically involved
	 * @param subjectStudyIdentifier the subject study identifier
	 */
	public static boolean addSubjectStudy(final Study study, final org.shanoir.uploader.model.rest.Subject subject, String subjectStudyIdentifier, SubjectType subjectType, boolean physicallyInvolved) {
		SubjectStudy subjectStudy = new SubjectStudy();
		subjectStudy.setStudy(new IdName(study.getId(), study.getName()));
		subjectStudy.setSubject(new IdName(subject.getId(), subject.getName()));
		if (!StringUtils.isEmpty(subjectStudyIdentifier)) {
			subjectStudy.setSubjectStudyIdentifier(subjectStudyIdentifier);
		}
		subjectStudy.setSubjectType(subjectType);
		subjectStudy.setPhysicallyInvolved(physicallyInvolved);
		subjectStudy.setTags(new ArrayList<>());
		// Challenge here: findSubjectByIdentifier returns a subjectStudyList and findSubjectsByStudyId a subjectStudy attribute
		if (subject.getSubjectStudyList() == null) {
			subject.setSubjectStudyList(new ArrayList<>());
			SubjectStudy existingSubjectStudy = subject.getSubjectStudy();
			if (existingSubjectStudy != null) {
				// do nothing: call of findSubjectsByStudyId
				return false;
			}
			subject.getSubjectStudyList().add(subjectStudy);
		} else {
			// Check that this SubjectStudy does not exist yet
			for (SubjectStudy sustu : subject.getSubjectStudyList()) {
				if (sustu.getStudy().getId().equals(study.getId())) {
					// Do not add a new SubjectStudy if it already exists
					return false;
				}
			}
			// Not yet existing: add it
			subject.getSubjectStudyList().add(subjectStudy);
		}
		return true;
	}

	/**
	 * Create upload folder from parent folder and dicom information
	 * @param workFolder the parent upload work folder
	 * @param dicomData the dicom data to import
	 * @return the created folder
	 */
	public static File createUploadFolder(final File workFolder, final String subjectIdentifier) {
		final String timeStamp = Util.getCurrentTimeStampForFS();
		final String folderName = workFolder.getAbsolutePath() + File.separator + subjectIdentifier
		+ "_" + timeStamp;
		File uploadFolder = new File(folderName);
		uploadFolder.mkdirs();
		return uploadFolder;
	}

	/**
	 * Initializes UploadJob object to be written to file system.
	 * 
	 * @param selectedSeries
	 * @param dicomData
	 * @param uploadJob
	 */
	public static void initUploadJob(ImportJob importJob, UploadJob uploadJob) {
		uploadJob.setUploadState(UploadState.READY);
		uploadJob.setUploadDate(Util.formatTimePattern(new Date()));
		/**
		 * Patient level
		 */
		// set hash of subject identifier in any case: pseudonymus mode or not
		Subject subject = importJob.getSubject();
		uploadJob.setSubjectIdentifier(subject.getIdentifier());
		// set all 10 hash values for pseudonymus mode
		if (ShUpConfig.isModePseudonymus()) {
			PseudonymusHashValues pseudonymusHashValues = subject.getPseudonymusHashValues();
			uploadJob.setBirthNameHash1(pseudonymusHashValues.getBirthNameHash1());
			uploadJob.setBirthNameHash2(pseudonymusHashValues.getBirthNameHash2());
			uploadJob.setBirthNameHash3(pseudonymusHashValues.getBirthNameHash3());
			uploadJob.setLastNameHash1(pseudonymusHashValues.getLastNameHash1());
			uploadJob.setLastNameHash2(pseudonymusHashValues.getLastNameHash2());
			uploadJob.setLastNameHash3(pseudonymusHashValues.getLastNameHash3());
			uploadJob.setFirstNameHash1(pseudonymusHashValues.getFirstNameHash1());
			uploadJob.setFirstNameHash2(pseudonymusHashValues.getFirstNameHash2());
			uploadJob.setFirstNameHash3(pseudonymusHashValues.getFirstNameHash3());
			uploadJob.setBirthDateHash(pseudonymusHashValues.getBirthDateHash());
		}
		LocalDate birthDate = subject.getBirthDate();
		uploadJob.setPatientBirthDate(Util.convertLocalDateToString(birthDate));
		uploadJob.setPatientSex(subject.getSex());

		/**
		 * Study level
		 */
		org.shanoir.ng.importer.model.Study study = importJob.getStudy();
		uploadJob.setStudyInstanceUID(study.getStudyInstanceUID());
		String studyDateStr = Util.convertLocalDateToString(study.getStudyDate());
		uploadJob.setStudyDate(studyDateStr);
		uploadJob.setStudyDescription(study.getStudyDescription());

		/**
		 * @todo: only write importJob json to disk to read it afterwards and avoid senseless conversions
		 * keep xml for the moment for the GUI only
		 */

		/**
		 * Serie level
		 */
		List<Serie> selectedSeries = new ArrayList<>(importJob.getSelectedSeries());
		Serie firstSerie = selectedSeries.iterator().next();
		MRI mriInformation = new MRI();
		InstitutionDicom institutionDicom = firstSerie.getInstitution();
		if(institutionDicom != null) {
			mriInformation.setInstitutionName(institutionDicom.getInstitutionName());
			mriInformation.setInstitutionAddress(institutionDicom.getInstitutionAddress());
		}
		EquipmentDicom equipmentDicom = firstSerie.getEquipment();
		if(equipmentDicom != null) {
			mriInformation.setManufacturer(equipmentDicom.getManufacturer());
			mriInformation.setManufacturersModelName(equipmentDicom.getManufacturerModelName());
			mriInformation.setDeviceSerialNumber(equipmentDicom.getDeviceSerialNumber());
			mriInformation.setStationName(equipmentDicom.getStationName());
			mriInformation.setMagneticFieldStrength(equipmentDicom.getMagneticFieldStrength());
		}
		uploadJob.setMriInformation(mriInformation);
		logger.info(mriInformation.toString());
	}

	public static ImportJob readImportJob(File uploadFolder) throws StreamReadException, DatabindException, IOException {
		File importJobJsonFile = new File(uploadFolder.getAbsolutePath() + File.separator + ImportFinishRunnable.IMPORT_JOB_JSON);
		if (importJobJsonFile.exists()) {
			ImportJob importJob = objectMapper.readValue(importJobJsonFile, ImportJob.class);
			return importJob;
		} else {
			throw new IOException(ImportFinishRunnable.IMPORT_JOB_JSON + " missing in folder: " + uploadFolder.getAbsolutePath());
		}
	}

	public static ImportJob createNewImportJob(Patient patient, org.shanoir.ng.importer.model.Study study) {
		ImportJob importJob = new ImportJob();
		importJob.setFromShanoirUploader(true);
		// create new patient here, that tree remains untouched
		Patient newPatientForJob = new Patient();
		newPatientForJob.setPatientName(patient.getPatientName());
		newPatientForJob.setPatientID(patient.getPatientID());
		newPatientForJob.setPatientLastName(patient.getPatientLastName());
		newPatientForJob.setPatientFirstName(patient.getPatientFirstName());
		newPatientForJob.setPatientBirthDate(patient.getPatientBirthDate());
		newPatientForJob.setPatientBirthName(patient.getPatientBirthName());
		newPatientForJob.setPatientSex(patient.getPatientSex());
		importJob.setPatient(newPatientForJob);
		// create new study here, that tree remains untouched
		org.shanoir.ng.importer.model.Study newStudyForJob = new org.shanoir.ng.importer.model.Study();
		newStudyForJob.setStudyDate(study.getStudyDate());
		newStudyForJob.setStudyInstanceUID(study.getStudyInstanceUID());
		newStudyForJob.setStudyDescription(study.getStudyDescription());
		importJob.setStudy(newStudyForJob);
		importJob.setSelectedSeries(new LinkedHashSet<Serie>());
		return importJob;
	}

	/**
	 * subjectId and examinationId are created in the window of ImportDialog and are not known before.
	 * 
	 * @param uploadJob
	 * @param subjectName
	 * @param subjectId
	 * @param examinationId
	 * @param study
	 * @param studyCard
	 * @return
	 * @throws IOException 
	 * @throws DatabindException 
	 * @throws StreamReadException 
	 */
	public static ImportJob prepareImportJob(ImportJob importJob, String subjectName, Long subjectId, Long examinationId, Study study, StudyCard studyCard) {
		// Handle study and study card
		importJob.setStudyId(study.getId());
		importJob.setStudyName(study.getName());
		// MS Datasets does only return StudyCard DTOs without IDs, as name is unique
		// see: /shanoir-ng-datasets/src/main/java/org/shanoir/ng/studycard/model/StudyCard.java
		importJob.setStudyCardName(studyCard.getName());
		importJob.setStudyCardId(studyCard.getId());
		importJob.setAcquisitionEquipmentId(studyCard.getAcquisitionEquipmentId());
		importJob.setExaminationId(examinationId);

		/**
		 * @todo: refactor to remove patients list from import job.
		 * for the moment, to finish the first refactor, keep the
		 * current structure required by the server: patients -
		 * patient - subject - study - series (selected)
		 */
		List<Patient> patients = new ArrayList<>();
		// handle patient and subject
		Patient patient = new Patient();
		patient.setPatientID(importJob.getSubject().getIdentifier());
		org.shanoir.ng.importer.model.Subject subject = new org.shanoir.ng.importer.model.Subject();
		subject.setId(subjectId);
		subject.setName(subjectName);
		importJob.setSubjectName(subjectName);
		patient.setSubject(subject);
		patients.add(patient);
		// handle study dicom == examination in Shanoir
		List<org.shanoir.ng.importer.model.Study> studiesImportJob = new ArrayList<org.shanoir.ng.importer.model.Study>();
		org.shanoir.ng.importer.model.Study studyImportJob = new org.shanoir.ng.importer.model.Study();
		// handle series for study now coming from job itself
		final List<Serie> series = new ArrayList<>(importJob.getSelectedSeries());
		for (Serie serie : series) {
			List<Instance> instances = serie.getInstances();
			if (instances == null) {
				serie.setSelected(false);
				continue;
			}
			/**
			 * Warning: the below switch is important, as all import jobs from ShUp
			 * are considered as "from-disk" on the server, nevertheless if within ShUp
			 * they come from a pacs or a local disk, so the below setReferencedFileID
			 * is necessary, that import-from-pacs with ShUp run on the server.
			 */
			for(Instance instance : instances) {
				// do not change referencedFileID in case of import from disk
				if (instance.getReferencedFileID() == null || instance.getReferencedFileID().length == 0) {
					String[] myStringArray = {instance.getSopInstanceUID() + DcmRcvManager.DICOM_FILE_SUFFIX};
					instance.setReferencedFileID(myStringArray);
				}
			}
			serie.setSelected(true);
		}
		studyImportJob.setSeries(series);
		studiesImportJob.add(studyImportJob);
		//importJob.setStudy(studyImportJob);
		patient.setStudies(studiesImportJob);
		importJob.setPatients(patients);
		return importJob;
	}

	/**
	 * Initializes UploadStatusServiceJob object
	 * 
	 */
	public static void initDataUploadJob(final ImportJob importjob, final UploadJob uploadJob, NominativeDataUploadJob dataUploadJob) {
		Patient patient = importjob.getPatient();
		Subject subject = importjob.getSubject();
		org.shanoir.ng.importer.model.Study study = importjob.getStudy();
		dataUploadJob.setPatientName(patient.getPatientFirstName() + " " + patient.getPatientLastName());
		dataUploadJob.setPatientPseudonymusHash(subject.getIdentifier());
		String studyDateStr = Util.convertLocalDateToString(study.getStudyDate()); 
		dataUploadJob.setStudyDate(studyDateStr);
		dataUploadJob.setIPP(patient.getPatientID());
		dataUploadJob.setMriSerialNumber(uploadJob.getMriInformation().getManufacturer()
				+ "(" + uploadJob.getMriInformation().getDeviceSerialNumber() + ")");
		dataUploadJob.setUploadPercentage("");
		dataUploadJob.setUploadState(UploadState.READY);
	}

	/**
	 * In case of the PACS download the destination folder of the DCM server is set to the uploadFolder.
	 * This means, the DICOM files send from the PACS after the c-move, will directly be stored in the
	 * uploadFolder in the workFolder. Each file has the name of its sopInstanceUID and all files are in
	 * the same folder in the end (no sub-folders involved).
	 * In case of the CD/DVD the CD can contain multiple sub-folders with sub-folders, that are referenced
	 * from the DICOMDIR. Therefore ShUp copies the original DICOM files from their deep location, see array
	 * of Tag.ReferencedFileID to the uploadFolder in a flat way: the uploadFolder does not contain sub-folders.
	 * To avoid overwrites because of the same file name, the original path to the file is used as file name,
	 * separated by "_" underscores.
	 * 
	 * @param isFromPACS
	 * @param selectedSeries
	 * @param uploadFolder
	 * @param dicomServerClient
	 * @param filePathDicomDir
	 * @return
	 * @throws FileNotFoundException 
	 */
	public static List<String> downloadOrCopyFilesIntoUploadFolder(boolean isFromPACS, JProgressBar progressBar, StringBuilder downloadOrCopyReport, String studyInstanceUID, List<Serie> selectedSeries, File uploadFolder, ImagesCreatorAndDicomFileAnalyzerService dicomFileAnalyzer, IDicomServerClient dicomServerClient, String filePathDicomDir) throws FileNotFoundException {
		List<String> allFileNames = null;
		if (isFromPACS) {
			allFileNames = dicomServerClient.retrieveDicomFiles(progressBar, downloadOrCopyReport, studyInstanceUID, selectedSeries, uploadFolder);
			if(allFileNames != null && !allFileNames.isEmpty()) {
				logger.info(uploadFolder.getName() + ": " + allFileNames.size() + " DICOM files downloaded from PACS.");
			} else {
				logger.info(uploadFolder.getName() + ": error with download from PACS.");
				return null;
			}
		} else {
			allFileNames = copyFilesToUploadFolder(progressBar, downloadOrCopyReport, dicomFileAnalyzer, selectedSeries, uploadFolder, filePathDicomDir);
			if(allFileNames != null) {
				logger.info(uploadFolder.getName() + ": " + allFileNames.size() + " DICOM files copied from CD/DVD/local file system.");
			} else {
				logger.error("Error while copying file from CD/DVD/local file system.");
				return null;
			}
		}
		return allFileNames;
	}

	public static List<String> copyFilesToUploadFolder(JProgressBar progressBar, StringBuilder downloadOrCopyReport, ImagesCreatorAndDicomFileAnalyzerService dicomFileAnalyzer, List<Serie> selectedSeries, final File uploadFolder, String filePathDicomDir) throws FileNotFoundException {
		List<String> allFileNames = new ArrayList<String>();
		int totalPercent = 0;
		int serieNumber = 0;
		int numberOfSeries = selectedSeries.size();
		for (Serie serie : selectedSeries) {
			serieNumber++;
			List<String> newFileNamesOfSerie = new ArrayList<String>();
			if (serie.getInstances() == null) {
				downloadOrCopyReport.append("Copy: serie (" + serie.getSeriesNumber() + ") " + serie.getSeriesDescription() + " has no images (ignored).\n");
				continue;
			}
			for (Instance instance : serie.getInstances()) {
				File sourceFile = dicomFileAnalyzer.getFileFromInstance(instance, serie, filePathDicomDir, false);
				String dicomFileName = null;
				if (sourceFile.getAbsolutePath().endsWith(DcmRcvManager.DICOM_FILE_SUFFIX)) {
					dicomFileName = sourceFile.getAbsolutePath().replace(File.separator, "_");
				} else {
					dicomFileName = sourceFile.getAbsolutePath().replace(File.separator, "_") + DcmRcvManager.DICOM_FILE_SUFFIX;
				}
				// clean Windows file system root here to avoid destFile-path
				// with two colons in the path, what is forbidden under Windows
				// and leads therefore to copy failures, that block exports
				if (SystemUtils.IS_OS_WINDOWS) {
					dicomFileName = dicomFileName.replace(":", "");
				}
				File destFile = new File(uploadFolder.getAbsolutePath() + File.separator + dicomFileName);
				FileUtil.copyFile(sourceFile, destFile);
				newFileNamesOfSerie.add(dicomFileName);
				instance.setReferencedFileID(new String[]{dicomFileName});
			}
			downloadOrCopyReport.append("Copy: serie (" + serie.getSeriesNumber() + ") " + serie.getSeriesDescription() + " copied with " + newFileNamesOfSerie.size() + " images.\n");
			allFileNames.addAll(newFileNamesOfSerie);
			totalPercent = Math.round(((float) serieNumber / numberOfSeries) * 100);
			progressBar.setValue(totalPercent);
		}
		return allFileNames;
	}

	public static org.shanoir.uploader.model.rest.Subject manageSubject(org.shanoir.uploader.model.rest.Subject subjectREST, Subject subject, String subjectName, ImagedObjectCategory category, String languageHemDom, String manualHemDom, SubjectStudy subjectStudy, SubjectType subjectType, boolean existingSubjectInStudy, boolean isPhysicallyInvolved, String subjectStudyIdentifier, Study study, StudyCard studyCard) {
		if (subjectREST == null) {
			try {
				subjectREST = fillSubjectREST(subject, subjectName, category, languageHemDom, manualHemDom);
			} catch (ParseException e) {
				logger.error(e.getMessage(), e);
				return null;
			}
			if(addSubjectStudy(study, subjectREST, subjectStudyIdentifier, subjectType, isPhysicallyInvolved)) {
				// create subject with subject-study filled to avoid access denied exception because of rights check
				Long centerId = studyCard.getAcquisitionEquipment().getCenter().getId();
				subjectREST = ShUpOnloadConfig.getShanoirUploaderServiceClient().createSubject(subjectREST, ShUpConfig.isModeSubjectCommonNameManual(), centerId);
				if (subjectREST == null) {
					return null;
				} else {
					logger.info("Subject created on server: " + subjectREST.toString());
				}
			}
		} else {
			// if rel-subject-study does not exist for existing subject, create one
			if (addSubjectStudy(study, subjectREST, subjectStudyIdentifier, subjectType, isPhysicallyInvolved)) {
				if (ShUpOnloadConfig.getShanoirUploaderServiceClient().createSubjectStudy(subjectREST) == null) {
					return null;
				}
			} // in case subject is already in study, do nothing
			logger.info("Subject used on server with ID: " + subjectREST.getId());
		}
		return subjectREST;
	}
	
	private static org.shanoir.uploader.model.rest.Subject fillSubjectREST(Subject subject, String subjectName, ImagedObjectCategory category, String languageHemDom, String manualHemDom) throws ParseException {
		org.shanoir.uploader.model.rest.Subject subjectREST = new org.shanoir.uploader.model.rest.Subject();
		subjectREST.setIdentifier(subject.getIdentifier());
		subjectREST.setBirthDate(subject.getBirthDate());
		if (Sex.F.name().equals(subject.getSex())) {
			subjectREST.setSex(Sex.F);
		} else if (Sex.M.name().equals(subject.getSex())) {
			subjectREST.setSex(Sex.M);
		} else if (Sex.O.name().equals(subject.getSex())) {
			subjectREST.setSex(Sex.O);
		}
		if (ShUpConfig.isModePseudonymus()) {
			subjectREST.setPseudonymusHashValues(subject.getPseudonymusHashValues());
		}
		if (ShUpConfig.isModeSubjectCommonNameManual()) {
			subjectREST.setName(subjectName);
		}
		subjectREST.setImagedObjectCategory(category);
		if (HemisphericDominance.Left.getName().compareTo(languageHemDom) == 0) {
			subjectREST.setLanguageHemisphericDominance(HemisphericDominance.Left);
		} else if (HemisphericDominance.Right.getName().compareTo(languageHemDom) == 0) {
			subjectREST.setLanguageHemisphericDominance(HemisphericDominance.Right);
		}
		if (HemisphericDominance.Left.getName().compareTo(manualHemDom) == 0) {
			subjectREST.setManualHemisphericDominance(HemisphericDominance.Left);
		} else if (HemisphericDominance.Right.getName().compareTo(manualHemDom) == 0) {
			subjectREST.setManualHemisphericDominance(HemisphericDominance.Right);
		}
		subjectREST.setSubjectStudyList(new ArrayList<SubjectStudy>());
		return subjectREST;
	}

	public static Long createExamination(Study study, org.shanoir.uploader.model.rest.Subject subjectREST, Date examinationDate, String examinationComment, Long centerId) {
		Examination examinationREST = new Examination();
		examinationREST.setStudyId(study.getId());
		examinationREST.setSubjectId(subjectREST.getId());
		examinationREST.setCenterId(centerId);
		examinationREST.setExaminationDate(examinationDate);
		examinationREST.setComment(examinationComment);
		examinationREST = ShUpOnloadConfig.getShanoirUploaderServiceClient().createExamination(examinationREST);
		if (examinationREST == null) {
			return null;
		} else {
			logger.info("Examination created on server with ID: " + examinationREST.getId());
			return examinationREST.getId();
		}
	}

	/**
	 * This method adjusts patient values, coming from the DICOM,
	 * with external values entered by better knowing users. Either
	 * added into the Excel table of the mass import or by-patient
	 * added into the GUI of ShUp. If nothing is added for modification,
	 * we assume, that the values from the DICOMs are correct and continue.
	 * 
	 * @param patient
	 * @param firstName
	 * @param lastName
	 * @param birthName
	 * @param birthDateString
	 * @return
	 */
	public static Patient adjustPatientWithPatientVerification(Patient patient, String firstName, String lastName, String birthName, String birthDateString) {
		if (firstName != null && !firstName.isEmpty()) {
			patient.setPatientFirstName(firstName);
		}
		if (lastName != null && !lastName.isEmpty()) {
			patient.setPatientLastName(lastName);
		}
		if (birthName != null && !birthName.isEmpty()) {
			patient.setPatientBirthName(birthName);
		}
		if (birthDateString != null && !birthDateString.isEmpty()) {
			LocalDate birthDate = Util.convertStringToLocalDate(birthDateString);
			patient.setPatientBirthDate(birthDate);	
		}
		return patient;
	}

	public static List<StudyCard> getAllStudyCards(List<Study> studies) throws Exception {
		IdList idList = new IdList();
		for (Iterator<Study> iterator = studies.iterator(); iterator.hasNext();) {
			Study study = (Study) iterator.next();
			idList.getIdList().add(study.getId());
		}
		List<StudyCard> studyCards = ShUpOnloadConfig.getShanoirUploaderServiceClient().findStudyCardsByStudyIds(idList);
		return studyCards;
	}

	public static boolean flagStudyCardCompatible(StudyCard studyCard, String manufacturerModelName, String deviceSerialNumber) {
		boolean isCompatible = checkEquipment(studyCard.getAcquisitionEquipment(), manufacturerModelName, deviceSerialNumber);
		studyCard.setCompatible(isCompatible);
		if (isCompatible) {
			return true; // correct equipment found, break for-loop acqEquip
		}
		return false;
	}

	private static boolean checkEquipment(AcquisitionEquipment acquisitionEquipment, String manufacturerModelName, String deviceSerialNumber) {
		if (acquisitionEquipment == null
			|| acquisitionEquipment.getManufacturerModel() == null
			|| acquisitionEquipment.getManufacturerModel().getName().isBlank()
			|| acquisitionEquipment.getSerialNumber() == null
			|| acquisitionEquipment.getSerialNumber().isBlank()) {
			return false;
		}
		if (acquisitionEquipment.getManufacturerModel().getName().equalsIgnoreCase(manufacturerModelName)) {
			if (acquisitionEquipment.getSerialNumber().equalsIgnoreCase(deviceSerialNumber)
				|| deviceSerialNumber.contains(acquisitionEquipment.getSerialNumber())) {
				return true;
			}
		}
		return false;		
	}

	public static StudyCard createStudyCard(Study studyREST, AcquisitionEquipment equipment, ImportJob importJob) {
		StudyCard studyCard = new StudyCard();
		studyCard.setStudyId(studyREST.getId());
		String studyCardName = studyREST.getName() + " - " + equipment.getCenter().getName() + " - " + equipment.getSerialNumber();
		studyCard.setName(studyCardName);
		studyCard.setAcquisitionEquipmentId(equipment.getId());
		studyCard.setAcquisitionEquipment(equipment);
		studyCard.setCenterId(equipment.getCenter().getId());
		return ShUpOnloadConfig.getShanoirUploaderServiceClient().createStudyCard(studyCard);
	}

	public static AcquisitionEquipment createEquipment(Center center, ManufacturerModel manufacturerModel, String deviceSerialNumber) {
		AcquisitionEquipment equipment = new AcquisitionEquipment();
		IdName centerIdName = new IdName();
		centerIdName.setId(center.getId());
		centerIdName.setName(center.getName());
		equipment.setCenter(centerIdName);
		equipment.setSerialNumber(deviceSerialNumber);
		equipment.setManufacturerModel(manufacturerModel);
		return ShUpOnloadConfig.getShanoirUploaderServiceClient().createEquipment(equipment);
	}

	public static ManufacturerModel createManufacturerModel(String manufacturerModelName, Manufacturer manufacturer, String modality, Double magneticFieldStrength) {
		ManufacturerModel manufacturerModel = new ManufacturerModel();
		manufacturerModel.setName(manufacturerModelName);
		manufacturerModel.setManufacturer(manufacturer);
		manufacturerModel.setDatasetModalityType(modality);
		manufacturerModel.setMagneticField(magneticFieldStrength);
		return ShUpOnloadConfig.getShanoirUploaderServiceClient().createManufacturerModel(manufacturerModel);
	}

	public static Manufacturer createManufacturer(String manufacturerName) {
		Manufacturer manufacturer = new Manufacturer();
		manufacturer.setName(manufacturerName);
		return ShUpOnloadConfig.getShanoirUploaderServiceClient().createManufacturer(manufacturer);
	}

	/**
	 * Find matching equipment via manufacturer model name + device serial number
	 * from entire database, no study restriction, equipment points to center for
	 * study card.
	 * 
	 * @param acquisitionEquipments
	 * @param manufacturerModelName
	 * @param deviceSerialNumber
	 * @return
	 */
	public static AcquisitionEquipment findEquipmentInAllEquipments(List<AcquisitionEquipment> acquisitionEquipments, String manufacturerModelName, String deviceSerialNumber) {
		for (AcquisitionEquipment acquisitionEquipment : acquisitionEquipments) {
			if(checkEquipment(acquisitionEquipment, manufacturerModelName, deviceSerialNumber)) {
				return acquisitionEquipment;
			}
		}
		return null;
	}

	public static ManufacturerModel findManufacturerModelInAllEquipments(List<AcquisitionEquipment> acquisitionEquipments, String manufacturer, String manufacturerModelName) {
		for (AcquisitionEquipment acquisitionEquipment : acquisitionEquipments) {
			if (acquisitionEquipment.getManufacturerModel().getManufacturer().getName().equalsIgnoreCase(manufacturer)
				&& acquisitionEquipment.getManufacturerModel().getName().equalsIgnoreCase(manufacturerModelName)) {
				return acquisitionEquipment.getManufacturerModel();
			}
		}
		return null;
	}

	public static Manufacturer findManufacturerInAllEquipments(List<AcquisitionEquipment> acquisitionEquipments, String manufacturer) {
		for (AcquisitionEquipment acquisitionEquipment : acquisitionEquipments) {
			if (acquisitionEquipment.getManufacturerModel().getManufacturer().getName().equalsIgnoreCase(manufacturer)) {
				return acquisitionEquipment.getManufacturerModel().getManufacturer();
			}
		}
		return null;
	}

	private static Center findCenterOfStudy(Study studyREST, UploadJob uploadJob) {
		String institutionName = uploadJob.getMriInformation().getInstitutionName().toLowerCase();
		List<StudyCenter> studyCenters = studyREST.getStudyCenterList();
		for (StudyCenter studyCenter : studyCenters) {
			String centerName = studyCenter.getCenter().getName().toLowerCase();
			if (centerName.contains(institutionName) || institutionName.contains(centerName)) {
				return studyCenter.getCenter();
			}
		}
		return null;
	}

}
