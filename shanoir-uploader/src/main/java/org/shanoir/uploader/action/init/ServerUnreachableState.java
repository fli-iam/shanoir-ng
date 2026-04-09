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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This concrete state class defines the state where application startup
 * has failed due to the fact that Shanoir remote server is unreachable:
 * This state is a dead end.
 *
 * @author mkain
 * @author atouboulic
 *
 */
public class ServerUnreachableState implements State {

    private static final Logger LOG = LoggerFactory.getLogger(ServerUnreachableState.class);

    public void load(StartupStateContext context) {
        context.getShUpStartupDialog().updateStartupText("\nShanoir server unreachable, ShanoirUploader stopped.");
        LOG.error("Shanoir Server not reachable.");
    }

}
