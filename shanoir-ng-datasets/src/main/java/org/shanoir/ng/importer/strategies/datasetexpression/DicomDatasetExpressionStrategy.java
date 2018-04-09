package org.shanoir.ng.importer.strategies.datasetexpression;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.shanoir.ng.dataset.DatasetExpression;
import org.shanoir.ng.dataset.DatasetExpressionFormat;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.dicom.DicomProcessing;
import org.shanoir.ng.importer.dto.ExpressionFormat;
import org.shanoir.ng.importer.dto.ImportJob;
import org.shanoir.ng.importer.dto.Serie;
import org.shanoir.ng.shared.model.EchoTime;
import org.shanoir.ng.shared.model.FlipAngle;
import org.shanoir.ng.shared.model.InversionTime;
import org.shanoir.ng.shared.model.RepetitionTime;
import org.shanoir.ng.shared.service.DicomServiceApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DicomDatasetExpressionStrategy implements DatasetExpressionStrategy {

	/** Logger. */
	private static final Logger LOG = LoggerFactory.getLogger(DicomDatasetExpressionStrategy.class);

	@Autowired
	DicomProcessing dicomProcessing;

	@Value("${backup.dicom.server.host}")
	private String backupDicomServerHost;

	@Value("${backup.dicom.server.port}")
	private String backupDicomServerWebPort;

	@Override
	public DatasetExpression generateDatasetExpression(Serie serie, ImportJob importJob,
			ExpressionFormat expressionFormat) {

		DatasetExpression pacsDatasetExpression = new DatasetExpression();
		pacsDatasetExpression.setCreationDate(LocalDate.now());
		pacsDatasetExpression.setDatasetExpressionFormat(DatasetExpressionFormat.DICOM);

		if (serie.getIsMultiFrame()) {
			pacsDatasetExpression.setMultiFrame(true);
			pacsDatasetExpression.setFrameCount(new Integer(serie.getMultiFrameCount()));
		}

		if (expressionFormat != null & expressionFormat.getType().equals("dcm")) {

			List<String> dcmFilesToSendToPacs = new ArrayList<String>();
			for (org.shanoir.ng.importer.dto.DatasetFile datasetFile : expressionFormat.getDatasetFiles()) {
				dcmFilesToSendToPacs.add(datasetFile.getPath());
				Date contentTime = null;
				Date acquisitionTime = null;
				Attributes dicomAttributes = null;
				try {
					dicomAttributes = dicomProcessing.getDicomObjectAttributes(datasetFile);
				} catch (IOException e) {
					LOG.error(e.getMessage());
				}
				DatasetFile pacsDatasetFile = new DatasetFile();
				pacsDatasetFile.setPacs(true);
				final String sOPInstanceUID = dicomAttributes.getString(Tag.SOPInstanceUID);
				final String studyInstanceUID = dicomAttributes.getString(Tag.StudyInstanceUID);
				final String seriesInstanceUID = dicomAttributes.getString(Tag.SeriesInstanceUID);
				String wadoRequest = "http://" + backupDicomServerHost + ":" + backupDicomServerWebPort
						+ "/wado?requestType=WADO&studyUID=" + studyInstanceUID + "&seriesUID=" + seriesInstanceUID
						+ "&objectUID=" + sOPInstanceUID;
				// set return type as application/dicom instead of
				// the standard image/jpeg
				wadoRequest += "&contentType=application/dicom";

				try {
					URL wadoURL = new URL(wadoRequest);
					pacsDatasetFile.setPath(wadoURL.getPath());
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				pacsDatasetExpression.getDatasetFiles().add(pacsDatasetFile);
				pacsDatasetFile.setDatasetExpression(pacsDatasetExpression);

				// calculate the acquisition duration for this acquisition
				acquisitionTime = dicomAttributes.getDate(Tag.AcquisitionTime);
				contentTime = dicomAttributes.getDate(Tag.ContentTime);
				if (acquisitionTime != null) {
					if (pacsDatasetExpression.getLastImageAcquisitionTime() == null) {
						pacsDatasetExpression.setLastImageAcquisitionTime(acquisitionTime);
					}
					if (pacsDatasetExpression.getFirstImageAcquisitionTime() == null) {
						pacsDatasetExpression.setFirstImageAcquisitionTime(acquisitionTime);
					}
					if (acquisitionTime.after(pacsDatasetExpression.getLastImageAcquisitionTime())) {
						pacsDatasetExpression.setLastImageAcquisitionTime(acquisitionTime);
					} else if (acquisitionTime.before(pacsDatasetExpression.getFirstImageAcquisitionTime())) {
						pacsDatasetExpression.setFirstImageAcquisitionTime(acquisitionTime);
					}
				}
				if (contentTime != null) {
					if (pacsDatasetExpression.getLastImageAcquisitionTime() == null) {
						pacsDatasetExpression.setLastImageAcquisitionTime(contentTime);
					}
					if (pacsDatasetExpression.getFirstImageAcquisitionTime() == null) {
						pacsDatasetExpression.setFirstImageAcquisitionTime(contentTime);
					}
					if (contentTime.after(pacsDatasetExpression.getLastImageAcquisitionTime())) {
						pacsDatasetExpression.setLastImageAcquisitionTime(contentTime);
					} else if (contentTime.before(pacsDatasetExpression.getFirstImageAcquisitionTime())) {
						pacsDatasetExpression.setFirstImageAcquisitionTime(contentTime);
					}
				}

				/**
				 * 
				 * Retrieve EchoTime list that will be added to the MrProtocol..
				 * 
				 */
				final Double echoTime = dicomAttributes.getDouble(Tag.EchoTime, -1D);
				final int[] echoNumbers = dicomAttributes.getInts(Tag.EchoNumbers);
				final Double flipAngle = dicomAttributes.getDouble(Tag.FlipAngle, -1D);
				final Double inversionTime = dicomAttributes.getDouble(Tag.InversionTime, -1D);
				final Double repetitionTime = dicomAttributes.getDouble(Tag.RepetitionTime, -1D);
				// Echo time
				if (echoTime != null && echoNumbers != null && echoNumbers.length == 1 && echoTime.doubleValue() != 0) {
					final EchoTime echoTimeObject = new EchoTime();
					echoTimeObject.setEchoNumber(echoNumbers[0]);
					echoTimeObject.setEchoTimeValue(echoTime);
					pacsDatasetExpression.addEchoTimeToMap(echoTimeObject.hashCode(), echoTimeObject);

				}
				// Flip angle
				if (flipAngle != null && flipAngle.doubleValue() != 0) {
					final FlipAngle flipAngleObject = new FlipAngle();
					flipAngleObject.setFlipAngleValue(flipAngle);
					pacsDatasetExpression.addFlipAngleToMap(flipAngle, flipAngleObject);
				}
				// Inversion time
				if (inversionTime != null && inversionTime.doubleValue() != 0) {
					final InversionTime inversionTimeObject = new InversionTime();
					inversionTimeObject.setInversionTimeValue(inversionTime);
					pacsDatasetExpression.addInversionTimeToMap(inversionTime, inversionTimeObject);

				}
				// Repetition time
				if (repetitionTime != null && repetitionTime.doubleValue() != 0) {
					final RepetitionTime repetitionTimeObject = new RepetitionTime();
					repetitionTimeObject.setRepetitionTimeValue(repetitionTime);
					pacsDatasetExpression.addRepetitionTimeToMap(repetitionTime, repetitionTimeObject);

				}
			}
		}
		return pacsDatasetExpression;
	}

}
