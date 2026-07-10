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


package org.shanoir.ng.utils;

import jakarta.annotation.PostConstruct;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class ShanoirWebClient implements WebClient {

    private WebClient webClient;

    @PostConstruct
    public void init() {
        webClient = WebClient.builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(500 * 1024 * 1024))
                .build();
    }

    @Override
    public RequestHeadersUriSpec<?> get() {
        return webClient.get();
    }

    @Override
    public RequestHeadersUriSpec<?> head() {
        return webClient.head();
    }

    @Override
    public RequestBodyUriSpec post() {
        return webClient.post();
    }

    @Override
    public RequestBodyUriSpec put() {
        return webClient.put();
    }

    @Override
    public RequestBodyUriSpec patch() {
        return webClient.patch();
    }

    @Override
    public RequestHeadersUriSpec<?> delete() {
        return webClient.delete();
    }

    @Override
    public RequestHeadersUriSpec<?> options() {
        return webClient.options();
    }

    @Override
    public RequestBodyUriSpec method(HttpMethod method) {
        return webClient.method(method);
    }

    @Override
    public Builder mutate() {
        return webClient.mutate();
    }
}
