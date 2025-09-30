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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.dicom.IDicomServerClient;
import org.shanoir.uploader.gui.DicomServerConfigurationWindow;

/**
 * This class implements the logic when a new DICOM server configuration is
 * introduced in the DicomServerConfigurationWindow class
 *
 * @author ifakhfakh
 *
 */

public class DicomServerConfigurationListener implements ActionListener {

	private static final Logger logger = LoggerFactory.getLogger(DicomServerConfigurationListener.class);

	DicomServerConfigurationWindow dicomWindow;

	private IDicomServerClient dicomServerClient;

	public DicomServerConfigurationListener(DicomServerConfigurationWindow dicomWindow, final IDicomServerClient dicomServerClient) {
		this.dicomWindow = dicomWindow;
		this.dicomServerClient = dicomServerClient;
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
				boolean connexionEstablished = echo(remoteHost, remotePortString, calledAET, localAET);
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
					if (echo(remoteHost, remotePortString, calledAET, localAET)) {

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

	// this method verifies the connection to the PACS
	public boolean echo(String remoteHost, String remotePortString, String calledAET, String localAET) {
		return dicomServerClient.echoDicomServer(calledAET, remoteHost, Integer.valueOf(remotePortString), localAET);
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
				Integer.parseInt(remotePortString);
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
				Integer.parseInt(remotePortString);
				Integer.parseInt(localPortString);
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
