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

package org.shanoir.ng.importer.strategies.datasetacquisition;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.shanoir.ng.dataset.modality.BidsDataType;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.MrDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.MrProtocol;
import org.shanoir.ng.datasetacquisition.model.mr.MrProtocolSCMetadata;
import org.shanoir.ng.dicom.DicomProcessing;
import org.shanoir.ng.download.AcquisitionAttributes;
import org.shanoir.ng.importer.dto.DatasetsWrapper;
import org.shanoir.ng.importer.dto.Serie;
import org.shanoir.ng.importer.strategies.dataset.DatasetStrategy;
import org.shanoir.ng.importer.strategies.protocol.MrProtocolStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * MR Dataset Acquisition Strategy used to create new Mr Dataset Acquisition.
 * Called by the ImportService.
 * 
 * Refer to Interface for more information
 * 
 * @author atouboul
 *
 */
@Component
public class MrDatasetAcquisitionStrategy implements DatasetAcquisitionStrategy {
	
	private static final Logger LOG = LoggerFactory.getLogger(MrDatasetAcquisitionStrategy.class);
	
    private static final Map<String, BidsDataType> dataTypeMapping;

    static {
        Map<String, BidsDataType> aMap = new HashMap<String, BidsDataType>();
        aMap.put("ANGIO_TIME", BidsDataType.ANAT);
        aMap.put("CINE", BidsDataType.ANAT);
        aMap.put("DIFFUSION", BidsDataType.DWI);
        aMap.put("FLUID_ATTENUATED", BidsDataType.ANAT);
        aMap.put("FMRI", BidsDataType.FUNC);
        aMap.put("MULTIECHO ", BidsDataType.ANAT);
        aMap.put("T1", BidsDataType.ANAT);
        aMap.put("T2", BidsDataType.ANAT);
        aMap.put("T2_STAR", BidsDataType.ANAT);
        dataTypeMapping = Collections.unmodifiableMap(aMap);
    }

	@Autowired
	private MrProtocolStrategy mrProtocolStrategy;
	
	@Autowired
	private DatasetStrategy<MrDataset> mrDatasetStrategy;

	@Override
	public DatasetAcquisition generateDeepDatasetAcquisitionForSerie(String userName, Long subjectId, Serie serie, int rank, AcquisitionAttributes<String> dicomAttributes) throws Exception {
		MrDatasetAcquisition mrDatasetAcquisition = (MrDatasetAcquisition) generateFlatDatasetAcquisitionForSerie(
				userName, serie, rank, dicomAttributes.getFirstDatasetAttributes());

		DatasetsWrapper<MrDataset> datasetsWrapper = mrDatasetStrategy.generateDatasetsForSerie(dicomAttributes, serie, subjectId);
		List<Dataset> genericizedList = new ArrayList<>();
		for (Dataset dataset : datasetsWrapper.getDatasets()) {
			dataset.setDatasetAcquisition(mrDatasetAcquisition);
			genericizedList.add(dataset);
		}
		mrDatasetAcquisition.setDatasets(genericizedList);
		
		// total acquisition time
		if(mrDatasetAcquisition.getMrProtocol().getAcquisitionDuration() == null) {
			Double totalAcquisitionTime = null;
			if (datasetsWrapper.getFirstImageAcquisitionTime() != null && datasetsWrapper.getLastImageAcquisitionTime() != null) {
				Duration duration = Duration.between(datasetsWrapper.getLastImageAcquisitionTime(), datasetsWrapper.getFirstImageAcquisitionTime());
				totalAcquisitionTime = Double.valueOf(duration.toMillis());
				mrDatasetAcquisition.getMrProtocol().setAcquisitionDuration(totalAcquisitionTime);
			} else {
				mrDatasetAcquisition.getMrProtocol().setAcquisitionDuration(null);
			}
		}
		return mrDatasetAcquisition;
	}

	@Override
	public DatasetAcquisition generateFlatDatasetAcquisitionForSerie(
			String userName, Serie serie, int rank,	Attributes attributes) throws IOException {
		LOG.info("Generating MrDatasetAcquisition for: {} - {} - Rank: {}",
				serie.getSequenceName(), serie.getProtocolName(), rank);
		MrDatasetAcquisition mrDatasetAcquisition = new MrDatasetAcquisition();
		mrDatasetAcquisition.setUsername(userName);
		mrDatasetAcquisition.setImportDate(LocalDate.now());
		mrDatasetAcquisition.setSeriesInstanceUID(serie.getSeriesInstanceUID());
		mrDatasetAcquisition.setRank(rank);
		mrDatasetAcquisition.setSortingIndex(serie.getSeriesNumber());
		mrDatasetAcquisition.setSoftwareRelease(attributes.getString(Tag.SoftwareVersions));

		LocalDateTime acquisitionStartTime = DicomProcessing.parseAcquisitionStartTime(
				attributes.getString(Tag.AcquisitionDate), attributes.getString(Tag.AcquisitionTime));
		mrDatasetAcquisition.setAcquisitionStartTime(acquisitionStartTime);
		MrProtocol mrProtocol = mrProtocolStrategy.generateProtocolForSerie(attributes, serie);
		mrDatasetAcquisition.setMrProtocol(mrProtocol);
		// Can be overridden by study cards
		String imageType = attributes.getString(Tag.ImageType, 2);		
		if (imageType != null && dataTypeMapping.get(imageType) != null) {
			if (mrDatasetAcquisition.getMrProtocol().getUpdatedMetadata() == null) {
				mrDatasetAcquisition.getMrProtocol().setUpdatedMetadata(new MrProtocolSCMetadata());
			}
			mrDatasetAcquisition.getMrProtocol().getUpdatedMetadata().setBidsDataType(dataTypeMapping.get(imageType));
		}
		return mrDatasetAcquisition;
	}

}
