package org.shanoir.uploader.dicom.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.shanoir.dicom.model.DicomTreeNode;
//import org.shanoir.services.dicom.server.ConfigBean;
import org.shanoir.util.ShanoirUtil;

/**
 * This class queries a DICOM server.
 * This class has been introduced for
 * the ShanoirUploader as it can be
 * used and called from outside a
 * JBoss Seam context.
 * @author mkain
 *
 */
public class DicomQueryHelper {

	private static Logger logger = Logger.getLogger(DicomQueryHelper.class);

	private IDcmQR dcmqr;

	private ConfigBean configBean;

	private String wantedModality;

	public DicomQueryHelper(final IDcmQR dcmqr, final ConfigBean configBean, final String wantedModality) {
		this.dcmqr = dcmqr;
		this.configBean = configBean;
		this.wantedModality = wantedModality;
	}


	/**
	 *
	 * @param patientNameCriterion
	 * @param studyDescriptionCriterion
	 * @param seriesDescriptionCriterion
	 * @param patientIdCriterion
	 * @param media
	 * @throws Exception
	 */
	public DicomTreeNode populateDicomTree(final String patientNameCriterion, final String studyDescriptionCriterion,
			final String seriesDescriptionCriterion, final String patientIdCriterion, final Date beforeStudyDate,
			final Date afterStudyDate, DicomTreeNode media, final String patientBirthDate, final String studyDate)
			throws Exception {

		if (patientNameCriterion != null && !"".equals(patientNameCriterion)
				|| (patientIdCriterion != null && !"".equals(patientIdCriterion))
				|| (patientBirthDate != null && !"".equals(patientBirthDate))) {
			String[] restrictions;
			if (patientNameCriterion != null && !"".equals(patientNameCriterion)
					&& (patientNameCriterion.contains("*") || !patientNameCriterion.contains("^"))) {
				restrictions = buildRestrictions("", patientIdCriterion, studyDescriptionCriterion,
						seriesDescriptionCriterion, beforeStudyDate, afterStudyDate, patientBirthDate, studyDate);
				String[] args = buildCommand("-P", false, restrictions, null, null);
				media = populateWithPatientLevel(media, dcmqr, restrictions, args, patientNameCriterion, true,
						studyDescriptionCriterion);
			} else {
				restrictions = buildRestrictions(patientNameCriterion, patientIdCriterion, studyDescriptionCriterion,
						seriesDescriptionCriterion, beforeStudyDate, afterStudyDate, patientBirthDate, studyDate);

				String[] args = buildCommand("-P", false, restrictions, null, null);
				media = populateWithPatientLevel(media, dcmqr, restrictions, args, patientNameCriterion, false,
						studyDescriptionCriterion);
			}

		} else if (studyDescriptionCriterion != null && !"".equals(studyDescriptionCriterion)
				|| (studyDate != null && !"".equals(studyDate))) {
			String[] restrictions = buildRestrictions(null, null, studyDescriptionCriterion,
					seriesDescriptionCriterion, beforeStudyDate, afterStudyDate, patientBirthDate, studyDate);
			String[] args = buildCommand(null, false, restrictions, null, null);
			media = populateWithStudyLevel(media, dcmqr, restrictions, args);
		} else if (seriesDescriptionCriterion != null && !"".equals(seriesDescriptionCriterion)) {
			String[] restrictions = buildRestrictions(null, null, null, seriesDescriptionCriterion, beforeStudyDate,
					afterStudyDate, patientBirthDate, studyDate);
			String[] args = buildCommand("-S", false, restrictions, null, null);
			media = populateWithSerieLevel(media, dcmqr, args);
		}

		if (media != null) {
			/*
			 * No result found if there is no patient or if no images are
			 * associated to the patients found
			 */
			boolean noResult = (media.getFirstTreeNode() == null);
			if (media.getTreeNodes().size() > 1) {
				for (final Iterator<DicomTreeNode> itePatient = media.getTreeNodes().values().iterator(); itePatient
						.hasNext();) {
					final DicomTreeNode patient = (DicomTreeNode) itePatient.next();
					if (patient.getFirstTreeNode() != null) {
						noResult &= false;
					}
				}
			}
			if (noResult) {
				media = null;
			}
		}
		return media;
	}

