package org.shanoir.ng.service;

import java.io.File;
import java.util.Collections;

import javax.transaction.Transactional;

import org.shanoir.ng.model.Dataset;
import org.shanoir.ng.model.NiftiConverter;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
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
	DatasetsCreatorAndNIfTIConverterService converterService;
	
	/**
	 * Converts some data
	 * @param message the string containing the converter ID + the workfolder where the dicom are
	 * @return true if the conversion is a success, false otherwise
	 */
	@RabbitListener(queues = RabbitMQConfiguration.NIFTI_CONVERSION_QUEUE)
	@RabbitHandler
	public boolean convertData(String message) {
		String[] messageSplit = message.split(";");
		Integer converterId = Integer.valueOf(messageSplit[0]);
		String workFolder = messageSplit[1];

		NiftiConverter converter = NiftiConverter.getType(converterId);		
		
		String workFolderResult = workFolder + File.separator + "result";
		File result = new File(workFolderResult);

		result.mkdirs();

		converterService.convertToNiftiExec(converterId, workFolder, workFolderResult, false);
		
		if (converter.isDicomifier()) {
			Dataset dataset = new Dataset();
			dataset.setName("name");
			converterService.niftiFileSortingDicom2Nifti(Collections.emptyList(), result, dataset);
		} else {
			converterService.niftiFileSorting(Collections.emptyList(), result, new File("serieId"));
		}
		
		return true;
	}
}
