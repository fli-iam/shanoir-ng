package org.shanoir.ng.anonymization.uidGenaration;

import java.math.BigInteger;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to generate UID.
 * 
 * @author ifakhfakh
 *
 */
public abstract class UIDGeneration {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(UIDGeneration.class);

	private static final String root = "1.4.9.12.34.1.8527";

	public static String getNewUID() throws uidException {
		String suffix = newSuffix();
		String newUID = root + "." + suffix;
		LOG.debug("newUID = " + newUID);
		return newUID;
	}

	private static String newSuffix() {
		String luuid = String.format("%040d", new BigInteger(UUID.randomUUID().toString().replace("-", ""), 16));
		LOG.debug("suffix = " + luuid);
		return luuid;
	}

}
