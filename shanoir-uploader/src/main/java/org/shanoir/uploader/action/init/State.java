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

/**
 * This class defines the interface State.
 *
 * The concretes states implementing classes are :
 *  {@link InitialStartupState}
 *  {@link ProxyConfigurationState}
 *  {@link ProxyManualConfigurationState}
 *     {@link AuthenticationConfigurationState}
 *  {@link AuthenticationManualConfigurationState}
 *  {@link WSDLConfigurationState}
 *  {@link PacsConfigurationState}
 *  {@link PacsManualConfigurationState}
 *  {@link ServerUnreachableState}
 *  {@link ReadyState}
 *
 *  Any question on this implementation : please refer to the design pattern State.
 *
 */
public interface State {

    public void load(StartupStateContext context) throws Exception;

}
