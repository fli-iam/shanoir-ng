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

package org.shanoir.uploader.action.init;


import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.service.rest.ServiceConfiguration;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This concrete state class defines the state when the ShanoirUploader :
 *         - Initialize the proxy environment variable if enabled
 *         - tests the connection to remote shanoir server on using the proxy
 *
 * As a result, the context will change either to :
 *         - a manual proxy configuration
 *         - step into the next state in case of success.
 *
 * @author atouboul
 * @author mkain
 *
 */
@Component
public class ProxyConfigurationState implements State {

    private static final Logger LOG = LoggerFactory.getLogger(ProxyConfigurationState.class);

    @Autowired
    private AuthenticationConfigurationState authenticationConfigurationState;

    @Autowired
    private ProxyManualConfigurationState proxyManualConfigurationState;

    public void load(StartupStateContext context) {
        String testURL = ServiceConfiguration.getInstance().getTestURL();
        int httpResponseCode = 0;
        try {
            httpResponseCode = ShanoirUploaderServiceClient.testProxy(testURL);
        } catch (Exception e) {
            LOG.error("Error during proxy test:", e);
        }
        LOG.info("Proxy test returned following code: " + httpResponseCode);
        switch (httpResponseCode) {
            case 200 :
                context.getShUpStartupDialog().updateStartupText("\n" + ShUpConfig.resourceBundle.getString("shanoir.uploader.startup.test.proxy.success"));
                context.setState(authenticationConfigurationState);
                context.nextState();
                break;
            default:
                context.getShUpStartupDialog().updateStartupText("\n" + ShUpConfig.resourceBundle.getString("shanoir.uploader.startup.test.proxy.fail"));
                context.setState(proxyManualConfigurationState);
                context.nextState();
                break;
        }
    }

}
