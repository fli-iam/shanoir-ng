package org.shanoir.uploader.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.shanoir.dicom.importer.Serie;
import org.shanoir.dicom.importer.UploadJob;
import org.shanoir.dicom.importer.UploadState;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.action.DicomDataTransferObject;
import org.shanoir.uploader.dicom.IDicomServerClient;
import org.shanoir.uploader.model.rest.IdName;
import org.shanoir.uploader.model.rest.Study;
import org.shanoir.uploader.model.rest.StudyCard;
import org.shanoir.uploader.model.rest.Subject;
import org.shanoir.uploader.model.rest.SubjectStudy;
import org.shanoir.uploader.model.rest.SubjectType;
import org.shanoir.uploader.model.rest.importer.ImportJob;
import org.shanoir.uploader.model.rest.importer.Instance;
import org.shanoir.uploader.nominativeData.NominativeDataUploadJob;
import org.shanoir.util.ShanoirUtil;
import org.shanoir.util.file.FileUtil;

/**
 * This class contains usefull methods for data upload that are used multiple times in the application.
 * @author Jcome
 *
 */
public class ImportUtils {
	
	private static Logger logger = Logger.getLogger(ImportUtils.class);

	/**
	 * Adds a subjectStudy to a given subject with the given study
	 * @param study the added study
	 * @param subject the subject we add a subjectStudy on
	 * @param sType the type of suject
	 * @param physicallyInvolved is the subject physically involved
	 * @param identifier the subject identifier
	 */
	public static void addSubjectStudy(final Study study, final Subject subject, SubjectType sType, boolean physicallyInvolved, String identifier) {
		SubjectStudy subjectStudy = new SubjectStudy();
		subjectStudy.setStudy(new IdName(study.getId(), study.getName()));
		subjectStudy.setSubject(new IdName(subject.getId(), subject.getName()));
		if (!StringUtils.isEmpty(identifier)) {
			subjectStudy.setSubjectStudyIdentifier(identifier);
		}
		subjectStudy.setSubjectType(sType);
		subjectStudy.setPhysicallyInvolved(physicallyInvolved);
		
		if (subject.getSubjectStudyList() == null) {
			subject.setSubjectStudyList(new ArrayList<>());
		}
		
		subject.getSubjectStudyList().add(subjectStudy);
	}

