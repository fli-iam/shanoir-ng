package org.shanoir.ng.vip.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
public class PipelineApiController implements PipelineApi {

    @Autowired
    private VipClientService vipClient;

    @Override
    public Mono<String> getPipelineAll() {
        return vipClient.getPipelineAll();
    }

    /**
     * @param identifier
     * @return
     */
    @Override
    public Mono<String> getPipeline(String identifier) {
        return vipClient.getPipeline(identifier);
    }
}
