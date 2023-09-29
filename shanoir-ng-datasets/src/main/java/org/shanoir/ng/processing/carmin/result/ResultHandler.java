package org.shanoir.ng.processing.carmin.result;

import org.shanoir.ng.processing.carmin.model.ExecutionMonitoring;

import java.io.File;
import java.util.List;

/**
 * This class defines the default class to be implemented for output processing.
 * @author jcome
 *
 */
public abstract class ResultHandler {

	/**
	 * Return true if the implementation can process the result of the given processing
	 *
	 * @param processing CarminDatasetProcessing
	 * @return
	 */
	public abstract boolean canProcess(ExecutionMonitoring processing) throws ResultHandlerException;

	/**
	 * This methods manages the single result of a Carmin  dataset processing
	 *
	 * @param resultFiles  the result file as tar.gz of the processing
	 * @param parentFolder the temporary arent folder in which we are currently working
	 * @param processing   the corresponding dataset processing.
	 */

	public abstract void manageTarGzResult(List<File> resultFiles, File parentFolder, ExecutionMonitoring processing) throws ResultHandlerException;

}
