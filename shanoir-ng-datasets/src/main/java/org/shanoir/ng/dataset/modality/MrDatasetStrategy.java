package org.shanoir.ng.dataset.modality;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.shanoir.ng.dataset.CardinalityOfRelatedSubjects;
import org.shanoir.ng.dataset.DatasetExpression;
import org.shanoir.ng.dataset.DatasetExpressionFormat;
import org.shanoir.ng.dataset.ProcessedDatasetType;
import org.shanoir.ng.datasetacquisition.mr.MrProtocol;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.dicom.DicomProcessing;
import org.shanoir.ng.importer.dto.Dataset;
import org.shanoir.ng.importer.dto.ImportJob;
import org.shanoir.ng.importer.dto.Serie;
import org.shanoir.ng.shared.model.EchoTime;
import org.shanoir.ng.shared.model.FlipAngle;
import org.shanoir.ng.shared.model.InversionTime;
import org.shanoir.ng.shared.model.RepetitionTime;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

public class MrDatasetStrategy {

	/** Logger. */
	private static final Logger LOG = LoggerFactory.getLogger(MrDatasetStrategy.class);

	@Autowired
	DicomProcessing dicomProcessing;

	@Value("${backup.pacs.web.port}")
	private String backupPacsWebPort;

	@Value("${backup.dicom.server.host}")
	private String backupDicomServerHost;

	/***
	 * 1st step : create the datasets we expect for the current serie and link them
	 * with the mr protocol ?? TODO ATO : Verify above line
	 *
	 * 2nd step : If these datasets have been sent to a pacs, create the associated
	 * dataset expression
	 * 
	 * 
	 ***/

	public List<org.shanoir.ng.dataset.Dataset> generateMrDatasetsForSerie(Attributes dicomAttributes, Serie serie,
			ImportJob importJob, MrProtocol mrProtocol) {

		List<org.shanoir.ng.dataset.Dataset> datasets = new ArrayList<org.shanoir.ng.dataset.Dataset>();

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

			/*
			 * First acquisition time of the dicom media.
			 */
			Date firstImageAcquisitionTime = null;

			/*
			 * Last acquisition time of the dicom media.
			 */
			Date lastImageAcquisitionTime = null;

			MrDataset mrDataset = generateSingleMrDataset(dicomAttributes, serie, dataset, datasetIndex, importJob);
			datasets.add(mrDataset);
//			generateSingleDatasetExpression();
			datasetIndex++;
		}
		return datasets;

	}

	public MrDataset generateSingleMrDataset(Attributes dicomAttributes, Serie serie, Dataset dataset, int datasetIndex,
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

		LOG.debug("createSingleDataset : End, return mrDataset=" + mrDataset);
		return mrDataset;
	}

