package org.shanoir.ng.importer.strategies.dataset;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.shanoir.ng.dataset.CardinalityOfRelatedSubjects;
import org.shanoir.ng.dataset.DatasetExpression;
import org.shanoir.ng.dataset.ProcessedDatasetType;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dicom.DicomProcessing;
import org.shanoir.ng.importer.dto.Dataset;
import org.shanoir.ng.importer.dto.DatasetWrapper;
import org.shanoir.ng.importer.dto.ExpressionFormat;
import org.shanoir.ng.importer.dto.ImportJob;
import org.shanoir.ng.importer.dto.Serie;
import org.shanoir.ng.importer.strategies.datasetexpression.DatasetExpressionFactory;
import org.shanoir.ng.importer.strategies.datasetexpression.DatasetExpressionStrategy;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MrDatasetStrategy<T> implements DatasetStrategy {

	/** Logger. */
	private static final Logger LOG = LoggerFactory.getLogger(MrDatasetStrategy.class);

	@Autowired
	DicomProcessing dicomProcessing;

	@Override
	public DatasetWrapper<MrDataset> generateDatasetsForSerie(Attributes dicomAttributes, Serie serie,
			ImportJob importJob) {
		
		DatasetWrapper<MrDataset> datasetWrapper = new DatasetWrapper<MrDataset>();

		/**
		 * retrieve number of dataset in current serie if Number of dataset > 1 then
		 * each dataset will be named with an int at the end of the name. else the is
		 * only one dataset => no need for extension.
		 */

		int datasetIndex;
		if (serie.getDatasets().size() > 1) {
			datasetIndex = 1;
		} else {
			datasetIndex = -1;
		}

		// TODO ATO : implement MrDAtasetAcquisitionHome.createMrDataset (issue by
		// createMrDatasetAcquisitionFromDicom()
		for (Dataset dataset : serie.getDatasets()) {
			// TODO ATO : implement line 350 - 372 MrDAtasetAcquisitionHome.createMrDataset
			MrDataset mrDataset = new MrDataset();
			mrDataset = (MrDataset) generateSingleDataset(dicomAttributes, serie, dataset, datasetIndex, importJob);
			if (mrDataset.getFirstImageAcquisitionTime() != null) {
				if (datasetWrapper.getFirstImageAcquisitionTime() == null) {
					datasetWrapper.setFirstImageAcquisitionTime(mrDataset.getFirstImageAcquisitionTime());
				} else {
					if (datasetWrapper.getFirstImageAcquisitionTime().after(mrDataset.getFirstImageAcquisitionTime())) {
						datasetWrapper.setFirstImageAcquisitionTime(mrDataset.getFirstImageAcquisitionTime());
					}
				}
			}
			if (mrDataset.getLastImageAcquisitionTime() != null) {
				if (datasetWrapper.getLastImageAcquisitionTime() == null) {
					datasetWrapper.setLastImageAcquisitionTime(mrDataset.getLastImageAcquisitionTime());
				} else {
					if (datasetWrapper.getLastImageAcquisitionTime().after(mrDataset.getLastImageAcquisitionTime())) {
						datasetWrapper.setLastImageAcquisitionTime(mrDataset.getLastImageAcquisitionTime());
					}
				}
			}

			datasetWrapper.getDataset().add(mrDataset);

			datasetIndex++;
		}

		return datasetWrapper;

	}

	/* (non-Javadoc)
	 * @see org.shanoir.ng.dataset.modality.DatasetStrategy#generateSingleMrDataset(org.dcm4che3.data.Attributes, org.shanoir.ng.importer.dto.Serie, org.shanoir.ng.importer.dto.Dataset, int, org.shanoir.ng.importer.dto.ImportJob)
	 */
	@Override
	public MrDataset generateSingleDataset(Attributes dicomAttributes, Serie serie, Dataset dataset, int datasetIndex,
			ImportJob importJob) {
		MrDataset mrDataset = new MrDataset();

		mrDataset.setCreationDate(Utils.DateToLocalDate(dicomAttributes.getDate(Tag.SeriesDate)));

		final String serieDescription = dicomAttributes.getString(Tag.SeriesDescription);
		final String modality = dicomAttributes.getString(Tag.Modality);

		// set the series description as the dataset comment & name
		if (serieDescription != null && !"".equals(serieDescription)) {
			mrDataset.getOriginMetadata().setName(computeDatasetName(serieDescription, datasetIndex));
			mrDataset.getOriginMetadata().setComment(serieDescription);
		}

		// Pre-select the type Reconstructed dataset
		mrDataset.getOriginMetadata().setProcessedDatasetType(ProcessedDatasetType.RECONSTRUCTEDDATASET);

		// TODO ATO : implement when import details are ready.. (json sent by michael to
		// me)

		// Set the study and the subject
		mrDataset.setSubjectId(importJob.getPatients().get(0).getFrontSubjectId());
		mrDataset.setGroupOfSubjectsId(importJob.getPatients().get(0).getFrontExperimentalGroupOfSubjectId());
		mrDataset.setStudyId(importJob.getFrontStudyId());

		// Set the modality from dicom fields
		// TODO ATO :VERIFY NOT NEEDED ANY MORE
		// mrDataset.getOriginMetadata().setDatasetModalityType(DatasetModalityType.MR_DATASET);

		CardinalityOfRelatedSubjects refCardinalityOfRelatedSubjects = null;
		if (mrDataset.getSubjectId() != null) {
			refCardinalityOfRelatedSubjects = CardinalityOfRelatedSubjects.SINGLE_SUBJECT_DATASET;
		} else {
			refCardinalityOfRelatedSubjects = CardinalityOfRelatedSubjects.MULTIPLE_SUBJECTS_DATASET;
		}
		mrDataset.getOriginMetadata().setCardinalityOfRelatedSubjects(refCardinalityOfRelatedSubjects);
		
		/**
		 *  The part below will generate automatically the datasetExpression according to :
		 *   -  type found in the importJob.serie.datasets.dataset.expressionFormat.type
		 *   
		 *  The DatasetExpressionFactory will return the proper object according to the expression format type and add it to the current mrDataset
		 * 
		 **/
		for (ExpressionFormat expressionFormat : dataset.getExpressionFormats()) {
			DatasetExpressionStrategy datasetExpressionStrategy = DatasetExpressionFactory.getDatasetExpressionStrategy(expressionFormat.getType());
			DatasetExpression datasetExpression = datasetExpressionStrategy.generateDatasetExpression(serie,importJob,expressionFormat);
			
			

			mrDataset.getEchoTimes().putAll(datasetExpression.getEchoTimes());
			mrDataset.getRepetitionTimes().putAll(datasetExpression.getRepetitionTimes());
			mrDataset.getFlipAngles().putAll(datasetExpression.getFlipAngles());
			mrDataset.getInversionTimes().putAll(datasetExpression.getInversionTimes());
			
			
			if (datasetExpression.getFirstImageAcquisitionTime() != null) {
				if (mrDataset.getFirstImageAcquisitionTime() == null) {
					mrDataset.setFirstImageAcquisitionTime(datasetExpression.getFirstImageAcquisitionTime());
				} else {
					if (mrDataset.getFirstImageAcquisitionTime().after(datasetExpression.getFirstImageAcquisitionTime())) {
						mrDataset.setFirstImageAcquisitionTime(datasetExpression.getFirstImageAcquisitionTime());
					}
				}
			}
			if (datasetExpression.getLastImageAcquisitionTime() != null) {
				if (mrDataset.getLastImageAcquisitionTime() == null) {
					mrDataset.setLastImageAcquisitionTime(datasetExpression.getLastImageAcquisitionTime());
				} else {
					if (mrDataset.getLastImageAcquisitionTime().after(datasetExpression.getLastImageAcquisitionTime())) {
						mrDataset.setLastImageAcquisitionTime(datasetExpression.getLastImageAcquisitionTime());
					}
				}
			}
			
			mrDataset.getDatasetExpressions().add(datasetExpression);
			
		}
		return mrDataset;
	}


//
//
//	/* ---- Fields set by the studyCard ---- */
//	final IMetadataExtractor metadataExtractor = dicomImporter.getMetadataExtractor();
//	HashMap<Integer, Object> tagMap;if(dicomFiles==null||dicomFiles.isEmpty())
//	{
//		tagMap = (HashMap<Integer, Object>) metadataExtractor.getValue(ShanoirConstants.DICOM_RETURNED_TYPES.STRING,
//				mrDatasetAcquisition.getRank(), studyCard.getDicomTagArray());
//	}else
//	{
//		tagMap = (HashMap<Integer, Object>) metadataExtractor.getValue(ShanoirConstants.DICOM_RETURNED_TYPES.STRING,
//				studyCard.getDicomTagArray(), dicomFiles.get(0));
//	}
//
//	setFieldsByStudyCard(mrDataset, studyCard, tagMap);
//
//}
//

//
//	return null;
//	}
	
	

	/* (non-Javadoc)
	 * @see org.shanoir.ng.dataset.modality.DatasetStrategy#computeDatasetName(java.lang.String, int)
	 */
	@Override
	public String computeDatasetName(String name, int index) {
		if (index == -1) {
			return name;
		} else {
			return name + " " + index;
		}
	}

}