	/**
	 * Create upload folder from parent folder and dicom information
	 * @param workFolder the parent upload work folder
	 * @param dicomData the dicom data to import
	 * @return the created folder
	 */
	public static File createUploadFolder(final File workFolder, final DicomDataTransferObject dicomData) {
		final String timeStamp = ShanoirUtil.getCurrentTimeStampForFS();
		final String folderName = workFolder.getAbsolutePath() + File.separator + dicomData.getSubjectIdentifier()
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
	public static void initUploadJob(final Set<org.shanoir.dicom.importer.Serie> selectedSeries,
			final DicomDataTransferObject dicomData, UploadJob uploadJob) {
		uploadJob.setUploadState(UploadState.READY);
		uploadJob.setUploadDate(ShanoirUtil.formatTimePattern(new Date()));
		/**
		 * Patient level
		 */
		// set hash of subject identifier in any case: pseudonymus mode or not
		uploadJob.setSubjectIdentifier(dicomData.getSubjectIdentifier());
		// set all 10 hash values for pseudonymus mode
		if (ShUpConfig.isModePseudonymus()) {
			uploadJob.setBirthNameHash1(dicomData.getBirthNameHash1());
			uploadJob.setBirthNameHash2(dicomData.getBirthNameHash2());
			uploadJob.setBirthNameHash3(dicomData.getBirthNameHash3());
			uploadJob.setLastNameHash1(dicomData.getLastNameHash1());
			uploadJob.setLastNameHash2(dicomData.getLastNameHash2());
			uploadJob.setLastNameHash3(dicomData.getLastNameHash3());
			uploadJob.setFirstNameHash1(dicomData.getFirstNameHash1());
			uploadJob.setFirstNameHash2(dicomData.getFirstNameHash2());
			uploadJob.setFirstNameHash3(dicomData.getFirstNameHash3());
			uploadJob.setBirthDateHash(dicomData.getBirthDateHash());
		}
		Date patientBirthDateFirstDayOfYear = getFirstDayOfTheYear(dicomData.getBirthDate());
		uploadJob.setPatientBirthDate(ShUpConfig.formatter.format(patientBirthDateFirstDayOfYear));

		uploadJob.setPatientSex(dicomData.getSex());

		/**
		 * Study level
		 */
		uploadJob.setStudyInstanceUID(dicomData.getStudyInstanceUID());
		uploadJob.setStudyDate(ShUpConfig.formatter.format(dicomData.getStudyDate()));
		uploadJob.setStudyDescription(dicomData.getStudyDescription());

		/**
		 * Serie level
		 */
		uploadJob.setSeries(selectedSeries);
	}

	/**
	 * For OFSEP, do not transfer the real birth date but the first day of the year
	 *
	 * @return the date of the first day of the year
	 */
	private static Date getFirstDayOfTheYear(Date pBirthDate) {
		if (logger.isDebugEnabled()) {
			logger.debug("getFirstDayOfTheYear : Begin");
			logger.debug("getFirstDayOfTheYear : current subject birth date=" + pBirthDate);
		}
		if (pBirthDate != null) {
			final GregorianCalendar birthDate = new GregorianCalendar();
			birthDate.setTime(pBirthDate);
			// set day and month to 01/01
			birthDate.set(Calendar.MONTH, Calendar.JANUARY);
			birthDate.set(Calendar.DAY_OF_MONTH, 1);
			birthDate.set(Calendar.HOUR, 1);
			if (logger.isDebugEnabled()) {
				logger.debug("getFirstDayOfTheYear : anonymity birth date=" + birthDate.getTime());
				logger.debug("getFirstDayOfTheYear : End");
			}
			return birthDate.getTime();
		}
		logger.debug("getFirstDayOfTheYear : End - return null");
		return null;
	}

	public static ImportJob prepareImportJob(UploadJob uploadJob, String subjectName, Long subjectId, Long examinationId, org.shanoir.uploader.model.rest.Study study, StudyCard studyCardSelected) {
		ImportJob importJob = new ImportJob();
		importJob.setFromShanoirUploader(true);
		// Handle study and study card
		org.shanoir.uploader.model.rest.Study studyShanoir = study;
		importJob.setStudyId(studyShanoir.getId());
		importJob.setStudyName(studyShanoir.getName());
		StudyCard studyCard = studyCardSelected;
		// MS Datasets does only return StudyCard DTOs without IDs, as name is unique
		// see: /shanoir-ng-datasets/src/main/java/org/shanoir/ng/studycard/model/StudyCard.java
		importJob.setStudyCardName(studyCard.getName());
		importJob.setAcquisitionEquipmentId(studyCard.getAcquisitionEquipmentId());
		importJob.setConverterId(studyCard.getNiftiConverterId());
		// handle patient and subject
		org.shanoir.uploader.model.rest.importer.Patient patient = new org.shanoir.uploader.model.rest.importer.Patient();
		patient.setPatientID(uploadJob.getSubjectIdentifier());
		Subject subject = new Subject();
		subject.setId(subjectId);
		subject.setName(subjectName);
		importJob.setSubjectName(subjectName);
		patient.setSubject(subject);
		List<org.shanoir.uploader.model.rest.importer.Patient> patients = new ArrayList<>();
		patients.add(patient);
		importJob.setPatients(patients);
		// handle study dicom == examination in Shanoir
		List<org.shanoir.uploader.model.rest.importer.Study> studiesImportJob = new ArrayList<org.shanoir.uploader.model.rest.importer.Study>();
		org.shanoir.uploader.model.rest.importer.Study studyImportJob = new org.shanoir.uploader.model.rest.importer.Study();
		studiesImportJob.add(studyImportJob);
		patient.setStudies(studiesImportJob);
		importJob.setExaminationId(examinationId);
		// handle series for study
		final Collection<Serie> seriesShUp = uploadJob.getSeries();
		final List<org.shanoir.uploader.model.rest.importer.Serie> seriesImportJob = new ArrayList<org.shanoir.uploader.model.rest.importer.Serie>();
		for (org.shanoir.dicom.importer.Serie serieShUp : seriesShUp){
			org.shanoir.uploader.model.rest.importer.Serie serieImportJob = new org.shanoir.uploader.model.rest.importer.Serie();
			serieImportJob.setSelected(true);
			serieImportJob.setSeriesInstanceUID(serieShUp.getId());
			serieImportJob.setSeriesNumber(serieShUp.getSeriesNumber());
			serieImportJob.setModality(serieShUp.getModality());
			serieImportJob.setProtocolName(serieShUp.getProtocol());
			seriesImportJob.add(serieImportJob);
			List<Instance> instancesImportJob = new ArrayList<Instance>();
			for (String filename : serieShUp.getFileNames()){
				Instance instance = new Instance();
				String[] myStringArray = {filename};
				instance.setReferencedFileID(myStringArray);
				instancesImportJob.add(instance);
			}
			serieImportJob.setInstances(instancesImportJob);
			serieImportJob.setImagesNumber(serieShUp.getFileNames().size());
		}
		studyImportJob.setSeries(seriesImportJob);
		return importJob;
	}

	/**
	 * Initializes UploadStatusServiceJob object
	 * 
	 */
	public static void initDataUploadJob(final Set<org.shanoir.dicom.importer.Serie> selectedSeries,
			final DicomDataTransferObject dicomData, NominativeDataUploadJob dataUploadJob) {
		dataUploadJob.setPatientName(dicomData.getFirstName() + " " + dicomData.getLastName());
		dataUploadJob.setPatientPseudonymusHash(dicomData.getSubjectIdentifier());
		dataUploadJob.setStudyDate(ShUpConfig.formatter.format(dicomData.getStudyDate()));
		dataUploadJob.setIPP(dicomData.getIPP());
		Serie firstSerie = selectedSeries.iterator().next();
		dataUploadJob.setMriSerialNumber(firstSerie.getMriInformation().getManufacturer()
				+ "(" + firstSerie.getMriInformation().getDeviceSerialNumber() + ")");
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
	 */
	public static List<String> downloadOrCopyFilesIntoUploadFolder(boolean isFromPACS, Set<Serie> selectedSeries, File uploadFolder, IDicomServerClient dicomServerClient, String filePathDicomDir) {
		List<String> allFileNames = null;
		if (isFromPACS) {
			allFileNames = dicomServerClient.retrieveDicomFiles(selectedSeries, uploadFolder);
			if(allFileNames != null && !allFileNames.isEmpty()) {
				logger.info(uploadFolder.getName() + ": " + allFileNames.size() + " DICOM files downloaded from PACS.");
			} else {
				logger.info(uploadFolder.getName() + ": error with download from PACS.");
				return null;
			}
		} else {
			allFileNames = copyFilesToUploadFolder(selectedSeries, uploadFolder, filePathDicomDir);
			//copyFilesToUploadFolderInSeriesFolder(selectedSeries, uploadFolder);
			if(allFileNames != null) {
				logger.info(uploadFolder.getName() + ": " + allFileNames.size() + " DICOM files copied from CD/DVD.");
			}
		}
		return allFileNames;
	}

	public static List<String> copyFilesToUploadFolder(Set<org.shanoir.dicom.importer.Serie> selectedSeries, final File uploadFolder, String filePathDicomDir) {
		List<String> allFileNames = new ArrayList<String>();
		for (org.shanoir.dicom.importer.Serie serie : selectedSeries) {
			List<String> newFileNamesOfSerie = new ArrayList<String>();
			List<String> oldFileNamesOfSerie = serie.getFileNames();
			File sourceFile;
			File destFile;
			for (Iterator iterator = oldFileNamesOfSerie.iterator(); iterator.hasNext();) {
				String dicomFileName = (String) iterator.next();
				sourceFile = new File(filePathDicomDir + File.separator + dicomFileName);
				dicomFileName = dicomFileName.replace(File.separator, "_");
				destFile = new File(uploadFolder.getAbsolutePath() + File.separator + dicomFileName);
				FileUtil.copyFile(sourceFile, destFile);
				newFileNamesOfSerie.add(dicomFileName);
			}
			serie.setFileNames(newFileNamesOfSerie);
			allFileNames.addAll(newFileNamesOfSerie);
		}
		return allFileNames;
	}

}
