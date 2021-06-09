/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.anonymization.anonymization;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.shanoir.ng.anonymization.uid.generation.UIDGeneration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Anonymization serviceImpl. mkain: bug fixing done for multi-threading errors,
 * e.g. when used by server. bug fixed for identical media storage sop instance
 * uid and sop instance uid and bug fixed for invalid uid generation.
 * 
 * @author ifakhfakh
 * @author mkain
 * 
 */
public class AnonymizationServiceImpl implements AnonymizationService {

	private static final Logger LOG = LoggerFactory.getLogger(AnonymizationServiceImpl.class);

	private static final String PRIVATE_TAGS = "0xggggeeee";
	private static final String CURVE_DATA_TAGS = "0x50xxxxxx";
	private static final String OVERLAY_COMMENTS_TAGS = "0x60xx4000";
	private static final String OVERLAY_DATA_TAGS = "0x60xx3000";
	
	private Random rand = new Random();
	
	private static Map<String, List<String>> tagsToDeleteForManufacturer;

	@Override
	public void anonymize(ArrayList<File> dicomFiles, String profile) throws Exception {
		long startTime = System.currentTimeMillis();
		final int totalAmount = dicomFiles.size();
		LOG.info("Start anonymization, for {} DICOM files.", totalAmount);
		Map<String, Profile> profiles = AnonymizationRulesSingleton.getInstance().getProfiles();
		Map<String, String> anonymizationMap = profiles.get(profile).getAnonymizationMap();
		tagsToDeleteForManufacturer = AnonymizationRulesSingleton.getInstance().getTagsToDeleteForManufacturer();
		// init here for multi-threading reasons
		Map<String, String> seriesInstanceUIDs = new HashMap<>();
		Map<String, String> studyInstanceUIDs = new HashMap<>();
		Map<String, String> studyIds = new HashMap<>();
		LOG.debug("anonymize : totalAmount={}", totalAmount);
		int current = 0;
		for (int i = 0; i < dicomFiles.size(); ++i) {
			final File file = dicomFiles.get(i);
			// Perform the anonymization
			performAnonymization(file, anonymizationMap, false, "", "", seriesInstanceUIDs, studyInstanceUIDs, studyIds);
			current++;
			final int currentPercent = current * 100 / totalAmount;
			LOG.debug("anonymize : anonymization current percent= {} %", currentPercent);
		}
		logInfos("End anonymization", startTime);
	}

	@Override
	public void anonymizeForShanoir(ArrayList<File> dicomFiles, String profile, String patientLastName,
			String patientFirstName, String patientID) throws Exception {
		String patientName = patientLastName + "^" + patientFirstName + "^^^";
		anonymizeForShanoir(dicomFiles, profile, patientName, patientID);
	}

	@Override
	public void anonymizeForShanoir(ArrayList<File> dicomFiles, String profile, String patientName, String patientID) throws Exception {
		long startTime = System.currentTimeMillis();
		final int totalAmount = dicomFiles.size();
		LOG.info("Start anonymization, for {} DICOM files.", totalAmount);
		Map<String, Profile> profiles = AnonymizationRulesSingleton.getInstance().getProfiles();
		Map<String, String> anonymizationMap = profiles.get(profile).getAnonymizationMap();
		tagsToDeleteForManufacturer = AnonymizationRulesSingleton.getInstance().getTagsToDeleteForManufacturer();
		// init here for multi-threading reasons

		Map<String, String> seriesInstanceUIDs = new HashMap<>();
		Map<String, String> studyInstanceUIDs = new HashMap<>();
		Map<String, String> studyIds = new HashMap<>();
		LOG.debug("anonymize : totalAmount={}", totalAmount);
		int current = 0;
		for (int i = 0; i < dicomFiles.size(); ++i) {
			final File file = dicomFiles.get(i);
			// Perform the anonymization
			performAnonymization(file, anonymizationMap, true, patientName, patientID, seriesInstanceUIDs, studyInstanceUIDs, studyIds);
			current++;
			final int currentPercent = current * 100 / totalAmount;
			LOG.debug("anonymize : anonymization current percent= {} %", currentPercent);
		}
		logInfos("End anonymization", startTime);
	}
	
