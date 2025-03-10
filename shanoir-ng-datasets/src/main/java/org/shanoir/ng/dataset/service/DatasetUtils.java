package org.shanoir.ng.dataset.service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.shanoir.ng.dataset.modality.*;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.dataset.model.DatasetMetadata;
import org.shanoir.ng.dataset.model.DatasetModalityType;
import org.shanoir.ng.dataset.model.DatasetType;
import org.shanoir.ng.datasetfile.DatasetFile;

public class DatasetUtils {

	/**
	 * Reads all dataset files depending on the format attached to one dataset.
	 * @param dataset
	 * @param pathURLs
	 * @throws MalformedURLException
	 */
	public static void getDatasetFilePathURLs(final Dataset dataset, List<URL> pathURLs, DatasetExpressionFormat format) throws MalformedURLException {
		List<DatasetExpression> datasetExpressions = dataset.getDatasetExpressions();
		for (Iterator<DatasetExpression> itExpressions = datasetExpressions.iterator(); itExpressions.hasNext();) {
			DatasetExpression datasetExpression = (DatasetExpression) itExpressions.next();
			if (datasetExpression.getDatasetExpressionFormat().equals(format)) {
				List<DatasetFile> datasetFiles = datasetExpression.getDatasetFiles();
				for (Iterator<DatasetFile> itFiles = datasetFiles.iterator(); itFiles.hasNext();) {
					DatasetFile datasetFile = (DatasetFile) itFiles.next();
					URL url = new URL(datasetFile.getPath().replace("%20", " "));
					pathURLs.add(url);
				}
			}
		}
	}
	
	
	public static Dataset buildDatasetFromType(String type) {
		
		DatasetMetadata originMetadata = new DatasetMetadata();
		Dataset dataset = null;
			
		switch(type) {
			case DatasetType.Names.Generic:
				dataset = new CalibrationDataset();
				originMetadata.setDatasetModalityType(DatasetModalityType.GENERIC_DATASET);
				break;
			case DatasetType.Names.Ct:
				dataset = new CtDataset();
				originMetadata.setDatasetModalityType(DatasetModalityType.CT_DATASET);
				break;
			case DatasetType.Names.Eeg:
				dataset = new EegDataset();
				originMetadata.setDatasetModalityType(DatasetModalityType.EEG_DATASET);
				break;
			case DatasetType.Names.Meg:
				dataset = new MegDataset();
				originMetadata.setDatasetModalityType(DatasetModalityType.EEG_DATASET);
				break;
			case DatasetType.Names.Mesh:
				dataset = new MeshDataset();
				originMetadata.setDatasetModalityType(DatasetModalityType.GENERIC_DATASET);
				break;
			case DatasetType.Names.Mr:
				dataset = new MrDataset();
				originMetadata.setDatasetModalityType(DatasetModalityType.MR_DATASET);
				break;
			case DatasetType.Names.ParameterQuantification:
				dataset = new ParameterQuantificationDataset();
				originMetadata.setDatasetModalityType(DatasetModalityType.GENERIC_DATASET);
				break;
			case DatasetType.Names.Pet:
				dataset = new PetDataset();
				originMetadata.setDatasetModalityType(DatasetModalityType.PET_DATASET);
				break;
			case DatasetType.Names.Registration:
				dataset = new RegistrationDataset();
				originMetadata.setDatasetModalityType(DatasetModalityType.GENERIC_DATASET);
				break;
			case DatasetType.Names.Segmentation:
				dataset = new SegmentationDataset();
				originMetadata.setDatasetModalityType(DatasetModalityType.GENERIC_DATASET);
				break;
			case DatasetType.Names.Spect:
				dataset = new SpectDataset();
				originMetadata.setDatasetModalityType(DatasetModalityType.SPECT_DATASET);
				break;
			case DatasetType.Names.Statistical:
				dataset = new StatisticalDataset();
				originMetadata.setDatasetModalityType(DatasetModalityType.GENERIC_DATASET);
				break;
			case DatasetType.Names.Template:
				dataset = new TemplateDataset();
				originMetadata.setDatasetModalityType(DatasetModalityType.GENERIC_DATASET);
				break;
			case DatasetType.Names.BIDS:
				dataset = new BidsDataset();
				originMetadata.setDatasetModalityType(DatasetModalityType.MR_DATASET);
				break;
			case DatasetType.Names.Xa:
				dataset = new XaDataset();
				originMetadata.setDatasetModalityType(DatasetModalityType.XA_DATASET);
				break;
			default:
				dataset = new GenericDataset();
				originMetadata.setDatasetModalityType(DatasetModalityType.GENERIC_DATASET);
				break;
		}
		dataset.setOriginMetadata(originMetadata);
		return dataset;
	}

	public static Dataset copyDatasetFromDataset(Dataset d) {
		DatasetType type = d.getType();
	
		return switch (type) {
			case Calibration -> new CalibrationDataset(d);
			case Ct -> new CtDataset(d);
			case Eeg -> new EegDataset(d);
			case Meg -> new MegDataset(d);
			case Mesh -> new MeshDataset(d);
			case ParameterQuantification -> new ParameterQuantificationDataset(d);
			case Pet -> new PetDataset(d);
			case Registration -> new RegistrationDataset(d);
			case Segmentation -> new SegmentationDataset(d);
			case Spect -> new SpectDataset(d);
			case Statistical -> new StatisticalDataset(d);
			case Template -> new TemplateDataset(d);
			case BIDS -> new BidsDataset(d);
			case Xa -> new XaDataset(d);
			default -> new GenericDataset(d);
		};
	}
	
}