	/**
	 * Builds the restrictions.
	 *
	 * @param patientName
	 *            the patient name
	 * @param patientId
	 *            the patient id
	 * @param studyDescription
	 *            the study description
	 * @param seriesDescription
	 *            the series description
	 * @param beforeStudyDate
	 * @param afterStudyDate
	 *
	 * @return the string[]
	 */
	private String[] buildRestrictions(final String patientName, final String patientId, final String studyDescription,
			final String seriesDescription, final Date beforeStudyDate, final Date afterStudyDate,
			final String patientBirthDate, final String studyDate) {
		logger.debug("buildRestrictions : Begin");

		final List<String> resultList = new ArrayList<String>();
		if (patientId != null && !"".equals(patientId)) {
			resultList.add("-qPatientID=" + patientId);
		} else {
			resultList.add("-rPatientID");
		}
		if (patientName != null && !"".equals(patientName)) {
			resultList.add("-qPatientName=" + patientName);
		} else {
			resultList.add("-rPatientName");
		}
		// Query PatientBirthDate
		if (patientBirthDate != null && !"".equals(patientBirthDate)) {
			resultList.add("-qPatientBirthDate=" + patientBirthDate);
		} else {
			resultList.add("-rPatientBirthDate");
		}

		// Query StudyDate
		if (studyDate != null && !"".equals(studyDate)) {
			resultList.add("-qStudyDate=" + studyDate);
		} else {
			resultList.add("-rStudyDate");
		}

		if (patientId != null && !"".equals(patientId)) {
			resultList.add("-qPatientID=" + patientId);
		} else {
			resultList.add("-rPatientID");
		}

		if (seriesDescription != null && !"".equals(seriesDescription)) {
			resultList.add("-qSeriesDescription=" + seriesDescription);
		} else {
			resultList.add("-rSeriesDescription");
		}
		if (studyDescription != null && !"".equals(studyDescription)) {
			resultList.add("-qStudyDescription=" + studyDescription);
		} else {
			resultList.add("-rStudyDescription");
		}
		if (afterStudyDate != null) {
			if (beforeStudyDate != null) {
				resultList.add("-qStudyDate=" + ShanoirUtil.convertDicomDateToString(afterStudyDate) + "-"
						+ ShanoirUtil.convertDicomDateToString(beforeStudyDate));
			} else {
				resultList.add("-qStudyDate=" + ShanoirUtil.convertDicomDateToString(afterStudyDate));
			}
		}
		// add MRI information to the query result
		resultList.add("-rInstitutionName");
		resultList.add("-rInstitutionAddress");
		resultList.add("-rStationName");
		resultList.add("-rManufacturer");
		resultList.add("-rManufacturerModelName");
		resultList.add("-rDeviceSerialNumber");
		
		// PatientBirthDate A string of characters of the format YYYYMMDD
		String[] result = new String[resultList.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = resultList.get(i);
		}
		logger.debug("buildRestrictions : End, return : " + resultList);

		return result;
	}

