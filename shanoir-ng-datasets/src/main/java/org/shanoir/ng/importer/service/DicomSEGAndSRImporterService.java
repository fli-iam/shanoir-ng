package org.shanoir.ng.importer.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.solr.client.solrj.SolrServerException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.shanoir.ng.dataset.modality.MeasurementDataset;
import org.shanoir.ng.dataset.modality.SegmentationDataset;
import org.shanoir.ng.dataset.model.CardinalityOfRelatedSubjects;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.dataset.model.DatasetMetadata;
import org.shanoir.ng.dataset.model.DatasetModalityType;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.ct.CtDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.MrDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.pet.PetDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.xa.XaDatasetAcquisition;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.dicom.web.StudyInstanceUIDHandler;
import org.shanoir.ng.dicom.web.service.DICOMWebService;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.shanoir.ng.shared.model.Subject;
import org.shanoir.ng.shared.repository.SubjectRepository;
import org.shanoir.ng.solr.service.SolrService;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * This class imports the measurements of the ohif-viewer, that are
 * send as DICOM SR Structured Report. It modifies the by the OHIF
 * viewer created DICOM SR to correspond to shanoir needs, creates
 * the dataset in the database and sends the dicom file to the pacs.
 *
 * The import only happens in the servers memory, as the structured
 * reports are very small memory footprint objects and as this avoids
 * us any implication of MS Import in this special case.
 *
 * @author mkain
 *
 */
@Service
public class DicomSEGAndSRImporterService {

	private static final Logger LOG = LoggerFactory.getLogger(DicomSEGAndSRImporterService.class);

	private static final String IMAGING_MEASUREMENT_REPORT = "Imaging Measurement Report";

	private static final String SEG = "SEG";

	private static final String SR = "SR";

	@Autowired
	private ExaminationRepository examinationRepository;

	@Autowired
    private SolrService solrService;

	@Autowired
	private DatasetService datasetService;

	@Autowired
	private SubjectRepository subjectRepository;

	@Autowired
	private DICOMWebService dicomWebService;

	@Autowired
	private StudyInstanceUIDHandler studyInstanceUIDHandler;

	@Value("${dcm4chee-arc.protocol}")
	private String dcm4cheeProtocol;

	@Value("${dcm4chee-arc.host}")
	private String dcm4cheeHost;

	@Value("${dcm4chee-arc.port.web}")
	private String dcm4cheePortWeb;

	@Value("${dcm4chee-arc.dicom.web.rs}")
	private String dicomWebRS;

