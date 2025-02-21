package org.shanoir.ng.vip.pipeline.service;

import jakarta.annotation.PostConstruct;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.vip.output.exception.ResultHandlerException;
import org.shanoir.ng.vip.shared.service.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class PipelineServiceImpl implements PipelineService {

    private static final Logger LOG = LoggerFactory.getLogger(PipelineServiceImpl.class);

    @Value("${vip.uri}")
    private String vipUrl;
    private final String vipPipelineUri = "/pipelines";
    private WebClient webClient;

    @Autowired
    private Utils utils;

    @PostConstruct
    public void init() {
        this.webClient = WebClient.create(vipUrl);
    }

    public Mono<String> getPipelineAll() {
        return webClient.get()
            .uri(vipPipelineUri)
            .headers(headers -> ((HttpHeaders) headers).addAll(utils.getUserHttpHeaders()))
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, response -> {
                if (response.statusCode().equals(HttpStatus.UNAUTHORIZED)) {
                    LOG.info("Unauthorized, current user can not request VIP API");
                    return Mono.empty();
                }
                return Mono.error(new ResultHandlerException("Can't get pipelines descriptions from VIP API", null));
            })
            .bodyToMono(String.class)
            .onErrorResume(e -> {
                ErrorModel model = new ErrorModel(HttpStatus.SERVICE_UNAVAILABLE.value(), "Can't get pipelines descriptions from VIP API", e.getMessage());
                return Mono.error(new RestServiceException(e, model));
            });
    }

    public Mono<String> getPipeline(String identifier, String version) {
        String url = vipPipelineUri + "/" + identifier + "/" + version;
        return webClient.get()
            .uri(url)
            .headers(headers -> headers.addAll(utils.getUserHttpHeaders()))
            .retrieve()
            .bodyToMono(String.class)
            .onErrorResume(e -> {
                ErrorModel model = new ErrorModel(HttpStatus.SERVICE_UNAVAILABLE.value(), "Can't get pipeline [" + identifier + "/" + version + "] description from VIP API", e.getMessage());
                return Mono.error(new RestServiceException(e, model));
            });
    }
}