	/**
	 * Builds the C find command.
	 *
	 * @param level
	 *            the level
	 * @param restrictions
	 *            the restrictions
	 * @param studyInstanceUID
	 *            the study instance UID
	 *
	 * @return the string[]
	 */
	public String[] buildCommand(final String level, final boolean cmove, final String[] restrictions, final String studyInstanceUID, final String seriesInstanceUID) {
		logger.debug("buildCommand : Begin");
		List<String> resultList = new ArrayList<String>();
		if (level != null) {
			resultList.add(level);
		}
		if (cmove) {
			resultList.add("-cmove");
			resultList.add(configBean.getLocalDicomServerAETCalling());
		}
		String calledServer = configBean.getDicomServerAETCalled() + "@" + configBean.getDicomServerHost() + ":"
				+ configBean.getDicomServerPort();
		resultList.add(calledServer);
		//Local Dicom Server AET Calling is not set when not using ShanoirUploader I think
		//So add this parameter only if had been set
		//if(configBean.getLocalDicomServerAETCalling() != null && !configBean.getLocalDicomServerAETCalling().equals("")){
			resultList.add("-device");
			resultList.add(configBean.getLocalDicomServerAETCalling());
		//}
		if (restrictions != null) {
			resultList.addAll(Arrays.asList(restrictions));
		}
		if (wantedModality != null && !"".equals(wantedModality)) {
			// do different treatment here for level == -I
			if (!"-I".equals(level)) {
				resultList.add("-qModality=" + wantedModality);
			}
		}
		if (studyInstanceUID != null) {
			resultList.add("-qStudyInstanceUID=" + studyInstanceUID);
		}
		if (seriesInstanceUID != null) {
			resultList.add("-qSeriesInstanceUID=" + seriesInstanceUID);
		}
		if (configBean.isDicomServerEnableTLS3DES()) {
			resultList.add("-tls");
			resultList.add("3DES");
			resultList.add("-keystore");
			resultList.add(configBean.getDicomServerKeystoreURL());
			resultList.add("-keystorepw");
			resultList.add(configBean.getDicomServerKeystorePassword());
			resultList.add("-truststore");
			resultList.add(configBean.getDicomServerTruststoreURL());
			resultList.add("-truststorepw");
			resultList.add(configBean.getDicomServerTruststorePassword());
		}
		if (!cmove) {
			// do different treatment here for level == -I
			if (!"-I".equals(level)) {
				resultList.add("-rNumberOfSeriesRelatedInstances");
				resultList.add("-rSeriesDate");
			}
			// do different treatment here for study level,
			// what is the default, NOT -I,-P,-S
			if (level == null) {
				resultList.add("-rPatientSex");
				resultList.add("-rPatientBirthDate");
			}
		}
		// We extend the timeout to 20s for receiving A-ASSOCIATE-AC
		resultList.add("-acceptTO");
		resultList.add("20000");
		String[] result = new String[resultList.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = resultList.get(i);
		}
		logger.debug("buildCommand : End, return : " + resultList);
		return result;
	}

	/**
	 * Populate with patient level.
	 *
	 * @param patientNameCriterion
	 *            the patient name criterion
	 * @param patientIdCriterion
	 *            the patient id criterion
	 * @param studyDescriptionCriterion
	 *            the study description criterion
	 * @param seriesDescriptionCriterion
	 *            the series description criterion
	 *
	 * @return the media
	 * @throws Exception
	 */
	private DicomTreeNode populateWithPatientLevel(DicomTreeNode media, final IDcmQR dcmqr,
			final String[] restrictions, String[] args, final String patientNameCriterion,
			final boolean isFuzzypatientNameCriterion, final String studyDescriptionCriterion) throws Exception {
		// first query: patient level
		logger.debug("C_FIND: launching dcmqr with args: " + ShanoirUtil.arrayToString(args));
		final Collection<DicomObject> patientList = dcmqr.query(args);
		for (final Iterator<DicomObject> itePatient = patientList.iterator(); itePatient.hasNext();) {
			final DicomObject patientDicomObject = itePatient.next();
			// patient name validated on level patient
			boolean patientNameOkLP;
			if (isFuzzypatientNameCriterion)
				patientNameOkLP = checkFuzzyPatientName(patientNameCriterion, patientDicomObject);
			else
				patientNameOkLP = checkPatientName(patientNameCriterion, patientDicomObject);

			if (patientNameOkLP) {

				final DicomTreeNode patient = media.initChildTreeNode(patientDicomObject);
				// true if at least one study matches for the current patient
				boolean studyFound = false;
				args = buildCommand(null, false, restrictions, null, null);
				// second query: study level
				logger.debug("C_FIND: launching dcmqr with args: " + ShanoirUtil.arrayToString(args));
				final Collection<DicomObject> studyList = dcmqr.query(args);
				studyFound = populateWithPatientLevelStudy(dcmqr, restrictions, /* patientNameCriterion */
						patientDicomObject.dataset().getString(Tag.PatientName), studyDescriptionCriterion, patient,
						studyFound, studyList);
				// If a study matches, add the current patient
				if (studyFound) {
					media.addTreeNode(patient.getId(), patient);
				}
				if (patient.getFirstTreeNode() == null) {
					media.getTreeNodes().remove(patient.getId());
				}
			}

		}
		return media;
	}

