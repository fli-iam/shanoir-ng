package org.shanoir.uploader.dicom.anonymize;

import java.io.File;
import java.util.GregorianCalendar;

import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.action.DicomDataTransferObject;
import org.shanoir.uploader.exception.PseudonymusException;
import org.shanoir.util.runtime.exec.StreamGobbler;

/**
 * This class is used for pseudonymization. Pseudonymization is a data management and
 * de-identification procedure by which personally identifiable information fields within
 * a data record are replaced by one or more artificial identifiers, or pseudonyms.
 * 
 * The pseudonymizer does not access to DICOM files. It retrieves data records in the
 * memory and replaces specific ones with pseudonyms.
 * 
 * In the Shanoir server the common name is displayed and used as patient ID in the GUI.
 * For Neurinfo during the import users can choose the common name == newPatientID.
 * For the OFSEP server this is predefined during the import, as an incremental counter.
 * Example: 0050362
 * The subjectIdentifier is an internal hash in the Shanoir database, that is normally
 * not displayed to the user.
 * Example: long hash value
 * 
 * @author mkain
 *
 */
public class Pseudonymizer {
	
	private static final String DEBUG = "DEBUG";

	private static Logger logger = Logger.getLogger(Pseudonymizer.class);

	public static final String PSEUDONYMUS_FOLDER = "pseudonymus";
	private static final String PSEUDONYMUS_SHANOIR = "PseudonymusShanoir";
	private static final String PSEUDONYMUS_SHANOIR_EXE = "PseudonymusShanoir.exe";

	private static final int SHA256_LENGTH = 64;
	
	private static final String I386 = "i386";
	private static final String X86 = "x86";
	private static final String X86_64 = "x86_64";
	private static final String AMD64 = "amd64";

	private static final String WINDOWS = "Windows";
	private static final String MAC_OSX = "MacOSX";
	private static final String LINUX_I386 = "Linux_i386";
	private static final String LINUX_X86_64 = "Linux_x86_64";

	private String pseudonymusKey;
	private String pseudonymusExePath;

	public Pseudonymizer(final String pseudonymusKey, final String pseudonymusFolderPath) throws PseudonymusException {
		logger.info("Pseudonymizer: initialization started.");
		this.pseudonymusKey = pseudonymusKey;
		final File pseudonymusFolder = new File(pseudonymusFolderPath);
		if (!pseudonymusFolder.exists()) {
			throw new PseudonymusException("Pseudonymus folder not existing: " + pseudonymusFolder.getAbsolutePath());
		}
		logger.info("Pseudonymizer: pseudonymus used in OS: "
				+ SystemUtils.OS_NAME);
		logger.info("Pseudonymizer: pseudonymus used in OS version: "
				+ SystemUtils.OS_VERSION);
		logger.info("Pseudonymizer: pseudonymus used in OS architecture: "
				+ SystemUtils.OS_ARCH);
		if (SystemUtils.IS_OS_LINUX) {
			if (I386.equals(SystemUtils.OS_ARCH)) {
				this.pseudonymusExePath = pseudonymusFolderPath + File.separator
						+ LINUX_I386 + File.separator + PSEUDONYMUS_SHANOIR;
			} else if (X86.equals(SystemUtils.OS_ARCH)
					|| X86_64.equals(SystemUtils.OS_ARCH)
					|| AMD64.equals(SystemUtils.OS_ARCH)) {
				this.pseudonymusExePath = pseudonymusFolderPath + File.separator
						+ LINUX_X86_64 + File.separator
						+ PSEUDONYMUS_SHANOIR;
			} else {
				logger.error("Pseudonymizer: Linux system not supported by pseudonymus.");
			}
		} else if (SystemUtils.IS_OS_MAC_OSX) {
			this.pseudonymusExePath = pseudonymusFolderPath + File.separator
					+ MAC_OSX + File.separator + PSEUDONYMUS_SHANOIR;
		} else if (SystemUtils.IS_OS_WINDOWS) {
			this.pseudonymusExePath = pseudonymusFolderPath + File.separator
					+ WINDOWS + File.separator + PSEUDONYMUS_SHANOIR_EXE;
		} else {
			logger.error("Pseudonymizer: operating system not supported by pseudonymus.");
		}
		logger.info("Pseudonymizer: pseudonymus exe path: " + pseudonymusExePath);
		logger.info("Pseudonymizer: initialization finished.");
	}
	
