package org.shanoir.downloader;

import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClientNG;
import org.shanoir.uploader.utils.ProxyUtil;
import org.shanoir.uploader.utils.Util;
import org.springframework.http.HttpHeaders;

/**
 * This class intends to be used as a binary executable to download datasets
 * from a remote Shanoir server to the local file system.
 *
 * @author aferial
 */
public final class ShanoirDownloader extends ShanoirCLI {

	/** -datasetId to set the id of the dataset to download. */
	private static Option datasetIdOption;

	/** -subjectId to set the id of the subject to download. */
	private static Option subjectIdOption;

	/** -studyId to set the id of the study to download. */
	private static Option studyIdOption;

	/** The Constant DESCRIPTION. */
	private static final String DESCRIPTION = "Import dataset with the specified ID to the given destination dir.\n"
			+ "Options:";

	/** -destDir to set the destination directory. */
	private static Option destDirOption;

	/** The Constant EXAMPLE. */
	private static final String EXAMPLE = "downloadDataset -destDir /tmp/dataset123 -host shanoir-qualif.irisa.fr -datasetId 123\n"
			+ "=> download the dataset 123 to the destination directory /tmp/dataset123.";
	/**
	 * -refDatasetExpressionFormatId to set the id of the ref dataset expression
	 * format.
	 */
	private static Option formatIdOption;

	/** -h used to request help on command line options. */
	private static Option helpOption;
	/** The Constant USAGE. */
	private static final String USAGE = "downloadDataset [Options] -datasetId <ID> -host <HOST>";

	private static final String NG_PROFILE = "dev-NG";

	/** -v returns the version of the application. */
	private static Option versionOption;
	static {
		OptionBuilder.hasArg(false);
		OptionBuilder.withDescription("Print help for this application");
		helpOption = OptionBuilder.create("h");
	}

	static {
		OptionBuilder.hasArg(false);
		OptionBuilder.withDescription("print the version information and exit");
		versionOption = OptionBuilder.create("v");
	}
	static {
		OptionBuilder.withArgName("datasetId");
		OptionBuilder.hasArg();
		OptionBuilder.isRequired(false);
		OptionBuilder.withDescription("The dataset id.");
		datasetIdOption = OptionBuilder.create("datasetId");
	}

	static {
		OptionBuilder.withArgName("subjectId");
		OptionBuilder.hasArg();
		OptionBuilder.isRequired(false);
		OptionBuilder.withDescription("The subject id.");
		subjectIdOption = OptionBuilder.create("subjectId");
	}

	static {
		OptionBuilder.withArgName("studyId");
		OptionBuilder.hasArg();
		OptionBuilder.isRequired(false);
		OptionBuilder.withDescription("The study id.");
		studyIdOption = OptionBuilder.create("studyId");
	}

	static {
		OptionBuilder.withArgName("formatId");
		OptionBuilder.hasArg();
		OptionBuilder.isRequired(false);
		OptionBuilder.withDescription(
				"The ref dataset expression format id. Default is Nifti. User ListReference service to see available RefDatasetExpressionFormats.");
		formatIdOption = OptionBuilder.create("formatId");
	}
	static {
		OptionBuilder.withArgName("destDir");
		OptionBuilder.hasArg();
		OptionBuilder.withDescription("Destination directory, " + SystemUtils.JAVA_IO_TMPDIR + " by default.");
		destDirOption = OptionBuilder.create("destDir");
	}

