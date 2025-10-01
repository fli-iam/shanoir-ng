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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This concrete state class defines the state when the shanoir uploader tests
 * the connection to the PACS after having failed with previous configuration.
 *
 * This state is doing the same as the AuthenticationManualConfigurationState.class
 * (except that the view will display dedicated GUI for entering new pacs configuration).
 *  NOTE : THIS IS NOT IMPLEMENTED YET.
 *
 * As a result, the context will change either to :
 *         - a Manual Pacs Configuration in case of failure
 *         - step to the READY state in case of success.
 *
 * NOTE : Currently this new state is always since GUI implementation is not done.
 *
 * @author atouboul
 *
 */
@Component
public class PacsManualConfigurationState implements State {

    @Autowired
    private ReadyState readyState;

    public void load(StartupStateContext context) {
        context.setState(readyState);
        context.nextState();
    }

}