	/**
	 * UTILISATION DE PSEUDONYMUS Le format des dates est bien celui que tu as
	 * indiqué : JJ/MM/AAAA. Pour ce qui concerne le lancement de la fonction
	 * elle-même, tu dois l’appeler pour : - Nom de naissance, nom et prénom :
	 * avec à chaque fois trois appels - Date de naissance : un appel Au final,
	 * tu dois donc stocker 10 variables. De préférence, quand tu me les
	 * transmettra (mais tu fais comme tu le souhaites au moment du stockage),
	 * ce serait bien si elles pouvaient avoir ces noms, dans cet ordre :
	 * birthNameHash_1 birthNameHash_2 birthNameHash_3 lastNameHash_1
	 * lastNameHash_2 lastNameHash_3 firstNameHash_1 firstNameHash_2
	 * firstNameHash_3 birthDateHash, avec 1 pour le traitement de la chaine
	 * brute, 2 pour soundex simple et 3 pour soundex avancé.
	 */
	public DicomDataTransferObject createHashValuesWithPseudonymus(final DicomDataTransferObject dicomData)
			throws PseudonymusException {
		/**
		 * Use pseudonymus to create hash values for all values.
		 */
		String birthNameHash1 = pseudonymusExec(dicomData.getBirthName(),
				pseudonymusExePath, 0);
		String birthNameHash2 = pseudonymusExec(dicomData.getBirthName(),
				pseudonymusExePath, 1);
		String birthNameHash3 = pseudonymusExec(dicomData.getBirthName(),
				pseudonymusExePath, 2);
		String lastNameHash1 = pseudonymusExec(dicomData.getLastName(),
				pseudonymusExePath, 0);
		String lastNameHash2 = pseudonymusExec(dicomData.getLastName(),
				pseudonymusExePath, 1);
		String lastNameHash3 = pseudonymusExec(dicomData.getLastName(),
				pseudonymusExePath, 2);
		String firstNameHash1 = pseudonymusExec(dicomData.getFirstName(),
				pseudonymusExePath, 0);
		String firstNameHash2 = pseudonymusExec(dicomData.getFirstName(),
				pseudonymusExePath, 1);
		String firstNameHash3 = pseudonymusExec(dicomData.getFirstName(),
				pseudonymusExePath, 2);
		final GregorianCalendar birthDateCal = new GregorianCalendar();
		birthDateCal.setTime(dicomData.getBirthDate());
		final String birthDate = ShUpConfig.formatter.format(birthDateCal.getTime());
		final String birthDateHash = pseudonymusExec(birthDate, pseudonymusExePath, 0);
		/**
		 * Store all created hash values in DTO.
		 */
		dicomData.setBirthNameHash1(birthNameHash1);
		dicomData.setBirthNameHash2(birthNameHash2);
		dicomData.setBirthNameHash3(birthNameHash3);
		dicomData.setLastNameHash1(lastNameHash1);
		dicomData.setLastNameHash2(lastNameHash2);
		dicomData.setLastNameHash3(lastNameHash3);
		dicomData.setFirstNameHash1(firstNameHash1);
		dicomData.setFirstNameHash2(firstNameHash2);
		dicomData.setFirstNameHash3(firstNameHash3);
		dicomData.setBirthDateHash(birthDateHash);
		
		/**
		 * Log all created hash values into su.log file.
		 */
		logger.info("BirthName hashs: " + birthNameHash1 + ";" + birthNameHash2 + ";" + birthNameHash3);
		logger.info("LastName hashs: " + lastNameHash1 + ";" + lastNameHash2 + ";" + lastNameHash3);
		logger.info("FirstName hashs: " + firstNameHash1 + ";" + firstNameHash2 + ";" + firstNameHash3);
		logger.info("BirthDate hash: " + birthDateHash);

		/**
		 * Alert the user if an error occured during pseudonymus hash creation.
		 */
		if ((birthNameHash1 == null || "".equals(birthNameHash1) || birthNameHash1.contains(DEBUG))
			|| (birthNameHash2 == null || "".equals(birthNameHash2) || birthNameHash2.contains(DEBUG))
			|| (birthNameHash3 == null || "".equals(birthNameHash3) || birthNameHash3.contains(DEBUG))
			|| (lastNameHash1 == null || "".equals(lastNameHash1) || lastNameHash1.contains(DEBUG))
			|| (lastNameHash2 == null || "".equals(lastNameHash2) || lastNameHash2.contains(DEBUG))
			|| (lastNameHash3 == null || "".equals(lastNameHash3) || lastNameHash3.contains(DEBUG))
			|| (firstNameHash1 == null || "".equals(firstNameHash1) || firstNameHash1.contains(DEBUG))
			|| (firstNameHash2 == null || "".equals(firstNameHash2) || firstNameHash2.contains(DEBUG))
			|| (firstNameHash3 == null || "".equals(firstNameHash3) || firstNameHash3.contains(DEBUG))
			|| (birthDateHash == null || "".equals(birthDateHash) || birthDateHash.contains(DEBUG))) {
			throw new PseudonymusException("Some hash fields are malformed!");
		}
		return dicomData;
	}

