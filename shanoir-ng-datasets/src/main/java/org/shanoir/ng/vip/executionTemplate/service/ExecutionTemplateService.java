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

package org.shanoir.ng.vip.executionTemplate.service;

import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.vip.executionTemplate.model.ExecutionTemplate;
import org.springframework.scheduling.annotation.Async;
import java.util.List;

public interface ExecutionTemplateService {

    /**
     * This method is called asynchroneously at the end of the import to check if an execution template has to be done.
     * @param createdAcquisitions the list of acqusitions to check for execution templates
     */
    @Async
    void createExecutionsFromExecutionTemplates(List<DatasetAcquisition> createdAcquisitions);


    /**
     * This method allows the template filters management while updating template
     * @param executionTemplate the newly created template without filters
     *
     * @return the newly created template with its filters
     */
    ExecutionTemplate update(ExecutionTemplate executionTemplate);
}
