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
     * @param name
     * @param version
     * @return
     */
    @Override
    public Mono<String> getPipeline(String name, String version) {
        return vipClient.getPipeline(name, version);
    }
}