	/**
	 * Exec the Pseudonymus command to hash DICOM values
	 * 
	 * @param input
	 *            the DICOM value we want to hash
	 * @param pseudonymusPath
	 *            the Pseudonymus path
	 * @param soundexValue
	 *            the soundex value
	 * 
	 * @return the hash
	 */
	private String pseudonymusExec(final String input,
			final String pseudonymusPath, final int soundexValue) {
		logger.debug("pseudonymusExec : Begin");
		logger.debug("pseudonymusExec : " + pseudonymusPath);
		String[] cmd = new String[4];
		cmd[0] = pseudonymusPath;
		cmd[1] = input;
		cmd[2] = Integer.toString(soundexValue);
		cmd[3] = this.pseudonymusKey;
		String result = exec(cmd, null);
		// cut of "DEBUG : ", which is added by StreamGobbler
		if (result != null && SHA256_LENGTH < result.length()) {
			result = result.substring(result.length() - SHA256_LENGTH - 1,
					result.length() - 1);
		}
		logger.debug("pseudonymusExec : End");
		return result;
	}
	
	/**
	 * Execute the command line given in argument. This method has been duplicated
	 * here and is not used anymore from shanoir.jar - ShanoirExec, as it added an
	 * exception into the log "java.lang.IllegalStateException: No active conversation
	 * context" as the seam context is missing, what is correct, but not nice in the log.
	 *
	 * @param cmd
	 *            the command line as a string array
	 * @param envp
	 *            array of strings, each element of which has environment
	 *            variable settings in the format name=value, or null if the
	 *            subprocess should inherit the environment of the current
	 *            process.
	 *
	 * @return the output result
	 */
	public static String exec(final String[] cmd, final String[] envp) {
		String executingCommand = "";
		for (final String item : cmd) {
			executingCommand += (item + " ");
		}
		logger.debug("exec : Executing " + executingCommand);
		StreamGobbler errorGobbler = null;
		StreamGobbler outputGobbler = null;
		String result = null;
		try {
			Runtime rt = Runtime.getRuntime();
			final Process proc = rt.exec(cmd, envp);
			// any error message?
			errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");
			// any output?
			outputGobbler = new StreamGobbler(proc.getInputStream(), "DEBUG");
			// kick them off
			errorGobbler.start();
			outputGobbler.start();
			// any error???
			final int exitVal = proc.waitFor();
			// let errorGobbler and outputGobbler finish execution before finishing main thread
			errorGobbler.join();
			outputGobbler.join();
			proc.getInputStream().close();
			proc.getErrorStream().close();
			logger.debug("exec : ExitValue: " + exitVal);
		} catch (final Exception exc) {
			logger.error("exec : ", exc);
			if (errorGobbler != null && outputGobbler != null) {
				result = errorGobbler.getStringDisplay();
				if (result != null && !"".equals(result)) {
					result += "\n\n";
				}
				result += outputGobbler.getStringDisplay();
				errorGobbler = null;
				outputGobbler = null;
				return result;
			}
		}
		if (errorGobbler != null) {
			result = errorGobbler.getStringDisplay();
			if (result != null && !"".equals(result)) {
				result += "\n\n";
			}
		}
		if (outputGobbler != null) {
			result += outputGobbler.getStringDisplay();
		}
		logger.debug("exec : return result " + result);
		return result;
	}
	
}
