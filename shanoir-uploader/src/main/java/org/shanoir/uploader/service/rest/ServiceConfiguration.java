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

import org.shanoir.uploader.ShUpConfig;

public class ServiceConfiguration {

    /** Constructeur privé */
    private ServiceConfiguration() {
    }

    /** Instance unique pré-initialisée */
    private static final ServiceConfiguration INSTANCE = new ServiceConfiguration();

    /** Point d'accès pour l'instance unique du singleton */
    public static ServiceConfiguration getInstance() {
        return INSTANCE;
    }

    public String getProxyHost() {
        if (ShUpConfig.proxyProperties.getProperty("proxy.host") != null
                && !ShUpConfig.proxyProperties.getProperty("proxy.host").equals("")) {
            return ShUpConfig.proxyProperties.getProperty("proxy.host");
        } else {
            return null;
        }
    }

    public String getProxyPort() {
        if (ShUpConfig.proxyProperties.getProperty("proxy.port") != null
                && !ShUpConfig.proxyProperties.getProperty("proxy.port").equals("")) {
            return ShUpConfig.proxyProperties.getProperty("proxy.port");
        } else {
            return null;
        }
    }

    public String getProxyUser() {
        if (ShUpConfig.proxyProperties.getProperty("proxy.user") != null
                && !ShUpConfig.proxyProperties.getProperty("proxy.user").equals("")) {
            return ShUpConfig.proxyProperties.getProperty("proxy.user");
        } else {
            return null;
        }
    }

    public String getProxyPassword() {
        if (ShUpConfig.proxyProperties.getProperty("proxy.password") != null
                && !ShUpConfig.proxyProperties.getProperty("proxy.password").equals("")) {
            return ShUpConfig.proxyProperties.getProperty("proxy.password");
        } else {
            return null;
        }
    }

    public String getTlsProtocol() {
        if (ShUpConfig.proxyProperties.getProperty("tls.protocols") != null
                && !ShUpConfig.proxyProperties.getProperty("tls.protocols").equals("")) {
            return ShUpConfig.proxyProperties.getProperty("tls.protocols");
        } else {
            return "TLSv1.3, TLSv1.2";
        }
    }

    public String getTlsCypherSuite() {
        if (ShUpConfig.proxyProperties.getProperty("tls.cipherSuites") != null
                && !ShUpConfig.proxyProperties.getProperty("tls.cipherSuites").equals("")) {
            return ShUpConfig.proxyProperties.getProperty("tls.cipherSuites");
        } else {
            return null;
        }
    }

    public Boolean isProxySecure() {
        return ShUpConfig.proxyProperties.getProperty("proxy.secure") != null
                && Boolean.valueOf(ShUpConfig.proxyProperties.getProperty("proxy.secure"));
    }

    public Boolean isProxyEnabled() {
        return ShUpConfig.proxyProperties.getProperty("proxy.enabled") != null
                && Boolean.valueOf(ShUpConfig.proxyProperties.getProperty("proxy.enabled"));
    }

    public String getTestURL() {
        return ShUpConfig.profileProperties.getProperty("shanoir.server.url");
    }
}
