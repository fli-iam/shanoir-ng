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
import java.util.Random;
import java.util.UUID;

public class UIDGeneration {

	private static final String ROOT = "1.4.9.12.34.1.8527";

	private Random rand = new Random();

	public String getNewUID() {
		String suffix = newSuffix();
		return ROOT + "." + suffix;
	}

	private String newSuffix() {
		String uUID = UUID.randomUUID().toString();
		uUID = uUID.replace("-", "");
		BigInteger bigInt = new BigInteger(uUID, 16);
		String lUUID = String.format("%040d", bigInt);
		// starting zero is not allowed in components of UIDs in DICOM
		// http://dicom.nema.org/dicom/2013/output/chtml/part05/chapter_9.html
		// Each component of a UID is a number and shall consist of one or more digits.
		// The first digit of each component shall not be zero unless the component is a single digit.
		if (lUUID.startsWith("0")) {
			int randomBetweenOneAndNine = rand.nextInt(9) + 1;
			lUUID = lUUID.replaceFirst("0", Integer.toString(randomBetweenOneAndNine));
		}
		return lUUID;
	}

}