//	public MrDataset generateSingleDatasetExpression(Serie serie, Dataset dataset, MrDataset mrDataset,
//			ImportJob importJob, MrProtocol mrProtocol) {
//		Date firstImageAcquisitionTime = null;
//		Date lastImageAcquisitionTime = null;
//		DatasetExpression datasetExpressionToPacs = new DatasetExpression();
//		datasetExpressionToPacs.setCreationDate(LocalDate.now());
//		datasetExpressionToPacs.setDatasetExpressionFormat(DatasetExpressionFormat.DICOM);
//		Attributes firstDatasetImageAttribute;
//		boolean firstDatesetImage = true;
//
//		if (serie.getIsMultiFrame()) {
//			datasetExpressionToPacs.setMultiFrame(true);
//			datasetExpressionToPacs.setFrameCount(new Integer(serie.getFrameCount()));
//		}
//
//		for (Image image : dataset) {
//			Date contentTime = null;
//			Date acquisitionTime = null;
//			Attributes dicomAttributes = null;
//			dicomAttributes = dicomProcessing.getDicomObjectAttributes(image);
//			if (firstDatesetImage) {
//				firstDatasetImageAttribute = dicomAttributes;
//				firstDatesetImage = false;
//			}
//			DatasetFile pacsDatasetFile = new DatasetFile();
//			pacsDatasetFile.setIsPacs(true);
//			final String sOPInstanceUID = dicomAttributes.getString(Tag.SOPInstanceUID);
//			final String studyInstanceUID = dicomAttributes.getString(Tag.StudyInstanceUID);
//			final String seriesInstanceUID = dicomAttributes.getString(Tag.SeriesInstanceUID);
//			String wadoRequest = "http://" + backupDicomServerHost + ":" + backupPacsWebPort
//					+ "/wado?requestType=WADO&studyUID=" + studyInstanceUID + "&seriesUID=" + seriesInstanceUID
//					+ "&objectUID=" + sOPInstanceUID;
//			// set return type as application/dicom instead of
//			// the standard image/jpeg
//			wadoRequest += "&contentType=application/dicom";
//
//			pacsDatasetFile.setPath(wadoRequest);
//
//			datasetExpressionToPacs.getDatasetFiles().add(pacsDatasetFile);
//			pacsDatasetFile.setDatasetExpression(datasetExpressionToPacs);
//
//			// calculate the acquisition duration for this acquisition
//			acquisitionTime = dicomAttributes.getDate(Tag.AcquisitionTime);
//			contentTime = dicomAttributes.getDate(Tag.ContentTime);
//			if (acquisitionTime != null) {
//				if (lastImageAcquisitionTime == null) {
//					lastImageAcquisitionTime = acquisitionTime;
//				}
//				if (firstImageAcquisitionTime == null) {
//					firstImageAcquisitionTime = acquisitionTime;
//				}
//				if (acquisitionTime.after(lastImageAcquisitionTime)) {
//					lastImageAcquisitionTime = acquisitionTime;
//				} else if (acquisitionTime.before(firstImageAcquisitionTime)) {
//					firstImageAcquisitionTime = acquisitionTime;
//				}
//			}
//			if (contentTime != null) {
//				if (lastImageAcquisitionTime == null) {
//					lastImageAcquisitionTime = contentTime;
//				}
//				if (firstImageAcquisitionTime == null) {
//					firstImageAcquisitionTime = contentTime;
//				}
//				if (contentTime.after(lastImageAcquisitionTime)) {
//					lastImageAcquisitionTime = contentTime;
//				} else if (contentTime.before(firstImageAcquisitionTime)) {
//					firstImageAcquisitionTime = contentTime;
//				}
//			}
//
//		}
//		datasetExpressionToPacs.setDataset(mrDataset);
//		mrDataset.getDatasetExpressions().add(datasetExpressionToPacs);
//
//		final Double inversionTime = firstDatasetImageAttribute.getDouble(Tag.InversionTime, -1D);
//		final Double repetitionTime = firstDatasetImageAttribute.getDouble(Tag.RepetitionTime, -1D);
//		final Double echoTime = firstDatasetImageAttribute.getDouble(Tag.EchoTime, -1D);
//		final Double flipAngle = firstDatasetImageAttribute.getDouble(Tag.FlipAngle, -1D);
//
//		EchoTime echoTimeClone = null;
//		for (final EchoTime echoTimeObject : mrProtocol.getEchoTimes()) {
//			if (echoTimeObject.getEchoTimeValue().doubleValue() == echoTime) {
//				echoTimeClone = echoTimeObject;
//				break;
//			}
//		}
//		if (echoTimeClone != null) {
//			mrProtocol.getEchoTimes().remove(echoTimeClone);
//			echoTimeClone = (EchoTime) echoTimeClone.clone();
//			mrDataset.setEchoTime(echoTimeClone);
//			mrProtocol.getEchoTimes().add(echoTimeClone);
//		}
//
//		RepetitionTime repetitionTimeClone = null;
//		for (final RepetitionTime repetitionTimeObject : mrProtocol.getRepetitionTimeList()) {
//			if (repetitionTimeObject.getRepetitionTimeValue().doubleValue() == repetitionTime) {
//				repetitionTimeClone = repetitionTimeObject;
//				break;
//			}
//		}
//		if (repetitionTimeClone != null) {
//			mrProtocol.getRepetitionTimeList().remove(repetitionTimeClone);
//			repetitionTimeClone = repetitionTimeClone.clone();
//			mrDataset.setRepetitionTime(repetitionTimeClone);
//			mrProtocol.getRepetitionTimeList().add(repetitionTimeClone);
//		}
//
//		FlipAngle flipAngleClone = null;
//		for (final FlipAngle flipAngleObject : mrProtocol.getFlipAngleList()) {
//			if (flipAngleObject.getFlipAngleValue().doubleValue() == flipAngle) {
//				flipAngleClone = flipAngleObject;
//			}
//		}
//		if (flipAngleClone != null) {
//			mrProtocol.getFlipAngleList().remove(flipAngleClone);
//			flipAngleClone = flipAngleClone.clone();
//			mrDataset.setFlipAngle(flipAngleClone);
//			mrProtocol.getFlipAngleList().add(flipAngleClone);
//		}
//
//		InversionTime inversionTimeClone = null;
//		for (final InversionTime inversionTimeObject : mrProtocol.getInversionTimeList()) {
//			if (inversionTimeObject.getInversionTimeValue().doubleValue() == inversionTime) {
//				mrDataset.setInversionTime(inversionTimeObject);
//			}
//		}
//		if (inversionTimeClone != null) {
//			mrProtocol.getInversionTimeList().remove(inversionTimeClone);
//			inversionTimeClone = inversionTimeClone.clone();
//			mrDataset.setInversionTime(inversionTimeClone);
//			mrProtocol.getInversionTimeList().add(inversionTimeClone);
//		}
//
//	}catch(
//
//	final IOException exc)
//	{
//        log.error("createMrDataset : ", exc);
//    }
//
//	// NON-IMAGES DATASETS & Spectroscopy
//	if(serie!=null&&(!serie.getNonImagesPathList().isEmpty()||dicomImporter.getMetadataExtractor().isSpectroscopy(index)))
//	{
//		final List<DatasetFile> nonImageDatasetFileList = new ArrayList<DatasetFile>();
//		for (final File nonImageFile : dicomFiles) {
//			log.debug("createMrDataset : adding the file " + nonImageFile.getName());
//			final DatasetFile nonImageDatasetFile = new DatasetFile();
//			nonImageDatasetFile.setPath(nonImageFile.toURI().toString().replaceAll(" ", "%20"));
//			nonImageDatasetFileList.add(nonImageDatasetFile);
//		}
//		if (!nonImageDatasetFileList.isEmpty()) {
//			// Create a MR dataset with some fields set by
//			// algorithms
//
//			final DatasetExpression datasetExpressionNonImage = createDatasetExpression(mrDataset,
//					datasetExpressionToPacs, null);
//			datasetExpressionNonImage.setRefDatasetExpressionFormat(refDatasetExpressionFormatDicom);
//			datasetExpressionNonImage.setRefDatasetProcessing(refDatasetProcessingFormatConversion);
//
//			for (final DatasetFile datasetFile : nonImageDatasetFileList) {
//				datasetFile.setDatasetExpression(datasetExpressionNonImage);
//				datasetExpressionNonImage.getDatasetFileList().add(datasetFile);
//			}
//		}
//		log.debug("createMrDataset : nonImageDatasetFileList=" + nonImageDatasetFileList);
//	}
//
//	if(!niftiFiles.isEmpty())
//	{
//		/** 3rd step : Create the nifti dataset expressions */
//		log.debug("createMrDataset : step 3 :  Create the nifti dataset expressions");
//		final DatasetExpression datasetExpressionNifti = createDatasetExpression(mrDataset, datasetExpressionToPacs,
//				null);
//		datasetExpressionNifti.setRefDatasetExpressionFormat(refDatasetExpressionFormatNifti);
//		datasetExpressionNifti.setRefDatasetProcessing(refDatasetProcessingFormatConversion);
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
//			if (dicomImporter.getMetadataExtractor().isConvertAs4D(index))
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
//	}
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
//	// total acquisition time
//	if(mrDatasetAcquisition.getMrProtocol().getAcquisitionDuration()==null)
//
//	{
//		Double totalAcquisitionTime = null;
//		if (firstImageAcquisitionTime != null && lastImageAcquisitionTime != null) {
//			totalAcquisitionTime = new Double(lastImageAcquisitionTime.getTime() - firstImageAcquisitionTime.getTime());
//			log.debug("createMrDataset : totalAcquisitionTime : " + totalAcquisitionTime);
//			mrDatasetAcquisition.getMrProtocol().setAcquisitionDuration(totalAcquisitionTime);
//			final IRefUnitOfMeasureHome refUnitOfMeasureHome = (IRefUnitOfMeasureHome) Component
//					.getInstance("refUnitOfMeasureHome");
//			final RefUnitOfMeasure ms = refUnitOfMeasureHome
//					.findById(Long.valueOf(ShanoirRefConstants.REF_UNIT_OF_MEASURE.MILLISECOND.ordinal() + 1));
//			mrDatasetAcquisition.getMrProtocol().setAcquisitionDurationUnitOfMeasure(ms);
//		} else {
//			mrDatasetAcquisition.getMrProtocol().setAcquisitionDuration(null);
//			log.warn(
//					"createMrDataset : cannot perform the calculation of the total acquisition time because the tag is null");
//		}
//	}
//
//	}catch(
//	final IndexOutOfBoundsException exc)
//	{
//StatusMessages.instance().add(Severity.ERROR, exc.getMessage());
//log.error("createMrDataset : error : ", exc);
//}
//
//	return null;
//	}

	public String computeDatasetName(String name, int index) {
		if (index == -1) {
			return name;
		} else {
			return name + " " + index;
		}
	}

}
