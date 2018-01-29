package org.shanoir.ng.datasetacquisition.mr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.shanoir.ng.dataset.Dataset;
import org.shanoir.ng.dataset.modality.DatasetStrategy;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.datasetacquisition.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.DatasetAcquisitionStrategy;
import org.shanoir.ng.dicom.DicomProcessing;
import org.shanoir.ng.examination.Examination;
import org.shanoir.ng.importer.dto.DatasetWrapper;
import org.shanoir.ng.importer.dto.ImportJob;
import org.shanoir.ng.importer.dto.Serie;
import org.shanoir.ng.shared.model.EchoTime;
import org.shanoir.ng.shared.model.FlipAngle;
import org.shanoir.ng.shared.model.InversionTime;
import org.shanoir.ng.shared.model.RepetitionTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * MR Dataset Acquisition Strategy used to create new Mr Dataset Acquisition.
 * Called by the ImportService. Requires an importJob
 * 
 * Refer to Interface for more information
 * 
 * @author atouboul
 *
 */

public class MrDatasetAcquisitionStrategy implements DatasetAcquisitionStrategy {
	
	/** Logger. */
	private static final Logger LOG = LoggerFactory.getLogger(MrDatasetAcquisitionStrategy.class);

	@Autowired
	DicomProcessing dicomProcessing;
	
	@Autowired
	MrProtocolStrategy mrProtocolStrategy;
	
	@Autowired
	DatasetStrategy mrDatasetStrategy;
	
	@Override
	public DatasetAcquisition generateDatasetAcquisitionForSerie(Serie serie, int rank, ImportJob importJob) {
		MrDatasetAcquisition mrDatasetAcquisition = new MrDatasetAcquisition();
		Attributes dicomAttributes = null;
		try {
			// TODO ATO : should always be a dicom: add check
			dicomAttributes = dicomProcessing.getDicomObjectAttributes(serie.getFirstDatasetFileForCurrentSerie());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOG.error("Unable to retrieve dicom attributes in File " + serie.getFirstDatasetFileForCurrentSerie().getPath(),e); 
		}
		mrDatasetAcquisition.setRank(rank);
		mrDatasetAcquisition.setSortingIndex(serie.getSeriesNumber());
		mrDatasetAcquisition.setSoftwareRelease(dicomAttributes.getString(Tag.SoftwareVersions));
		
		// TODO ATO : replace 1L with equipment contained in Study Card
		mrDatasetAcquisition.setAcquisitionEquipmentId(1L);
		
	
		// TODO ATO add Compatibility check between study card Equipment and dicomEquipment if not done at front level. 
		DatasetWrapper datasetWrapper = mrDatasetStrategy.generateDatasetsForSerie(dicomAttributes, serie, importJob);
		mrDatasetAcquisition.setDatasets(datasetWrapper.getDataset());

		mrDatasetAcquisition.setMrProtocol(mrProtocolStrategy.generateMrProtocolForSerie(dicomAttributes, serie));
		
		// total acquisition time
		if(mrDatasetAcquisition.getMrProtocol().getAcquisitionDuration()==null){
			Double totalAcquisitionTime = null;
			if (datasetWrapper.getFirstImageAcquisitionTime() != null && datasetWrapper.getLastImageAcquisitionTime() != null) {
				totalAcquisitionTime = new Double(datasetWrapper.getLastImageAcquisitionTime().getTime() - datasetWrapper.getFirstImageAcquisitionTime().getTime());
				mrDatasetAcquisition.getMrProtocol().setAcquisitionDuration(totalAcquisitionTime);
			} else {
				mrDatasetAcquisition.getMrProtocol().setAcquisitionDuration(null);
			}
		}
		
		// building EchoTime, InversionTime, FlipAngle, RepetitionTime for MrProcotol based on dataset
		Map<Integer,EchoTime> echoTimes = new HashMap<>(); 
		Map<Double,FlipAngle> flipAngles = new HashMap<>();  
		Map<Double,InversionTime> inversionTimes = new HashMap<>();
		Map<Double,RepetitionTime> repetitionTimes = new HashMap<>();
		
		for (Object dataset : datasetWrapper.getDataset()) {
			MrDataset mrDataset = (MrDataset) dataset;
			echoTimes.putAll(mrDataset.getEchoTimes());
			flipAngles.putAll(mrDataset.getFlipAngles());
			inversionTimes.putAll(mrDataset.getInversionTimes());
			repetitionTimes.putAll(mrDataset.getRepetitionTimes());

		}
		mrDatasetAcquisition.getMrProtocol().setEchoTimes(new ArrayList<EchoTime>(echoTimes.values()));
		mrDatasetAcquisition.getMrProtocol().setRepetitionTimeList(new ArrayList<RepetitionTime>(repetitionTimes.values()));
		mrDatasetAcquisition.getMrProtocol().setFlipAngles(new ArrayList<FlipAngle>(flipAngles.values()));
		mrDatasetAcquisition.getMrProtocol().setInversionTimeList(new ArrayList<InversionTime>(inversionTimes.values()));
		
		// TODO ATO add persistence.
		
//		try {
//			datasetAcquisitionServiceImpl.save(datasetAcquisition);
//		} catch (ShanoirException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		return mrDatasetAcquisition;
	}
	
	

}
