package org.shanoir.uploader.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretListener;
import javax.swing.text.NumberFormatter;

import org.apache.http.HttpResponse;
import org.apache.log4j.Logger;
import org.bouncycastle.util.test.NumberParsing;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.action.DownloadOrCopyActionListener;
import org.shanoir.uploader.action.FindDicomActionListener;
import org.shanoir.uploader.action.ImportDialogOpener;
import org.shanoir.uploader.action.ImportDialogOpenerNG;
import org.shanoir.uploader.action.RSDocumentListener;
import org.shanoir.uploader.action.SelectionActionListener;
import org.shanoir.uploader.dicom.IDicomServerClient;
import org.shanoir.uploader.dicom.anonymize.Pseudonymizer;
import org.shanoir.uploader.exception.PseudonymusException;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClientNG;
import org.shanoir.uploader.service.rest.UrlConfig;
import org.shanoir.uploader.service.soap.ServiceConfiguration;

import org.shanoir.downloader.ShanoirDownloader;

import org.apache.http.Header;
import org.springframework.http.HttpHeaders;
import org.apache.commons.io.FileUtils;

/**
 * The MainWindow of the ShanoirUploader.
 * 
 * @author mkain
 * 
 */
@SuppressWarnings("serial")
public class MainWindow extends JFrame {

	private static Logger logger = Logger.getLogger(MainWindow.class);

	public JFrame frame = this;

	public DicomTree dicomTree;
	public JPanel contentPane;
	public JScrollPane scrollPane;
	public JTextField patientNameTF;
	public JTextField patientIDTF;
	public JTextField studyDescriptionTF;
	public JTextField seriesDescriptionTF;
	public JButton downloadOrCopyButton;
	public JTextField newPatientIDTF;
	public JTextField lastNameTF;
	public JTextField firstNameTF;
	public JTextField birthNameTF;
	public JTextField birthDateTF;
	// public JTextField sexTF;
	public ButtonGroup sexRG;
	public JRadioButton msexR, fsexR;
	public JTextField birthDateReasearchTF;
	public JTextField examinationDateReasearchTF;
	
	public JButton queryButton;

	public ImportDialog importDialog;

	public JDialog errorDialog;
	public JLabel errorLabel;
	public JButton errorButton;
	public JLabel connexionStatus;

	private FindDicomActionListener fAL;
	private DownloadOrCopyActionListener dOCAL;
	private SelectionActionListener sAL;

	public JMenu mnAutoimport;
	public boolean isFromPACS;
	public boolean isDicomObjectSelected = false;

	public UtilDateModel model;
	public UtilDateModel studyModel;
	public String dateRS = "";
	public String studyDate = "";
	JScrollPane scrollPaneUpload;

	public JLabel startedUploadsLB;
	public JLabel finishedUploadsLB;
	public JLabel errorUploadsLB;
	public JLabel errorAlert;
	public JProgressBar uploadProgressBar;
	
	public IDicomServerClient dicomServerClient;
	public File shanoirUploaderFolder;

	public ResourceBundle resourceBundle;
	public ShUpConfig shanoirUploaderConfiguration;
	
	private ImportDialogOpener importDialogOpener;
	private ImportDialogOpenerNG importDialogOpenerNG;

	/**
	 * Create the frame.
	 */
	public MainWindow(final IDicomServerClient dicomServerClient,
			final File shanoirUploaderFolder,
			final UrlConfig urlConfig,
			final ResourceBundle resourceBundle) {		
		this.dicomServerClient=dicomServerClient;
		this.shanoirUploaderFolder=shanoirUploaderFolder;
		this.resourceBundle=resourceBundle;
		String JFRAME_TITLE = "ShanoirUploader " + ShUpConfig.SHANOIR_UPLOADER_VERSION;
		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (Exception e) {
			try {
				UIManager.setLookAndFeel(UIManager
						.getSystemLookAndFeelClassName());
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			}
		}

		setTitle(JFRAME_TITLE);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1800, 1200);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JMenuBar menuBar = new JMenuBar();
		contentPane.add(menuBar, BorderLayout.NORTH);

		JMenu mnFile = new JMenu(resourceBundle.getString("shanoir.uploader.fileMenu"));
		menuBar.add(mnFile);

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		JMenuItem mntmOpenDicomFromCD = new JMenuItem(resourceBundle.getString("shanoir.uploader.fileMenu.openCD"));
		fAL = new FindDicomActionListener(this, fileChooser, dicomServerClient);
		mntmOpenDicomFromCD.addActionListener(fAL);
		mnFile.add(mntmOpenDicomFromCD);

		JMenu mnConfiguration = new JMenu(resourceBundle.getString("shanoir.uploader.configurationMenu"));
		menuBar.add(mnConfiguration);

