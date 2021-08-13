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

package org.shanoir.ng.importer.strategies.datasetexpression;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.dicom.DicomProcessing;
import org.shanoir.ng.importer.dto.ExpressionFormat;
import org.shanoir.ng.importer.dto.ImportJob;
import org.shanoir.ng.importer.dto.Serie;
import org.shanoir.ng.shared.dateTime.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DicomDatasetExpressionStrategy implements DatasetExpressionStrategy {

	/** Logger. */
	private static final Logger LOG = LoggerFactory.getLogger(DicomDatasetExpressionStrategy.class);

	@Autowired
	DicomProcessing dicomProcessing;

	@Value("${dcm4chee-arc.protocol}")
	private String dcm4cheeProtocol;
	
	@Value("${dcm4chee-arc.host}")
	private String dcm4cheeHost;

	@Value("${dcm4chee-arc.port.web}")
	private String dcm4cheePortWeb;
	
	@Value("${dcm4chee-arc.dicom.web}")
	private boolean dicomWeb;
	
	@Value("${dcm4chee-arc.dicom.wado.uri}")
	private String dicomWADOURI;
	
	@Value("${dcm4chee-arc.dicom.web.rs}")
	private String dicomWebRS;

	@Override
	public DatasetExpression generateDatasetExpression(Serie serie, ImportJob importJob, ExpressionFormat expressionFormat) throws MalformedURLException {
		DatasetExpression pacsDatasetExpression = new DatasetExpression();
		pacsDatasetExpression.setCreationDate(LocalDateTime.now());
		pacsDatasetExpression.setDatasetExpressionFormat(DatasetExpressionFormat.DICOM);

		if (Boolean.TRUE.equals(serie.getIsMultiFrame())) {
			pacsDatasetExpression.setMultiFrame(true);
			pacsDatasetExpression.setFrameCount(serie.getMultiFrameCount());
		}

		if (expressionFormat != null && expressionFormat.getType().equals("dcm")) {
			for (org.shanoir.ng.importer.dto.DatasetFile datasetFile : expressionFormat.getDatasetFiles()) {
				LocalDateTime contentTime = null;
				LocalDateTime acquisitionTime = null;
				Attributes dicomAttributes = null;
				try {
					dicomAttributes = dicomProcessing.getDicomObjectAttributes(datasetFile, serie.getIsEnhanced());
				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
				}
				DatasetFile pacsDatasetFile = new DatasetFile();
				pacsDatasetFile.setPacs(true);

				final String studyInstanceUID = dicomAttributes.getString(Tag.StudyInstanceUID);
				final String seriesInstanceUID = dicomAttributes.getString(Tag.SeriesInstanceUID);
				final String sOPInstanceUID = dicomAttributes.getString(Tag.SOPInstanceUID);
				final StringBuffer wadoStrBuf = new StringBuffer();
				wadoStrBuf.append(dcm4cheeProtocol + dcm4cheeHost + ":" + dcm4cheePortWeb);
				// Use WADO-RS if true, WADO-URI if otherwise
				if (dicomWeb) {
					wadoStrBuf.append(dicomWebRS + "/" + studyInstanceUID
							+ "/series/" + seriesInstanceUID + "/instances/" + sOPInstanceUID);
				} else {
					wadoStrBuf.append(dicomWADOURI + "?requestType=WADO&studyUID="
							+ studyInstanceUID + "&seriesUID=" + seriesInstanceUID + "&objectUID=" + sOPInstanceUID
							+ "&contentType=application/dicom");
				}
				URL wadoURL = new URL(wadoStrBuf.toString());
				pacsDatasetFile.setPath(wadoURL.toString());
				
				pacsDatasetExpression.getDatasetFiles().add(pacsDatasetFile);
				pacsDatasetFile.setDatasetExpression(pacsDatasetExpression);

				// calculate the acquisition duration for this acquisition
				acquisitionTime = DateTimeUtils.dateToLocalDateTime(dicomAttributes.getDate(Tag.AcquisitionTime));
				contentTime = DateTimeUtils.dateToLocalDateTime(dicomAttributes.getDate(Tag.ContentTime));
				if (acquisitionTime != null) {
					if (pacsDatasetExpression.getLastImageAcquisitionTime() == null) {
						pacsDatasetExpression.setLastImageAcquisitionTime(acquisitionTime);
					}
					if (pacsDatasetExpression.getFirstImageAcquisitionTime() == null) {
						pacsDatasetExpression.setFirstImageAcquisitionTime(acquisitionTime);
					}
					if (acquisitionTime.isAfter(pacsDatasetExpression.getLastImageAcquisitionTime())) {
						pacsDatasetExpression.setLastImageAcquisitionTime(acquisitionTime);
					} else if (acquisitionTime.isBefore(pacsDatasetExpression.getFirstImageAcquisitionTime())) {
						pacsDatasetExpression.setFirstImageAcquisitionTime(acquisitionTime);
					}
				}
				if (contentTime != null) {
					if (pacsDatasetExpression.getLastImageAcquisitionTime() == null) {
						pacsDatasetExpression.setLastImageAcquisitionTime(contentTime);
					}
					if (pacsDatasetExpression.getFirstImageAcquisitionTime() == null) {
						pacsDatasetExpression.setFirstImageAcquisitionTime(contentTime);
					}
					if (contentTime.isAfter(pacsDatasetExpression.getLastImageAcquisitionTime())) {
						pacsDatasetExpression.setLastImageAcquisitionTime(contentTime);
					} else if (contentTime.isBefore(pacsDatasetExpression.getFirstImageAcquisitionTime())) {
						pacsDatasetExpression.setFirstImageAcquisitionTime(contentTime);
					}
				}
			}
		}
		return pacsDatasetExpression;
	}

}
