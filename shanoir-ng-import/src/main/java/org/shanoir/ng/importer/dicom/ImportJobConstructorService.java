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

package org.shanoir.ng.importer.dicom;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.emf.MultiframeExtractor;
import org.dcm4che3.io.DicomInputStream;
import org.shanoir.ng.importer.model.EchoTime;
import org.shanoir.ng.importer.model.EquipmentDicom;
import org.shanoir.ng.importer.model.Image;
import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.importer.model.Patient;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.ng.importer.model.Study;
import org.shanoir.ng.shared.dateTime.DateTimeUtils;
import org.shanoir.ng.utils.ImportUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ImportJobConstructorService {

	private static final Logger LOG = LoggerFactory.getLogger(ImportJobConstructorService.class);

	private static final String DOUBLE_EQUAL = "==";

	private static final String SEMI_COLON = ";";

	@Value("${shanoir.import.series.isspectroscopy}")
	private String isSpectroscopy;

	public ImportJob reconstructImportJob(final ImportJob importJob, File folder) throws IOException {
		importJob.setWorkFolder(folder.getAbsolutePath());
		Patient patient = importJob.getPatients().get(0);
		for (Study study : patient.getStudies()) {
			for (Serie serie : study.getSeries()) {
				serie.setNonImages(new ArrayList<Object>());
				boolean firstImageOfSerie = true;
				for (Iterator<Image> iterator = serie.getImages().iterator(); iterator.hasNext();) {
					Image image=iterator.next();
					File imageFile = new File(
							folder.getAbsolutePath() + File.separator + image.getPath());
					try (DicomInputStream dIS = new DicomInputStream(imageFile)) {
						Attributes datasetAttributes = dIS.readDataset(-1, -1);
						String seriesDescription = null;
						if (firstImageOfSerie) {
							if (serie.getSopClassUID() == null) {
								String sopClassUID = datasetAttributes.getString(Tag.SOPClassUID);
								serie.setSopClassUID(sopClassUID);
							}
							seriesDescription = datasetAttributes.getString(Tag.SeriesDescription);
							if (serie.getSeriesDescription() == null) {
								serie.setSeriesDescription(seriesDescription);
							}
							serie.setSeriesInstanceUID(datasetAttributes.getString(Tag.SeriesInstanceUID));
							String protocolNameDicomFile = datasetAttributes.getString(Tag.ProtocolName);
							if (protocolNameDicomFile != null && !protocolNameDicomFile.isEmpty()) {
								serie.setProtocolName(protocolNameDicomFile);
							}
							serie.setSeriesDescription(datasetAttributes.getString(Tag.SeriesDescription));
							serie.setSeriesDate(DateTimeUtils.dateToLocalDate(datasetAttributes.getDate(Tag.StudyDate)));
							serie.setNumberOfSeriesRelatedInstances(datasetAttributes.getInt(Tag.NumberOfSeriesRelatedInstances,0));
							EquipmentDicom equipment = new EquipmentDicom(
								datasetAttributes.getString(Tag.Manufacturer),
								datasetAttributes.getString(Tag.ManufacturerModelName),
								datasetAttributes.getString(Tag.DeviceSerialNumber));
							serie.setEquipment(equipment);
							serie.setIsCompressed(checkSeriesIsCompressed(datasetAttributes));
							if (UID.EnhancedMRImageStorage.equals(serie.getSopClassUID())) {
								serie.setSequenceName(datasetAttributes.getString(Tag.PulseSequenceName));
								serie.setIsEnhanced(true);
								serie.setIsMultiFrame(true);
								Integer frameCount = Integer.valueOf(getFrameCount(datasetAttributes));
								serie.setMultiFrameCount(frameCount);
							} else {
								serie.setSequenceName(datasetAttributes.getString(Tag.SequenceName));
								serie.setIsEnhanced(false);
								serie.setIsMultiFrame(false);
								serie.setMultiFrameCount(0);
							}
							firstImageOfSerie = false;
						}
	
						if (serie.getSopClassUID().startsWith("1.2.840.10008.5.1.4.1.1.66")) {
							// do nothing here as instances array will be deleted after split
							iterator.remove();
						} else {
							// divide here between non-images and images, non-images at first
							if (UID.PrivateSiemensCSANonImageStorage.equals(serie.getSopClassUID())
									|| UID.MRSpectroscopyStorage.equals(serie.getSopClassUID())
									|| checkSerieIsSpectroscopy(seriesDescription)) {
								iterator.remove();
								serie.getNonImages().add(image);
								serie.setIsSpectroscopy(true);
								LOG.warn("Attention: spectroscopy serie is included in this import!");
								// images at the second
							} else {
								// do not change here: use absolute path all time and find other solution for
								// image preview
								addImageSeparateDatasetsInfo(image, datasetAttributes, serie.getSopClassUID());
								serie.setIsSpectroscopy(false);
							}
						}
					}
				}
			}
		}
		return importJob;
	}

	/**
	 * This method uses the properties string isspectroscopy to check if a serie
	 * contains spectroscopy.
	 */
	private boolean checkSerieIsSpectroscopy(final String seriesDescription) {
		final String[] seriesDescriptionsToIdentifySpectroscopyInSerie = isSpectroscopy.split(SEMI_COLON);
		for (final String item : seriesDescriptionsToIdentifySpectroscopyInSerie) {
			final String tag = item.split(DOUBLE_EQUAL)[0];
			final String value = item.split(DOUBLE_EQUAL)[1];
			LOG.debug("checkIsSpectroscopy : tag={}, value={}", tag, value);
			String wildcard = ImportUtils.wildcardToRegex(value);
			if (seriesDescription != null && seriesDescription.matches(wildcard)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * This method adds all required infos to separate datasets within series for
	 * each image.
	 * 
	 * @param image
	 * @param datasetAttributes
	 */
	private void addImageSeparateDatasetsInfo(Image image, Attributes datasetAttributes, String sopClassUID) {
		Attributes attributes = null;
		if (UID.EnhancedMRImageStorage.equals(sopClassUID)) {
			MultiframeExtractor emf = new MultiframeExtractor();
			// sequenceAttributes =
			attributes = emf.extract(datasetAttributes, 0);
		} else {
			attributes = datasetAttributes;
		}

		if (image.getAcquisitionNumber() == 0) {
			image.setAcquisitionNumber(attributes.getInt(Tag.AcquisitionNumber,0));
		}

		if (image.getImageOrientationPatient() == null) {
			List<Double> imageOrientationPatient = new ArrayList<>();
			double[] imageOrientationPatientArray = attributes.getDoubles(Tag.ImageOrientationPatient);
			if (imageOrientationPatientArray != null) {
				for (int i = 0; i < imageOrientationPatientArray.length; i++) {
					imageOrientationPatient.add(imageOrientationPatientArray[i]);
				}
				image.setImageOrientationPatient(imageOrientationPatient);
			} else {
				LOG.info("imageOrientationPatientArray in dcm file null: {}", image.getPath());
			}
		}
		
		if (image.getRepetitionTime() == null) {
			Double repetitionTime = attributes.getDouble(Tag.RepetitionTime, 0);
			image.setRepetitionTime(repetitionTime);
		}

		if (image.getInversionTime() == null) {
			Double inversionTime = attributes.getDouble(Tag.InversionTime, 0);
			image.setInversionTime(inversionTime);
		}

		if (image.getFlipAngle() == null) {
			String flipAngle = attributes.getString(Tag.FlipAngle);
			if (flipAngle == null) {
				flipAngle = "0";
			}
			image.setFlipAngle(flipAngle);
		}

		if (image.getEchoTimes() == null) {
			EchoTime echoTime = new EchoTime();
			Integer anEchoNumber = attributes.getInt(Tag.EchoNumbers, 0);
			Double anEchoTime = attributes.getDouble(Tag.EchoTime, 0.0);
			echoTime.setEchoNumber(anEchoNumber);
			echoTime.setEchoTime(anEchoTime);
			Set<EchoTime> echoTimes = new HashSet<>();
			echoTimes.add(echoTime);
			image.setEchoTimes(echoTimes);
		}
	}
	
	/**
	 * Adds on analyzing the transfersyntaxuid if serie is compressed or not.
	 * 
	 * @param serie
	 * @param datasetAttributes
	 */
	private boolean checkSeriesIsCompressed(Attributes datasetAttributes) {
		String transferSyntaxUID = datasetAttributes.getString(Tag.TransferSyntaxUID);
		return transferSyntaxUID != null && transferSyntaxUID.startsWith("1.2.840.10008.1.2.4");
	}
	
	/**
	 * Get the frame count of the given dicom object.
	 *
	 * @param dcmObj
	 *            the dcmObj
	 * @return the frame count
	 */
	private int getFrameCount(final Attributes attributes) {
		if (attributes != null) {
			Attributes pffgs = attributes.getNestedDataset(Tag.PerFrameFunctionalGroupsSequence);
			if (pffgs != null) {
				return pffgs.size();
			} else {
				return 0;
			}
		} else {
			return -1;
		}
	}

}
