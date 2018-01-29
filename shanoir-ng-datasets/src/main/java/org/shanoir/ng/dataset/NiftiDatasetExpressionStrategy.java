package org.shanoir.ng.dataset;

import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.importer.dto.Dataset;
import org.shanoir.ng.importer.dto.ExpressionFormat;
import org.shanoir.ng.importer.dto.ImportJob;
import org.shanoir.ng.importer.dto.Serie;

public class NiftiDatasetExpressionStrategy implements DatasetExpressionStrategy {

	@Override
	public DatasetExpression generateDatasetExpression(Serie serie, Dataset dataset, MrDataset mrDataset,
			ImportJob importJob, ExpressionFormat expressionFormat) {

////		if(!niftiFiles.isEmpty())
////		{
////			/** 3rd step : Create the nifti dataset expressions */
////			log.debug("createMrDataset : step 3 :  Create the nifti dataset expressions");
////			final DatasetExpression datasetExpressionNifti = createDatasetExpression(mrDataset, datasetExpressionToPacs,
////					null);
////			datasetExpressionNifti.setRefDatasetExpressionFormat(refDatasetExpressionFormatNifti);
////			datasetExpressionNifti.setRefDatasetProcessing(refDatasetProcessingFormatConversion);
////			/* Set the Nifti Dataset expression as the "original nifti conversion" */
////			datasetExpressionNifti.setOriginalNiftiConversion(true);
////
////			/* Get the value of the dcm2nii version setted during conversion */
////			IConvertToNifti niftiConverter = (IConvertToNifti) Component.getInstance("niftiConverter");
////			if (niftiConverter != null) {
////				/* Have to set converter to clidcm if files have been converted using clidcm */
////				boolean is4D = false;
////				/*
////				 * Load the clidcm converter because the converter can switch from dcm2nii to
////				 * clidcm in case of 4d volume sequence
////				 */
////				INiftiConverterHome niftiConverterHome = (INiftiConverterHome) Component.getInstance("niftiConverterHome");
////				NiftiConverter clidcmConverter = niftiConverterHome.findById(3L);
////
////				if (dicomImporter.getMetadataExtractor().isConvertAs4D(index))
////					is4D = true;
////				if (niftiConverter.getNiftiConverter() != null) {
////					if (is4D && niftiConverter.getNiftiConverter().isClidcm(true) && clidcmConverter != null) {
////						datasetExpressionNifti.setNiftiConverter(clidcmConverter);
////					} else {
////						datasetExpressionNifti.setNiftiConverter(niftiConverter.getNiftiConverter());
////					}
////
////				}
////				if (niftiConverter.getNiftiConverterVersion() != null
////						&& !("").equals(niftiConverter.getNiftiConverterVersion())) {
////					if (is4D && niftiConverter.getNiftiConverter().isClidcm(true) && clidcmConverter != null) {
////						datasetExpressionNifti.setNiftiConverterVersion(clidcmConverter.getName());
////					} else {
////						datasetExpressionNifti.setNiftiConverterVersion(niftiConverter.getNiftiConverterVersion());
////					}
////				}
////			}
////
////			for (final File niftiFile : niftiFiles) {
////				log.debug("createMrDataset : processing the file " + niftiFile.getName());
////				final DatasetFile niftiDatasetFile = new DatasetFile();
////				niftiDatasetFile.setPath(niftiFile.toURI().toString().replaceAll(" ", "%20"));
////				datasetExpressionNifti.getDatasetFileList().add(niftiDatasetFile);
////				niftiDatasetFile.setDatasetExpression(datasetExpressionNifti);
////			}
////
////			// if necessary, rename the bvec and bval files (for DTI)
////			renameBvecBval(datasetExpressionNifti);
////
////			/*
////			 * if there was some DTI images, then we can now use the bvec and bval files to
////			 * create the diffusion gradients and add them to the MR Protocol. Indeed, it is
////			 * more likely to do so now because extracting the diffusion gradients from the
////			 * dicom files is tricky.
////			 */
////			extractDiffusionGradients(mrDatasetAcquisition.getMrProtocol(), mrDataset, datasetExpressionNifti);
////		}
//	}
		return null;
	}
}

