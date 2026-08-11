/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.vip.output.handler;

import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.vip.executionMonitoring.model.ExecutionMonitoring;
import org.shanoir.ng.vip.output.exception.ResultHandlerException;
import java.io.File;
import java.util.List;

public abstract class OutputHandler {

    /**
     * Return true if the implementation can process the result of the processing relative to the given string
     *
     * @param pipelineIdentifier string
     * @return true if execution monitoring can be process by this handler instance
     */
    public abstract boolean canProcess(String pipelineIdentifier) throws ResultHandlerException;

    /**
     * This methods manages the single result of an execution
     *
     * @param resultFiles  the result file as tar.gz of the processing
     * @param parentFolder the temporary arent folder in which we are currently working
     * @param processing   the corresponding dataset processing.
     * @param resourceId   the tag of the packed inputs.
     */
    public abstract void manageTarGzResult(List<File> resultFiles, File parentFolder, ExecutionMonitoring processing, String resourceId) throws ResultHandlerException;

    /**
     * This methods manages the single result of an execution delayed with postProcessing.
     *
     * @param resultFiles  the result file as tar.gz of the processing
     * @param processing the monitoring related to the exec
     */
    public abstract void manageDelayedOutput(List<File> resultFiles, DatasetProcessing processing);
}