	private void logInfos(final String methodName, long startTime) {
		long stopTime = System.currentTimeMillis();
	    long elapsedTime = stopTime - startTime;
		LOG.info("{}, duration (ms): {}", methodName, elapsedTime);
	}

	private void anonymizePatientMetaData(Attributes attributes, String patientName, String patientID,
			String patientBirthDate) {
		anonymizeTagAccordingToVR(attributes, Tag.PatientName, patientName);
		anonymizeTagAccordingToVR(attributes, Tag.PatientID, patientID);

		// patient birth date
		if (patientBirthDate != null && patientBirthDate.length() != 0) {
			String newDate = patientBirthDate.substring(0, 4) + "01" + "01";
			anonymizeTagAccordingToVR(attributes, Tag.PatientBirthDate, newDate);
		}
	}

	/**
	 * Perform the anonymization for an DICOM image according to chosen profile. To
	 * have a consistent DICOM file: the attribute in the "header" (0002,0003) Media
	 * Storage SOP Instance UID and the attribute in the "body" (0008 0018) SOP
	 * Instance UID have to match. If they do not match the PACS returns the
	 * following error: SOP Instance UID in Dataset [xxx] differs from Affected SOP
	 * Instance UID [yyy]. The problem is, that when doing the dcmSend, the tool
	 * reads the SOP Instance UID from the meta-information/header and sends the
	 * file with a C-STORE request and an Affected SOP Instance UID (== header) in
	 * the request header. If the file arrives in the PACS, the SOP Instance UID in
	 * the file does not match with the request header and this is refused.
	 * 
	 * Further does each part of an UID has to start with a non-zero value, see
	 * UIDGeneration code.
	 * 
	 * @param dicomFile
	 *            the image path
	 * @param profile
	 *            anonymization profile
	 * @throws Exception
	 */
	public void performAnonymization(final File dicomFile, Map<String, String> anonymizationMap, boolean isShanoirAnonymization,
			String patientName, String patientID, Map<String, String> seriesInstanceUIDs,
			Map<String, String> studyInstanceUIDs, Map<String, String> studyIds) throws Exception {
		DicomInputStream din = null;
		DicomOutputStream dos = null;
		try {
			din = new DicomInputStream(dicomFile);
			
			/**
			 * DICOM "header"/meta-information fields: read tags
			 */
			Attributes metaInformationAttributes = din.readFileMetaInformation();
			for (int tagInt : metaInformationAttributes.tags()) {
				String tagString = String.format("0x%08x", Integer.valueOf(tagInt));
				if (anonymizationMap.containsKey(tagString)) {
					final String action = anonymizationMap.get(tagString);
					anonymizeTag(tagInt, action, metaInformationAttributes);
				}
			}
			final String mediaStorageSOPInstanceUIDGenerated = metaInformationAttributes
					.getString(Tag.MediaStorageSOPInstanceUID);
			
			/**
			 * DICOM "body": read tags
			 */
			Attributes datasetAttributes = din.readDataset(-1, -1);
			
			// temporarily keep the patient credentials in memory to search in private tags
			String patientNameAttr = datasetAttributes.getString(Tag.PatientName);
			String[] patientNameArrayAttr = null;
			if (patientNameAttr != null && !patientNameAttr.isEmpty()) {
				patientNameArrayAttr = patientNameAttr.split("\\^");
			}
			String patientIDAttr = datasetAttributes.getString(Tag.PatientID);
			String patientBirthNameAttr = datasetAttributes.getString(Tag.PatientBirthName);
			// temporarily keep the patient birth date for isShanoirAnonymization
			String patientBirthDateAttr = datasetAttributes.getString(Tag.PatientBirthDate);

			// anonymize DICOM files according to selected profile
			for (int tagInt : datasetAttributes.tags()) {
				String tagString = String.format("0x%08X", Integer.valueOf(tagInt));
				String gggg = tagString.substring(2, 6);
				Integer intgggg = Integer.decode("0x" + gggg);
				// odd: for private tags
				if (intgggg % 2 == 1) {
					String action = anonymizationMap.get(PRIVATE_TAGS);
					String value = datasetAttributes.getString(tagInt);
					// only act below in case of K: keep, if X: delete for private tags, no need
					if (value != null && !value.isEmpty() && action.equals("K")) {
						action = checkForPHIInPrivateTags(patientNameArrayAttr, patientIDAttr, patientBirthNameAttr, patientBirthDateAttr, tagInt, value, action);
						action = handleTagsToDeleteForManufacturer(datasetAttributes, tagString, action);
					}
					anonymizeTag(tagInt, action, datasetAttributes);
				// even: public tags
				} else if (anonymizationMap.containsKey(tagString)) {
					if (tagInt == Tag.SOPInstanceUID) {
						anonymizeSOPInstanceUID(tagInt, datasetAttributes, mediaStorageSOPInstanceUIDGenerated);
					} else if (tagInt == Tag.SeriesInstanceUID) {
						anonymizeSeriesInstanceUID(tagInt, datasetAttributes, seriesInstanceUIDs);
					} else if (tagInt == Tag.StudyInstanceUID) {
						anonymizeStudyInstanceUID(tagInt, datasetAttributes, studyInstanceUIDs);
					} else if (tagInt == Tag.StudyID) {
						anonymizeStudyId(tagInt, datasetAttributes, studyIds);
					} else {
						final String action = anonymizationMap.get(tagString);
						anonymizeTag(tagInt, action, datasetAttributes);
					}
				} else {
					if (0x50000000 <= tagInt && tagInt <= 0x50FFFFFF) {
						final String action = anonymizationMap.get(CURVE_DATA_TAGS);
						anonymizeTag(tagInt, action, datasetAttributes);
					} else if (0x60004000 <= tagInt && tagInt <= 0x60FF4000) {
						final String action = anonymizationMap.get(OVERLAY_COMMENTS_TAGS);
						anonymizeTag(tagInt, action, datasetAttributes);
					} else if (0x60003000 <= tagInt && tagInt <= 0x60FF3000) {
						final String action = anonymizationMap.get(OVERLAY_DATA_TAGS);
						anonymizeTag(tagInt, action, datasetAttributes);
					}
				}
			}
			// Special anonymization of patient data if isShanoirAnonymization
			if (isShanoirAnonymization) {
				anonymizePatientMetaData(datasetAttributes, patientName, patientID, patientBirthDateAttr);
			}
			LOG.debug("finish anonymization: begin storage");
			dos = new DicomOutputStream(dicomFile);
			dos.writeDataset(metaInformationAttributes, datasetAttributes);
			LOG.debug("finish anonymization: end storage");
		} catch (final IOException exc) {
			LOG.error("performAnonymization : error while anonimizing file " + dicomFile.toString() + " : ", exc);
		} finally {
			try {
				if (din != null) {
					din.close();
				}
				if (dos != null) {
					dos.close();
				}
			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * Handle tags to delete for manufacturer here
	 * 
	 * @param datasetAttributes
	 * @param tagString
	 * @param action
	 * @return
	 */
	private String handleTagsToDeleteForManufacturer(Attributes datasetAttributes, String tagString, String action) {
		String manufacturer = datasetAttributes.getString(Tag.Manufacturer);
		List<String> tagsToDelete = tagsToDeleteForManufacturer.get(manufacturer);
		if (tagsToDelete != null) {
			for (Iterator<String> iterator = tagsToDelete.iterator(); iterator.hasNext();) {
				String tagToDelete = iterator.next();
				if (tagString.equals(tagToDelete)) {
					action = "X";
					break;
				}
			}
		}
		return action;
	}

	/**
	 * @param patientNameArrayAttr
	 * @param patientIDAttr
	 * @param patientBirthNameAttr
	 * @param patientBirthDateAttr
	 * @param tagInt
	 * @param value
	 * @throws Exception
	 */
	private String checkForPHIInPrivateTags(String[] patientNameArrayAttr, String patientIDAttr, String patientBirthNameAttr,
			String patientBirthDateAttr, int tagInt, String value, String action) throws Exception {
		// check for patient name elements
		for (int i = 0; i < patientNameArrayAttr.length; i++) {
			String patientNamePart = patientNameArrayAttr[i];
			if(checkTagContainsValuePHI(tagInt, value, patientNamePart)) {
				return "X";
			}
		}
		if (checkTagContainsValuePHI(tagInt, value, patientIDAttr)
			|| checkTagContainsValuePHI(tagInt, value, patientBirthNameAttr)
			|| checkTagContainsValuePHI(tagInt, value, patientBirthDateAttr)) {
			return "X";
		}
		return action;
	}

	/**
	 * @param tagInt
	 * @param value
	 * @param compareValuePHI
	 * @throws Exception
	 */
	private boolean checkTagContainsValuePHI(int tagInt, String value, String compareValuePHI) throws Exception {
		if (compareValuePHI != null && !compareValuePHI.isEmpty() && compareValuePHI.length() > 2 && value.contains(compareValuePHI)) {
			LOG.warn("Potential PHI found in private tag (--> remove/delete): " + tagInt + ": " + value);
			return true;
		}
		return false;
	}

	/**
	 * Tag Anonymization
	 * 
	 * @param tagInt
	 *            : the tag to anonymize
	 * @param action
	 *            : the action letter to apply
	 * @param attributes
	 *            : the list of dicom attributes to modify
	 */
	private void anonymizeTag(Integer tagInt, String action, Attributes attributes) {
		String value = getFinalValueForTag(action);
		if (value == null) {
			attributes.remove(tagInt);
		} else if ("KEEP".equals(value)) {
			// do nothing
		} else {
			anonymizeTagAccordingToVR(attributes, tagInt, value);
		}
	}

	private void anonymizeSOPInstanceUID(int tagInt, Attributes attributes, String mediaStorageSOPInstanceUID) {
		anonymizeTagAccordingToVR(attributes, tagInt, mediaStorageSOPInstanceUID);
	}

	private void anonymizeSeriesInstanceUID(int tagInt, Attributes attributes, Map<String, String> seriesInstanceUIDs) {
		String value;
		if (seriesInstanceUIDs != null && seriesInstanceUIDs.size() != 0
				&& seriesInstanceUIDs.get(attributes.getString(tagInt)) != null) {
			value = seriesInstanceUIDs.get(attributes.getString(tagInt));
		} else {
			UIDGeneration generator = new UIDGeneration();
			String newUID = null;
			try {
				newUID = generator.getNewUID();
			} catch (Exception e) {
				LOG.error(e.getMessage());
			}
			value = newUID;
			seriesInstanceUIDs.put(attributes.getString(tagInt), value);
		}
		anonymizeTagAccordingToVR(attributes, tagInt, value);
	}

	private void anonymizeStudyInstanceUID(int tagInt, Attributes attributes, Map<String, String> studyInstanceUIDs) {
		String value;
		if (studyInstanceUIDs != null && studyInstanceUIDs.size() != 0
				&& studyInstanceUIDs.get(attributes.getString(tagInt)) != null) {
			value = studyInstanceUIDs.get(attributes.getString(tagInt));
		} else {
			UIDGeneration generator = new UIDGeneration();
			String newUID = null;
			try {
				newUID = generator.getNewUID();
			} catch (Exception e) {
				LOG.error(e.getMessage());
			}
			value = newUID;
			studyInstanceUIDs.put(attributes.getString(tagInt), value);
		}
		anonymizeTagAccordingToVR(attributes, tagInt, value);
	}

	private void anonymizeStudyId(int tagInt, Attributes attributes, Map<String, String> studyIds) {
		String value;
		if (studyIds != null && studyIds.size() != 0 && studyIds.get(attributes.getString(tagInt)) != null) {
			value = studyIds.get(attributes.getString(tagInt));
		} else {
			char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < 10; i++) {
				char c = chars[rand.nextInt(chars.length)];
				sb.append(c);
			}
			String output = sb.toString();
			value = output.toString();
			studyIds.put(attributes.getString(tagInt), value);
		}
		anonymizeTagAccordingToVR(attributes, tagInt, value);
	}

	/**
	 * Get the anonymized value of the tag
	 * 
	 * @param action
	 *            : the action letter to apply
	 * @return
	 */
	private String getFinalValueForTag(final String action) {
		String result = "";
		if (action != null) {
			if (action.equals("X")) {
				result = null;
			} else if (action.equals("Z")) {
				result = "";
			} else if (action.equals("D")) {
				SecureRandom random = new SecureRandom();
				result = new BigInteger(130, random).toString(32);
			} else if (action.equals("U")) {
				UIDGeneration generator = new UIDGeneration();
				String newUID = null;
				try {
					newUID = generator.getNewUID();
				} catch (Exception e) {
					LOG.error(e.getMessage());
				}
				result = newUID;
			} else if (action.equals("K")) {
				result = "KEEP";
			}
		}
		return result;
	}

	/**
	 * anonymize Tag According To its VR
	 * 
	 * @param attributes
	 *            : the list of dicom attributes to modify
	 * @param tag
	 *            : the tag to anonymize
	 * @param value
	 *            : the new value of the tag after anonymization
	 */
	private void anonymizeTagAccordingToVR(Attributes attributes, int tag, String value) {
		VR vr = attributes.getVR(tag);
		if (vr == null) {
			return;
		}
		// VR.AT = Attribute Tag
		// VR.SL = Signed Long || VR.UL = Unsigned Long
		// VR.SS = Signed Short || VR.US = Unsigned Short
		if (vr.equals(VR.SL) || vr.equals(VR.UL) || vr.equals(VR.AT) || vr.equals(VR.SS) || vr.equals(VR.US)) {
			Integer iValue = Integer.decode(value);
			attributes.setInt(tag, vr, iValue);
		}

		// VR.FD = Floating Point Double
		else if (vr.equals(VR.FD)) {
			Double dValue = Double.valueOf(value);
			attributes.setDouble(tag, vr, dValue);
		}

		// VR.FL = Floating Point Single
		else if (vr.equals(VR.FL)) {
			Float fValue = Float.valueOf(value);
			attributes.setFloat(tag, vr, fValue);
		}

		// VR.OB = Other Byte String
		else if (vr.equals(VR.OB)) {
			byte[] b = new byte[1];
			attributes.setBytes(tag, vr, b);
		}

		// VR.SQ = Sequence of Items || VR.UN = Unknown
		else if (vr.equals(VR.SQ) || vr.equals(VR.UN)) {
			attributes.setNull(tag, vr);
		}

		// Unlimited string:
		// VR.AE = Age String
		// VR.AS = Application Entity
		// VR.CS = Code String
		// VR.DA = Date
		// VR.DS = Date Time
		// VR.DT = Decimal String
		// VR.IS = Integer String
		// VR.LO = Long String
		// VR.LT = Long Text
		// VR.OF = Other Float String
		// VR.OW = Other Word String
		// VR.PN = Person Name
		// VR.SH = Short String
		// VR.ST = Short Text
		// VR.TM = Time
		// VR.UI = Unique Identifier (UID)
		// VR.UT = Unlimited Text
		else if (vr.equals(VR.AE) || vr.equals(VR.AS) || vr.equals(VR.CS) || vr.equals(VR.DA) || vr.equals(VR.DS)
				|| vr.equals(VR.DT) || vr.equals(VR.IS) || vr.equals(VR.LO) || vr.equals(VR.LT) || vr.equals(VR.OW)
				|| vr.equals(VR.PN) || vr.equals(VR.SH) || vr.equals(VR.ST) || vr.equals(VR.TM) || vr.equals(VR.UI)
				|| vr.equals(VR.UT) || vr.equals(VR.OF)) {
			attributes.setString(tag, vr, value);
		}

		else {
			attributes.setString(tag, vr, value);
		}

		// N.B.: Doesn't exist in the library:
		// VR.UR = Universal Resource Identifier or Universal
		// Resource Locator (URI/URL)
		// VR.OD = Other Double String
	}

}
