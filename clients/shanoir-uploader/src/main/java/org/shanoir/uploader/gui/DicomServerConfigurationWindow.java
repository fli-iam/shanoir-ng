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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.apache.log4j.Logger;
import org.shanoir.uploader.action.DicomServerConfigurationListener;

/**
 * The DICOM Server Configuration window of the ShanoirUploader.
 * 
 * @author ifakhfakh
 * 
 */

public class DicomServerConfigurationWindow extends JFrame {

	public JTextField aetTF;
	public JTextField hostNameTF;
	public JTextField portTF;
	public JTextField keyStoreURLTF;
	public JPasswordField keyStorePasswordTF;
	public JTextField trustStoreURLTF;
	public JPasswordField trustStorePasswordTF;
	public JTextField aetLocalPACSTF;
	public JTextField hostNameLocalPACSTF;
	public JTextField portLocalPACSTF;
	public JButton echoButton;
	public JButton configureButton;
	public boolean isDicomServerEnableTLS3DES = false;

	private static Logger logger = Logger.getLogger(DicomServerConfigurationWindow.class);

	// private static final String JFRAME_TITLE = "DICOM Server Configuration";
	DicomServerConfigurationListener dSCL;

	public File shanoirUploaderFolder;
	public ResourceBundle resourceBundle;

	public DicomServerConfigurationWindow(File shanoirUploaderFolder, ResourceBundle resourceBundle) {
		this.shanoirUploaderFolder = shanoirUploaderFolder;
		this.resourceBundle = resourceBundle;

		// Create the frame.
		JFrame frame = new JFrame(resourceBundle.getString("shanoir.uploader.configurationMenu.dicomServer.title"));

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

		JLabel configurationLabel = new JLabel(resourceBundle
				.getString("shanoir.uploader.configurationMenu.dicomServer.configurationRemotePACSLabel"));
		Font newLabelFont = new Font(configurationLabel.getFont().getName(), Font.BOLD,
				configurationLabel.getFont().getSize());
		configurationLabel.setFont(newLabelFont);
		addItem(configurationPanel, configurationLabel, 0, 0, 3, GridBagConstraints.WEST);

		// AET field
		JLabel aetLabel = new JLabel(
				resourceBundle.getString("shanoir.uploader.configurationMenu.dicomServer.aetLabel"));
		addItem(configurationPanel, aetLabel, 0, 1, 1, GridBagConstraints.EAST);
		aetTF = new JTextField();
		addItem(configurationPanel, aetTF, 1, 1, 2, GridBagConstraints.WEST);
		aetTF.setColumns(15);
		aetTF.setText("");

		// Host Name field
		JLabel hostNameLabel = new JLabel(
				resourceBundle.getString("shanoir.uploader.configurationMenu.dicomServer.hostNameLabel"));
		addItem(configurationPanel, hostNameLabel, 0, 2, 1, GridBagConstraints.EAST);
		hostNameTF = new JTextField();
		addItem(configurationPanel, hostNameTF, 1, 2, 2, GridBagConstraints.WEST);
		hostNameTF.setColumns(15);
		hostNameTF.setText("");

		// Port field
		JLabel portLabel = new JLabel(
				resourceBundle.getString("shanoir.uploader.configurationMenu.dicomServer.portLabel"));
		addItem(configurationPanel, portLabel, 0, 3, 1, GridBagConstraints.EAST);
		portTF = new JTextField();
		addItem(configurationPanel, portTF, 1, 3, 2, GridBagConstraints.WEST);
		portTF.setColumns(15);
		portTF.setText("");

		/**
		 * Local PaCS
		 * 
		 */
		JLabel configurationLocalPACSLabel = new JLabel(
				resourceBundle.getString("shanoir.uploader.configurationMenu.dicomServer.configurationLocalPACSLabel"));
		configurationLocalPACSLabel.setFont(newLabelFont);
		addItem(configurationPanel, configurationLocalPACSLabel, 0, 7, 3, GridBagConstraints.WEST);

		// Local PACS AET field
		JLabel aetLocalPACSLabel = new JLabel(
				resourceBundle.getString("shanoir.uploader.configurationMenu.dicomServer.localAetLabel"));
		addItem(configurationPanel, aetLocalPACSLabel, 0, 8, 1, GridBagConstraints.EAST);

		aetLocalPACSTF = new JTextField();
		addItem(configurationPanel, aetLocalPACSTF, 1, 8, 2, GridBagConstraints.WEST);
		aetLocalPACSTF.setColumns(15);
		aetLocalPACSTF.setText("");

		// Local PACS Host Name field
		JLabel hostNameLocalPACSLabel = new JLabel(
				resourceBundle.getString("shanoir.uploader.configurationMenu.dicomServer.localHostNameLabel"));
		addItem(configurationPanel, hostNameLocalPACSLabel, 0, 9, 1, GridBagConstraints.EAST);
		hostNameLocalPACSTF = new JTextField();
		addItem(configurationPanel, hostNameLocalPACSTF, 1, 9, 2, GridBagConstraints.WEST);
		hostNameLocalPACSTF.setColumns(15);
		hostNameLocalPACSTF.setText("");

		// Local PACS Port field
		JLabel portLocalPACSLabel = new JLabel(
				resourceBundle.getString("shanoir.uploader.configurationMenu.dicomServer.localPortLabel"));
		addItem(configurationPanel, portLocalPACSLabel, 0, 10, 1, GridBagConstraints.EAST);
		portLocalPACSTF = new JTextField();
		addItem(configurationPanel, portLocalPACSTF, 1, 10, 2, GridBagConstraints.WEST);
		portLocalPACSTF.setColumns(15);
		portLocalPACSTF.setText("");

		// echo and configure buttons
		echoButton = new JButton(resourceBundle.getString("shanoir.uploader.configurationMenu.dicomServer.echoButton"));
		addItem(configurationPanel, echoButton, 0, 11, 1, GridBagConstraints.EAST);
		configureButton = new JButton(
				resourceBundle.getString("shanoir.uploader.configurationMenu.dicomServer.configureButton"));
		addItem(configurationPanel, configureButton, 2, 11, 1, GridBagConstraints.WEST);

		// listener
		dSCL = new DicomServerConfigurationListener(this);
		echoButton.addActionListener(dSCL);
		configureButton.addActionListener(dSCL);

		JLabel cmoveLabel = new JLabel(resourceBundle
				.getString("shanoir.uploader.configurationMenu.dicomServer.cmoveLabel"));
		Font cmoveLabelFont = new Font(cmoveLabel.getFont().getName(), Font.BOLD,
				cmoveLabel.getFont().getSize());
		cmoveLabel.setFont(cmoveLabelFont);
		addItem(configurationPanel, cmoveLabel, 0, 12, 3, GridBagConstraints.WEST);
		
		// Size the frame.
		frame.pack();

		// center the frame
		// frame.setLocationRelativeTo( null );
		Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
		int windowWidth = 650;
		int windowHeight = 460;
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
