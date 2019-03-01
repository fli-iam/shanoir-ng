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

package org.shanoir.ng.anonymization.uid.generation;

import java.math.BigInteger;
import java.util.UUID;

public class UIDGeneration {
	
	private final String root = "1.4.9.12.34.1.8527";

	
	public String getNewUID() throws UIDException {
		String suffix = newSuffix();
		String newUID = root + "." + suffix;
		return newUID;
	}


	private String newSuffix() {
		String luuid = String.format("%040d", new BigInteger(UUID.randomUUID().toString().replace("-", ""), 16));
		return luuid;
	}
	
	

}
