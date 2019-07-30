package org.shanoir.uploader.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.io.File;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.apache.log4j.Logger;
import org.shanoir.uploader.action.ShanoirServerConfigurationListener;
import org.shanoir.uploader.service.SoapWebService;
import org.shanoir.uploader.service.wsdl.ServiceConfiguration;

/**
 * The Shanoir Server Configuration window of the ShanoirUploader.
 * 
 * @author ifakhfakh
 * 
 */

public class ShanoirServerConfigurationWindow extends JDialog {

	private static Logger logger = Logger.getLogger(DicomServerConfigurationWindow.class);

	public JTextField userNameTF;
	public JPasswordField passwordTF;
	public JButton pingButton;
	public JButton configureButton;
	ShanoirServerConfigurationListener sSCL;

	public File shanoirUploaderFolder;
	public String SHANOIR_SERVER_PROPERTIES;
	public ServiceConfiguration serviceConfiguration;
	public ResourceBundle resourceBundle;

	public ShanoirServerConfigurationWindow(File shanoirUploaderFolder, String SHANOIR_SERVER_PROPERTIES,
			ServiceConfiguration serviceConfiguration, ResourceBundle resourceBundle) {

		this.shanoirUploaderFolder = shanoirUploaderFolder;
		this.SHANOIR_SERVER_PROPERTIES = SHANOIR_SERVER_PROPERTIES;
		this.serviceConfiguration = serviceConfiguration;
		this.resourceBundle = resourceBundle;

		// Create the frame.
		JFrame frame = new JFrame(resourceBundle.getString("shanoir.uploader.configurationMenu.shanoirServer.title"));

		// What happens when the frame closes?
		frame.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

		// Panel content
		JPanel masterPanel = new JPanel(new BorderLayout());
		frame.setContentPane(masterPanel);
		final JPanel configurationPanel = new JPanel();
		configurationPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		masterPanel.add(configurationPanel, BorderLayout.NORTH);

		GridBagLayout gBLPanel = new GridBagLayout();
		gBLPanel.columnWidths = new int[] { 0, 0, 0 };
		gBLPanel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
		gBLPanel.columnWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };
		gBLPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		configurationPanel.setLayout(gBLPanel);

		JLabel configurationLabel = new JLabel(
				resourceBundle.getString("shanoir.uploader.configurationMenu.shanoirServer.configurationLabel"));
		Font newLabelFont = new Font(configurationLabel.getFont().getName(), Font.BOLD,
				configurationLabel.getFont().getSize());
		configurationLabel.setFont(newLabelFont);
		addItem(configurationPanel, configurationLabel, 0, 0, 3, GridBagConstraints.WEST);

		// User Name field
		JLabel userNameLabel = new JLabel(
				resourceBundle.getString("shanoir.uploader.configurationMenu.shanoirServer.userName"));
		addItem(configurationPanel, userNameLabel, 0, 1, 1, GridBagConstraints.EAST);
		userNameTF = new JTextField();
		addItem(configurationPanel, userNameTF, 1, 1, 2, GridBagConstraints.WEST);
		userNameTF.setColumns(15);
		userNameTF.setText("");

		// Password field
		JLabel passwordLabel = new JLabel(
				resourceBundle.getString("shanoir.uploader.configurationMenu.shanoirServer.password"));
		addItem(configurationPanel, passwordLabel, 0, 2, 1, GridBagConstraints.EAST);
		passwordTF = new JPasswordField();
		addItem(configurationPanel, passwordTF, 1, 2, 2, GridBagConstraints.WEST);
		passwordTF.setColumns(15);
		passwordTF.setText("");

		// echo and configure buttons
		pingButton = new JButton(
				resourceBundle.getString("shanoir.uploader.configurationMenu.shanoirServer.pingButton"));
		addItem(configurationPanel, pingButton, 0, 7, 1, GridBagConstraints.EAST);
		configureButton = new JButton(
				resourceBundle.getString("shanoir.uploader.configurationMenu.shanoirServer.configureButton"));
		addItem(configurationPanel, configureButton, 2, 7, 1, GridBagConstraints.WEST);

		// listener
		sSCL = new ShanoirServerConfigurationListener(this, SoapWebService.getInstance().getShanoirUploaderService());
		pingButton.addActionListener(sSCL);
		configureButton.addActionListener(sSCL);

		// Size the frame.
		frame.pack();

		// center the frame
		// frame.setLocationRelativeTo( null );
		Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
		int windowWidth = 390;
		int windowHeight = 221;
		// set position and size
		frame.setBounds(center.x - windowWidth / 2, center.y - windowHeight / 2, windowWidth, windowHeight);

		// Show it.
		frame.setVisible(true);
	}

	private void addItem(JPanel p, JComponent c, int x, int y, int width, int align) {
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx = x;
		gc.gridy = y;
		gc.gridwidth = width;
		gc.anchor = align;
		gc.insets = new Insets(10, 10, 10, 10);
		p.add(c, gc);
	}

}
