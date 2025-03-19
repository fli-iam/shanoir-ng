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
