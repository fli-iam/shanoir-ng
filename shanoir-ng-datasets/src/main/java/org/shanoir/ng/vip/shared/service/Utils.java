package org.shanoir.ng.vip.shared.service;

import org.shanoir.ng.utils.KeycloakUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

@Service
public class Utils {

    public HttpHeaders getUserHttpHeaders() {
        HttpHeaders headers = KeycloakUtil.getKeycloakHeader();
        headers.add("apikey", "imo804d70m73d4n54f18uhr5j0");
        headers.remove("Authorization");
        return headers;
    }
}
