package org.shanoir.ng.datasetacquisition.mr;

import java.io.IOException;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.shanoir.ng.dataset.modality.MrDatasetStrategy;
import org.shanoir.ng.datasetacquisition.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.DatasetAcquisitionStrategy;
import org.shanoir.ng.dicom.DicomProcessing;
import org.shanoir.ng.examination.Examination;
import org.shanoir.ng.importer.dto.ImportJob;
import org.shanoir.ng.importer.dto.Serie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class MrDatasetAcquisitionStrategy implements DatasetAcquisitionStrategy {
	
	/** Logger. */
	private static final Logger LOG = LoggerFactory.getLogger(MrDatasetAcquisitionStrategy.class);
	
	@Autowired
	MrDatasetAcquisition mrDatasetAcquisition;

	@Autowired
	DicomProcessing dicomProcessing;
	
	@Autowired
	MrProtocolStrategy mrProtocolStrategy;
	
	@Autowired
	MrDatasetStrategy mrDatasetStrategy;
	
	@Override
	public DatasetAcquisition generateDatasetAcquisitionForSerie(Serie serie, int rank, Examination examination,ImportJob importJob) {
		Attributes dicomAttributes = null;
		try {
			// TODO ATO : should always be a dicom: add check
			dicomAttributes = dicomProcessing.getDicomObjectAttributes(serie.getFirstDatasetFileForCurrentSerie());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOG.error("Unable to retrieve dicom attributes in File " + serie.getFirstDatasetFileForCurrentSerie().getPath(),e); 
		}
		mrDatasetAcquisition.setExamination(examination);
		mrDatasetAcquisition.setRank(rank);
		mrDatasetAcquisition.setSortingIndex(serie.getSeriesNumber());
		mrDatasetAcquisition.setSoftwareRelease(dicomAttributes.getString(Tag.SoftwareVersions));
		
		// TODO ATO : replace 1L with equipment contained in Study Card
		mrDatasetAcquisition.setAcquisitionEquipmentId(1L);
		
		
		
//		datasetAcquisition.setDatasets(datasets);
		mrDatasetAcquisition.setMrProtocol(mrProtocolStrategy.generateMrProtocolForSerie(dicomAttributes, serie));
		
		// TODO ATO add Compatibility check between study card Equipment and dicomEquipment if not done at front level. 
		
		
		mrDatasetAcquisition.setDatasets(mrDatasetStrategy.generateMrDatasetsForSerie(dicomAttributes, serie, importJob, mrDatasetAcquisition.getMrProtocol()));

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
