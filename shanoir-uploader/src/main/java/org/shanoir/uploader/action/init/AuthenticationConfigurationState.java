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

import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This concrete state class defines the state when the ShanoirUploader tests
 * the User Authentication to remote shanoir server - loads the property file
 * containing user/(crypted)password for connecting to the remote shanoir server
 * - loads the testCredential WSDL webservice - test if the user can connect to
 * the remote shanoir server
 *
 * As a result, the context will change either to : - a manual user
 * authentication in case of failure - step into the next state in case of
 * success.
 *
 * @author atouboul
 * @author mkain
 *
 */
@Component
public class AuthenticationConfigurationState implements State {

    @Autowired
    private ShanoirUploaderServiceClient shanoirUploaderServiceClient;

    @Autowired
    private AuthenticationManualConfigurationState authenticationManualConfigurationState;

    public void load(StartupStateContext context) {
        shanoirUploaderServiceClient.configure();
        ShUpOnloadConfig.setShanoirUploaderServiceClient(shanoirUploaderServiceClient);
        // https://github.com/fli-iam/shanoir-ng/issues/615, KeycloakInstalled removed here as not working in CHUs
        context.setState(authenticationManualConfigurationState);
        context.nextState();
        return;
    }

}
