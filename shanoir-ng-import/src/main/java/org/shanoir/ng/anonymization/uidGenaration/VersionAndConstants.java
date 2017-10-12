package org.shanoir.ng.anonymization.uidGenaration;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

/**
 * <p>Various pre-defined constants for identifying this software.</p>
 *
 * @author  dclunie
 */

public class VersionAndConstants {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/dicom/VersionAndConstants.java,v 1.8 2011/07/09 15:34:41 dclunie Exp $";

	/***/
	public static final String softwareVersion = "001"; // must be [A-Z0-9_] and
														// <= 4 chars else
														// screws up
														// ImplementationVersionName

	/***/
	public static final String implementationVersionName = "PIXELMEDJAVA" + softwareVersion;

	public static final String uidRoot = "1.3.6.1.4.1.5962";
	/***/
	public static final String uidQualifierForThisToolkit = "99";
	/***/
	public static final String uidQualifierForUIDGenerator = "1";
	/***/
	public static final String uidQualifierForImplementationClassUID = "2";
	/***/
	public static final String uidQualifierForInstanceCreatorUID = "3";
	/***/
	public static final String implementationClassUID = uidRoot + "." + uidQualifierForThisToolkit + "."
			+ uidQualifierForImplementationClassUID;
	/***/
	public static final String instanceCreatorUID = uidRoot + "." + uidQualifierForThisToolkit + "."
			+ uidQualifierForInstanceCreatorUID;

	public static final String releaseString = "General Release";

	/**
	 * <p>
	 * Get the date the package was built.
	 * </p>
	 *
	 * @return the build date
	 */
	public static String getBuildDate() {
		String buildDate = "";
		try {
			buildDate = (new BufferedReader(
					new InputStreamReader(VersionAndConstants.class.getResourceAsStream("/BUILDDATE")))).readLine();
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
		return buildDate;
	}

}