	@Transactional
	public boolean importDicomSEGAndSR(InputStream inputStream) {
		// DicomInputStream consumes the input stream to read the data
		try (DicomInputStream dIS = new DicomInputStream(inputStream)) {
			Attributes metaInformationAttributes = dIS.readFileMetaInformation();
			Attributes datasetAttributes = dIS.readDataset();
			String modality = datasetAttributes.getString(Tag.Modality);
			// check for modality: DICOM SEG or SR
			if (SEG.equals(modality) || SR.equals(modality)) {
				// IMPORTANT: do this before to use correct StudyInstanceUID afterwards
				Examination examination = modifyDicom(datasetAttributes);
				Dataset dataset = findDataset(examination, datasetAttributes);
				if (dataset == null) {
					LOG.error("Error: importDicomSEGAndSR: source dataset could not be found.");
					return false;
				}
				createDataset(modality, examination, dataset, datasetAttributes);
				sendToPacs(metaInformationAttributes, datasetAttributes);
			} else {
				LOG.error("Error: importDicomSEGAndSR: other modality sent then SEG or SR.");
				return false;
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return false;
		}
		return true;
	}

	/**
	 * This method replaces values of dicom tags within the DICOM SR file
	 * and searches the corresponding examination and returns it:
	 * - use user name as person name, who created the measurement
	 * - replace with correct study instance UID from pacs for correct storage
	 * - add subject name according to shanoir, as viewer sends a strange P-000001.
	 *
	 * Note: the approach to replace the newly created SeriesInstanceUID
	 * with the referenced SeriesInstanceUID, available via CurrentRequested-
	 * ProcedureEvidenceSequence -> ReferencedSeriesSequence -> SeriesInstanceUID
	 * did not work to get it displayed correctly in the viewer, but lead even to
	 * an error in the viewer.
	 *
	 * @param datasetAttributes
	 */
	private Examination modifyDicom(Attributes datasetAttributes) {
		String examinationUID = datasetAttributes.getString(Tag.StudyInstanceUID);
		Long examinationID = Long.valueOf(examinationUID.substring(StudyInstanceUIDHandler.PREFIX.length()));
		Examination examination = examinationRepository.findById(examinationID).get();
		// replace artificial examinationUID with real StudyInstanceUID in DICOM server
		String studyInstanceUID = studyInstanceUIDHandler.findStudyInstanceUIDFromCacheOrDatabase(examinationUID);
		datasetAttributes.setString(Tag.StudyInstanceUID, VR.UI, studyInstanceUID);
		// replace subject name, that is sent by the viewer wrongly with P-0000001 etc.
		Optional<Subject> subjectOpt = subjectRepository.findById(examination.getSubject().getId());
		String subjectName = "error_subject_name_not_found_in_db";
		if (subjectOpt.isPresent()) {
			subjectName = subjectOpt.get().getName();
		}
		datasetAttributes.setString(Tag.PatientName, VR.PN, subjectName);
		datasetAttributes.setString(Tag.PatientID, VR.LO, subjectName);
		// set user name, as person, who created the measurement/segmentation
		final String userName = KeycloakUtil.getTokenUserName();
		datasetAttributes.setString(Tag.PersonName, VR.PN, userName);
		// set as well person observer name in content sequence
		Sequence contentSequence = datasetAttributes.getSequence(Tag.ContentSequence);
		if (contentSequence != null) {
			Attributes itemContentSequence = contentSequence.get(1);
			itemContentSequence.setString(Tag.PersonName, VR.PN, userName);
		}
		return examination;
	}

	/**
	 * A measurement dataset is related to the dataset, that has been annotated.
	 * We use the information in the DICOM SR object to find the correct dataset
	 * in shanoir database using studyInstanceUID, seriesInstanceUID and SOPInstanceUID.
	 *
	 * @param datasetAttributes
	 * @return
	 */
	private Dataset findDataset(Examination examination, Attributes datasetAttributes) {
		String studyInstanceUID = datasetAttributes.getString(Tag.StudyInstanceUID);
		String seriesInstanceUID = null;
		String sOPInstanceUID = null;
		// DICOM SR: use CurrentRequestedProcedureEvidenceSequence
		Sequence evidenceSequence = datasetAttributes.getSequence(Tag.CurrentRequestedProcedureEvidenceSequence);
		if (evidenceSequence != null) {
			Attributes itemEvidenceSequence = evidenceSequence.get(0);
			if (itemEvidenceSequence == null) {
				LOG.error("Error: missing sequences/attributes CurrentRequestedProcedureEvidenceSequence in DICOM SR.");
				return null;
			}
			Sequence seriesSequence = itemEvidenceSequence.getSequence(Tag.ReferencedSeriesSequence);
			if (seriesSequence != null) {
				Attributes itemSeriesSequence = seriesSequence.get(0);
				if (itemEvidenceSequence != null) {
					Sequence sOPSequence = itemSeriesSequence.getSequence(Tag.ReferencedSOPSequence);
					if (sOPSequence != null) {
						Attributes itemSOPSequence = sOPSequence.get(0);
						seriesInstanceUID = itemSeriesSequence.getString(Tag.SeriesInstanceUID);
						sOPInstanceUID = itemSOPSequence.getString(Tag.ReferencedSOPInstanceUID);
					}
				}
			}
		// DICOM SEG: use ReferencedSeriesSequence
		} else {
			// Get SeriesInstanceUID at first
			Sequence referencedSeriesSequence = datasetAttributes.getSequence(Tag.ReferencedSeriesSequence);
			if (referencedSeriesSequence == null) {
				LOG.error("Error: missing sequences/attributes ReferencedSeriesSequence in DICOM SEG.");
				return null;
			}
			Attributes itemReferencedSeriesSequence = referencedSeriesSequence.get(0);
			if (itemReferencedSeriesSequence == null) {
				LOG.error("Error: missing sequences/attributes itemReferencedSeriesSequence in DICOM SEG.");
				return null;
			}
			seriesInstanceUID = itemReferencedSeriesSequence.getString(Tag.SeriesInstanceUID);
			// Get SOPInstanceUID second
			Sequence referencedInstanceSequence = itemReferencedSeriesSequence.getSequence(Tag.ReferencedInstanceSequence);
			if (referencedInstanceSequence == null) {
				LOG.error("Error: missing sequences/attributes ReferencedInstanceSequence in DICOM SEG.");
				return null;
			}
			Attributes itemReferencedInstanceSequence = referencedInstanceSequence.get(0);
			if (itemReferencedInstanceSequence == null) {
				LOG.error("Error: missing sequences/attributes itemReferencedInstanceSequence in DICOM SEG.");
				return null;
			}
			sOPInstanceUID = itemReferencedInstanceSequence.getString(Tag.ReferencedSOPInstanceUID);
		}
		return findDatasetByUIDs(examination, studyInstanceUID, seriesInstanceUID, sOPInstanceUID);
	}

	/**
	 * Find origin dataset using the 3 UIDs in dataset_file.path attribute.
	 *
	 * @param examination
	 * @param studyInstanceUID
	 * @param seriesInstanceUID
	 * @param sOPInstanceUID
	 * @return
	 */
	private Dataset findDatasetByUIDs(Examination examination, String studyInstanceUID, String seriesInstanceUID, String sOPInstanceUID) {
		List<DatasetAcquisition> acquisitions = examination.getDatasetAcquisitions();
		for (DatasetAcquisition acquisition : acquisitions) {
			if (acquisition instanceof MrDatasetAcquisition
					|| acquisition instanceof CtDatasetAcquisition
					|| acquisition instanceof PetDatasetAcquisition
					|| acquisition instanceof XaDatasetAcquisition) {
				List<Dataset> datasets = acquisition.getDatasets();
				for (Dataset dataset : datasets) {
					List<DatasetExpression> expressions = dataset.getDatasetExpressions();
					for (DatasetExpression expression : expressions) {
						// only DICOM is of interest here
						if (expression.getDatasetExpressionFormat().equals(DatasetExpressionFormat.DICOM)) {
							List<DatasetFile> files = expression.getDatasetFiles();
							for (DatasetFile file : files) {
								if (file.isPacs()) {
									String path = file.getPath();
									if (path.contains(studyInstanceUID) && path.contains(seriesInstanceUID) && path.contains(sOPInstanceUID)) {
										return dataset;
									}
								}
							}
						}
					}
				}
			}
		}
		LOG.error("Error: dataset could not be found with UIDs from DICOM SEG or SR.");
		return null;
	}

	/**
	 * Create the dataset in the database.
	 *
	 * @param examination
	 * @param dataset
	 * @param datasetAttributes
	 * @throws MalformedURLException
	 */
	private void createDataset(String modality, Examination examination, Dataset dataset, Attributes datasetAttributes) throws MalformedURLException, IOException, SolrServerException {
		Dataset newMsOrSegDataset = null;
		if (SEG.equals(modality)) {
			newMsOrSegDataset = new SegmentationDataset();
		} else {
			newMsOrSegDataset = new MeasurementDataset();
		}
		// keep link to original dataset
		newMsOrSegDataset.setReferencedDatasetForSuperimposition(dataset);
		newMsOrSegDataset.setStudyId(examination.getStudyId());
		newMsOrSegDataset.setSubjectId(examination.getSubject().getId());
		newMsOrSegDataset.setCreationDate(LocalDate.now());
		// for rights check: keep link to original acquisition
		newMsOrSegDataset.setDatasetAcquisition(dataset.getDatasetAcquisition());
		createMetadata(datasetAttributes, dataset.getOriginMetadata().getDatasetModalityType(), newMsOrSegDataset);
		createDatasetExpression(datasetAttributes, newMsOrSegDataset);
		Dataset createdDataset = datasetService.create(newMsOrSegDataset);
		solrService.indexDataset(createdDataset.getId());
	}

	/**
	 * This method extracts important data, that could later be stored even
	 * inside shanoir-database, to optimize mass data export of all measurements
	 * e.g. for one study. This code works and has been tested to access the most
	 * important measurement attributes and was hard to construct. Even if currently
	 * not used, we keep it for a very high later usage.
	 *
	 * @param datasetAttributes
	 * @param dataset
	 */
	private void createMetadata(Attributes datasetAttributes, DatasetModalityType modalityType, Dataset dataset) {
		DatasetMetadata originMetadata = new DatasetMetadata();
		String reportName = datasetAttributes.getString(Tag.SeriesDescription);
		if (reportName == null || reportName.isEmpty()) {
			originMetadata.setName(IMAGING_MEASUREMENT_REPORT);
		} else {
			originMetadata.setName(reportName);
		}
		originMetadata.setDatasetModalityType(modalityType);
		originMetadata.setCardinalityOfRelatedSubjects(CardinalityOfRelatedSubjects.SINGLE_SUBJECT_DATASET);
		dataset.setOriginMetadata(originMetadata);
		dataset.setUpdatedMetadata(originMetadata);
		Sequence contentSequence = datasetAttributes.getSequence(Tag.ContentSequence);
		if (contentSequence != null) {
			Attributes contentSequenceAttributes = contentSequence.get(4);
			if (contentSequenceAttributes != null) {
				// level of imaging measurements
				Sequence imagingMeasurementsSequence = contentSequenceAttributes.getSequence(Tag.ContentSequence);
				if (imagingMeasurementsSequence != null) {
					Attributes imagingMeasurementsAttributes = imagingMeasurementsSequence.get(0);
					if (imagingMeasurementsAttributes != null) {
						// level of measurement group
						Sequence measurementGroupSequence = imagingMeasurementsAttributes.getSequence(Tag.ContentSequence);
						if (measurementGroupSequence != null) {
							// get tracking identifier
							Attributes measurementGroupAttributes = measurementGroupSequence.get(0);
							if (measurementGroupAttributes != null) {
								String trackingIdentifier = measurementGroupAttributes.getString(Tag.TextValue);
								if (trackingIdentifier != null && !trackingIdentifier.isEmpty()) {
									String trackingIdentifierType = trackingIdentifier.substring(trackingIdentifier.indexOf(":") + 1);
									originMetadata.setComment(trackingIdentifierType);
								}
							}
							// as it was complicated to acquire the below code and as in the future
							// we might access dedicated values from DICOM SR, I keep it as comment
							// level measured values
							// Attributes measurementGroupAttributes2 = measurementGroupSequence.get(2);
							// if (measurementGroupAttributes2 != null) {
							// 	Sequence measuredValueSequence = measurementGroupAttributes2.getSequence(Tag.MeasuredValueSequence);
							// 	if (measuredValueSequence != null) {
							// 		Attributes measuredValueAttributes = measuredValueSequence.get(0);
							// 		if (measuredValueAttributes != null) {
							// 			// get numeric value and graphic data
							// 			String numericValue = measuredValueAttributes.getString(Tag.NumericValue);
							// 		}
							// 	}
							// 	Sequence graphicDataSequence = measurementGroupAttributes2.getSequence(Tag.ContentSequence);
							// 	if (graphicDataSequence != null) {
							// 		Attributes graphicDataAttributes = graphicDataSequence.get(0);
							// 		if (graphicDataAttributes != null) {
							// 			String graphicData = graphicDataAttributes.getString(Tag.GraphicData);
							// 		}
							// 	}
							// }
						}
					}
				}
			}
		}
	}

	/**
	 * Create the necessary dataset expression.
	 *
	 * @param datasetAttributes
	 * @param measurementDataset
	 * @throws MalformedURLException
	 */
	private void createDatasetExpression(Attributes datasetAttributes, Dataset dataset)
			throws MalformedURLException {
		DatasetExpression expression = new DatasetExpression();
		expression.setCreationDate(LocalDateTime.now());
		expression.setDatasetExpressionFormat(DatasetExpressionFormat.DICOM);
		expression.setDataset(dataset);
		dataset.setDatasetExpressions(Collections.singletonList(expression));
		List<DatasetFile> files = createDatasetFiles(datasetAttributes, expression);
		expression.setDatasetFiles(files);
	}

	/**
	 * Create the dataset files, as WADO-RS links in that case,
	 * as OHIF viewer works only with new version of dcm4chee (arc-light 5.x).
	 *
	 * @param datasetAttributes
	 * @param expression
	 * @return
	 * @throws MalformedURLException
	 */
	private List<DatasetFile> createDatasetFiles(Attributes datasetAttributes, DatasetExpression expression)
			throws MalformedURLException {
		DatasetFile datasetFile = new DatasetFile();
		final String studyInstanceUID = datasetAttributes.getString(Tag.StudyInstanceUID);
		final String seriesInstanceUID = datasetAttributes.getString(Tag.SeriesInstanceUID);
		final String sOPInstanceUID = datasetAttributes.getString(Tag.SOPInstanceUID);
		final StringBuffer wadoStrBuf = new StringBuffer();
		wadoStrBuf.append(dcm4cheeProtocol + dcm4cheeHost + ":" + dcm4cheePortWeb);
		wadoStrBuf.append(dicomWebRS + "/" + studyInstanceUID
					+ "/series/" + seriesInstanceUID + "/instances/" + sOPInstanceUID);
		URL wadoURL = new URL(wadoStrBuf.toString());
		datasetFile.setPath(wadoURL.toString());
		datasetFile.setPacs(true);
		datasetFile.setDatasetExpression(expression);
		List<DatasetFile> files = new ArrayList<DatasetFile>();
		files.add(datasetFile);
		return files;
	}

	/**
	 * This method writes both attributes to an output stream and converts
	 * this one to an input stream, that can be used to send the manipulated
	 * file to the backend pacs.
	 *
	 * @param metaInformationAttributes
	 * @param datasetAttributes
	 * @throws IOException
	 * @throws Exception
	 */
	private void sendToPacs(Attributes metaInformationAttributes, Attributes datasetAttributes)
			throws IOException, Exception {
		/**
		 * Create a new output stream to write the changes into and use its bytes
		 * to produce a new input stream to send later by http client to the DICOM server.
		 */
		ByteArrayOutputStream bAOS = new ByteArrayOutputStream();
		// close calls to the outer stream, close the inner stream
		try (DicomOutputStream dOS = new DicomOutputStream(bAOS, metaInformationAttributes.getString(Tag.TransferSyntaxUID))) {
			dOS.writeDataset(metaInformationAttributes, datasetAttributes);
			try (InputStream finalInputStream = new ByteArrayInputStream(bAOS.toByteArray())) {
				dicomWebService.sendDicomInputStreamToPacs(finalInputStream);
			}
		}
	}

}
