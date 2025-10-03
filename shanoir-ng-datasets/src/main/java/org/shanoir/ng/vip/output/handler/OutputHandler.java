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

import org.shanoir.ng.vip.execution.service.ExecutionTrackingServiceImpl;
import org.shanoir.ng.vip.executionMonitoring.model.ExecutionMonitoring;
import org.shanoir.ng.vip.output.exception.ResultHandlerException;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.File;
import java.util.List;

public abstract class OutputHandler {

    @Autowired
    @SuppressWarnings("checkstyle:VisibilityModifier")
    protected ExecutionTrackingServiceImpl executionTrackingService;

    /**
     * Return true if the implementation can process the result of the given processing
     *
     * @param monitoring ExecutionMonitoring
     * @return true if execution monitoring can be process by this handler instance
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