	/**
	 * Populate the media with a query at the study level.
	 *
	 * @param seriesDescriptionCriterion
	 *            the series description criterion
	 * @param studyDescriptionCriterion
	 *            the study description criterion
	 *
	 * @return the media
	 * @throws Exception
	 */
	private DicomTreeNode populateWithStudyLevel(DicomTreeNode media, final IDcmQR dcmqr, final String[] restrictions,
			String[] args) throws Exception {
		// First query: study level
		logger.debug("C_FIND: Populate with study level, study command: launching dcmqr with args: " + ShanoirUtil.arrayToString(args));
		final Collection<DicomObject> dicomObjectList = dcmqr.query(args);
		logger.debug("Populate with study level, study command: " + dicomObjectList.size() + " result objects.");
		for (final Iterator<DicomObject> iteStudy = dicomObjectList.iterator(); iteStudy.hasNext();) {
			final DicomObject dicomObject = iteStudy.next();
			final DicomTreeNode patient = media.initChildTreeNode(dicomObject);
			logger.debug("Populate with study level, study command: new patient object initialized: " + patient.getDisplayString());
			final DicomTreeNode study = patient.initChildTreeNode(dicomObject);
			logger.debug("Populate with study level, study command: new study object initialized: " + study.getDisplayString());

			// Second query: series level
			args = buildCommand("-S", false, restrictions, dicomObject.dataset().getString(Tag.StudyInstanceUID), null);
			logger.debug("C_FIND: Populate with study level, series command: launching dcmqr with args: " + ShanoirUtil.arrayToString(args));
			final Collection<DicomObject> serieList = dcmqr.query(args);
			logger.debug("Populate with study level, series command: " + serieList.size() + " result objects.");
			for (final Iterator<DicomObject> iteSerie = serieList.iterator(); iteSerie.hasNext();) {
				final DicomObject ser = iteSerie.next();
				final DicomTreeNode serie = study.initChildTreeNode(ser);
				logger.debug("Populate with study level, study command: new serie object initialized: " + serie.getDisplayString());
				media.addTreeNodes(patient, study, serie);
			}
		}
		return media;
	}

	/**
	 * Populate the media with a query at the serie level.
	 *
	 * @param seriesDescriptionCriterion
	 *            the series description criterion
	 *
	 * @return the media
	 * @throws Exception
	 */
	private DicomTreeNode populateWithSerieLevel(DicomTreeNode media, final IDcmQR dcmqr, final String[] args)
			throws Exception {
		logger.debug("C_FIND: launching dcmqr with args: " + ShanoirUtil.arrayToString(args));
		final Collection<DicomObject> serieList = dcmqr.query(args);
		for (final Iterator<DicomObject> iteSerie = serieList.iterator(); iteSerie.hasNext();) {
			final DicomObject dicomObject = iteSerie.next();
			final DicomTreeNode patient = media.initChildTreeNode(dicomObject);
			final DicomTreeNode study = patient.initChildTreeNode(dicomObject);
			final DicomTreeNode serie = study.initChildTreeNode(dicomObject);
			media.addTreeNodes(patient, study, serie);
		}
		return media;
	}

	/**
	 * The normal behavior in DICOM for a query on a patient's name is to find
	 * all the data with the patient's name beginning with the given patient's
	 * name. Thus if the user tries to find the subject 0001, he will get all
	 * the subjects beginning by this string, like 00012, 00011, etc. We then
	 * restrict to the exact value.
	 *
	 * @param patientNameCriterion
	 * @param dicomObject
	 * @param patientNameOk
	 * @return
	 */
	private boolean checkPatientName(final String patientNameCriterion, final DicomObject dicomObject) {
		boolean patientNameOk = true;
		if (patientNameCriterion != null && !"".equals(patientNameCriterion)) {
			final String patientNameFound = dicomObject.dataset().getString(Tag.PatientName);
			logger.debug("populate : patientName found at patient level : " + patientNameFound);
			if (patientNameFound == null) {
				patientNameOk = false;
			} else if (!patientNameFound.equals(patientNameCriterion)) {
				patientNameOk = false;
			}
		}
		return patientNameOk;
	}

