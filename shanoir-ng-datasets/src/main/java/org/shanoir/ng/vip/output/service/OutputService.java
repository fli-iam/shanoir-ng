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

package org.shanoir.ng.vip.output.service;

import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.vip.executionMonitoring.model.ExecutionMonitoring;
import org.shanoir.ng.vip.output.exception.ResultHandlerException;
import org.shanoir.ng.vip.output.handler.OutputHandler;

public interface OutputService {

    /**
     *
     * Process the result of the given execution
     *
     * @param monitoring
     * @throws ResultHandlerException
     */
    void process(ExecutionMonitoring monitoring) throws ResultHandlerException, EntityNotFoundException;

    /**
     *
     * Process the result of the given execution in post_processing context, so with a specific output handler
     *
     * @param monitoring
     * @param outputHandler
     * @throws ResultHandlerException
     */
    void process(ExecutionMonitoring monitoring, OutputHandler outputHandler) throws ResultHandlerException, EntityNotFoundException;
}
