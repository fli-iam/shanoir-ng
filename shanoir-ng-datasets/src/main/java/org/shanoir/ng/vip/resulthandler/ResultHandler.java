package org.shanoir.ng.vip.resulthandler;

import org.shanoir.ng.vip.monitoring.model.ExecutionMonitoring;

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
	 * @param processing ExecutionMonitoring
	 * @return
	 */
	public abstract boolean canProcess(ExecutionMonitoring monitoring) throws ResultHandlerException;

	/**
	 * This methods manages the single result of an execution
	 *
	 * @param resultFiles  the result file as tar.gz of the processing
	 * @param parentFolder the temporary arent folder in which we are currently working
	 * @param processing   the corresponding dataset processing.
	 */

	public abstract void manageTarGzResult(List<File> resultFiles, File parentFolder, ExecutionMonitoring processing) throws ResultHandlerException;

}