	private boolean checkFuzzyPatientName(String patientNameCriterion, final DicomObject dicomObject) {
		boolean patientNameOk = true;

		/*
		 * Extract lastNameCriterion, firstName1Criterion and
		 * firstName2Criterion from the patientNameCriterion
		 */
		String lastNameCriterion = "";
		String firstName1Criterion = "";
		String firstName2Criterion = "";

		if (patientNameCriterion.contains("^")) {

			lastNameCriterion = patientNameCriterion.substring(0, patientNameCriterion.indexOf("^"));
			patientNameCriterion = patientNameCriterion.substring(patientNameCriterion.indexOf("^") + 1,
					patientNameCriterion.length());

			if (patientNameCriterion.contains("^")) {
				firstName1Criterion = patientNameCriterion.substring(0, patientNameCriterion.indexOf("^"));
				firstName2Criterion = patientNameCriterion.substring(patientNameCriterion.indexOf("^") + 1,
						patientNameCriterion.length());
			} else {
				firstName1Criterion = patientNameCriterion;
			}

		} else
			lastNameCriterion = patientNameCriterion;

		/*
		 * Extract lastNameFound, firstName1Found and firstName2Found from the
		 * patientNameFound
		 */

		String patientNameFound = dicomObject.dataset().getString(Tag.PatientName);

		String lastNameFound = "";
		String firstName1Found = "";
		String firstName2Found = "";

		if (patientNameFound.contains("^")) {

			lastNameFound = patientNameFound.substring(0, patientNameFound.indexOf("^"));
			patientNameFound = patientNameFound.substring(patientNameFound.indexOf("^") + 1, patientNameFound.length());

			if (patientNameFound.contains("^")) {
				firstName1Found = patientNameFound.substring(0, patientNameFound.indexOf("^"));
				firstName2Found = patientNameFound.substring(patientNameCriterion.indexOf("^") + 1,
						patientNameFound.length());
			} else {
				firstName1Found = patientNameFound;
			}

		} else
			lastNameFound = patientNameFound;

		/* Compare the the lastNameCriterion and the lastNameFound */

		boolean lastNameMatch = true;
		boolean lastNameRegex = false;
		if (!(lastNameCriterion == "")) {
			if (lastNameCriterion.contains("*")) {
				lastNameCriterion = lastNameCriterion.replaceAll("\\*", "\\.\\*");
				lastNameRegex = true;

			}

			if (lastNameRegex) {
				lastNameMatch = lastNameFound.matches(lastNameCriterion);
			} else {
				if (lastNameCriterion != null)
					lastNameMatch = lastNameFound.contentEquals(lastNameCriterion);
			}
		}
		/* Compare the the firstName1Criterion and the firstName1Found */

		boolean firstName1Match = true;
		boolean firstName1Regex = false;
		if (!(firstName1Criterion == "")) {
			if (firstName1Criterion.contains("*")) {
				firstName1Criterion = firstName1Criterion.replaceAll("\\*", "\\.\\*");
				firstName1Regex = true;

			}
			// }
			if (firstName1Regex) {
				firstName1Match = firstName1Found.matches(firstName1Criterion);
			} else {
				if (firstName1Criterion != null)
					firstName1Match = firstName1Found.contentEquals(firstName1Criterion);
			}
		}
		/* Compare the the firstName2Criterion and the firstName2Found */

		boolean firstName2Match = true;
		boolean firstName2Regex = false;
		if (!(firstName2Criterion == "")) {
			if (firstName2Criterion.contains("*")) {
				firstName2Criterion = firstName2Criterion.replaceAll("\\*", "\\.\\*");
				firstName2Regex = true;

			}

			if (firstName2Regex) {
				firstName2Match = firstName2Found.matches(firstName2Criterion);
			} else {
				if (firstName2Criterion != null)
					firstName2Match = firstName2Found.contentEquals(firstName2Criterion);

			}
		}
		patientNameOk = lastNameMatch && firstName1Match && firstName2Match;
		return patientNameOk;
	}

