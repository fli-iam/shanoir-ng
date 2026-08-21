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

package org.shanoir.ng.vip.pipeline.service;

import reactor.core.publisher.Mono;

public interface PipelineService {

    /**
     * Get all the user pipelines description from <a href="https://app.swaggerhub.com/apis/CARMIN/carmin-common_api_for_research_medical_imaging_network/0.3.1#/default/listPipelines">VIP API</a>
     *
     * @return JSON as string
     */
    Mono<String> getPipelineAll();

    /**
     * Get the pipeline description from <a href="https://app.swaggerhub.com/apis/CARMIN/carmin-common_api_for_research_medical_imaging_network/0.3.1#/default/getPipeline">VIP API</a>
     *
     * @param identifier
     * @param version
     * @return JSON as string
     */
    Mono<String> getPipeline(String identifier, String version);
}
