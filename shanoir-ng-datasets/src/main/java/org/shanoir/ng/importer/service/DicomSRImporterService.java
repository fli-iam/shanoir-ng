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

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.shanoir.ng.dataset.modality.MeasurementDataset;
import org.shanoir.ng.dataset.model.CardinalityOfRelatedSubjects;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.dataset.model.DatasetMetadata;
import org.shanoir.ng.dataset.model.DatasetModalityType;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.repository.DatasetAcquisitionRepository;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.dicom.web.StudyInstanceUIDHandler;
import org.shanoir.ng.dicom.web.service.DICOMWebService;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.shanoir.ng.shared.model.Subject;
import org.shanoir.ng.shared.repository.SubjectRepository;
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
 * @author mkain
 *
 */
@Service
public class DicomSRImporterService {
	
	private static final Logger LOG = LoggerFactory.getLogger(DicomSRImporterService.class);

	private static final String SR = "SR";

	@Autowired
	private ExaminationRepository examinationRepository;
	
	@Autowired
	private DatasetAcquisitionRepository datasetAcquisitionRepository;
	
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
	public boolean importDicomSR(InputStream inputStream) {
		// DicomInputStream consumes the input stream to read the data
		try (DicomInputStream dIS = new DicomInputStream(inputStream)) {
			Attributes metaInformationAttributes = dIS.readFileMetaInformation();
			Attributes datasetAttributes = dIS.readDataset();
			// check for modality: DICOM SR
			if (SR.equals(datasetAttributes.getString(Tag.Modality))) {
				Examination examination = modifyDicomSR(datasetAttributes);
				createDataset(examination, datasetAttributes);
				sendToPacs(metaInformationAttributes, datasetAttributes);
			} else {
				LOG.error("Error: importDicomSR: other modality sent then SR.");
				return false;
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return false;
		}
		return true;
	}

	private void createDataset(Examination examination, Attributes datasetAttributes) throws MalformedURLException {
		// seriesNumber is used as sortingIndex, used to find correct serie == acquisition
		int seriesNumber = datasetAttributes.getInt(Tag.SeriesNumber, -1);
		DatasetAcquisition acquisition = datasetAcquisitionRepository.findByExaminationIdAndSortingIndex(examination.getId(), seriesNumber);
		MeasurementDataset measurementDataset = new MeasurementDataset();
		measurementDataset.setStudyId(examination.getStudyId());
		measurementDataset.setSubjectId(examination.getSubjectId());
		measurementDataset.setCreationDate(LocalDate.now());
		measurementDataset.setDatasetAcquisition(acquisition);
		// Metadata
		DatasetMetadata originMetadata = new DatasetMetadata();
		originMetadata.setName("Imaging Measurement Report");
		originMetadata.setDatasetModalityType(DatasetModalityType.MR_DATASET);
		originMetadata.setCardinalityOfRelatedSubjects(CardinalityOfRelatedSubjects.SINGLE_SUBJECT_DATASET);		
		measurementDataset.setOriginMetadata(originMetadata);
		measurementDataset.setUpdatedMetadata(originMetadata);
		createDatasetExpression(datasetAttributes, measurementDataset);
		acquisition.getDatasets().add(measurementDataset);
		datasetAcquisitionRepository.save(acquisition);
	}

	private void createDatasetExpression(Attributes datasetAttributes, MeasurementDataset measurementDataset)
			throws MalformedURLException {
		DatasetExpression expression = new DatasetExpression();
		expression.setCreationDate(LocalDateTime.now());
		expression.setDatasetExpressionFormat(DatasetExpressionFormat.DICOM);
		expression.setDataset(measurementDataset);
		measurementDataset.setDatasetExpressions(Collections.singletonList(expression));		
		List<DatasetFile> files = createDatasetFiles(datasetAttributes, expression);
		expression.setDatasetFiles(files);
	}

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
		DicomOutputStream dOS = new DicomOutputStream(bAOS, metaInformationAttributes.getString(Tag.TransferSyntaxUID));
		dOS.writeDataset(metaInformationAttributes, datasetAttributes);
		InputStream finalInputStream = new ByteArrayInputStream(bAOS.toByteArray());
		dicomWebService.sendDicomInputStreamToPacs(finalInputStream);
		finalInputStream.close();
		dOS.close();
	}

	/**
	 * This method replaces values of dicom tags within the DICOM SR file:
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
	private Examination modifyDicomSR(Attributes datasetAttributes) {
		// replace artificial examinationUID with real StudyInstanceUID in DICOM server
		String examinationUID = datasetAttributes.getString(Tag.StudyInstanceUID);
		String studyInstanceUID = studyInstanceUIDHandler.findStudyInstanceUIDFromCacheOrDatabase(examinationUID);
		datasetAttributes.setString(Tag.StudyInstanceUID, VR.UI, studyInstanceUID);
		// replace subject name, that is sent by the viewer wrongly with P-0000001 etc.
		Long examinationID = Long.valueOf(examinationUID.substring(StudyInstanceUIDHandler.PREFIX.length()));
		Examination examination = examinationRepository.findById(examinationID).get();
		Optional<Subject> subjectOpt = subjectRepository.findById(examination.getSubjectId());
		String subjectName = "error_subject_name_not_found_in_db";
		if (subjectOpt.isPresent()) {
			subjectName = subjectOpt.get().getName();
		}
		datasetAttributes.setString(Tag.PatientName, VR.PN, subjectName);
		datasetAttributes.setString(Tag.PatientID, VR.LO, subjectName);
		// set user name, as person, who created the measurement
		final String userName = KeycloakUtil.getTokenUserName();
		datasetAttributes.setString(Tag.PersonName, VR.PN, userName);
		return examination;
	}

}
