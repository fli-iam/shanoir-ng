package org.shanoir.uploader.dicom.query;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.shanoir.uploader.utils.IShanoirConfigBean;

/**
 * 
 * @author mkain
 *
 */
public class ConfigBean {

	static Logger logger = Logger.getLogger(ConfigBean.class);

	/** The AET of the Q/R SCP. */
	private String dicomServerAETCalled;

	/** True if ciphering and certificate TLS/3DES enabled. */
	private boolean dicomServerEnableTLS3DES;

	/** IP of the Q/R SCP. */
	private String dicomServerHost;

	/** Key store password of the Q/R SCP. */
	private String dicomServerKeystorePassword;

	/** Key store url of the Q/R SCP. */
	private String dicomServerKeystoreURL;

	/** Port of the Q/R SCP. */
	private int dicomServerPort;

	/** The protocol of the Q/R SCP. */
	private String dicomServerProtocol;

	/** Trust store password of the Q/R SCP. */
	private String dicomServerTruststorePassword;

	/** Trust store url of the Q/R SCP. */
	private String dicomServerTruststoreURL;

	/** The web port of the remote Pacs server. */
	private int dicomServerWebPort;

	/** The local AET of the Q/R SCP. */
	private String localDicomServerAETCalling;

	/** IP of the local Q/R SCP. */
	private String localDicomServerHost;

	/** Port of the local Q/R SCP. */
	private int localDicomServerPort;

	public String getDicomServerAETCalled() {
		return dicomServerAETCalled;
	}

	public String getDicomServerHost() {
		return dicomServerHost;
	}

	public String getDicomServerKeystorePassword() {
		return dicomServerKeystorePassword;
	}

	public String getDicomServerKeystoreURL() {
		return dicomServerKeystoreURL;
	}

	public int getDicomServerPort() {
		return dicomServerPort;
	}

	public String getDicomServerProtocol() {
		return dicomServerProtocol;
	}

	public String getDicomServerTruststorePassword() {
		return dicomServerTruststorePassword;
	}

	public String getDicomServerTruststoreURL() {
		return dicomServerTruststoreURL;
	}

	public int getDicomServerWebPort() {
		return dicomServerWebPort;
	}

	public String getLocalDicomServerAETCalling() {
		return localDicomServerAETCalling;
	}

	public String getLocalDicomServerHost() {
		return localDicomServerHost;
	}

	public int getLocalDicomServerPort() {
		return localDicomServerPort;
	}

	public void initWithShanoirConfigBean(final IShanoirConfigBean configBean) {
		final String host = configBean.getBackupDicomServerHost();
		final int port = configBean.getBackupDicomServerPort();
		final String aetCalled = configBean.getBackupDicomServerCalledAET();
		final int webPort = configBean.getBackupDicomServerWebPort();
		final String protocol = configBean.getBackupDicomServerProtocol();

		final boolean isTLS3DESEnabled = configBean.isBackupDicomServerEnableTls3des();
		final String keystoreURL = configBean.getBackupDicomServerKeyStoreURL();
		final String keystorePassword = configBean.getBackupDicomServerKeyStorePassword();
		final String truststoreURL = configBean.getBackupDicomServerTrustStoreURL();
		final String truststorePassword = configBean.getBackupDicomServerTrustStorePassword();

		logger.info("initWithDefaultValues : host=" + host);
		logger.info("initWithDefaultValues : port=" + port);
		logger.info("initWithDefaultValues : aetCalled=" + aetCalled);
		logger.info("initWithDefaultValues : webPort=" + webPort);
		logger.info("initWithDefaultValues : protocol=" + protocol);

		logger.info("initWithDefaultValues : isTLS3DESEnabled=" + isTLS3DESEnabled);
		logger.info("initWithDefaultValues : keystoreURL=" + keystoreURL);
		logger.info("initWithDefaultValues : keystorePassword=" + keystorePassword);
		logger.info("initWithDefaultValues : truststoreURL=" + truststoreURL);
		logger.info("initWithDefaultValues : truststorePassword=" + truststorePassword);

		setDicomServerHost(host);
		setDicomServerPort(port);
		setDicomServerAETCalled(aetCalled);
		setDicomServerProtocol(protocol);
		setDicomServerWebPort(webPort);
		setDicomServerEnableTLS3DES(isTLS3DESEnabled);
		setDicomServerKeystorePassword(keystorePassword);
		setDicomServerKeystoreURL(keystoreURL);
		setDicomServerTruststorePassword(truststorePassword);
		setDicomServerTruststoreURL(truststoreURL);
	}

