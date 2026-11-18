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

package org.shanoir.uploader.service.rest;

import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.ssl.DefaultHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.http.ssl.TLS;

/**
 * Builds the TLS socket factories used to connect to Shanoir servers.
 *
 * The server certificate chain is validated against the given SSLContext
 * (by default the JVM truststore), and the host name of the URL is checked
 * against the certificate (subject alternative names) with the standard
 * {@link DefaultHostnameVerifier} of HttpClient.
 */
public final class TlsSocketFactories {

    private TlsSocketFactories() {
    }

    /**
     * @param sslContext SSLContext to use, or null for the default one (JVM truststore)
     * @param tlsVersions TLS versions to enable, none for the HttpClient defaults
     * @return a socket factory validating certificate chain and host name
     */
    public static SSLConnectionSocketFactory withHostnameVerification(final SSLContext sslContext, final TLS... tlsVersions) {
        final SSLConnectionSocketFactoryBuilder builder = SSLConnectionSocketFactoryBuilder.create()
                .setHostnameVerifier(new DefaultHostnameVerifier());
        if (sslContext != null) {
            builder.setSslContext(sslContext);
        }
        if (tlsVersions != null && tlsVersions.length > 0) {
            builder.setTlsVersions(tlsVersions);
        }
        return builder.build();
    }

}