	/**
	 * Handles study and series level after the patient level has been handled.
	 *
	 * @param dcmqr
	 * @param restrictions
	 * @param patientNameCriterion
	 * @param studyDescriptionCriterion
	 * @param patient
	 * @param studyFound
	 * @param studyList
	 * @return
	 * @throws Exception
	 */
	private boolean populateWithPatientLevelStudy(final IDcmQR dcmqr, final String[] restrictions,
			final String patientNameCriterion, final String studyDescriptionCriterion, final DicomTreeNode patient,
			boolean studyFound, final Collection<DicomObject> studyList) throws Exception {
		for (final Iterator<DicomObject> iteStudy = studyList.iterator(); iteStudy.hasNext();) {
			final DicomObject dicomObject = iteStudy.next();
			// patient name validated on level study
			boolean patientNameOkLS = checkPatientName(patientNameCriterion, dicomObject);
			if (patientNameOkLS) {
				// check the study description
				final String studyDescriptionFromDicomTags = dicomObject.dataset().getString(Tag.StudyDescription);
				logger.debug("populate : studyDescription=" + studyDescriptionCriterion);
				logger.debug("populate : studyDescriptionFromDicomTags=" + studyDescriptionFromDicomTags);
				/*
				 * If the user typed something like 'abc*' then replace it as a
				 * regex by 'abc'. If the user typed no '*' character, then
				 * compare strictly the content of the string
				 */
				boolean match = false;
				String studyDescriptionCriterionReplaced = null;
				boolean regex = false;
				if (studyDescriptionFromDicomTags != null) {
					studyDescriptionCriterionReplaced = studyDescriptionCriterion;
					if (studyDescriptionCriterion != null && !studyDescriptionCriterion.equals("")) {
						if (studyDescriptionCriterion.contains("*")) {
							studyDescriptionCriterionReplaced = studyDescriptionCriterionReplaced.replaceAll("\\*",
									"\\.\\*");
							regex = true;
							logger.debug("populate : regex found, new study description criterion : "
									+ studyDescriptionCriterionReplaced);
						}
					}
					if (regex) {
						match = studyDescriptionFromDicomTags.matches(studyDescriptionCriterionReplaced);
					} else {
						if (studyDescriptionCriterionReplaced != null) {
							match = studyDescriptionFromDicomTags.contentEquals(studyDescriptionCriterionReplaced);
						}
					}
					logger.debug("populate : match=" + match);
				}
				if (match
						|| (studyDescriptionCriterionReplaced == null
						|| "".equals(studyDescriptionCriterionReplaced))) {
					final DicomTreeNode study = patient.initChildTreeNode(dicomObject);
					studyFound = true;
					boolean atLeastOneWantedModality = false;
					atLeastOneWantedModality = populateWithPatientLevelSerie(dcmqr, restrictions, dicomObject, study,
							atLeastOneWantedModality);
					if (atLeastOneWantedModality) {
						logger.debug("populate : adding study " + study);
						patient.addTreeNode(study.getId(), study);
					}
				} else {
					logger.debug("populate : the study description '" + studyDescriptionFromDicomTags
							+ "' doesn't match the search criteria : study='"
							+ studyDescriptionCriterionReplaced + "'");
				}
				logger.debug("populate : getting next patient");
			}
		}
		return studyFound;
	}

	/**
	 * Handles series level after the patient and study level has been handled.
	 *
	 * @param dcmqr
	 * @param restrictions
	 * @param studyDicomObject
	 * @param study
	 * @param atLeastOneWantedModality
	 * @return
	 * @throws Exception
	 */
	private boolean populateWithPatientLevelSerie(final IDcmQR dcmqr, final String[] restrictions,
			final DicomObject studyDicomObject, final DicomTreeNode study, boolean atLeastOneWantedModality) throws Exception {
		final String[] args = buildCommand("-S", false, restrictions, studyDicomObject.dataset().getString(Tag.StudyInstanceUID), null);
		logger.debug("C_FIND: launching dcmqr with args: " + ShanoirUtil.arrayToString(args));
		final Collection<DicomObject> serieList = dcmqr.query(args);
		for (final Iterator<DicomObject> iteSerie = serieList.iterator(); iteSerie.hasNext();) {
			final DicomObject dicomObject = iteSerie.next();
			final DicomTreeNode serie = study.initChildTreeNode(dicomObject);
			final String modality = serie.getDescriptionMap().get("modality");
			if (modality != null) {
				if (wantedModality.equalsIgnoreCase(modality)) {
					atLeastOneWantedModality = true;
				}
				// Don't add serie of modality PR
				if (!"PR".equals(modality) && !"SR".equals(modality)) {
					logger.debug("populate : adding serie " + serie);
					study.addTreeNode(serie.getId(), serie);
				} else {
					logger.debug("populate : not adding serie " + serie
							+ " because it is of modality " + modality);
				}
			}
		}
		return atLeastOneWantedModality;
	}

}