		// add Server Configuration and Dicom configuration Menu Items
		JMenuItem mntmDicomServerConfiguration = new JMenuItem(
				resourceBundle.getString("shanoir.uploader.configurationMenu.dicomServer"));
		mnConfiguration.add(mntmDicomServerConfiguration);
		mntmDicomServerConfiguration.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				DicomServerConfigurationWindow dscw = new DicomServerConfigurationWindow(
						shanoirUploaderFolder, resourceBundle );
				dscw.hostNameTF.setText(ShUpConfig.dicomServerProperties.getProperty("dicom.server.host"));
				dscw.portTF.setText(ShUpConfig.dicomServerProperties.getProperty("dicom.server.port"));
				dscw.aetTF.setText(ShUpConfig.dicomServerProperties.getProperty("dicom.server.aet.called"));
				dscw.hostNameLocalPACSTF.setText(ShUpConfig.dicomServerProperties.getProperty("local.dicom.server.host"));
				dscw.portLocalPACSTF.setText(ShUpConfig.dicomServerProperties.getProperty("local.dicom.server.port"));
				dscw.aetLocalPACSTF.setText(ShUpConfig.dicomServerProperties.getProperty("local.dicom.server.aet.calling"));
			}
		});

		JMenuItem mntmShanoirServerConfiguration = new JMenuItem(
				resourceBundle.getString("shanoir.uploader.configurationMenu.shanoirServer"));
		mnConfiguration.add(mntmShanoirServerConfiguration);

		mntmShanoirServerConfiguration.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ShanoirServerConfigurationWindow sscw = new ShanoirServerConfigurationWindow(
						shanoirUploaderFolder, ServiceConfiguration.getInstance(),resourceBundle);
			}
		});
		
		// Language Configuration Menu
		JMenuItem mntmLanguage = new JMenuItem(
				resourceBundle.getString("shanoir.uploader.configurationMenu.language"));
		mnConfiguration.add(mntmLanguage);
		mntmLanguage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			LanguageConfigurationWindow sscw = new LanguageConfigurationWindow(shanoirUploaderFolder, resourceBundle);
			}
		});

		JMenu mnHelp = new JMenu(resourceBundle.getString("shanoir.uploader.helpMenu"));
		menuBar.add(mnHelp);
		
		JMenuItem mntmAboutShUp = new JMenuItem(resourceBundle.getString("shanoir.uploader.helpMenu.aboutShUp"));
		mnHelp.add(mntmAboutShUp);
		mntmAboutShUp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				AboutWindow aboutW = new AboutWindow(resourceBundle);
			}
		});

		JMenu profileSelected = new JMenu(resourceBundle.getString("shanoir.uploader.profileMenu") + ShUpConfig.profileSelected);
		menuBar.add(Box.createHorizontalGlue());
		menuBar.add(profileSelected);

		/*** add TabbedPane to display the upload jobs in progress ***/
		JTabbedPane tabbedPane;
		tabbedPane = new JTabbedPane();

		JSplitPane splitPane = new JSplitPane();

		scrollPane = new JScrollPane();
		splitPane.setRightComponent(scrollPane);
		sAL = new SelectionActionListener(this, resourceBundle);

		JPanel masterPanel = new JPanel(new BorderLayout());
		splitPane.setLeftComponent(masterPanel);

		final JPanel queryPanel = new JPanel();
		queryPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		JPanel editPanel = new JPanel();
		editPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		masterPanel.add(queryPanel, BorderLayout.NORTH);
		masterPanel.add(editPanel, BorderLayout.SOUTH);

		GridBagLayout gBLPanel = new GridBagLayout();
		gBLPanel.columnWidths = new int[] { 0, 0, 0 };
		gBLPanel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
		gBLPanel.columnWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };
		gBLPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				Double.MIN_VALUE };
		queryPanel.setLayout(gBLPanel);
		editPanel.setLayout(gBLPanel);

		/**
		 * Query panel elements
		 */
		JLabel queryPanelLabel = new JLabel(
				resourceBundle.getString("shanoir.uploader.queryBoxMessage"));
		GridBagConstraints gbc_queryPanelLabel = new GridBagConstraints();
		gbc_queryPanelLabel.anchor = GridBagConstraints.WEST;
		gbc_queryPanelLabel.insets = new Insets(10, 10, 10, 10);
		gbc_queryPanelLabel.gridx = 0;
		gbc_queryPanelLabel.gridy = 0;
		gbc_queryPanelLabel.gridwidth = 2;
		queryPanel.add(queryPanelLabel, gbc_queryPanelLabel);

		JLabel patientNameLabel = new JLabel(resourceBundle.getString("shanoir.uploader.patientNameLabel"));
		patientNameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_patientNameLabel = new GridBagConstraints();
		gbc_patientNameLabel.anchor = GridBagConstraints.EAST;
		gbc_patientNameLabel.insets = new Insets(10, 10, 10, 10);
		gbc_patientNameLabel.gridx = 0;
		gbc_patientNameLabel.gridy = 1;
		queryPanel.add(patientNameLabel, gbc_patientNameLabel);

		patientNameTF = new JTextField();
		GridBagConstraints gbc_patientNameTF = new GridBagConstraints();
		gbc_patientNameTF.insets = new Insets(10, 10, 10, 10);
		gbc_patientNameTF.fill = GridBagConstraints.HORIZONTAL;
		gbc_patientNameTF.gridx = 1;
		gbc_patientNameTF.gridy = 1;
		queryPanel.add(patientNameTF, gbc_patientNameTF);
		patientNameTF.setColumns(15);
		patientNameTF.setText("");

		/**
		 * Help Button
		 */
		JButton helpButton;
		helpButton = new JButton(resourceBundle.getString("shanoir.uploader.helpButton"));
		GridBagConstraints gbc_HelpButton = new GridBagConstraints();
		gbc_HelpButton.insets = new Insets(0, 0, 5, 0);
		gbc_HelpButton.gridwidth = 3;
		gbc_HelpButton.gridx = 2;
		gbc_HelpButton.gridy = 1;
		queryPanel.add(helpButton, gbc_HelpButton);

		helpButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				String message = "<html><b> - </b>The patient name should be in this form:</html> "
						+ "\n"
						+ "\n"
						+ "<html>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
						+ "<b>"
						+ "LastName"
						+ "<b>"
						+ "</html>"
						+ "\n"
						+ "or"
						+ "\n"
						+ "<html>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
						+ "<b>"
						+ "LastName, FirstName"
						+ "<b>"
						+ "</html>"
						+ "\n"
						+ "or"
						+ "\n"
						+ "<html>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
						+ "<b>"
						+ "LastName, FirstName1, FirstName2"
						+ "<b>"
						+ "</html>"
						+ "\n"
						+ "\n"
						+

						"<html><b> - </b>The wildcard character &nbsp;&nbsp; \"*\" &nbsp;&nbsp;can be used :</html>"
						+ "\n"
						+ "\n"
						+

						"<html>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
						+ "<b>"
						+ "with the patient FirstName"
						+ "<b>"
						+ "</html>"
						+ "\n"
						+ "or"
						+ "\n"
						+ "<html>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
						+ "<b>"
						+ "with the patient LastName only if the LastName introduced contains at least 4 characters"
						+ "<b>" + "</html>" + "\n";

				;
				JOptionPane.showMessageDialog(queryPanel, message, "Help",
						JOptionPane.INFORMATION_MESSAGE);
			}
		});

		// If fields Patient name, Patient ID and Study description are empty
		// Query DICOM server button is grey
		CaretListener caretQueryPACSfields = new CaretListener() {
			public void caretUpdate(javax.swing.event.CaretEvent e) {
				if (patientNameTF.getText().length() != 0
						|| patientIDTF.getText().length() != 0
						|| studyDescriptionTF.getText().length() != 0
						|| dateRS.length() != 0 || studyDate.length() != 0)
					queryButton.setEnabled(true);
				else
					queryButton.setEnabled(false);

			}
		};
		patientNameTF.addCaretListener(caretQueryPACSfields);

		JLabel PatientIDLabel = new JLabel(resourceBundle.getString("shanoir.uploader.patientIDLabel"));
		PatientIDLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_PatientIDLabel = new GridBagConstraints();
		gbc_PatientIDLabel.anchor = GridBagConstraints.EAST;
		gbc_PatientIDLabel.insets = new Insets(10, 10, 10, 10);
		gbc_PatientIDLabel.gridx = 0;
		gbc_PatientIDLabel.gridy = 2;
		queryPanel.add(PatientIDLabel, gbc_PatientIDLabel);

		patientIDTF = new JTextField();
		GridBagConstraints gbc_patientIDTF = new GridBagConstraints();
		gbc_patientIDTF.insets = new Insets(10, 10, 10, 10);
		gbc_patientIDTF.fill = GridBagConstraints.HORIZONTAL;
		gbc_patientIDTF.gridx = 1;
		gbc_patientIDTF.gridy = 2;
		queryPanel.add(patientIDTF, gbc_patientIDTF);
		patientIDTF.setColumns(15);
		patientIDTF.setText("");
		patientIDTF.addCaretListener(caretQueryPACSfields);

		JLabel studyDescriptionLabel = new JLabel(resourceBundle.getString("shanoir.uploader.studyDescriptionLabel"));
		GridBagConstraints gbc_studyDescriptionLabel = new GridBagConstraints();
		gbc_studyDescriptionLabel.anchor = GridBagConstraints.EAST;
		gbc_studyDescriptionLabel.insets = new Insets(10, 10, 10, 10);
		gbc_studyDescriptionLabel.gridx = 0;
		gbc_studyDescriptionLabel.gridy = 3;
		queryPanel.add(studyDescriptionLabel, gbc_studyDescriptionLabel);

		studyDescriptionTF = new JTextField();
		GridBagConstraints gbc_studyDescriptionTF = new GridBagConstraints();
		gbc_studyDescriptionTF.insets = new Insets(10, 10, 10, 10);
		gbc_studyDescriptionTF.fill = GridBagConstraints.HORIZONTAL;
		gbc_studyDescriptionTF.gridx = 1;
		gbc_studyDescriptionTF.gridy = 3;
		queryPanel.add(studyDescriptionTF, gbc_studyDescriptionTF);
		studyDescriptionTF.setColumns(15);
		studyDescriptionTF.setText("");
		studyDescriptionTF.addCaretListener(caretQueryPACSfields);

		JLabel seriesDescriptionLabel = new JLabel("Series description:");
		GridBagConstraints gbc_seriesDescriptionLabel = new GridBagConstraints();
		gbc_seriesDescriptionLabel.anchor = GridBagConstraints.EAST;
		gbc_seriesDescriptionLabel.insets = new Insets(10, 10, 10, 10);
		gbc_seriesDescriptionLabel.gridx = 0;
		gbc_seriesDescriptionLabel.gridy = 4;

		seriesDescriptionTF = new JTextField();
		GridBagConstraints gbc_seriesDescriptionTF = new GridBagConstraints();
		gbc_seriesDescriptionTF.insets = new Insets(10, 10, 10, 10);
		gbc_seriesDescriptionTF.fill = GridBagConstraints.HORIZONTAL;
		gbc_seriesDescriptionTF.gridx = 1;
		gbc_seriesDescriptionTF.gridy = 4;
		seriesDescriptionTF.setColumns(15);

		// Add Birth Date field
		JLabel birthDateReasearchLabel = new JLabel(resourceBundle.getString("shanoir.uploader.patientBirthDateLabel"));
		GridBagConstraints gbc_birthDateReasearchLabel = new GridBagConstraints();
		gbc_birthDateReasearchLabel.anchor = GridBagConstraints.EAST;
		gbc_birthDateReasearchLabel.insets = new Insets(10, 10, 10, 10);
		gbc_birthDateReasearchLabel.gridx = 0;
		gbc_birthDateReasearchLabel.gridy = 4;
		queryPanel.add(birthDateReasearchLabel, gbc_birthDateReasearchLabel);

		model = new UtilDateModel();

		Properties p = new Properties();
		p.put("text.today", "Today");
		p.put("text.month", "Month");
		p.put("text.year", "Year");
		JDatePanelImpl datePanel = new JDatePanelImpl(model, p);
		DateLabelFormatter dLP = new DateLabelFormatter();
		model.getValue();

		final JDatePickerImpl datePicker = new JDatePickerImpl(datePanel, dLP);
		GridBagConstraints gbc_birthDateReasearchTF = new GridBagConstraints();
		gbc_birthDateReasearchTF.insets = new Insets(10, 10, 10, 10);
		gbc_birthDateReasearchTF.fill = GridBagConstraints.HORIZONTAL;
		gbc_birthDateReasearchTF.gridx = 1;
		gbc_birthDateReasearchTF.gridy = 4;
		queryPanel.add(datePicker, gbc_birthDateReasearchTF);

		datePicker.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
				if (model.getValue() != null) {
					dateRS = formatter.format(model.getValue());
					queryButton.setEnabled(true);
				} else {
					dateRS = "";
					if (patientNameTF.getText().length() == 0
							&& patientIDTF.getText().length() == 0
							&& studyDescriptionTF.getText().length() == 0
							&& studyDate.length() == 0)
						queryButton.setEnabled(false);
				}
			}
		});

		// Add Examination date field
		// 0008,0020 StudyDate
		JLabel studyDateLabel = new JLabel(resourceBundle.getString("shanoir.uploader.studyDateLabel"));
		GridBagConstraints gbc_studyDateLabel = new GridBagConstraints();
		gbc_studyDateLabel.anchor = GridBagConstraints.EAST;
		gbc_studyDateLabel.insets = new Insets(10, 10, 10, 10);
		gbc_studyDateLabel.gridx = 0;
		gbc_studyDateLabel.gridy = 5;
		queryPanel.add(studyDateLabel, gbc_studyDateLabel);

		studyModel = new UtilDateModel();

		Properties prop = new Properties();
		prop.put("text.today", "Today");
		prop.put("text.month", "Month");
		prop.put("text.year", "Year");
		JDatePanelImpl studyDatePanel = new JDatePanelImpl(studyModel, prop);
		DateLabelFormatter studyDLP = new DateLabelFormatter();
		final JDatePickerImpl studyDatePicker = new JDatePickerImpl(
				studyDatePanel, studyDLP);

		GridBagConstraints gbc_studyDatePicker = new GridBagConstraints();
		gbc_studyDatePicker.insets = new Insets(10, 10, 10, 10);
		gbc_studyDatePicker.fill = GridBagConstraints.HORIZONTAL;
		gbc_studyDatePicker.gridx = 1;
		gbc_studyDatePicker.gridy = 5;
		queryPanel.add(studyDatePicker, gbc_studyDatePicker);

		studyDatePicker.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				queryButton.setEnabled(true);
				final SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
				if (studyModel.getValue() != null) {
					studyDate = formatter.format(studyModel.getValue());
					queryButton.setEnabled(true);
				} else {
					studyDate = "";
					if (patientNameTF.getText().length() == 0
							&& patientIDTF.getText().length() == 0
							&& studyDescriptionTF.getText().length() == 0
							&& dateRS.length() == 0)
						queryButton.setEnabled(false);
				}
			}
		});

		queryButton = new JButton(resourceBundle.getString("shanoir.uploader.queryButton"));
		GridBagConstraints gbc_queryButton = new GridBagConstraints();
		gbc_queryButton.insets = new Insets(0, 0, 5, 0);
		gbc_queryButton.gridwidth = 3;
		gbc_queryButton.gridx = 0;
		gbc_queryButton.gridy = 6;
		queryPanel.add(queryButton, gbc_queryButton);
		queryButton.setEnabled(false);
		queryButton.addActionListener(fAL);

		JSeparator separator = new JSeparator();
		GridBagConstraints gbc_separator = new GridBagConstraints();
		gbc_separator.insets = new Insets(10, 10, 10, 10);
		gbc_separator.gridx = 1;
		gbc_separator.gridy = 6;
		queryPanel.add(separator, gbc_separator);

		/**
		 * Edit panel
		 */
		JLabel editPanelLabel = new JLabel(
				resourceBundle.getString("shanoir.uploader.sendBoxMessage"));
		GridBagConstraints gbc_editPanelLabel = new GridBagConstraints();
		gbc_editPanelLabel.anchor = GridBagConstraints.WEST;
		gbc_editPanelLabel.insets = new Insets(10, 10, 10, 10);
		gbc_editPanelLabel.gridx = 0;
		gbc_editPanelLabel.gridy = 0;
		gbc_editPanelLabel.gridwidth = 2;
		editPanel.add(editPanelLabel, gbc_editPanelLabel);

		// this is uncommented for OFSEP only, implement this later for Neurinfo
		JLabel newPatientIDLabel = new JLabel("New Patient ID:");
		newPatientIDLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_newPatientIDLabel = new GridBagConstraints();
		gbc_newPatientIDLabel.anchor = GridBagConstraints.EAST;
		gbc_newPatientIDLabel.insets = new Insets(0, 0, 5, 5);
		gbc_newPatientIDLabel.gridx = 0;
		gbc_newPatientIDLabel.gridy = 0;

		newPatientIDTF = new JTextField();
		GridBagConstraints gbc_newPatientIDTF = new GridBagConstraints();
		gbc_newPatientIDTF.insets = new Insets(10, 10, 10, 10);
		gbc_newPatientIDTF.fill = GridBagConstraints.HORIZONTAL;
		gbc_newPatientIDTF.gridx = 1;
		gbc_newPatientIDTF.gridy = 0;
		newPatientIDTF.setColumns(15);

		JLabel lastNameLabel = new JLabel(resourceBundle.getString("shanoir.uploader.lastNameLabel"));
		lastNameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gBCLastNameLabel = new GridBagConstraints();
		gBCLastNameLabel.anchor = GridBagConstraints.EAST;
		gBCLastNameLabel.insets = new Insets(10, 10, 10, 10);
		gBCLastNameLabel.gridx = 0;
		gBCLastNameLabel.gridy = 1;
		editPanel.add(lastNameLabel, gBCLastNameLabel);

		lastNameTF = new JTextField();
		GridBagConstraints gBCLastNameTF = new GridBagConstraints();
		gBCLastNameTF.insets = new Insets(10, 10, 10, 10);
		gBCLastNameTF.fill = GridBagConstraints.HORIZONTAL;
		gBCLastNameTF.gridx = 1;
		gBCLastNameTF.gridy = 1;
		gBCLastNameTF.gridwidth = 2;
		editPanel.add(lastNameTF, gBCLastNameTF);
		lastNameTF.setColumns(15);

		lastNameTF.getDocument().addDocumentListener(
				new RSDocumentListener(this));

		// add a button to copy the last name to the birth name
		ImageIcon copyIcon = new ImageIcon(getClass().getClassLoader()
				.getResource("images/copyLastNameToBirthName.16x16.png"));
		JButton birthNameCopyButton = new JButton(copyIcon);
		GridBagConstraints gBCBithNameCopyButton = new GridBagConstraints();
		gBCBithNameCopyButton.anchor = GridBagConstraints.EAST;
		gBCBithNameCopyButton.insets = new Insets(10, 10, 10, 10);
		gBCBithNameCopyButton.gridx = 3;
		gBCBithNameCopyButton.gridy = 1;
		editPanel.add(birthNameCopyButton, gBCBithNameCopyButton);

		JLabel firstNameLabel = new JLabel(resourceBundle.getString("shanoir.uploader.firstNameLabel"));
		firstNameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gBCFirstNameLabel = new GridBagConstraints();
		gBCFirstNameLabel.anchor = GridBagConstraints.EAST;
		gBCFirstNameLabel.insets = new Insets(10, 10, 10, 10);
		gBCFirstNameLabel.gridx = 0;
		gBCFirstNameLabel.gridy = 2;
		editPanel.add(firstNameLabel, gBCFirstNameLabel);

		firstNameTF = new JTextField();
		GridBagConstraints gBCFirstNameTF = new GridBagConstraints();
		gBCFirstNameTF.insets = new Insets(10, 10, 10, 10);
		gBCFirstNameTF.fill = GridBagConstraints.HORIZONTAL;
		gBCFirstNameTF.gridx = 1;
		gBCFirstNameTF.gridy = 2;
		gBCFirstNameTF.gridwidth = 2;
		editPanel.add(firstNameTF, gBCFirstNameTF);
		firstNameTF.setColumns(15);

		firstNameTF.getDocument().addDocumentListener(
				new RSDocumentListener(this));

		JLabel birthNameLabel = new JLabel(resourceBundle.getString("shanoir.uploader.BirthNameLabel"));
		birthNameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gBCBirthNameLabel = new GridBagConstraints();
		gBCBirthNameLabel.anchor = GridBagConstraints.EAST;
		gBCBirthNameLabel.insets = new Insets(10, 10, 10, 10);
		gBCBirthNameLabel.gridx = 0;
		gBCBirthNameLabel.gridy = 3;
		editPanel.add(birthNameLabel, gBCBirthNameLabel);

		birthNameTF = new JTextField();
		GridBagConstraints gBCBirthNameTF = new GridBagConstraints();
		gBCBirthNameTF.insets = new Insets(10, 10, 10, 10);
		gBCBirthNameTF.fill = GridBagConstraints.HORIZONTAL;
		gBCBirthNameTF.gridx = 1;
		gBCBirthNameTF.gridy = 3;
		gBCBirthNameTF.gridwidth = 2;
		editPanel.add(birthNameTF, gBCBirthNameTF);
		birthNameTF.setColumns(15);

		birthNameCopyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				birthNameTF.setText(lastNameTF.getText());
			}
		});

		birthNameTF.getDocument().addDocumentListener(
				new RSDocumentListener(this));

		JLabel birthDateLabel = new JLabel(resourceBundle.getString("shanoir.uploader.BirthDateLabel"));
		birthDateLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gBCBirthDateLabel = new GridBagConstraints();
		gBCBirthDateLabel.anchor = GridBagConstraints.EAST;
		gBCBirthDateLabel.insets = new Insets(10, 10, 10, 10);
		gBCBirthDateLabel.gridx = 0;
		gBCBirthDateLabel.gridy = 4;
		editPanel.add(birthDateLabel, gBCBirthDateLabel);

		birthDateTF = new JTextField();
		GridBagConstraints gBCBirthDateTF = new GridBagConstraints();
		gBCBirthDateTF.insets = new Insets(10, 10, 10, 10);
		gBCBirthDateTF.fill = GridBagConstraints.HORIZONTAL;
		gBCBirthDateTF.gridx = 1;
		gBCBirthDateTF.gridy = 4;
		gBCBirthDateTF.gridwidth = 2;
		editPanel.add(birthDateTF, gBCBirthDateTF);
		birthDateTF.setColumns(15);
		birthDateTF.getDocument().addDocumentListener(
				new RSDocumentListener(this));

		JLabel sexLabel = new JLabel(resourceBundle.getString("shanoir.uploader.sexLabel"));
		sexLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gBCSexLabel = new GridBagConstraints();
		gBCSexLabel.anchor = GridBagConstraints.EAST;
		gBCSexLabel.insets = new Insets(10, 10, 10, 10);
		gBCSexLabel.gridx = 0;
		gBCSexLabel.gridy = 5;
		editPanel.add(sexLabel, gBCSexLabel);

		sexRG = new ButtonGroup();

		msexR = new JRadioButton("M");
		sexRG.add(msexR);
		editPanel.add(msexR);

		fsexR = new JRadioButton("F");
		sexRG.add(fsexR);
		editPanel.add(fsexR);

		GridBagConstraints gBCSexTF = new GridBagConstraints();
		gBCSexTF.insets = new Insets(10, 10, 10, 10);
		gBCSexTF.fill = GridBagConstraints.HORIZONTAL;
		gBCSexTF.gridx = 1;
		gBCSexTF.gridy = 5;
		editPanel.add(msexR, gBCSexTF);

		GridBagConstraints gBCSexTF2 = new GridBagConstraints();
		gBCSexTF2.insets = new Insets(10, 10, 10, 10);
		gBCSexTF.fill = GridBagConstraints.HORIZONTAL;
		gBCSexTF2.gridx = 2;
		gBCSexTF2.gridy = 5;
		editPanel.add(fsexR, gBCSexTF2);

		downloadOrCopyButton = new JButton(resourceBundle.getString("shanoir.uploader.downloadOrCopyButton"));
		GridBagConstraints gbc_btnRetrieve = new GridBagConstraints();
		gbc_btnRetrieve.insets = new Insets(0, 0, 5, 0);
		gbc_btnRetrieve.gridwidth = 3;
		gbc_btnRetrieve.gridx = 0;
		gbc_btnRetrieve.gridy = 6;
		editPanel.add(downloadOrCopyButton, gbc_btnRetrieve);
		
		menuBar.add(Box.createHorizontalGlue());
		
		/**
		 * Init pseudonymizer and subjectIdentifierGenerator and create
		 * DownloadOrCopyActionListener, and add AL to button.
		 */
		File pseudonymusFolder = new File(ShUpOnloadConfig.getWorkFolder().getParentFile().getAbsolutePath() + File.separator + Pseudonymizer.PSEUDONYMUS_FOLDER);
		Pseudonymizer pseudonymizer = null;
		try {
			pseudonymizer = new Pseudonymizer(ShUpConfig.basicProperties.getProperty(ShUpConfig.MODE_PSEUDONYMUS_KEY_FILE), pseudonymusFolder.getAbsolutePath());
		} catch (PseudonymusException e) {
			logger.error(e.getMessage(), e);
		}
		dOCAL = new DownloadOrCopyActionListener(this, pseudonymizer, dicomServerClient);
		downloadOrCopyButton.addActionListener(dOCAL);		
		downloadOrCopyButton.setEnabled(false);	
		
		/**
		 * Init ImportDialog and its Opener here.
		 */

		importDialogOpener = new ImportDialogOpener(this, ShUpOnloadConfig.getShanoirUploaderServiceClient());
		importDialogOpenerNG = new ImportDialogOpenerNG(this, ShUpOnloadConfig.getShanoirUploaderServiceClientNG());
		
		// add ShUp principal panel (splitPane) and upload job display pane
		// (scrollPaneUpload) to TabbedPane
		final JPanel notificationPanel = new JPanel();
		// add Notification panel to display in the main window the upload state
		notificationPanel
				.setBorder(BorderFactory.createLineBorder(Color.black));
		final JSplitPane mainSplitPane = new JSplitPane(
				JSplitPane.VERTICAL_SPLIT);
		mainSplitPane.setLeftComponent(splitPane);
		mainSplitPane.setRightComponent(notificationPanel);
		notificationPanel.setLayout(gBLPanel);

		// Content of Notification Panel
		JLabel notificationCurrentUploads = new JLabel();
		GridBagConstraints gbc_notificationCurrentUploads = new GridBagConstraints();
		gbc_notificationCurrentUploads.insets = new Insets(10, 10, 10, 10);
		gbc_notificationCurrentUploads.fill = GridBagConstraints.WEST;
		gbc_notificationCurrentUploads.gridx = 0;
		gbc_notificationCurrentUploads.gridy = 0;
		notificationCurrentUploads.setText(resourceBundle.getString("shanoir.uploader.currentUploadsSummary"));
		notificationPanel.add(notificationCurrentUploads,
				gbc_notificationCurrentUploads);
		Font font = new Font("Courier", Font.BOLD, 12);
		notificationCurrentUploads.setFont(font);

		startedUploadsLB = new JLabel();
		GridBagConstraints gbc_startedUploadsLB = new GridBagConstraints();
		gbc_startedUploadsLB.insets = new Insets(10, 10, 10, 10);
		gbc_startedUploadsLB.fill = GridBagConstraints.EAST;
		gbc_startedUploadsLB.gridx = 0;
		gbc_startedUploadsLB.gridy = 1;
		notificationPanel.add(startedUploadsLB, gbc_startedUploadsLB);

		uploadProgressBar = new JProgressBar(0, 100);
		uploadProgressBar.setValue(0);
		uploadProgressBar.setStringPainted(true);
		uploadProgressBar.setVisible(true);

		GridBagConstraints gbc_progressBar = new GridBagConstraints();
		gbc_progressBar.insets = new Insets(10, 10, 10, 10);
		gbc_progressBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_progressBar.gridx = 1;
		gbc_progressBar.gridy = 1;
		notificationPanel.add(uploadProgressBar, gbc_progressBar);

		finishedUploadsLB = new JLabel();
		GridBagConstraints gbc_finishedUploadsLB = new GridBagConstraints();
		gbc_finishedUploadsLB.insets = new Insets(10, 10, 10, 10);
		gbc_finishedUploadsLB.fill = GridBagConstraints.EAST;
		gbc_finishedUploadsLB.gridx = 0;
		gbc_finishedUploadsLB.gridy = 2;
		notificationPanel.add(finishedUploadsLB, gbc_finishedUploadsLB);

		errorUploadsLB = new JLabel();
		GridBagConstraints gbc_errorUploadsLB = new GridBagConstraints();
		gbc_errorUploadsLB.insets = new Insets(10, 10, 10, 10);
		gbc_errorUploadsLB.fill = GridBagConstraints.EAST;
		gbc_errorUploadsLB.gridx = 0;
		gbc_errorUploadsLB.gridy = 3;
		notificationPanel.add(errorUploadsLB, gbc_errorUploadsLB);

		errorAlert = new JLabel();
		GridBagConstraints gbc_errorAlert = new GridBagConstraints();
		gbc_errorAlert.insets = new Insets(10, 10, 10, 10);
		gbc_errorAlert.fill = GridBagConstraints.EAST;
		gbc_errorAlert.gridx = 1;
		gbc_errorAlert.gridy = 3;
		gbc_errorAlert.gridheight = 2;
		notificationPanel.add(errorAlert, gbc_errorAlert);
		
		menuBar.add(Box.createRigidArea(new Dimension(8, 0)));
		
		tabbedPane.addTab(resourceBundle.getString("shanoir.uploader.mainWindowTab"),
			null, mainSplitPane, resourceBundle.getString("shanoir.uploader.mainWindowTab.tooltip"));
		JPanel currentUploadsPanel;
		currentUploadsPanel = new JPanel(false);
		tabbedPane.addTab(resourceBundle.getString("shanoir.uploader.currentUploadsTab"), null, currentUploadsPanel,
				resourceBundle.getString("shanoir.uploader.currentUploadsTab.tooltip"));
		scrollPaneUpload = new JScrollPane();
		scrollPaneUpload.setBounds(0, 0, MAXIMIZED_HORIZ, MAXIMIZED_VERT);
		scrollPaneUpload.setPreferredSize(new Dimension(898, 600));
		currentUploadsPanel.add(scrollPaneUpload);
		
		/**
		 * Create Download Panel
		 */

		final JPanel downloadPanel = new JPanel(false);
		downloadPanel.setLayout(gBLPanel);
		tabbedPane.addTab(resourceBundle.getString("shanoir.uploader.downloadDatasetsTab"), null, downloadPanel,
				resourceBundle.getString("shanoir.uploader.downloadDatasetsTab.tooltip"));

		NumberFormat format = NumberFormat.getInstance();
		NumberFormatter formatter = new NumberFormatter(format);
		formatter.setValueClass(Long.class);
		formatter.setMinimum(Long.MIN_VALUE);
		formatter.setMaximum(Long.MAX_VALUE);
		formatter.setAllowsInvalid(true);

		int posY = 0;
		JLabel downloadDatasetIDLabel = new JLabel(resourceBundle.getString("shanoir.uploader.DatasetIDLabel"));
		downloadDatasetIDLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_downloadDatasetIDLabel = new GridBagConstraints();
		gbc_downloadDatasetIDLabel.anchor = GridBagConstraints.EAST;
		gbc_downloadDatasetIDLabel.insets = new Insets(10, 10, 10, 10);
		gbc_downloadDatasetIDLabel.gridx = 0;
		gbc_downloadDatasetIDLabel.gridy = posY;
		downloadPanel.add(downloadDatasetIDLabel, gbc_downloadDatasetIDLabel);

		JFormattedTextField downloadDatasetIDTF = new JFormattedTextField(formatter);
		downloadDatasetIDTF.setFocusLostBehavior(javax.swing.JFormattedTextField.COMMIT);
		GridBagConstraints gbc_downloadDatasetIDTF = new GridBagConstraints();
		gbc_downloadDatasetIDTF.insets = new Insets(10, 10, 10, 10);
		gbc_downloadDatasetIDTF.fill = GridBagConstraints.HORIZONTAL;
		gbc_downloadDatasetIDTF.gridx = 1;
		gbc_downloadDatasetIDTF.gridy = posY++;
		downloadPanel.add(downloadDatasetIDTF, gbc_downloadDatasetIDTF);
		downloadDatasetIDTF.setColumns(15);
		downloadDatasetIDTF.setText("");

		JLabel downloadSubjectIDLabel = new JLabel(resourceBundle.getString("shanoir.uploader.subjectIDLabel"));
		downloadSubjectIDLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_downloadSubjectIDLabel = new GridBagConstraints();
		gbc_downloadSubjectIDLabel.anchor = GridBagConstraints.EAST;
		gbc_downloadSubjectIDLabel.insets = new Insets(10, 10, 10, 10);
		gbc_downloadSubjectIDLabel.gridx = 0;
		gbc_downloadSubjectIDLabel.gridy = posY;
		downloadPanel.add(downloadSubjectIDLabel, gbc_downloadSubjectIDLabel);

		JFormattedTextField downloadSubjectIDTF = new JFormattedTextField(formatter);
		downloadSubjectIDTF.setFocusLostBehavior(javax.swing.JFormattedTextField.COMMIT);
		GridBagConstraints gbc_downloadSubjectIDTF = new GridBagConstraints();
		gbc_downloadSubjectIDTF.insets = new Insets(10, 10, 10, 10);
		gbc_downloadSubjectIDTF.fill = GridBagConstraints.HORIZONTAL;
		gbc_downloadSubjectIDTF.gridx = 1;
		gbc_downloadSubjectIDTF.gridy = posY++;
		downloadPanel.add(downloadSubjectIDTF, gbc_downloadSubjectIDTF);
		downloadSubjectIDTF.setColumns(15);
		downloadSubjectIDTF.setText("");

		JLabel downloadStudyIDLabel = new JLabel(resourceBundle.getString("shanoir.uploader.studyIDLabel"));
		downloadStudyIDLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_downloadStudyIDLabel = new GridBagConstraints();
		gbc_downloadStudyIDLabel.anchor = GridBagConstraints.EAST;
		gbc_downloadStudyIDLabel.insets = new Insets(10, 10, 10, 10);
		gbc_downloadStudyIDLabel.gridx = 0;
		gbc_downloadStudyIDLabel.gridy = posY;
		downloadPanel.add(downloadStudyIDLabel, gbc_downloadStudyIDLabel);

		JFormattedTextField downloadStudyIDTF = new JFormattedTextField(formatter);
		downloadStudyIDTF.setFocusLostBehavior(javax.swing.JFormattedTextField.COMMIT);
		GridBagConstraints gbc_downloadStudyIDTF = new GridBagConstraints();
		gbc_downloadStudyIDTF.insets = new Insets(10, 10, 10, 10);
		gbc_downloadStudyIDTF.fill = GridBagConstraints.HORIZONTAL;
		gbc_downloadStudyIDTF.gridx = 1;
		gbc_downloadStudyIDTF.gridy = posY++;
		downloadPanel.add(downloadStudyIDTF, gbc_downloadStudyIDTF);
		downloadStudyIDTF.setColumns(15);
		downloadStudyIDTF.setText("");

		JLabel formatLabel = new JLabel(resourceBundle.getString("shanoir.uploader.formatLabel"));
		formatLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gBCFormatLabel = new GridBagConstraints();
		gBCFormatLabel.anchor = GridBagConstraints.EAST;
		gBCFormatLabel.insets = new Insets(10, 10, 10, 10);
		gBCFormatLabel.gridx = 0;
		gBCFormatLabel.gridy = posY;
		downloadPanel.add(formatLabel, gBCFormatLabel);

		ButtonGroup formatRG = new ButtonGroup();

		JRadioButton dicomformatR = new JRadioButton("Dicom");
		formatRG.add(dicomformatR);
		downloadPanel.add(dicomformatR);

		JRadioButton niftiformatR = new JRadioButton("Nifti");
		formatRG.add(niftiformatR);
		downloadPanel.add(niftiformatR);

		GridBagConstraints gBCFormatTF = new GridBagConstraints();
		gBCFormatTF.insets = new Insets(10, 10, 10, 10);
		gBCFormatTF.fill = GridBagConstraints.HORIZONTAL;
		gBCFormatTF.anchor = GridBagConstraints.WEST;
		gBCFormatTF.gridx = 1;
		gBCFormatTF.gridy = posY++;
		downloadPanel.add(dicomformatR, gBCFormatTF);

		GridBagConstraints gBCFormatTF2 = new GridBagConstraints();
		gBCFormatTF2.insets = new Insets(10, 10, 10, 10);
		gBCFormatTF2.fill = GridBagConstraints.HORIZONTAL;
		gBCFormatTF2.anchor = GridBagConstraints.EAST;
		gBCFormatTF2.gridx = 1;
		gBCFormatTF2.gridy = posY++;
		downloadPanel.add(niftiformatR, gBCFormatTF2);

		// JLabel outputDirectoryLabel = new JLabel(resourceBundle.getString("shanoir.uploader.outputDirectoryLabel"));
		// birthDateLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		// GridBagConstraints gBCBOutputDirectoryLabel = new GridBagConstraints();
		// gBCBOutputDirectoryLabel.anchor = GridBagConstraints.EAST;
		// gBCBOutputDirectoryLabel.insets = new Insets(10, 10, 10, 10);
		// gBCBOutputDirectoryLabel.gridx = 0;
		// gBCBOutputDirectoryLabel.gridy = posY;
		// downloadPanel.add(outputDirectoryLabel, gBCBOutputDirectoryLabel);

		// JTextField outputDirectoryTF = new JTextField();
		// GridBagConstraints gBCOutputDirectoryTF = new GridBagConstraints();
		// gBCOutputDirectoryTF.insets = new Insets(10, 10, 10, 10);
		// gBCOutputDirectoryTF.fill = GridBagConstraints.HORIZONTAL;
		// gBCOutputDirectoryTF.gridx = 1;
		// gBCOutputDirectoryTF.gridy = posY;
		// gBCOutputDirectoryTF.gridwidth = 2;
		// downloadPanel.add(outputDirectoryTF, gBCOutputDirectoryTF);
		// outputDirectoryTF.setColumns(15);
		// outputDirectoryTF.setText(System.getProperty("user.home"));

		// JButton outputDirectoryButton;
		// outputDirectoryButton = new JButton("...");
		// GridBagConstraints gbc_outputDirectoryButton = new GridBagConstraints();
		// gbc_outputDirectoryButton.insets = new Insets(0, 0, 5, 0);
		// gbc_outputDirectoryButton.fill = GridBagConstraints.HORIZONTAL;
		// gbc_outputDirectoryButton.anchor = GridBagConstraints.EAST;
		// gbc_outputDirectoryButton.gridx = 2;
		// gbc_outputDirectoryButton.gridy = posY++;
		// downloadPanel.add(outputDirectoryButton, gbc_outputDirectoryButton);
		
		// outputDirectoryButton.addActionListener(new java.awt.event.ActionListener() {
		// 	public void actionPerformed(java.awt.event.ActionEvent evt) {

		// 		JFileChooser fileChooser = new JFileChooser();
		// 		fileChooser.setDialogTitle(resourceBundle.getString("shanoir.uploader.chooseDirectory"));   
		// 		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		// 		int userSelection = fileChooser.showSaveDialog(frame);
				
		// 		if (userSelection == JFileChooser.APPROVE_OPTION) {

		// 			File outputDirectory = fileChooser.getSelectedFile();
		// 			outputDirectoryTF.setText(outputDirectory.getPath());
		// 		}
				
		// 	}
		// });


		JButton downloadButton;
		downloadButton = new JButton(resourceBundle.getString("shanoir.uploader.downloadButton"));
		GridBagConstraints gbc_downloadButton = new GridBagConstraints();
		gbc_downloadButton.insets = new Insets(0, 0, 5, 0);
		gbc_downloadButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_downloadButton.gridx = 0;
		gbc_downloadButton.gridy = posY;
		downloadPanel.add(downloadButton, gbc_downloadButton);

		ImageIcon downloadIcon = new ImageIcon(getClass().getClassLoader().getResource("images/spinner.gif"));
		JLabel downloadAnimationLabel = new JLabel();
		downloadAnimationLabel.setIcon(downloadIcon);
		downloadIcon.setImageObserver(downloadAnimationLabel);
		GridBagConstraints gbc_downloadAnimationLabel = new GridBagConstraints();
		gbc_downloadAnimationLabel.insets = new Insets(0, 0, 5, 0);
		gbc_downloadAnimationLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_downloadAnimationLabel.anchor = GridBagConstraints.EAST;
		gbc_downloadAnimationLabel.gridx = 1;
		gbc_downloadAnimationLabel.gridy = posY;
		// downloadAnimationLabel.setVisible(false);
		downloadPanel.add(downloadAnimationLabel, gbc_downloadAnimationLabel);

		downloadButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {

				try {
					
					String format = dicomformatR.isSelected() ? "dcm" : "nii";
					
					Long datasetId = null;
					Long studyId = null;
					Long subjectId = null;

					if (!downloadDatasetIDTF.getText().isEmpty()) {
						datasetId = Long.parseLong(downloadDatasetIDTF.getText());
					}
					if (!downloadStudyIDTF.getText().isEmpty()) {
						studyId = Long.parseLong(downloadStudyIDTF.getText());
					}
					if (!downloadSubjectIDTF.getText().isEmpty()) {
						subjectId = Long.parseLong(downloadSubjectIDTF.getText());
					}

					if(datasetId == null && studyId == null && subjectId == null) {
						String message = resourceBundle.getString("shanoir.uploader.invalidId");
						throw new Exception(message);
					}

					// File outputDirectory = new File(outputDirectoryTF.getText());
					File outputDirectory = new File(System.getProperty("user.home"));

					downloadAnimationLabel.setVisible(true);
					
					ShanoirUploaderServiceClientNG shng = ShUpOnloadConfig.getShanoirUploaderServiceClientNG();

					String message = "";

					if (datasetId != null) {
						message = ShanoirDownloader.downloadDataset(outputDirectory, datasetId, format, shng);
					}

					if (studyId != null && subjectId == null) {
						message = ShanoirDownloader.downloadDatasetByStudy(outputDirectory, studyId, format, shng);
					}

					if (studyId == null && subjectId != null) {
						message = ShanoirDownloader.downloadDatasetBySubject(outputDirectory, subjectId, format, shng);
					}

					if (studyId != null && subjectId != null) {
						message = ShanoirDownloader.downloadDatasetBySubjectIdStudyId(outputDirectory, studyId, subjectId, format, shng);
					}
					
					downloadAnimationLabel.setVisible(false);
					if(message == "") {
						message = resourceBundle.getString("shanoir.uploader.downloadComplete");
					}
					JOptionPane.showMessageDialog(frame, message, "Info", JOptionPane.INFORMATION_MESSAGE);

				} catch (Exception e) {
					downloadAnimationLabel.setVisible(false);
					logger.error(e.getMessage());
					String message = resourceBundle.getString("shanoir.uploader.downloadError") + " \n\n" + e.getMessage();
					JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		contentPane.add(tabbedPane, BorderLayout.CENTER);
	}

	public FindDicomActionListener getFindDicomActionListener() {
		return fAL;
	}
	
	public SelectionActionListener getSAL() {
		return sAL;
	}

	public void updateAccessGranted(boolean accessGranted) {
		if (accessGranted && ShUpOnloadConfig.isAutoImportEnabled()) {
			mnAutoimport.setText(resourceBundle.getString("shanoir.uploader.yesKeycloakToken"));
		} else {
			mnAutoimport.setText(resourceBundle.getString("shanoir.uploader.noKeycloakToken"));
		}
	}

	public ImportDialogOpener getImportDialogOpener() {
		return importDialogOpener;
	}

	public void setImportDialogOpener(ImportDialogOpener importDialogOpener) {
		this.importDialogOpener = importDialogOpener;
	}

	public ImportDialogOpenerNG getImportDialogOpenerNG() {
		return importDialogOpenerNG;
	}

	public void setImportDialogOpenerNG(ImportDialogOpenerNG importDialogOpenerNG) {
		this.importDialogOpenerNG = importDialogOpenerNG;
	}
	
}
