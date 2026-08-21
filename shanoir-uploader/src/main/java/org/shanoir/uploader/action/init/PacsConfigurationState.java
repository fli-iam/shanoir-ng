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

import java.net.MalformedURLException;

import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.dicom.DicomServerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This concrete state class defines the state when the ShanoirUploader tests the connection to the PACS
 *
 * As a result, the context will change either to :
 *         - a Manual Pacs Configuration in case of failure
 *         - step to the READY state in case of success.
 *
 * @author atouboul
 *
 */
@Component
public class PacsConfigurationState implements State {

    private static final Logger LOG = LoggerFactory.getLogger(PacsConfigurationState.class);

    public ShUpOnloadConfig shUpOnloadConfig = ShUpOnloadConfig.getInstance();

    @Autowired
    private ReadyState readyState;

    @Autowired
    private PacsManualConfigurationState pacsManualConfigurationState;

    public void load(StartupStateContext context) {
        initDicomServerClient();
        /**
         * Test if shanoir is able to contact the configured pacs in dicom_server.properties
         */
        if (shUpOnloadConfig.getDicomServerClient().echoDicomServer()) {
            context.setState(readyState);
        } else {
            context.setState(pacsManualConfigurationState);
        }
        context.nextState();
    }

    /*
     * Initialize the DicomServerClient.
     */
    private void initDicomServerClient() {
        DicomServerClient dSC;
        try {
            dSC = new DicomServerClient(ShUpConfig.dicomServerProperties, shUpOnloadConfig.getWorkFolder());
            shUpOnloadConfig.setDicomServerClient(dSC);
            LOG.info("PacsConfigurationState: DicomServerClient successfully initialized.");
        } catch (MalformedURLException e) {
            LOG.info("Error with init of DicomServerClient: " + e.getMessage(), e);
        }
    }

}
