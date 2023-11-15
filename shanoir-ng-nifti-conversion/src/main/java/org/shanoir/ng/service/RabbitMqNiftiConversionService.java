package org.shanoir.ng.service;

import java.io.File;
import java.util.Collections;

import org.shanoir.ng.model.Dataset;
import org.shanoir.ng.model.NiftiConverter;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This service allows to trigger the conversion of dicom to nifti files.
 * @author jcome
 *
 */
@Service
public class RabbitMqNiftiConversionService {
	
	@Autowired
	NIfTIConverterService converterService;

	private static final Logger LOG = LoggerFactory.getLogger(RabbitMqNiftiConversionService.class);

	/**
	 * Converts BRUKER to DICOM data
	 * @param message the string containing the workfolder where the dicom
	 * @return true if the conversion is a success, false otherwise
	 */
	@RabbitListener(queues = RabbitMQConfiguration.BRUKER_CONVERSION_QUEUE)
	@RabbitHandler
	public boolean convertBrukerToDicom(String message) {
		try {
			String[] messageSplit = message.split(";");
			String inputFolder = messageSplit[0];
			String outputFolder = messageSplit[1];
			this.converterService.brukerToDicomExec(inputFolder, outputFolder);
		} catch (Exception e) {
			LOG.error("Could not convert data from bruker to dicom: ", e);
			return false;
		}
		return true;
	}

	/**
	 * Converts some data
	 * @param message the string containing the converter ID + the workfolder where the dicom are
	 * @return true if the conversion is a success, false otherwise
	 */
	@RabbitListener(queues = RabbitMQConfiguration.NIFTI_CONVERSION_QUEUE)
	@RabbitHandler
	public boolean convertData(String message) {
		try {
			String[] messageSplit = message.split(";");
			int converterId = Integer.parseInt(messageSplit[0]);
			LOG.error("Starting conversion with converter: " + converterId);

			String workFolder = messageSplit[1];

			NiftiConverter converter = NiftiConverter.getType(converterId);

			String workFolderResult = workFolder + File.separator + "result";
			File result = new File(workFolderResult);

			result.mkdirs();

			converterService.convertToNiftiExec(Long.valueOf(converterId), workFolder, workFolderResult);

			if (NiftiConverter.DICOMIFIER.equals(converter)) {
				Dataset dataset = new Dataset();
				dataset.setName("name");
				converterService.niftiFileSortingDicom2Nifti(Collections.emptyList(), result, dataset);
			} else {
				converterService.niftiFileSorting(Collections.emptyList(), result, new File("serieId"));
			}
		} catch (Exception e) {
			LOG.error("Could not convert data to nifti", e);
			return false;
		}
		return true;
	}
}
