package org.shanoir.uploader;

import java.io.File;
import java.io.IOException;

import org.keycloak.adapters.ServerRequest.HttpFailure;
import org.keycloak.adapters.installed.KeycloakInstalled;
import org.keycloak.common.VerificationException;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.shanoir.uploader.dicom.IDicomServerClient;
import org.shanoir.uploader.nominativeData.CurrentNominativeDataController;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClientNG;
import org.shanoir.uploader.service.rest.UrlConfig;
import org.shanoir.uploader.service.soap.ShanoirUploaderServiceClient;

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

	private static JobDataMap jobDataMap = new JobDataMap();

	private static JobDataMap jobDataMapMain = new JobDataMap();

	private static JobDetail uploadServiceJob;

	private static Trigger trigger;

	private static Scheduler scheduler;

	private static CurrentNominativeDataController currentNominativeDataController;

	private static ShanoirUploaderServiceClient shanoirUploaderServiceClient;
	
	private static KeycloakInstalled keycloakInstalled = null;
	
	private static ShanoirUploaderServiceClientNG shanoirUploaderServiceClientNG;

	private static UrlConfig urlConfig = new UrlConfig();

	private static boolean autoImportEnabled;

	private static boolean shanoirNg;

	public static String tokenString;

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

	public static JobDataMap getJobDataMap() {
		return jobDataMap;
	}

	public static void setJobDataMap(JobDataMap jobDataMap) {
		ShUpOnloadConfig.jobDataMap = jobDataMap;
	}

	public static JobDataMap getJobDataMapMain() {
		return jobDataMapMain;
	}

	public static void setJobDataMapMain(JobDataMap jobDataMapMain) {
		ShUpOnloadConfig.jobDataMapMain = jobDataMapMain;
	}

	public static JobDetail getUploadServiceJob() {
		return uploadServiceJob;
	}

	public static void setUploadServiceJob(JobDetail uploadServiceJob) {
		ShUpOnloadConfig.uploadServiceJob = uploadServiceJob;
	}

	public static Trigger getTrigger() {
		return trigger;
	}

	public static void setTrigger(Trigger trigger) {
		ShUpOnloadConfig.trigger = trigger;
	}

	public static Scheduler getScheduler() {
		return scheduler;
	}

	public static void setScheduler(Scheduler scheduler) {
		ShUpOnloadConfig.scheduler = scheduler;
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

	public static boolean isShanoirNg() {
		return shanoirNg;
	}

	public static void setShanoirNg(boolean shanoirNg) {
		ShUpOnloadConfig.shanoirNg = shanoirNg;
	}
	
	public static String getTokenString() throws VerificationException, IOException, HttpFailure  {
		return keycloakInstalled != null ? keycloakInstalled.getTokenString() : tokenString;
	}
	
	public static KeycloakInstalled getKeycloakInstalled() {
		return keycloakInstalled;
	}

	public static void setKeycloakInstalled(KeycloakInstalled keycloakInstalled) {
		ShUpOnloadConfig.keycloakInstalled = keycloakInstalled;
	}

	public static ShanoirUploaderServiceClientNG getShanoirUploaderServiceClientNG() {
		return shanoirUploaderServiceClientNG;
	}

	public static void setShanoirUploaderServiceClientNG(ShanoirUploaderServiceClientNG shanoirUploaderServiceClientNG) {
		ShUpOnloadConfig.shanoirUploaderServiceClientNG = shanoirUploaderServiceClientNG;
	}

}
