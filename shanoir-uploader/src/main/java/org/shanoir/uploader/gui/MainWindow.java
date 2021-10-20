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

import org.apache.log4j.Logger;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.action.DownloadOrCopyActionListener;
import org.shanoir.uploader.action.FindDicomActionListener;
import org.shanoir.uploader.action.ImportDialogOpener;
import org.shanoir.uploader.action.NoOrYesAnonRChangeListener;
import org.shanoir.uploader.action.RSDocumentListener;
import org.shanoir.uploader.action.SelectionActionListener;
import org.shanoir.uploader.dicom.IDicomServerClient;
import org.shanoir.uploader.dicom.anonymize.Pseudonymizer;
import org.shanoir.uploader.exception.PseudonymusException;
import org.shanoir.uploader.service.rest.UrlConfig;


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
	public JScrollPane dicomTreeJScrollPane;
	public JTextField patientNameTF;
	public JTextField patientIDTF;
	public JTextField studyDescriptionTF;
	public JTextField seriesDescriptionTF;
	
	public JPanel editPanel;
	public ButtonGroup anonymisedBG;
	public JRadioButton noAnonR, yesAnonR;
	public JLabel newPatientIDLabel;
	public JTextField newPatientIDTF;
	public JLabel dummyLabelAsLayoutBuffer;
	public JLabel lastNameLabel;
	public JTextField lastNameTF;
	public JButton birthNameCopyButton;
	public JLabel firstNameLabel;
	public JTextField firstNameTF;
	public JLabel birthNameLabel;
	public JTextField birthNameTF;
	public JTextField birthDateTF;
	public ButtonGroup sexRG;
	public JRadioButton mSexR, fSexR;
	public JButton downloadOrCopyButton;
	
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

		/**
		 * Handle menu bar here:
		 */
		
		JMenuBar menuBar = new JMenuBar();
		contentPane.add(menuBar, BorderLayout.NORTH);

		JMenu mnFile = new JMenu(resourceBundle.getString("shanoir.uploader.fileMenu"));
		menuBar.add(mnFile);

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		ImageIcon searchIcon = new ImageIcon(getClass().getClassLoader().getResource("images/search.png"));
		JMenuItem mntmOpenDicomFromCD = new JMenuItem(resourceBundle.getString("shanoir.uploader.fileMenu.openCD"), searchIcon);
		fAL = new FindDicomActionListener(this, fileChooser, dicomServerClient);
		mntmOpenDicomFromCD.addActionListener(fAL);
		mnFile.add(mntmOpenDicomFromCD);

		JMenu mnConfiguration = new JMenu(resourceBundle.getString("shanoir.uploader.configurationMenu"));
		menuBar.add(mnConfiguration);
		
		JMenu mnImport = new JMenu(resourceBundle.getString("shanoir.uploader.importMenu"));
		menuBar.add(mnImport);
		
		JMenuItem mnImportExcell = new JMenuItem(resourceBundle.getString("shanoir.uploader.importMenu.csv"));
		mnImport.add(mnImportExcell);
		
		mnImportExcell.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ImportFromCSVWindow importcsv = new ImportFromCSVWindow(shanoirUploaderFolder, resourceBundle, scrollPaneUpload, dicomServerClient, ShUpOnloadConfig.getShanoirUploaderServiceClient());
			}
		});

		// add Server Configuration and Dicom configuration Menu Items
		JMenuItem mntmDicomServerConfiguration = new JMenuItem(
				resourceBundle.getString("shanoir.uploader.configurationMenu.dicomServer"));
		mnConfiguration.add(mntmDicomServerConfiguration);
		mntmDicomServerConfiguration.addActionListener(new ActionListener() {
			@Override
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

		// Language Configuration Menu
		JMenuItem mntmLanguage = new JMenuItem(resourceBundle.getString("shanoir.uploader.configurationMenu.language"));
		mnConfiguration.add(mntmLanguage);
		mntmLanguage.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				LanguageConfigurationWindow sscw = new LanguageConfigurationWindow(shanoirUploaderFolder,
						resourceBundle);
			}
		});

		JMenu mnHelp = new JMenu(resourceBundle.getString("shanoir.uploader.helpMenu"));
		menuBar.add(mnHelp);

		JMenuItem mntmAboutShUp = new JMenuItem(resourceBundle.getString("shanoir.uploader.helpMenu.aboutShUp"));
		mnHelp.add(mntmAboutShUp);
		mntmAboutShUp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				AboutWindow aboutW = new AboutWindow(resourceBundle);
			}
		});

		JMenu profileSelected = new JMenu("<html><b>" + resourceBundle.getString("shanoir.uploader.profileMenu") + ShUpConfig.profileSelected + "</b></html>");
		menuBar.add(Box.createRigidArea(new Dimension(400,5)));
		menuBar.add(profileSelected);

		/**
		 * Handle inside content here:
		 */
		JTabbedPane tabbedPane = new JTabbedPane();

		JSplitPane splitPaneQueryAndDicomTree = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		final JPanel queryPanel = new JPanel();
		queryPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		splitPaneQueryAndDicomTree.setLeftComponent(queryPanel);
		dicomTreeJScrollPane = new JScrollPane();
		splitPaneQueryAndDicomTree.setRightComponent(dicomTreeJScrollPane);
		sAL = new SelectionActionListener(this, resourceBundle);

		JPanel editCurrentUploadsPanel = new JPanel(new BorderLayout());
		editPanel = new JPanel();
		editPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		editCurrentUploadsPanel.add(editPanel, BorderLayout.NORTH);

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
		gbc_queryPanelLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_queryPanelLabel.insets = new Insets(5, 5, 0, 0);
		gbc_queryPanelLabel.gridwidth = 3;
		gbc_queryPanelLabel.gridx = 0;
		gbc_queryPanelLabel.gridy = 0;
		queryPanel.add(queryPanelLabel, gbc_queryPanelLabel);

		JLabel patientNameLabel = new JLabel(resourceBundle.getString("shanoir.uploader.patientNameLabel"));
		patientNameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_patientNameLabel = new GridBagConstraints();
		gbc_patientNameLabel.anchor = GridBagConstraints.EAST;
		gbc_patientNameLabel.insets = new Insets(5, 5, 0, 0);
		gbc_patientNameLabel.gridx = 0;
		gbc_patientNameLabel.gridy = 1;
		queryPanel.add(patientNameLabel, gbc_patientNameLabel);

		patientNameTF = new JTextField();
		GridBagConstraints gbc_patientNameTF = new GridBagConstraints();
		gbc_patientNameTF.insets = new Insets(5, 5, 0, 0);
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
		gbc_HelpButton.insets = new Insets(5, 2, 0, 2);
		gbc_HelpButton.gridx = 2;
		gbc_HelpButton.gridy = 1;
		queryPanel.add(helpButton, gbc_HelpButton);

		helpButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
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

				
				JOptionPane.showMessageDialog(queryPanel, message, "Help",
						JOptionPane.INFORMATION_MESSAGE);
			}
		});

		// If fields Patient name, Patient ID and Study description are empty
		// Query DICOM server button is grey
		CaretListener caretQueryPACSfields = new CaretListener() {
			@Override
			public void caretUpdate(javax.swing.event.CaretEvent e) {
				if (patientNameTF.getText().length() != 0
						|| patientIDTF.getText().length() != 0
						|| studyDescriptionTF.getText().length() != 0
						|| dateRS.length() != 0 || studyDate.length() != 0) {
					queryButton.setEnabled(true);
				} else {
					queryButton.setEnabled(false);
				}

			}
		};
		patientNameTF.addCaretListener(caretQueryPACSfields);

		JLabel PatientIDLabel = new JLabel(resourceBundle.getString("shanoir.uploader.patientIDLabel"));
		PatientIDLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_PatientIDLabel = new GridBagConstraints();
		gbc_PatientIDLabel.anchor = GridBagConstraints.EAST;
		gbc_PatientIDLabel.insets = new Insets(5, 5, 0, 0);
		gbc_PatientIDLabel.gridx = 0;
		gbc_PatientIDLabel.gridy = 2;
		queryPanel.add(PatientIDLabel, gbc_PatientIDLabel);

		patientIDTF = new JTextField();
		GridBagConstraints gbc_patientIDTF = new GridBagConstraints();
		gbc_patientIDTF.insets = new Insets(5, 5, 0, 0);
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
		gbc_studyDescriptionLabel.insets = new Insets(5, 5, 0, 0);
		gbc_studyDescriptionLabel.gridx = 0;
		gbc_studyDescriptionLabel.gridy = 3;
		queryPanel.add(studyDescriptionLabel, gbc_studyDescriptionLabel);

		studyDescriptionTF = new JTextField();
		GridBagConstraints gbc_studyDescriptionTF = new GridBagConstraints();
		gbc_studyDescriptionTF.insets = new Insets(5, 5, 0, 0);
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
		gbc_birthDateReasearchLabel.insets = new Insets(5, 5, 0, 0);
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
		gbc_birthDateReasearchTF.insets = new Insets(5, 5, 0, 0);
		gbc_birthDateReasearchTF.fill = GridBagConstraints.HORIZONTAL;
		gbc_birthDateReasearchTF.gridx = 1;
		gbc_birthDateReasearchTF.gridy = 4;
		queryPanel.add(datePicker, gbc_birthDateReasearchTF);

		datePicker.addActionListener(new ActionListener() {
			@Override
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
							&& studyDate.length() == 0) {
						queryButton.setEnabled(false);
					}
				}
			}
		});

		// Add Examination date field
		// 0008,0020 StudyDate
		JLabel studyDateLabel = new JLabel(resourceBundle.getString("shanoir.uploader.studyDateLabel"));
		GridBagConstraints gbc_studyDateLabel = new GridBagConstraints();
		gbc_studyDateLabel.anchor = GridBagConstraints.EAST;
		gbc_studyDateLabel.insets = new Insets(5, 5, 0, 0);
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
		gbc_studyDatePicker.insets = new Insets(5, 5, 0, 0);
		gbc_studyDatePicker.fill = GridBagConstraints.HORIZONTAL;
		gbc_studyDatePicker.gridx = 1;
		gbc_studyDatePicker.gridy = 5;
		queryPanel.add(studyDatePicker, gbc_studyDatePicker);

		studyDatePicker.addActionListener(new ActionListener() {
			@Override
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
							&& dateRS.length() == 0) {
						queryButton.setEnabled(false);
					}
				}
			}
		});

		queryButton = new JButton(resourceBundle.getString("shanoir.uploader.queryButton"), searchIcon);
		GridBagConstraints gbc_queryButton = new GridBagConstraints();
		gbc_queryButton.insets = new Insets(5, 5, 5, 0);
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
		gbc_editPanelLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_editPanelLabel.insets = new Insets(10, 10, 10, 10);
		gbc_editPanelLabel.gridx = 0;
		gbc_editPanelLabel.gridy = 0;
		gbc_editPanelLabel.gridwidth = 4;
		editPanel.add(editPanelLabel, gbc_editPanelLabel);

		/**
		 * Radio buttons for anonymised: no or yes
		 */
		JLabel anonymisedLabel = new JLabel(resourceBundle.getString("shanoir.uploader.editPanel.anonymisedLabel"));
		anonymisedLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gBCAnonymisedLabel = new GridBagConstraints();
		gBCAnonymisedLabel.anchor = GridBagConstraints.EAST;
		gBCAnonymisedLabel.insets = new Insets(10, 10, 10, 10);
		gBCAnonymisedLabel.gridx = 0;
		gBCAnonymisedLabel.gridy = 1;
		editPanel.add(anonymisedLabel, gBCAnonymisedLabel);
		// create buttons and fill into group
		anonymisedBG = new ButtonGroup();
		noAnonR = new JRadioButton("No");
		noAnonR.setSelected(true);
		noAnonR.setEnabled(false);
		anonymisedBG.add(noAnonR);
		editPanel.add(noAnonR);
		yesAnonR = new JRadioButton("Yes");
		yesAnonR.setEnabled(false);
		anonymisedBG.add(yesAnonR);
		editPanel.add(yesAnonR);
		// define gBC for noAnonR
		GridBagConstraints gBCNoAnonR = new GridBagConstraints();
		gBCNoAnonR.insets = new Insets(10, 10, 10, 10);
		gBCNoAnonR.fill = GridBagConstraints.HORIZONTAL;
		gBCNoAnonR.gridx = 1;
		gBCNoAnonR.gridy = 1;
		editPanel.add(noAnonR, gBCNoAnonR);
		// define gBC for yesAnonR
		GridBagConstraints gBCYesAnonR = new GridBagConstraints();
		gBCYesAnonR.insets = new Insets(10, 10, 10, 10);
		gBCYesAnonR.fill = GridBagConstraints.HORIZONTAL;
		gBCYesAnonR.gridx = 2;
		gBCYesAnonR.gridy = 1;
		editPanel.add(yesAnonR, gBCYesAnonR);
		
		newPatientIDLabel = new JLabel(resourceBundle.getString("shanoir.uploader.newPatientIDLabel"));
		newPatientIDTF = new JTextField();
		newPatientIDTF.getDocument().addDocumentListener(new RSDocumentListener(this));
		dummyLabelAsLayoutBuffer = new JLabel("           "); // used to correct layout for button copyToBirthName
		dummyLabelAsLayoutBuffer.setHorizontalAlignment(SwingConstants.RIGHT);

		lastNameLabel = new JLabel(resourceBundle.getString("shanoir.uploader.lastNameLabel"));
		lastNameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gBCLastNameLabel = new GridBagConstraints();
		gBCLastNameLabel.anchor = GridBagConstraints.EAST;
		gBCLastNameLabel.insets = new Insets(10, 10, 10, 10);
		gBCLastNameLabel.gridx = 0;
		gBCLastNameLabel.gridy = 2;
		editPanel.add(lastNameLabel, gBCLastNameLabel);

		lastNameTF = new JTextField();
		GridBagConstraints gBCLastNameTF = new GridBagConstraints();
		gBCLastNameTF.insets = new Insets(10, 10, 10, 10);
		gBCLastNameTF.fill = GridBagConstraints.HORIZONTAL;
		gBCLastNameTF.gridx = 1;
		gBCLastNameTF.gridy = 2;
		gBCLastNameTF.gridwidth = 2;
		lastNameTF.setColumns(15);
		lastNameTF.setEnabled(false);
		editPanel.add(lastNameTF, gBCLastNameTF);

		lastNameTF.getDocument().addDocumentListener(new RSDocumentListener(this));

		// add a button to copy the last name to the birth name
		ImageIcon copyIcon = new ImageIcon(getClass().getClassLoader()
				.getResource("images/copyLastNameToBirthName.16x16.png"));
		birthNameCopyButton = new JButton(copyIcon);
		GridBagConstraints gBCBithNameCopyButton = new GridBagConstraints();
		gBCBithNameCopyButton.anchor = GridBagConstraints.EAST;
		gBCBithNameCopyButton.insets = new Insets(10, 10, 10, 10);
		gBCBithNameCopyButton.gridx = 3;
		gBCBithNameCopyButton.gridy = 2;
		birthNameCopyButton.setEnabled(false);
		editPanel.add(birthNameCopyButton, gBCBithNameCopyButton);

		firstNameLabel = new JLabel(resourceBundle.getString("shanoir.uploader.firstNameLabel"));
		firstNameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gBCFirstNameLabel = new GridBagConstraints();
		gBCFirstNameLabel.anchor = GridBagConstraints.EAST;
		gBCFirstNameLabel.insets = new Insets(10, 10, 10, 10);
		gBCFirstNameLabel.gridx = 0;
		gBCFirstNameLabel.gridy = 3;
		editPanel.add(firstNameLabel, gBCFirstNameLabel);

		firstNameTF = new JTextField();
		GridBagConstraints gBCFirstNameTF = new GridBagConstraints();
		gBCFirstNameTF.insets = new Insets(10, 10, 10, 10);
		gBCFirstNameTF.fill = GridBagConstraints.HORIZONTAL;
		gBCFirstNameTF.gridx = 1;
		gBCFirstNameTF.gridy = 3;
		gBCFirstNameTF.gridwidth = 2;
		firstNameTF.setColumns(15);
		firstNameTF.setEnabled(false);
		editPanel.add(firstNameTF, gBCFirstNameTF);

		firstNameTF.getDocument().addDocumentListener(new RSDocumentListener(this));

		birthNameLabel = new JLabel(resourceBundle.getString("shanoir.uploader.BirthNameLabel"));
		birthNameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gBCBirthNameLabel = new GridBagConstraints();
		gBCBirthNameLabel.anchor = GridBagConstraints.EAST;
		gBCBirthNameLabel.insets = new Insets(10, 10, 10, 10);
		gBCBirthNameLabel.gridx = 0;
		gBCBirthNameLabel.gridy = 4;
		editPanel.add(birthNameLabel, gBCBirthNameLabel);

		birthNameTF = new JTextField();
		GridBagConstraints gBCBirthNameTF = new GridBagConstraints();
		gBCBirthNameTF.insets = new Insets(10, 10, 10, 10);
		gBCBirthNameTF.fill = GridBagConstraints.HORIZONTAL;
		gBCBirthNameTF.gridx = 1;
		gBCBirthNameTF.gridy = 4;
		gBCBirthNameTF.gridwidth = 2;
		birthNameTF.setColumns(15);
		birthNameTF.setEnabled(false);
		editPanel.add(birthNameTF, gBCBirthNameTF);

		birthNameCopyButton.addActionListener(new ActionListener() {
			@Override
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
		gBCBirthDateLabel.gridy = 5;
		editPanel.add(birthDateLabel, gBCBirthDateLabel);

		birthDateTF = new JTextField();
		GridBagConstraints gBCBirthDateTF = new GridBagConstraints();
		gBCBirthDateTF.insets = new Insets(10, 10, 10, 10);
		gBCBirthDateTF.fill = GridBagConstraints.HORIZONTAL;
		gBCBirthDateTF.gridx = 1;
		gBCBirthDateTF.gridy = 5;
		gBCBirthDateTF.gridwidth = 2;
		editPanel.add(birthDateTF, gBCBirthDateTF);
		birthDateTF.setEnabled(false);
		birthDateTF.setColumns(15);
		birthDateTF.getDocument().addDocumentListener(
				new RSDocumentListener(this));

		/**
		 * Radio buttons for sex: M or F
		 */
		JLabel sexLabel = new JLabel(resourceBundle.getString("shanoir.uploader.sexLabel"));
		sexLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gBCSexLabel = new GridBagConstraints();
		gBCSexLabel.anchor = GridBagConstraints.EAST;
		gBCSexLabel.insets = new Insets(10, 10, 10, 10);
		gBCSexLabel.gridx = 0;
		gBCSexLabel.gridy = 6;
		editPanel.add(sexLabel, gBCSexLabel);
		sexRG = new ButtonGroup();
		mSexR = new JRadioButton("M");
		mSexR.setEnabled(false);
		sexRG.add(mSexR);
		editPanel.add(mSexR);
		fSexR = new JRadioButton("F");
		fSexR.setEnabled(false);
		sexRG.add(fSexR);
		editPanel.add(fSexR);
		GridBagConstraints gBCMSexR = new GridBagConstraints();
		gBCMSexR.insets = new Insets(10, 10, 10, 10);
		gBCMSexR.fill = GridBagConstraints.HORIZONTAL;
		gBCMSexR.gridx = 1;
		gBCMSexR.gridy = 6;
		editPanel.add(mSexR, gBCMSexR);
		GridBagConstraints gBCFSexR = new GridBagConstraints();
		gBCFSexR.insets = new Insets(10, 10, 10, 10);
		gBCMSexR.fill = GridBagConstraints.HORIZONTAL;
		gBCFSexR.gridx = 2;
		gBCFSexR.gridy = 6;
		editPanel.add(fSexR, gBCFSexR);

		NoOrYesAnonRChangeListener changeListener = new NoOrYesAnonRChangeListener(this);
		noAnonR.addChangeListener(changeListener);
		yesAnonR.addChangeListener(changeListener);
		
		/**
		 * Last button for download or copy action:
		 */
		ImageIcon downloadIcon = new ImageIcon(getClass().getClassLoader().getResource("images/download.png"));
		downloadOrCopyButton = new JButton(resourceBundle.getString("shanoir.uploader.downloadOrCopyButton"), downloadIcon);
		GridBagConstraints gbc_btnRetrieve = new GridBagConstraints();
		gbc_btnRetrieve.insets = new Insets(0, 0, 5, 0);
		gbc_btnRetrieve.gridwidth = 4;
		gbc_btnRetrieve.gridx = 0;
		gbc_btnRetrieve.gridy = 7;
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
		
		// add ShUp principal panel (splitPane) and upload job display pane
		// (scrollPaneUpload) to TabbedPane
		final JPanel notificationPanel = new JPanel();
		// add Notification panel to display in the main window the upload state
		notificationPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		editCurrentUploadsPanel.add(notificationPanel, BorderLayout.CENTER);

		final JSplitPane mainSplitPane = new JSplitPane();
		mainSplitPane.setLeftComponent(splitPaneQueryAndDicomTree);
		mainSplitPane.setRightComponent(editCurrentUploadsPanel);
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
		
		// add main split pane here
		tabbedPane.addTab(resourceBundle.getString("shanoir.uploader.mainWindowTab"), null, mainSplitPane,
				resourceBundle.getString("shanoir.uploader.mainWindowTab.tooltip"));
		JPanel currentUploadsPanel = new JPanel(false);
		// and below the current uploads panel
		tabbedPane.addTab(resourceBundle.getString("shanoir.uploader.currentUploadsTab"), null, currentUploadsPanel,
				resourceBundle.getString("shanoir.uploader.currentUploadsTab.tooltip"));
		scrollPaneUpload = new JScrollPane();
		scrollPaneUpload.setBounds(0, 0, MAXIMIZED_HORIZ, MAXIMIZED_VERT);
		scrollPaneUpload.setPreferredSize(new Dimension(898, 600));
		currentUploadsPanel.add(scrollPaneUpload);
		final DownloaderPanel downloaderPanel = new DownloaderPanel(frame, gBLPanel, resourceBundle, logger);
		tabbedPane.addTab(resourceBundle.getString("shanoir.uploader.downloadDatasetsTab"), null, downloaderPanel,
				resourceBundle.getString("shanoir.uploader.downloadDatasetsTab.tooltip"));
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
	
}
