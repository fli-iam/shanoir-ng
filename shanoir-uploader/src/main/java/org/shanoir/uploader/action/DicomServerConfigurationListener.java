package org.shanoir.uploader.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.dcm4che2.tool.dcmecho.DcmEcho;
import org.shanoir.uploader.ShUpConfig;
//import org.shanoir.uploader.crypto.BlowfishAlgorithm;
import org.shanoir.uploader.gui.DicomServerConfigurationWindow;

/**
 * This class implements the logic when a new DICOM server configuration is
 * introduced in the DicomServerConfigurationWindow class
 * 
 * @author ifakhfakh
 * 
 */

public class DicomServerConfigurationListener implements ActionListener {
	private static final String DCMECHO = "DCMECHO";
	private static Logger logger = Logger.getLogger(DicomServerConfigurationListener.class);

	DicomServerConfigurationWindow dicomWindow;

	public DicomServerConfigurationListener(DicomServerConfigurationWindow dicomWindow) {
		this.dicomWindow = dicomWindow;
	}

	public void actionPerformed(ActionEvent event) {

		String remoteHost = dicomWindow.hostNameTF.getText();
		String remotePortString = dicomWindow.portTF.getText();
		String calledAET = dicomWindow.aetTF.getText();
		String localHost = dicomWindow.hostNameLocalPACSTF.getText();
		String localPortString = dicomWindow.portLocalPACSTF.getText();
		String localAET = dicomWindow.aetLocalPACSTF.getText();
		ResourceBundle resourceBundle = dicomWindow.resourceBundle;

		// Echo button
		if (event.getSource() == dicomWindow.echoButton) {
			// check configuration parameters
			boolean configurationParametersOK = checkFormEchoParameters(remoteHost, remotePortString, calledAET,
					resourceBundle);
			if (configurationParametersOK) {
				boolean connexionEstablished = echo(remoteHost, remotePortString, calledAET);
				if (connexionEstablished) {
					String message = "<html><b>"
							+ resourceBundle.getString(
									"shanoir.uploader.configurationMenu.dicomServer.echo.succeeded.message.part1")
							+ "</html>" + "\n" + "\n" + "<html> <b>"
							+ resourceBundle.getString(
									"shanoir.uploader.configurationMenu.dicomServer.echo.succeeded.message.part2")
							+ "</html>";
					JOptionPane.showMessageDialog(new JFrame(), message,
							resourceBundle
									.getString("shanoir.uploader.configurationMenu.dicomServer.echo.succeeded.title"),
							JOptionPane.INFORMATION_MESSAGE);
				} else {
					String message = "<html><b>" + resourceBundle.getString(
							"shanoir.uploader.configurationMenu.dicomServer.echo.failed.message") + "</html>";
					JOptionPane.showMessageDialog(new JFrame(), message,
							resourceBundle
									.getString("shanoir.uploader.configurationMenu.dicomServer.echo.failed.title"),
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		// Configure button
		if (event.getSource() == dicomWindow.configureButton) {
			logger.info("Dicom Server configuration: Starting...");

			String fileName = dicomWindow.shanoirUploaderFolder + File.separator + ShUpConfig.DICOM_SERVER_PROPERTIES;

			final File propertiesFile = new File(fileName);
			boolean propertiesExists = propertiesFile.exists();
			if (propertiesExists) {

				boolean configurationParametersOK = checkFormConfigureParameters(remoteHost, remotePortString,
						calledAET, localHost, localPortString, localAET, resourceBundle);
				if (configurationParametersOK) {
					if (echo(remoteHost, remotePortString, calledAET)) {

						try {
							Properties props = ShUpConfig.dicomServerProperties;
							props.setProperty("dicom.server.host", remoteHost);
							props.setProperty("dicom.server.port", remotePortString);
							props.setProperty("dicom.server.aet.called", calledAET);

							// Local PACS parameters
							props.setProperty("local.dicom.server.aet.calling", localAET);
							props.setProperty("local.dicom.server.host", localHost);
							props.setProperty("local.dicom.server.port", localPortString);

							// Store the new configuration in the
							// dicom_server.properties file
							OutputStream out = new FileOutputStream(propertiesFile);
							props.store(out, "DICOM Server Configuration");

							String message = "<html>" + resourceBundle.getString(
									"shanoir.uploader.configurationMenu.dicomServer.configure.succeeded.message.part1")
									+ "</html>" + "\n" + "\n" + "<html> <b> "
									+ resourceBundle.getString(
											"shanoir.uploader.configurationMenu.dicomServer.configure.succeeded.message.part2")
									+ "</html>" + "\n";
							JOptionPane.showMessageDialog(new JFrame(), message,
									resourceBundle.getString(
											"shanoir.uploader.configurationMenu.dicomServer.configure.succeeded.title"),
									JOptionPane.INFORMATION_MESSAGE);
						} catch (Exception e) {
							logger.error("Failed to configure connexion :");
							logger.error(e.getMessage(), e);
						}
					} else {
						String message = resourceBundle
								.getString("shanoir.uploader.configurationMenu.dicomServer.configure.failed.message");
						JOptionPane.showMessageDialog(new JFrame(), message,
								resourceBundle.getString(
										"shanoir.uploader.configurationMenu.dicomServer.configure.failed.title"),
								JOptionPane.WARNING_MESSAGE);
					}
				}
			}
		}
	}

	// this method verifies the connexion to the PACS
	public boolean echo(String remoteHost, String remotePortString, String calledAET) {
		logger.info("echo: Starting...");
		final DcmEcho dcmecho = new DcmEcho(DCMECHO);

		dcmecho.setRemoteHost(remoteHost);
		dcmecho.setRemotePort((int) (Integer.parseInt(remotePortString)));
		dcmecho.setCalledAET(calledAET, false);
		if (dicomWindow.isDicomServerEnableTLS3DES) {
			dcmecho.setTlsNeedClientAuth(false);
			dcmecho.setTls3DES_EDE_CBC();
			dcmecho.setKeyStoreURL(dicomWindow.keyStoreURLTF.getText());
			// get keystore password
			char[] input1 = dicomWindow.keyStorePasswordTF.getPassword();
			String keyStorePassword = "";
			for (int i = 0; i < input1.length; i++)
				keyStorePassword += input1[i];

			dcmecho.setKeyStorePassword(keyStorePassword);
			dcmecho.setTrustStoreURL(dicomWindow.trustStoreURLTF.getText());
			// get truststore password
			char[] input2 = dicomWindow.keyStorePasswordTF.getPassword();
			String trustStorePassword = "";
			for (int i = 0; i < input2.length; i++)
				trustStorePassword += input2[i];
			dcmecho.setTrustStorePassword(trustStorePassword);

		}
		try {
			dcmecho.open();
		} catch (Exception e) {
			logger.error("echo: Failed to open connection:" + e.getMessage());
			return false;
		}
		try {
			dcmecho.echo();
			logger.info("echo: Success.");
			return true;
		} catch (Exception e) {
			logger.error("echo: Failed to echo:" + e.getMessage());
			return false;
		} finally {
			try {
				dcmecho.close();
			} catch (Exception e) {
				logger.error("echo: Failed to close connection:" + e.getMessage());
				return false;
			}
		}
	}

	// check only remoteHost, remotePortString and calledAET parameters (remote PACS
	// paramers) for echo button
	boolean checkFormEchoParameters(String remoteHost, String remotePortString, String calledAET,
			ResourceBundle resourceBundle) {
		boolean configurationParametersOK = true;

		if ((remoteHost == null || "".equals(remoteHost)) || (remotePortString == null || "".equals(remotePortString)
				|| (calledAET == null || "".equals(calledAET)))) {
			String message = resourceBundle.getString(
					"shanoir.uploader.configurationMenu.dicomServer.configure.checkFormConfigureParameters.mandatoryFields.message");
			JOptionPane.showMessageDialog(new JFrame(), message, resourceBundle.getString(
					"shanoir.uploader.configurationMenu.dicomServer.configure.checkFormConfigureParameters.mandatoryFields.title"),
					JOptionPane.ERROR_MESSAGE);
			configurationParametersOK = false;
		} else {
			try {
				int remotePortInt = Integer.parseInt(remotePortString);
			} catch (NumberFormatException e) {
				String message = resourceBundle.getString(
						"shanoir.uploader.configurationMenu.dicomServer.configure.checkFormConfigureParameters.portNumber.message");
				JOptionPane.showMessageDialog(new JFrame(), message, resourceBundle.getString(
						"shanoir.uploader.configurationMenu.dicomServer.configure.checkFormConfigureParameters.portNumber.title"),
						JOptionPane.ERROR_MESSAGE);
				configurationParametersOK = false;
			}
		}

		return configurationParametersOK;
	}

	// check all parameters for configure button
	boolean checkFormConfigureParameters(String remoteHost, String remotePortString, String calledAET, String localHost,
			String localPortString, String localAET, ResourceBundle resourceBundle) {
		boolean configurationParametersOK = true;
		if ((remoteHost == null || "".equals(remoteHost))
				|| (remotePortString == null || "".equals(remotePortString)
						|| (calledAET == null || "".equals(calledAET)))
				|| (localHost == null || "".equals(localHost))
				|| (localPortString == null || "".equals(localPortString) || (localAET == null || "".equals(localAET)))

		) {
			String message = resourceBundle.getString(
					"shanoir.uploader.configurationMenu.dicomServer.configure.checkFormConfigureParameters.mandatoryFields.message");
			JOptionPane.showMessageDialog(new JFrame(), message, resourceBundle.getString(
					"shanoir.uploader.configurationMenu.dicomServer.configure.checkFormConfigureParameters.mandatoryFields.title"),
					JOptionPane.ERROR_MESSAGE);
			configurationParametersOK = false;
		} else {
			try {
				int remotePortInt = Integer.parseInt(remotePortString);
				int localPortInt = Integer.parseInt(localPortString);
			} catch (NumberFormatException e) {
				String message = resourceBundle.getString(
						"shanoir.uploader.configurationMenu.dicomServer.configure.checkFormConfigureParameters.portNumber.message");
				JOptionPane.showMessageDialog(new JFrame(), message, resourceBundle.getString(
						"shanoir.uploader.configurationMenu.dicomServer.configure.checkFormConfigureParameters.portNumber.title"),
						JOptionPane.ERROR_MESSAGE);
				configurationParametersOK = false;
			}
		}
		return configurationParametersOK;
	}

	// loads the existing Dicom Server properties file
	public static Properties loadProperties(String fileName) {
		InputStream propsFile;
		Properties tempProp = new Properties();
		try {
			propsFile = new FileInputStream(fileName);
			tempProp.load(propsFile);
			propsFile.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			System.exit(0);
		}
		return tempProp;
	}

}
