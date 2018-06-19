package org.shanoir.ng.importer.strategies.datasetexpression;

import org.shanoir.ng.dataset.DatasetExpression;
import org.shanoir.ng.dataset.DatasetExpressionFormat;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.importer.dto.Dataset;
import org.shanoir.ng.importer.dto.ExpressionFormat;
import org.shanoir.ng.importer.dto.ImportJob;
import org.shanoir.ng.importer.dto.Serie;
import org.shanoir.ng.processing.DatasetProcessingType;
import org.springframework.stereotype.Component;

@Component
public class NiftiDatasetExpressionStrategy implements DatasetExpressionStrategy {

	@Override
	public DatasetExpression generateDatasetExpression(Serie serie, ImportJob importJob,
			ExpressionFormat expressionFormat) {

//		final DatasetExpression datasetExpressionNifti = createDatasetExpression(mrDataset, datasetExpressionToPacs,
//				null);
//		datasetExpressionNifti.setDatasetExpressionFormat(DatasetExpressionFormat.NIFTI_SINGLE_FILE);
//		datasetExpressionNifti.setDatasetProcessingType(DatasetProcessingType.FORMAT_CONVERSION);
//		datasetExpressionNifti.setNiftiConverterId(serie.getNiftiConverterId()));
//		
//		/* Set the Nifti Dataset expression as the "original nifti conversion" */
//		datasetExpressionNifti.setOriginalNiftiConversion(true);
//
//		/* Get the value of the dcm2nii version setted during conversion */
//		IConvertToNifti niftiConverter = (IConvertToNifti) Component.getInstance("niftiConverter");
//		if (niftiConverter != null) {
//			/* Have to set converter to clidcm if files have been converted using clidcm */
//			boolean is4D = false;
//			/*
//			 * Load the clidcm converter because the converter can switch from dcm2nii to
//			 * clidcm in case of 4d volume sequence
//			 */
//			INiftiConverterHome niftiConverterHome = (INiftiConverterHome) Component.getInstance("niftiConverterHome");
//			NiftiConverter clidcmConverter = niftiConverterHome.findById(3L);
//
//			if (isConvertAs4D(index))
//				is4D = true;
//			if (niftiConverter.getNiftiConverter() != null) {
//				if (is4D && niftiConverter.getNiftiConverter().isClidcm(true) && clidcmConverter != null) {
//					datasetExpressionNifti.setNiftiConverter(clidcmConverter);
//				} else {
//					datasetExpressionNifti.setNiftiConverter(niftiConverter.getNiftiConverter());
//				}
//
//			}
//			if (niftiConverter.getNiftiConverterVersion() != null
//					&& !("").equals(niftiConverter.getNiftiConverterVersion())) {
//				if (is4D && niftiConverter.getNiftiConverter().isClidcm(true) && clidcmConverter != null) {
//					datasetExpressionNifti.setNiftiConverterVersion(clidcmConverter.getName());
//				} else {
//					datasetExpressionNifti.setNiftiConverterVersion(niftiConverter.getNiftiConverterVersion());
//				}
//			}
//		}
//
//		for (final File niftiFile : niftiFiles) {
//			log.debug("createMrDataset : processing the file " + niftiFile.getName());
//			final DatasetFile niftiDatasetFile = new DatasetFile();
//			niftiDatasetFile.setPath(niftiFile.toURI().toString().replaceAll(" ", "%20"));
//			datasetExpressionNifti.getDatasetFileList().add(niftiDatasetFile);
//			niftiDatasetFile.setDatasetExpression(datasetExpressionNifti);
//		}
//
//		// if necessary, rename the bvec and bval files (for DTI)
//		renameBvecBval(datasetExpressionNifti);
//
//		/*
//		 * if there was some DTI images, then we can now use the bvec and bval files to
//		 * create the diffusion gradients and add them to the MR Protocol. Indeed, it is
//		 * more likely to do so now because extracting the diffusion gradients from the
//		 * dicom files is tricky.
//		 */
//		extractDiffusionGradients(mrDatasetAcquisition.getMrProtocol(), mrDataset, datasetExpressionNifti);
		return null;
	}

//	/**
//	 * Check for the given property if the dicom value matches the value in the
//	 * configuration file.
//	 *
//	 * @param serieNumber
//	 *            the serie number
//	 * @param property
//	 *            the property
//	 *
//	 * @return true, if check dicom from properties
//	 */
//	private boolean checkDicomFromProperties(final int serieNumber, final String property) {
//		boolean result = false;
//		final String[] seriesDescriptionForDiffusion = ShanoirConfig.getPropertyAsArray(property);
//		for (final String item : seriesDescriptionForDiffusion) {
//			int tag = Tag.toTag(item.split("==")[0]);
//			String value = item.split("==")[1];
//			log.debug("checkDicomFromProperties : tag=" + tag + ", value=" + value);
//			final String dicomValue = getString(serieNumber, tag);
//			String wildcard = ShanoirUtil.wildcardToRegex(value);
//			if (dicomValue != null && dicomValue.matches(wildcard)) {
//				result = true;
//				break;
//			}
//		}
//		log.debug("checkDicomFromProperties : End, return " + result);
//		return result;
//	}
//	
//	/**
//	 * Return the value of the given property key as an array of Strings. The value in the property file must be separated by a comma.
//	 * @param key
//	 *            key
//	 * @return
//	 */
//	public static String[] getPropertyAsArray(final String key) {
//		String property = getProperty(key);
//		String[] result = null;
//		if (property != null) {
//			result = property.split(";");
//		}
//		return result;
//	}
//	
//	/*
//	 * (non-Javadoc)
//	 *
//	 * @see
//	 * org.shanoir.dicom.extractor.IMetadataExtractor#isConvertAs4D
//	 * (int)
//	 */
//	public boolean isConvertAs4D(final int serieNumber) {
//		return checkDicomFromProperties(serieNumber, "convertAs4D");
//	}

}
