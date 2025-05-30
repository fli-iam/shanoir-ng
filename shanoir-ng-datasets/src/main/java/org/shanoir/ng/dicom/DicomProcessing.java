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

package org.shanoir.ng.dicom;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections4.ListUtils;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;
import org.shanoir.ng.anonymization.uid.generation.UIDGeneration;
import org.shanoir.ng.download.AcquisitionAttributes;
import org.shanoir.ng.download.ExaminationAttributes;
import org.shanoir.ng.importer.dto.Dataset;
import org.shanoir.ng.importer.dto.DatasetFile;
import org.shanoir.ng.importer.dto.ExpressionFormat;
import org.shanoir.ng.importer.dto.Serie;
import org.shanoir.ng.importer.dto.Study;
import org.shanoir.ng.importer.strategies.datasetacquisition.GenericDatasetAcquisitionStrategy;
import org.shanoir.ng.shared.dateTime.DateTimeUtils;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class DicomProcessing {

	private static final Logger LOG = LoggerFactory.getLogger(GenericDatasetAcquisitionStrategy.class);

	private static UIDGeneration uidGenerator = new UIDGeneration();
	
	@Autowired
	private static WADOURLHandler wadoURLHandler;

	// public static int countUniqueInstances(AcquisitionAttributes acquisitionAttributes) {
	// 	Set<String> instanceUIDs = new HashSet<>();
	// 	List<Attributes> allAttributes = acquisitionAttributes.getAllDatasetAttributes();
	// 	for (Attributes datasetAttributes : allAttributes) {
	// 		String instanceUID = datasetAttributes.getString(Tag.InstanceNumber);
	// 		instanceUIDs.add(instanceUID);
	// 	}
	// 	return instanceUIDs.size();
	// }

	public static int countUniqueInstances(Serie serie, Boolean isEnhancedMR) throws IOException {
 		Set<String> instanceUIDs = new HashSet<>();
 		for (Dataset dataset : ListUtils.emptyIfNull(serie.getDatasets())) {
 			for (ExpressionFormat format : ListUtils.emptyIfNull(dataset.getExpressionFormats())) {
 				for (DatasetFile datasetFile : ListUtils.emptyIfNull(format.getDatasetFiles())) {
 					Attributes attributes = getDicomObjectAttributes(datasetFile, isEnhancedMR);
 					String instanceUID = attributes.getString(Tag.InstanceNumber);
 					instanceUIDs.add(instanceUID);
 				}
 			}
 		}
 		return instanceUIDs.size();
 	}

	public static LocalDateTime parseAcquisitionStartTime(String acqDate, String acqTime) {
		if (acqDate != null && acqTime != null) {
			try {
				return LocalDateTime.of(DateTimeUtils.pacsStringToLocalDate(acqDate), DateTimeUtils.stringToLocalTime(acqTime));
			} catch (DateTimeParseException e) {
				LOG.warn("could not parse the acquisition date : " + acqDate + " and time : " + acqTime);
				return null;
			}
		} else {
			return null;
		}
	}

	public static Attributes getDicomObjectAttributes(DatasetFile image, Boolean isEnhancedMR) throws IOException {
		File dicomFile = new File(image.getPath());
		try (DicomInputStream dIS = new DicomInputStream(dicomFile)) {
			Attributes datasetAttributes;
			if (isEnhancedMR != null && isEnhancedMR) {
				// In case of Enhanced MR, we need to the pixel data in order to use Dcm4chee emf extract method.
				datasetAttributes = dIS.readDataset(-1);
			} else {
				// Else we do not load the picture in Ram for faster performance.
				datasetAttributes = dIS.readDataset(Tag.PixelData);
			}
			return datasetAttributes;
		}
	}

    public ExaminationAttributes<String> getDicomExaminationAttributes(Study study, Boolean isEnhanced) throws ShanoirException {
		ExaminationAttributes<String> attributes = new ExaminationAttributes<>(wadoURLHandler);
		if (study != null) {
			for (Serie serie : study.getSeries()) {
				attributes.addAcquisitionAttributes(serie.getSeriesInstanceUID(), getDicomAcquisitionAttributes(serie, isEnhanced));
			}
		}
		return attributes;
    }

	public static ExaminationAttributes<String> getDicomExaminationAttributes(Study study) throws ShanoirException {
		ExaminationAttributes<String> attributes = new ExaminationAttributes<>(wadoURLHandler);
		if (study != null) {
			for (Serie serie : study.getSeries()) {
				attributes.addAcquisitionAttributes(serie.getSeriesInstanceUID(), getDicomAcquisitionAttributes(serie));
			}
		}
		return attributes;
    }

	public static AcquisitionAttributes<String> getDicomAcquisitionAttributes(Serie serie, Boolean isEnhanced) throws ShanoirException {
		AcquisitionAttributes<String> attributes = new AcquisitionAttributes<>();
		String sopUID = null;
		if (!CollectionUtils.isEmpty(serie.getImages())) {
			sopUID = serie.getImages().get(0).getSOPInstanceUID();
		} else {
			sopUID = uidGenerator.getNewUID();
		}
		// In case of Quality Check during Import from ShUp, Serie does not have any Dataset and conditions are applied on DICOM metadata only.
		if (!CollectionUtils.isEmpty(serie.getDatasets())) {
			for (Dataset dataset : serie.getDatasets()) {
				dataset.setFirstImageSOPInstanceUID(sopUID);
				try {
					attributes.addDatasetAttributes(dataset.getFirstImageSOPInstanceUID(), getDicomObjectAttributes(serie.getFirstDatasetFileForCurrentSerie(), isEnhanced));
				} catch (IOException e) {
					throw new ShanoirException("Could not read dicom metadata from file for serie " + serie.getSopClassUID(), e);
				}
			}
		}
		return attributes;
	}

	public static AcquisitionAttributes<String> getDicomAcquisitionAttributes(Serie serie) throws ShanoirException {
		return getDicomAcquisitionAttributes(serie, serie.getIsEnhanced());
	}

}
