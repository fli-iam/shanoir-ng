package org.shanoir.ng.vip.controller;

import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.vip.resulthandler.DefaultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
public class PipelineApiController implements PipelineApi {

    private static final Logger LOG = LoggerFactory.getLogger(PipelineApiController.class);

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