	/**
	 * Main method.
	 *
	 * @param args
	 *            the args
	 *
	 * @throws ParserConfigurationException
	 *             the parser configuration exception
	 */
	public static void main(final String[] args) throws ParserConfigurationException {

		if (java.util.Arrays.asList(args).contains("-sslDisableVerifier")) {
			HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
				public boolean verify(String s, SSLSession sslSession) {
					return true;
				}
			});

		}

		// Define the options needed in addition of those from the generic
		// ShanoirTkCLI
		Options opts = new Options();
		opts.addOption(helpOption);
		opts.addOption(versionOption);
		opts.addOption(datasetIdOption);
		opts.addOption(subjectIdOption);
		opts.addOption(studyIdOption);
		opts.addOption(destDirOption);
		opts.addOption(formatIdOption);

		ShanoirDownloader shanoirDownloader = new ShanoirDownloader(opts);

		try {
			shanoirDownloader.parse(args);
			shanoirDownloader.initialize();
			shanoirDownloader.download();
		} catch (MissingArgumentException e) {
			exit(e.getMessage());
		} catch (java.text.ParseException e) {
			exit(e.getMessage());
		} catch (DatatypeConfigurationException e) {
			exit(e.getMessage());
		}

		System.exit(0);
	}

	/** Our business Service. */
	private ShanoirUploaderServiceClientNG shanoirUploaderServiceClientNG;

	/**
	 * @param opts
	 *            the specific options of the command line
	 */
	public ShanoirDownloader(final Options opts) {
		super(opts, DESCRIPTION, EXAMPLE, USAGE);
	}

	public void initialize() {
		initProperties(ShUpConfig.BASIC_PROPERTIES, ShUpConfig.basicProperties);
		// setup proxy on using proxy.properties in .su_v7.0.1 normally, if not from .jar
		initProperties(ShUpConfig.PROXY_PROPERTIES, ShUpConfig.proxyProperties);
		ProxyUtil.initializeSystemProxy();
		initProperties(ShUpConfig.PROFILE_DIR + NG_PROFILE + "/" + ShUpConfig.PROFILE_PROPERTIES,
				ShUpConfig.profileProperties);
		ShUpConfig.profileProperties.setProperty("shanoir.server.url", getHost());
	}

	/**
	 * Reads properties from .su folder into memory, or copies property file if not
	 * existing.
	 */
	private void initProperties(final String fileName, final Properties properties) {
		final File propertiesFile = new File(ShUpConfig.shanoirUploaderFolder + File.separator + fileName);
		if (propertiesFile.exists()) {
			// do nothing
		} else {
			Util.copyFileFromJar(fileName, propertiesFile);
		}
		loadPropertiesFromFile(properties, propertiesFile);
	}
	
	private void loadPropertiesFromFile(final Properties properties, final File propertiesFile) {
		try {
			final FileInputStream fIS = new FileInputStream(propertiesFile);
			properties.load(fIS);
			fIS.close();
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	public static void saveResponseToFile(File destDir, HttpResponse response) throws IOException {
		Header header = response.getFirstHeader(HttpHeaders.CONTENT_DISPOSITION);
		String fileName = header.getValue();
		fileName = fileName.replace("attachment;filename=", "");

		final File downloadedFile = new File(destDir + "/" + fileName);

		FileUtils.copyInputStreamToFile(response.getEntity().getContent(), downloadedFile);
	}

	public static String downloadDataset(File destDir, Long datasetId, String format,
			ShanoirUploaderServiceClientNG shng) throws Exception {
		System.out.println("Downloading dataset " + datasetId + "...");
		HttpResponse response = shng.downloadDatasetById(datasetId, format);
		String message = "";
		if (response == null) {
			message = "Dataset with id " + datasetId + " not found.";
			System.out.println(message);
			return message;
		}

		saveResponseToFile(destDir, response);
		return message;
	}

	public static String downloadDatasets(File destDir, List<Long> datasetIds, String format,
			ShanoirUploaderServiceClientNG shng) throws Exception {
		System.out.println("Downloading dataset " + datasetIds + "...");
		HttpResponse response = shng.downloadDatasetsByIds(datasetIds, format);
		String message = "";
		if (response == null) {
			if (datasetIds.size() > 0) {
				String datasetIdsString = datasetIds.stream().map(Object::toString).collect(Collectors.joining(", "));
				message = "Datasets with ids [" + datasetIdsString + "] not found.";
			} else {
				message = "Could not get datasets: no dataset ids provided.";
			}

			System.out.println(message);
			return message;
		}

		saveResponseToFile(destDir, response);
		return message;
	}

	public static String downloadDatasetByStudy(File destDir, Long studyId, String format,
			ShanoirUploaderServiceClientNG shng) throws Exception {
		HttpResponse response = shng.downloadDatasetsByStudyId(studyId, format);
		String message = "";
		if (response == null) {
			message = "Datasets of study " + studyId + " not found.";
			System.out.println(message);
			return message;
		}

		saveResponseToFile(destDir, response);
		return message;
	}

	public static String downloadDatasetBySubject(File destDir, Long subjectId, String format,
			ShanoirUploaderServiceClientNG shng) throws Exception {
		List<Long> datasetIds = shng.findDatasetIdsBySubjectId(subjectId);
		String message = "";
		if (datasetIds == null) {
			message = "No dataset found.";
			System.out.println(message);
			return message;
		}
		message = downloadDatasets(destDir, datasetIds, format, shng);
		return message;
	}

	public static String downloadDatasetBySubjectIdStudyId(File destDir, Long subjectId, Long studyId, String format,
			ShanoirUploaderServiceClientNG shng) throws Exception {
		List<Long> datasetIds = shng.findDatasetIdsBySubjectIdStudyId(subjectId, studyId);
		String message = "";
		if (datasetIds == null) {
			message = "No dataset found.";
			System.out.println(message);
			return message;
		}
		message = downloadDatasets(destDir, datasetIds, format, shng);
		return message;
	}

	/**
	 * This method download Dataset corresponding to the properties set by the user.
	 */
	private void download() {
		String[] args = cl.getArgs();
		for (String arg : args) {
			System.out.println(
					"WARNING: invalid " + arg + " argument. Try -" + arg + " (with the '-' character) instead.");
		}

		File destDir = new File(SystemUtils.JAVA_IO_TMPDIR);
		if (cl.hasOption("destDir")) {
			destDir = new File(cl.getOptionValue("destDir"));
			if (!destDir.isDirectory()) {
				throw new IllegalArgumentException("Destination is not a directory! destDir: " + destDir);
			}
		}

		shanoirUploaderServiceClientNG = new ShanoirUploaderServiceClientNG();
		
		String user = cl.getOptionValue("user");
		String password = cl.getOptionValue("password");
		if(password == null) {
			Console console = System.console();
			char[] passwordArray = console.readPassword("Enter your Shanoir password: ");
			password = new String(passwordArray);
		}
		String token = shanoirUploaderServiceClientNG.loginWithKeycloakForToken(user, password);
		
		if (token != null) {
			ShUpOnloadConfig.setTokenString(token);
		} else {
			System.out.println("ERROR: login not successful.");
		}

		try {
			String format = "nii";

			if (cl.hasOption("formatId")) {
				format = Long.parseLong(cl.getOptionValue("formatId")) == 6 ? "dcm" : "nii";
			}

			if (cl.hasOption("datasetId")) {
				Long datasetId = Long.parseLong(cl.getOptionValue("datasetId"));
				downloadDataset(destDir, datasetId, format, shanoirUploaderServiceClientNG);

			} else {

				if (cl.hasOption("studyId") && !cl.hasOption("subjectId")) {
					Long studyId = Long.parseLong(cl.getOptionValue("studyId"));
					downloadDatasetByStudy(destDir, studyId, format, shanoirUploaderServiceClientNG);
				}

				if (cl.hasOption("subjectId") && !cl.hasOption("studyId")) {
					Long subjectId = Long.parseLong(cl.getOptionValue("subjectId"));
					downloadDatasetBySubject(destDir, subjectId, format, shanoirUploaderServiceClientNG);
				}

				if (cl.hasOption("subjectId") && cl.hasOption("studyId")) {
					Long studyId = Long.parseLong(cl.getOptionValue("studyId"));
					Long subjectId = Long.parseLong(cl.getOptionValue("subjectId"));
					downloadDatasetBySubjectIdStudyId(destDir, subjectId, studyId, format,
							shanoirUploaderServiceClientNG);
				}

			}

		} catch (NumberFormatException e) {
			e.printStackTrace();
			exit("Download failed: could not parse your dataset, subject or study id, make sure it only contains numbers.");
		} catch (Exception e) {
			e.printStackTrace();
			exit("Download failed: " + e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.shanoir.toolkit.ShanoirTkCLI#postParse()
	 */
	@Override
	protected void postParse() throws MissingArgumentException, DatatypeConfigurationException {
		if (cl.hasOption("studyId") && cl.hasOption("subjectId") && cl.hasOption("datasetId")) {
			exit("Either -datasetId -subjectId or -studyId is required");
		}
	}

}