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

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;
import org.shanoir.ng.anonymization.uid.generation.UIDGeneration;
import org.shanoir.ng.download.AcquisitionAttributes;
import org.shanoir.ng.download.ExaminationAttributes;
import org.shanoir.ng.importer.dto.Dataset;
import org.shanoir.ng.importer.dto.DatasetFile;
import org.shanoir.ng.importer.dto.Serie;
import org.shanoir.ng.importer.dto.Study;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class DicomProcessing {

	UIDGeneration uidGenerator = new UIDGeneration();
	
	@Autowired
	private WADOURLHandler wadoURLHandler;

	public Attributes getDicomObjectAttributes(DatasetFile image, Boolean isEnhancedMR) throws IOException {
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
		ExaminationAttributes<String> attributes = new ExaminationAttributes<String>(wadoURLHandler);
		if (study != null) {
			for (Serie serie : study.getSeries()) {
				attributes.addAcquisitionAttributes(serie.getSeriesInstanceUID(), getDicomAcquisitionAttributes(serie, isEnhanced));
			}
		}
		return attributes;
    }

	public ExaminationAttributes<String> getDicomExaminationAttributes(Study study) throws ShanoirException {
		ExaminationAttributes<String> attributes = new ExaminationAttributes<String>(wadoURLHandler);
		if (study != null) {
			for (Serie serie : study.getSeries()) {
				attributes.addAcquisitionAttributes(serie.getSeriesInstanceUID(), getDicomAcquisitionAttributes(serie));
			}
		}
		return attributes;
    }

	public AcquisitionAttributes<String> getDicomAcquisitionAttributes(Serie serie, Boolean isEnhanced) throws ShanoirException {
		AcquisitionAttributes<String> attributes = new AcquisitionAttributes<String>();
		String sopUID = null;
		if (!CollectionUtils.isEmpty(serie.getImages())) {
			sopUID = serie.getImages().get(0).getSOPInstanceUID();
		} else {
			sopUID = uidGenerator.getNewUID();
		}
		for (Dataset dataset : serie.getDatasets()) {
			try {
				dataset.setFirstImageSOPInstanceUID(sopUID);
				attributes.addDatasetAttributes(dataset.getFirstImageSOPInstanceUID(), getDicomObjectAttributes(serie.getFirstDatasetFileForCurrentSerie(), isEnhanced));
			} catch (IOException e) {
				throw new ShanoirException("Could not read dicom metadata from file for serie " + serie.getSopClassUID(), e);
			}
		}
		return attributes;
	}

	public AcquisitionAttributes<String> getDicomAcquisitionAttributes(Serie serie) throws ShanoirException {
		return getDicomAcquisitionAttributes(serie, serie.getIsEnhanced());
	}

}
