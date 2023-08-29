package org.shanoir.uploader.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.shanoir.ng.importer.dicom.ImagesCreatorAndDicomFileAnalyzerService;
import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.importer.model.Instance;
import org.shanoir.ng.importer.model.Patient;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.action.DicomDataTransferObject;
import org.shanoir.uploader.dicom.IDicomServerClient;
import org.shanoir.uploader.dicom.query.SerieTreeNode;
import org.shanoir.uploader.model.rest.IdName;
import org.shanoir.uploader.model.rest.Study;
import org.shanoir.uploader.model.rest.StudyCard;
import org.shanoir.uploader.model.rest.Subject;
import org.shanoir.uploader.model.rest.SubjectStudy;
import org.shanoir.uploader.model.rest.SubjectType;
import org.shanoir.uploader.nominativeData.NominativeDataUploadJob;
import org.shanoir.uploader.upload.UploadJob;
import org.shanoir.uploader.upload.UploadState;

/**
 * This class contains useful methods for data upload that are used multiple times in the application.
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
		} else {
			// Check that this subjectStudy does not exist yet
			for (SubjectStudy sustu : subject.getSubjectStudyList()) {
				if (sustu.getStudy().getId().equals(study.getId())) {
					// Do not add a new subject study if it already exists
					return;
				}
			}
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
		final String timeStamp = Util.getCurrentTimeStampForFS();
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
	public static void initUploadJob(final Set<org.shanoir.uploader.dicom.query.SerieTreeNode> selectedSeries,
			final DicomDataTransferObject dicomData, UploadJob uploadJob) {
		uploadJob.setUploadState(UploadState.READY);
		uploadJob.setUploadDate(Util.formatTimePattern(new Date()));
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
	 */
	public static ImportJob prepareImportJob(UploadJob uploadJob, String subjectName, Long subjectId, Long examinationId, Study study, StudyCard studyCard) {
		ImportJob importJob = new ImportJob();
		importJob.setFromShanoirUploader(true);
		// Handle study and study card
		importJob.setStudyId(study.getId());
		importJob.setStudyName(study.getName());
		// MS Datasets does only return StudyCard DTOs without IDs, as name is unique
		// see: /shanoir-ng-datasets/src/main/java/org/shanoir/ng/studycard/model/StudyCard.java
		importJob.setStudyCardName(studyCard.getName());
		importJob.setStudyCardId(studyCard.getId());
		importJob.setAcquisitionEquipmentId(studyCard.getAcquisitionEquipmentId());
		importJob.setConverterId(studyCard.getNiftiConverterId());
		importJob.setExaminationId(examinationId);

		List<Patient> patients = new ArrayList<>();
		// handle patient and subject
		Patient patient = new Patient();
		patient.setPatientID(uploadJob.getSubjectIdentifier());
		org.shanoir.ng.importer.model.Subject subject = new org.shanoir.ng.importer.model.Subject();
		subject.setId(subjectId);
		subject.setName(subjectName);
		importJob.setSubjectName(subjectName);
		patient.setSubject(subject);
		patients.add(patient);
		// handle study dicom == examination in Shanoir
		List<org.shanoir.ng.importer.model.Study> studiesImportJob = new ArrayList<org.shanoir.ng.importer.model.Study>();
		org.shanoir.ng.importer.model.Study studyImportJob = new org.shanoir.ng.importer.model.Study();
		// handle series for study
		final List<Serie> series = new ArrayList<Serie>();
		final Collection<SerieTreeNode> serieTreeNodes = uploadJob.getSeries();
		for (SerieTreeNode serieTreeNode : serieTreeNodes) {
			Serie serie = new Serie();
			serie.setSelected(serieTreeNode.isSelected());
			serie.setSeriesInstanceUID(serieTreeNode.getId());
			serie.setSeriesNumber(serieTreeNode.getSeriesNumber());
			serie.setModality(serieTreeNode.getModality());
			serie.setProtocolName(serieTreeNode.getProtocol());
			List<Instance> instances = new ArrayList<Instance>();
			for (String filename : serieTreeNode.getFileNames()){
				Instance instance = new Instance();
				String[] myStringArray = {filename};
				instance.setReferencedFileID(myStringArray);
				instances.add(instance);
			}
			serie.setInstances(instances);
			serie.setImagesNumber(serieTreeNode.getFileNames().size());
			series.add(serie);
		}
		studyImportJob.setSeries(series);
		studiesImportJob.add(studyImportJob);
		patient.setStudies(studiesImportJob);
		importJob.setPatients(patients);
		return importJob;
	}

	/**
	 * Initializes UploadStatusServiceJob object
	 * 
	 */
	public static void initDataUploadJob(final Set<org.shanoir.uploader.dicom.query.SerieTreeNode> selectedSeries,
			final DicomDataTransferObject dicomData, NominativeDataUploadJob dataUploadJob) {
		dataUploadJob.setPatientName(dicomData.getFirstName() + " " + dicomData.getLastName());
		dataUploadJob.setPatientPseudonymusHash(dicomData.getSubjectIdentifier());
		dataUploadJob.setStudyDate(ShUpConfig.formatter.format(dicomData.getStudyDate()));
		dataUploadJob.setIPP(dicomData.getIPP());
		SerieTreeNode firstSerie = selectedSeries.iterator().next();
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
	 * @throws FileNotFoundException 
	 */
	public static List<String> downloadOrCopyFilesIntoUploadFolder(boolean isFromPACS, Set<SerieTreeNode> selectedSeries, File uploadFolder, ImagesCreatorAndDicomFileAnalyzerService dicomFileAnalyzer, IDicomServerClient dicomServerClient, String filePathDicomDir) throws FileNotFoundException {
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
			allFileNames = copyFilesToUploadFolder(dicomFileAnalyzer, selectedSeries, uploadFolder, filePathDicomDir);
			if(allFileNames != null) {
				logger.info(uploadFolder.getName() + ": " + allFileNames.size() + " DICOM files copied from CD/DVD/local file system.");
			} else {
				logger.error("Error while copying file from CD/DVD/local file system.");
			}
		}
		return allFileNames;
	}

	public static List<String> copyFilesToUploadFolder(ImagesCreatorAndDicomFileAnalyzerService dicomFileAnalyzer, Set<org.shanoir.uploader.dicom.query.SerieTreeNode> selectedSeries, final File uploadFolder, String filePathDicomDir) throws FileNotFoundException {
		List<String> allFileNames = new ArrayList<String>();
		for (SerieTreeNode serieTreeNode : selectedSeries) {
			Serie serie = serieTreeNode.getSerie();
			List<String> newFileNamesOfSerie = new ArrayList<String>();
			for (Instance instance : serie.getInstances()) {
				File sourceFile = dicomFileAnalyzer.getFileFromInstance(instance, serie, filePathDicomDir, false);
				String dicomFileName = sourceFile.getAbsolutePath().replace(File.separator, "_");
				File destFile = new File(uploadFolder.getAbsolutePath() + File.separator + dicomFileName);
				FileUtil.copyFile(sourceFile, destFile);
				newFileNamesOfSerie.add(dicomFileName);	
			}
			serieTreeNode.setFileNames(newFileNamesOfSerie);
			allFileNames.addAll(newFileNamesOfSerie);
		}
		return allFileNames;
	}

}
