package org.shanoir.ng.vip.shared.service;

import org.shanoir.ng.utils.KeycloakUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

@Service
public class Utils {

    public HttpHeaders getUserHttpHeaders() {
        HttpHeaders headers = KeycloakUtil.getKeycloakHeader();
        return headers;
    }
}