	public void initWithPropertiesFile(final Properties properties) {

		// Init called PACS server (ex: NeurInfo-recherche)
		final String host = properties.getProperty("dicom.server.host");
		final int port = toInt(properties.getProperty("dicom.server.port"));
		final String aetCalled = properties.getProperty("dicom.server.aet.called");
		final String protocol = properties.getProperty("dicom.server.protocol");
		final int webPort = toInt(properties.getProperty("dicom.server.web.port"));

		final boolean isTLS3DESEnabled = toBol(properties.getProperty("dicom.server.enableTLS3DES"));
		final String keystoreURL = properties.getProperty("dicom.server.keystore.url");
		final String keystorePassword = properties.getProperty("dicom.server.keystore.password");
		final String truststoreURL = properties.getProperty("dicom.server.truststore.url");
		final String truststorePassword = properties.getProperty("dicom.server.truststore.password");

		setDicomServerHost(host);
		setDicomServerPort(port);
		setDicomServerAETCalled(aetCalled);
		setDicomServerProtocol(protocol);
		setDicomServerWebPort(webPort);

		setDicomServerEnableTLS3DES(isTLS3DESEnabled);
		setDicomServerKeystoreURL(keystoreURL);
		setDicomServerKeystorePassword(keystorePassword);
		setDicomServerTruststoreURL(truststoreURL);
		setDicomServerTruststorePassword(truststorePassword);

		logger.info("initWithValues : host=" + host);
		logger.info("initWithValues : port=" + port);
		logger.info("initWithValues : aetCalled=" + aetCalled);
		logger.info("initWithValues : webPort=" + webPort);
		logger.info("initWithValues : protocol=" + protocol);

		logger.info("initWithValues : isTLS3DESEnabled=" + isTLS3DESEnabled);
		logger.info("initWithValues : keystoreURL=" + keystoreURL);
		logger.info("initWithValues : keystorePassword=" + keystorePassword);
		logger.info("initWithValues : truststoreURL=" + truststoreURL);
		logger.info("initWithValues : truststorePassword=" + truststorePassword);


		// Init calling PACS (Shanoir)
		final String localHost = properties.getProperty("local.dicom.server.host");
		final int localPort = toInt(properties.getProperty("local.dicom.server.port"));
		final String localAETCalling = properties.getProperty("local.dicom.server.aet.calling");

		setLocalDicomServerHost(localHost);
		setLocalDicomServerPort(localPort);
		setLocalDicomServerAETCalling(localAETCalling);
		
		logger.info("initWithValues : localHost=" + localHost);
		logger.info("initWithValues : localPort=" + localPort);
		logger.info("initWithValues : localAETCalling=" + localAETCalling);
	}

	private boolean toBol(final String string) {
		boolean result = false;
		if (string != null && !"".equals(string)) {
			result = Boolean.valueOf(string);
		}
		return result;
	}

	private int toInt(final String string) {
		if (string != null && !"".equals(string)) {
			return Integer.parseInt(string);
		} else {
			return 0;
		}
	}

	public boolean isDicomServerEnableTLS3DES() {
		return dicomServerEnableTLS3DES;
	}

	public void setDicomServerAETCalled(final String dicomServerAETCalled) {
		this.dicomServerAETCalled = dicomServerAETCalled;
	}

	public void setDicomServerEnableTLS3DES(final boolean dicomServerEnableTLS3DES) {
		this.dicomServerEnableTLS3DES = dicomServerEnableTLS3DES;
	}

	public void setDicomServerHost(final String dicomServerHost) {
		this.dicomServerHost = dicomServerHost;
	}

	public void setDicomServerKeystorePassword(final String dicomServerKeystorePassword) {
		this.dicomServerKeystorePassword = dicomServerKeystorePassword;
	}

	public void setDicomServerKeystoreURL(final String dicomServerKeystoreURL) {
		this.dicomServerKeystoreURL = dicomServerKeystoreURL;
	}

	public void setDicomServerPort(final int dicomServerPort) {
		this.dicomServerPort = dicomServerPort;
	}

	public void setDicomServerProtocol(final String dicomServerProtocol) {
		this.dicomServerProtocol = dicomServerProtocol;
	}

	public void setDicomServerTruststorePassword(final String dicomServerTruststorePassword) {
		this.dicomServerTruststorePassword = dicomServerTruststorePassword;
	}

	public void setDicomServerTruststoreURL(final String dicomServerTruststoreURL) {
		this.dicomServerTruststoreURL = dicomServerTruststoreURL;
	}

	public void setDicomServerWebPort(final int dicomServerWebPort) {
		this.dicomServerWebPort = dicomServerWebPort;
	}

	public void setLocalDicomServerAETCalling(String localDicomServerAETCalling) {
		this.localDicomServerAETCalling = localDicomServerAETCalling;
	}

	public void setLocalDicomServerHost(String localDicomServerHost) {
		this.localDicomServerHost = localDicomServerHost;
	}

	public void setLocalDicomServerPort(int localDicomServerPort) {
		this.localDicomServerPort = localDicomServerPort;
	}

}
