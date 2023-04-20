package org.shanoir.ng.processing.carmin.output;

import java.io.File;

import org.shanoir.ng.processing.carmin.model.CarminDatasetProcessing;

/**
 * This class defines the default class to be implemented for output processing.
 * @author jcome
 *
 */
public abstract class OutputProcessing {

	/**
	 * This methods manages the single result of a Carmin  dataset processing
	 * @param resultFile the result file as tar.gz of the processing
	 * @param parentFolder the temporary arent folder in which we are currently working
	 * @param processing the corresponding dataset processing.
	 */
	public abstract void manageTarGzResult(File resultFile, File parentFolder, CarminDatasetProcessing processing);

}
