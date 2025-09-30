package org.shanoir.uploader;

import java.io.File;

import org.shanoir.uploader.dicom.IDicomServerClient;
import org.shanoir.uploader.dicom.anonymize.Pseudonymizer;
import org.shanoir.uploader.nominativeData.CurrentNominativeDataController;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClient;
import org.shanoir.uploader.service.rest.UrlConfig;

/**
 *
 * This class contains all dynamic data needed by ShanoirUploader upon startup.
 *
 * @author atouboul
 * @author mkain
 *
 */
public class ShUpOnloadConfig {

	private static File workFolder;

	private static IDicomServerClient dicomServerClient;

	private static CurrentNominativeDataController currentNominativeDataController;

	private static ShanoirUploaderServiceClient shanoirUploaderServiceClient;

	private static Pseudonymizer pseudonymizer;

	private static UrlConfig urlConfig = new UrlConfig();

	private static boolean autoImportEnabled;

	private static String tokenString;

	/** Constructeur privé */
	private ShUpOnloadConfig() {
	}

	/** Instance unique pré-initialisée */
	private static ShUpOnloadConfig INSTANCE = new ShUpOnloadConfig();

	/** Point d'accès pour l'instance unique du singleton */
	public static ShUpOnloadConfig getInstance() {
		return INSTANCE;
	}

	public static File getWorkFolder() {
		return workFolder;
	}

	public static void setWorkFolder(File workFolder) {
		ShUpOnloadConfig.workFolder = workFolder;
	}

	public static IDicomServerClient getDicomServerClient() {
		return dicomServerClient;
	}

	public static void setDicomServerClient(IDicomServerClient dicomServerClient) {
		ShUpOnloadConfig.dicomServerClient = dicomServerClient;
	}

	public static CurrentNominativeDataController getCurrentNominativeDataController() {
		return currentNominativeDataController;
	}

	public static void setCurrentNominativeDataController(
			CurrentNominativeDataController currentNominativeDataController) {
		ShUpOnloadConfig.currentNominativeDataController = currentNominativeDataController;
	}

	public static ShanoirUploaderServiceClient getShanoirUploaderServiceClient() {
		return shanoirUploaderServiceClient;
	}

	public static void setShanoirUploaderServiceClient(ShanoirUploaderServiceClient shanoirUploaderServiceClient) {
		ShUpOnloadConfig.shanoirUploaderServiceClient = shanoirUploaderServiceClient;
	}

	public static UrlConfig getUrlConfig() {
		return urlConfig;
	}

	public static void setUrlConfig(UrlConfig urlConfig) {
		ShUpOnloadConfig.urlConfig = urlConfig;
	}

	public static boolean isAutoImportEnabled() {
		return autoImportEnabled;
	}

	public static void setAutoImportEnabled(boolean autoImportEnabled) {
		ShUpOnloadConfig.autoImportEnabled = autoImportEnabled;
	}

	public static String getTokenString() throws Exception  {
		return tokenString;
	}

	public static void setTokenString(String tokenString) {
		ShUpOnloadConfig.tokenString = tokenString;
	}

	public static Pseudonymizer getPseudonymizer() {
		return pseudonymizer;
	}

	public static void setPseudonymizer(Pseudonymizer pseudonymizer) {
		ShUpOnloadConfig.pseudonymizer = pseudonymizer;
	}

}
