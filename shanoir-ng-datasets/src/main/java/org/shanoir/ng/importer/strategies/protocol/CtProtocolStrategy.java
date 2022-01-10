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

package org.shanoir.ng.importer.strategies.protocol;

import org.dcm4che3.data.Attributes;
import org.shanoir.ng.datasetacquisition.model.ct.CtProtocol;
import org.shanoir.ng.importer.dto.Serie;
import org.springframework.stereotype.Component;

@Component
public class CtProtocolStrategy {

	public CtProtocol generateProtocolForSerie(Attributes attributes, Serie serie) {		
		CtProtocol protocol = new CtProtocol();        
		return protocol;
	}

}
