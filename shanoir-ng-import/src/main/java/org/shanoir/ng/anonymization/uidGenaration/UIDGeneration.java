package org.shanoir.ng.anonymization.uidGenaration;

import java.math.BigInteger;
import java.util.UUID;

public class UIDGeneration {
	
	private final String root = "1.4.9.12.34.1.8527";

	
	public String getNewUID() throws uidException {
		String suffix = newSuffix();
		String newUID = root + "." + suffix;
		System.out.println("newUID = " + newUID);
		return newUID;
	}


	private String newSuffix() {
		String luuid = String.format("%040d", new BigInteger(UUID.randomUUID().toString().replace("-", ""), 16));
		System.out.println("suffix = " + luuid); 
		return luuid;
	}
	
	

}
