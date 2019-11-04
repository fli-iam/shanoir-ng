package org.shanoir.uploader.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.jboss.seam.util.Hex;
import org.shanoir.uploader.cryptography.BlowfishAlgorithm;
import org.shanoir.uploader.gui.ShanoirServerConfigurationWindow;
import org.shanoir.uploader.service.soap.ShanoirUploaderServiceClient;

/**
 * This class implements the logic when a new Shanoir account is introduced in
 * the ShanoirServerConfigurationWindow class
 * 
 * @author ifakhfakh
 * 
 */
public class ShanoirServerConfigurationListener implements ActionListener {

	private static Logger logger = Logger.getLogger(ShanoirServerConfigurationListener.class);

	private ShanoirServerConfigurationWindow shanoirWindow;
	private ShanoirUploaderServiceClient loginService;

	public ShanoirServerConfigurationListener(ShanoirServerConfigurationWindow shanoirWindow, ShanoirUploaderServiceClient loginService) {
		this.shanoirWindow = shanoirWindow;
		this.loginService = loginService;
	}

	public void actionPerformed(ActionEvent event) {
		ResourceBundle resourceBundle = shanoirWindow.resourceBundle;
		String userName = shanoirWindow.userNameTF.getText();
		char[] input1 = shanoirWindow.passwordTF.getPassword();
		String password = "";
		for (int i = 0; i < input1.length; i++)
			password += input1[i];
		// Ping button
		if (event.getSource() == shanoirWindow.pingButton) {
			logger.info("ping to Shanoir: Starting...");
			boolean isavalidAccount;
			isavalidAccount = ping(userName, password);
			if (isavalidAccount) {
				String message = "<html><b> " + resourceBundle.getString(
						"shanoir.uploader.configurationMenu.shanoirServer.ping.succeeded.message") + "</html>";
				JOptionPane.showMessageDialog(new JFrame(), message,
						resourceBundle
								.getString("shanoir.uploader.configurationMenu.shanoirServer.ping.succeeded.title"),
						JOptionPane.INFORMATION_MESSAGE);
			} else {
				String message = "<html><b>" + resourceBundle
						.getString("shanoir.uploader.configurationMenu.shanoirServer.ping.failed.message") + " </html>";
				JOptionPane.showMessageDialog(new JFrame(), message,
						resourceBundle.getString("shanoir.uploader.configurationMenu.shanoirServer.ping.failed.title"),
						JOptionPane.ERROR_MESSAGE);
			}
		}

		// configure button
		if (event.getSource() == shanoirWindow.configureButton) {
			logger.info("Shanoir configuration: Starting...");
			String fileName = shanoirWindow.shanoirUploaderFolder + File.separator
					+ shanoirWindow.SHANOIR_SERVER_PROPERTIES;
			final File propertiesFile = new File(fileName);
			boolean propertiesExists = propertiesFile.exists();
			if (propertiesExists) {
				if (ping(userName, password)) {
					try {
						Properties props = loadProperties(fileName);
						props.setProperty("shanoir.server.user.name", userName);
						// encrypt password
						byte[] ibyte = password.getBytes();
						byte[] encryptedPasswordByte = BlowfishAlgorithm.encrypt(ibyte);
						String encryptedPassword = String.valueOf(Hex.encodeHex(encryptedPasswordByte));
						props.setProperty("shanoir.server.user.password", encryptedPassword);
						// Store the new configuration in the
						// shanoir_server.properties file
						OutputStream out = new FileOutputStream(propertiesFile);
						props.store(out, "SHANOIR Server Configuration");
						String message = "<html>" + resourceBundle.getString(
								"shanoir.uploader.configurationMenu.shanoirServer.configure.succeeded.message.part1")
								+ "</html>" + "\n" + "\n" + "<html> <b> "
								+ resourceBundle.getString(
										"shanoir.uploader.configurationMenu.shanoirServer.configure.succeeded.message.part2")
								+ " </html>" + "\n";
						JOptionPane.showMessageDialog(new JFrame(), message,
								resourceBundle.getString(
										"shanoir.uploader.configurationMenu.shanoirServer.configure.succeeded.title"),
								JOptionPane.INFORMATION_MESSAGE);
					} catch (Exception e) {
						logger.error("Failed to configure shanoir connection :", e);
					}
				} else {
					String message = resourceBundle
							.getString("shanoir.uploader.configurationMenu.shanoirServer.configure.failed.message");
					JOptionPane.showMessageDialog(new JFrame(), message,
							resourceBundle.getString(
									"shanoir.uploader.configurationMenu.shanoirServer.configure.failed.title"),
							JOptionPane.WARNING_MESSAGE);
				}
			}
		}
	}

	private boolean ping(String username, String password) {
		boolean result = false;
		try {
			result = loginService.login(username, password);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return result;
	}

	// loads the existing Shanoir Server properties file
	private Properties loadProperties(String fileName) {
		InputStream propsFile;
		Properties tempProp = new Properties();
		try {
			propsFile = new FileInputStream(fileName);
			tempProp.load(new InputStreamReader(propsFile, "UTF-8"));
			propsFile.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			System.exit(0);
		}
		return tempProp;
	}

}
