package org.shanoir.uploader.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
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

import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;
import org.shanoir.ng.importer.dicom.ImagesCreatorAndDicomFileAnalyzerService;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.action.DownloadOrCopyActionListener;
import org.shanoir.uploader.action.FindDicomActionListener;
import org.shanoir.uploader.action.ImportDialogOpener;
import org.shanoir.uploader.action.RSDocumentListener;
import org.shanoir.uploader.action.SelectionActionListener;
import org.shanoir.uploader.dicom.IDicomServerClient;
import org.shanoir.uploader.dicom.anonymize.Pseudonymizer;
import org.shanoir.uploader.exception.PseudonymusException;
import org.shanoir.uploader.service.rest.UrlConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The MainWindow of the ShanoirUploader.
 * 
 * @author mkain
 * 
 */
@SuppressWarnings("serial")
public class MainWindow extends JFrame {

	private static final Logger logger = LoggerFactory.getLogger(MainWindow.class);

	public JFrame frame = this;

	public DicomTree dicomTree;
	public JPanel contentPane;
	public JScrollPane dicomTreeJScrollPane;
	public JTextField patientNameTF;
	public JTextField patientIDTF;
	public JTextField studyDescriptionTF;
	public JTextField seriesDescriptionTF;
	public ButtonGroup modalityRG;
	public JRadioButton mrRB, ctRB, ptRB, nmRB, noRB;
	public ButtonGroup queryLevelRG;
	public JRadioButton pRB, sRB;

	public JPanel editPanel;
	public ButtonGroup anonymisedBG;
	public JLabel lastNameLabel;
	public JTextField lastNameTF;
	public JButton birthNameCopyButton;
	public JLabel firstNameLabel;
	public JTextField firstNameTF;
	public JLabel birthNameLabel;
	public JTextField birthNameTF;
	public JTextField birthDateTF;
	public ButtonGroup sexRG;
	public JRadioButton fSexR, mSexR, oSexR;
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

	public UtilDateModel birthDateModel;
	public UtilDateModel studyDateModel;
	public String birthDate = "";
	public String studyDate = "";
	public String modality = "";
	JScrollPane scrollPaneUpload;

	public JLabel startedDownloadsLB;
	public JProgressBar downloadProgressBar;
	public JLabel errorDownloadsLB;
	public JLabel downloadErrorAlert;

	public JLabel startedUploadsLB;
	public JProgressBar uploadProgressBar;
	public JLabel finishedUploadsLB;
	public JLabel errorUploadsLB;
	public JLabel uploadErrorAlert;
	
	public IDicomServerClient dicomServerClient;
	public File shanoirUploaderFolder;

	public ResourceBundle resourceBundle;
	public ShUpConfig shanoirUploaderConfiguration;
	
	private ImportDialogOpener importDialogOpener;
	
	private ImagesCreatorAndDicomFileAnalyzerService dicomFileAnalyzer;

	/**
	 * Create the frame.
	 */
	public MainWindow(final IDicomServerClient dicomServerClient, final File shanoirUploaderFolder,
			final UrlConfig urlConfig, final ResourceBundle resourceBundle) {
		this.dicomServerClient = dicomServerClient;
		this.dicomFileAnalyzer = new ImagesCreatorAndDicomFileAnalyzerService();
		this.shanoirUploaderFolder = shanoirUploaderFolder;
		this.resourceBundle = resourceBundle;
		String JFRAME_TITLE = "ShanoirUploader " + ShUpConfig.SHANOIR_UPLOADER_VERSION + " " + ShUpConfig.RELEASE_DATE;
		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (Exception e) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
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
		
		JMenuItem mnImportExcell = new JMenuItem(resourceBundle.getString("shanoir.uploader.importMenu.table"));
		mnImport.add(mnImportExcell);

		mnImportExcell.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ImportFromTableWindow importTable = new ImportFromTableWindow(shanoirUploaderFolder, resourceBundle, scrollPaneUpload, dicomServerClient, dicomFileAnalyzer, ShUpOnloadConfig.getShanoirUploaderServiceClient(), dOCAL);
			}
		});

		JMenuItem mnImportFolder = new JMenuItem(resourceBundle.getString("shanoir.uploader.importMenu.folder"));
		mnImport.add(mnImportFolder);

		mnImportFolder.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ImportFromFolderWindow importFolder = new ImportFromFolderWindow(shanoirUploaderFolder, resourceBundle, scrollPaneUpload, dicomServerClient, dicomFileAnalyzer, ShUpOnloadConfig.getShanoirUploaderServiceClient());
			}
		});

		// add Server Configuration and Dicom configuration Menu Items
		JMenuItem mntmDicomServerConfiguration = new JMenuItem(
				resourceBundle.getString("shanoir.uploader.configurationMenu.dicomServer"));
		mnConfiguration.add(mntmDicomServerConfiguration);
		mntmDicomServerConfiguration.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				DicomServerConfigurationWindow dscw = new DicomServerConfigurationWindow(dicomServerClient,
						shanoirUploaderFolder, resourceBundle);
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

		JMenu profileSelected = new JMenu("<html>"
			+ "[ " + resourceBundle.getString("shanoir.uploader.profileMenu") + ShUpConfig.profileSelected + " ]"
			+ " "
			+ "[ " + resourceBundle.getString("shanoir.uploader.accountMenu") + ShUpConfig.username + " ]</html>");
		menuBar.add(Box.createRigidArea(new Dimension(200,5)));
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
		gbc_queryPanelLabel.insets = new Insets(10, 10, 10, 10);
		gbc_queryPanelLabel.gridwidth = 3;
		gbc_queryPanelLabel.gridx = 0;
		gbc_queryPanelLabel.gridy = 0;
		queryPanel.add(queryPanelLabel, gbc_queryPanelLabel);

		JLabel queryLevelLabel = new JLabel(resourceBundle.getString("shanoir.uploader.queryLevelLabel"));
		GridBagConstraints gbc_queryLevelLabel = new GridBagConstraints();
		gbc_queryLevelLabel.anchor = GridBagConstraints.EAST;
		gbc_queryLevelLabel.insets = new Insets(5, 5, 2, 0);
		gbc_queryLevelLabel.gridx = 0;
		gbc_queryLevelLabel.gridy = 1;
		queryPanel.add(queryLevelLabel, gbc_queryLevelLabel);
		
		JPanel queryRadioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
		queryLevelRG = new ButtonGroup();

		// "Patient" Radio Button
		pRB = new JRadioButton("Patient");
		pRB.setSelected(true);
		queryLevelRG.add(pRB);
		queryRadioPanel.add(pRB);

		// "Study" Radio Button
		sRB = new JRadioButton(resourceBundle.getString("shanoir.uploader.queryLevelStudy"));
		queryLevelRG.add(sRB);
		queryRadioPanel.add(sRB);

		GridBagConstraints gbc_radioPanel = new GridBagConstraints();
		gbc_radioPanel.insets = new Insets(2, 0, 0, 0);
		gbc_radioPanel.gridx = 1;
		gbc_radioPanel.gridy = 1;
		gbc_radioPanel.gridwidth = 2;
		gbc_radioPanel.anchor = GridBagConstraints.WEST;
		gbc_radioPanel.fill = GridBagConstraints.HORIZONTAL;
		queryPanel.add(queryRadioPanel, gbc_radioPanel);

		JLabel patientNameLabel = new JLabel(resourceBundle.getString("shanoir.uploader.patientNameLabel"));
		patientNameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_patientNameLabel = new GridBagConstraints();
		gbc_patientNameLabel.anchor = GridBagConstraints.EAST;
		gbc_patientNameLabel.insets = new Insets(5, 5, 0, 0);
		gbc_patientNameLabel.gridx = 0;
		gbc_patientNameLabel.gridy = 2;
		queryPanel.add(patientNameLabel, gbc_patientNameLabel);

		patientNameTF = new JTextField();
		GridBagConstraints gbc_patientNameTF = new GridBagConstraints();
		gbc_patientNameTF.insets = new Insets(5, 5, 0, 10);
		gbc_patientNameTF.fill = GridBagConstraints.HORIZONTAL;
		gbc_patientNameTF.gridwidth = 6;
		gbc_patientNameTF.gridx = 1;
		gbc_patientNameTF.gridy = 2;
		queryPanel.add(patientNameTF, gbc_patientNameTF);
		patientNameTF.setColumns(15);
		patientNameTF.setText("");
		patientNameTF.setToolTipText(resourceBundle.getString("shanoir.uploader.patientNameLabel.tooltip"));


		// If fields Patient name, Patient ID and Study description are empty
		// Query DICOM server button is grey
		CaretListener caretQueryPACSfields = new CaretListener() {
			@Override
			public void caretUpdate(javax.swing.event.CaretEvent e) {
				if (patientNameTF.getText().length() != 0
						|| patientIDTF.getText().length() != 0
						|| studyDescriptionTF.getText().length() != 0
						|| birthDate.length() != 0 || studyDate.length() != 0) {
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
		gbc_PatientIDLabel.gridy = 3;
		queryPanel.add(PatientIDLabel, gbc_PatientIDLabel);

		patientIDTF = new JTextField();
		GridBagConstraints gbc_patientIDTF = new GridBagConstraints();
		gbc_patientIDTF.insets = new Insets(5, 5, 0, 10);
		gbc_patientIDTF.fill = GridBagConstraints.HORIZONTAL;
		gbc_patientIDTF.gridwidth = 6;
		gbc_patientIDTF.gridx = 1;
		gbc_patientIDTF.gridy = 3;
		queryPanel.add(patientIDTF, gbc_patientIDTF);
		patientIDTF.setColumns(15);
		patientIDTF.setText("");
		patientIDTF.addCaretListener(caretQueryPACSfields);
		
		// Add Birth Date field
		JLabel birthDateReasearchLabel = new JLabel(resourceBundle.getString("shanoir.uploader.patientBirthDateLabel"));
		GridBagConstraints gbc_birthDateReasearchLabel = new GridBagConstraints();
		gbc_birthDateReasearchLabel.anchor = GridBagConstraints.EAST;
		gbc_birthDateReasearchLabel.insets = new Insets(5, 5, 0, 0);
		gbc_birthDateReasearchLabel.gridx = 0;
		gbc_birthDateReasearchLabel.gridy = 4;
		queryPanel.add(birthDateReasearchLabel, gbc_birthDateReasearchLabel);

		birthDateModel = new UtilDateModel();

		Properties p = new Properties();
		p.put("text.today", "Today");
		p.put("text.month", "Month");
		p.put("text.year", "Year");
		JDatePanelImpl birthDatePanel = new JDatePanelImpl(birthDateModel, p);
		DateLabelFormatter dLP = new DateLabelFormatter();
		final JDatePickerImpl birthDatePicker = new JDatePickerImpl(birthDatePanel, dLP);
		birthDatePicker.setTextEditable(true);

		GridBagConstraints gbc_birthDateResearchTF = new GridBagConstraints();
		gbc_birthDateResearchTF.insets = new Insets(5, 5, 0, 10);
		gbc_birthDateResearchTF.fill = GridBagConstraints.HORIZONTAL;
		gbc_birthDateResearchTF.gridwidth = 6;
		gbc_birthDateResearchTF.gridx = 1;
		gbc_birthDateResearchTF.gridy = 4;
		queryPanel.add(birthDatePicker, gbc_birthDateResearchTF);

		final SimpleDateFormat manualFormatter = new SimpleDateFormat("dd/MM/yyyy");
		final SimpleDateFormat dicomFormatter = new SimpleDateFormat("yyyyMMdd");
		JFormattedTextField birthDateTextField = birthDatePicker.getJFormattedTextField();
        birthDateTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                try {
					String birthDateText = birthDateTextField.getText();
					if (birthDateText != null && !"".equals(birthDateText)) {
						Date birthDateDate = manualFormatter.parse(birthDateText);
						birthDateModel.setValue(birthDateDate);
						birthDate = dicomFormatter.format(birthDateDate);
						queryButton.setEnabled(true);
					} else {
						birthDate = "";
						if (patientNameTF.getText().length() == 0
								&& patientIDTF.getText().length() == 0
								&& studyDescriptionTF.getText().length() == 0
								&& studyDate.length() == 0) {
							queryButton.setEnabled(false);
						}
					}
                } catch (ParseException ex) {
                    logger.error(ex.getMessage(), e);
					birthDate = "";
					if (patientNameTF.getText().length() == 0
							&& patientIDTF.getText().length() == 0
							&& studyDescriptionTF.getText().length() == 0
							&& studyDate.length() == 0) {
						queryButton.setEnabled(false);
					}
                }
            }
        });

		birthDatePicker.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (birthDateModel.getValue() != null) {
					birthDate = dicomFormatter.format(birthDateModel.getValue());
					queryButton.setEnabled(true);
				} else {
					birthDate = "";
					if (patientNameTF.getText().length() == 0
							&& patientIDTF.getText().length() == 0
							&& studyDescriptionTF.getText().length() == 0
							&& studyDate.length() == 0) {
						queryButton.setEnabled(false);
					}
				}
			}
		});
		
		JLabel studyDescriptionLabel = new JLabel(resourceBundle.getString("shanoir.uploader.studyDescriptionLabel"));
		GridBagConstraints gbc_studyDescriptionLabel = new GridBagConstraints();
		gbc_studyDescriptionLabel.anchor = GridBagConstraints.EAST;
		gbc_studyDescriptionLabel.insets = new Insets(5, 5, 0, 0);
		gbc_studyDescriptionLabel.gridx = 0;
		gbc_studyDescriptionLabel.gridy = 5;
		queryPanel.add(studyDescriptionLabel, gbc_studyDescriptionLabel);

		studyDescriptionTF = new JTextField();
		GridBagConstraints gbc_studyDescriptionTF = new GridBagConstraints();
		gbc_studyDescriptionTF.insets = new Insets(5, 5, 0, 10);
		gbc_studyDescriptionTF.fill = GridBagConstraints.HORIZONTAL;
		gbc_studyDescriptionTF.gridwidth = 6;
		gbc_studyDescriptionTF.gridx = 1;
		gbc_studyDescriptionTF.gridy = 5;
		queryPanel.add(studyDescriptionTF, gbc_studyDescriptionTF);
		studyDescriptionTF.setColumns(15);
		studyDescriptionTF.setText("");
		studyDescriptionTF.addCaretListener(caretQueryPACSfields);

		// Add Examination date field
		// 0008,0020 StudyDate
		JLabel studyDateLabel = new JLabel(resourceBundle.getString("shanoir.uploader.studyDateLabel"));
		GridBagConstraints gbc_studyDateLabel = new GridBagConstraints();
		gbc_studyDateLabel.anchor = GridBagConstraints.EAST;
		gbc_studyDateLabel.insets = new Insets(5, 5, 0, 0);
		gbc_studyDateLabel.gridx = 0;
		gbc_studyDateLabel.gridy = 6;
		queryPanel.add(studyDateLabel, gbc_studyDateLabel);

		studyDateModel = new UtilDateModel();

		Properties prop = new Properties();
		prop.put("text.today", "Today");
		prop.put("text.month", "Month");
		prop.put("text.year", "Year");
		JDatePanelImpl studyDatePanel = new JDatePanelImpl(studyDateModel, prop);
		DateLabelFormatter studyDLP = new DateLabelFormatter();
		final JDatePickerImpl studyDatePicker = new JDatePickerImpl(
				studyDatePanel, studyDLP);
		studyDatePicker.setTextEditable(true);

		GridBagConstraints gbc_studyDatePicker = new GridBagConstraints();
		gbc_studyDatePicker.insets = new Insets(5, 5, 0, 10);
		gbc_studyDatePicker.fill = GridBagConstraints.HORIZONTAL;
		gbc_studyDatePicker.gridwidth = 6;
		gbc_studyDatePicker.gridx = 1;
		gbc_studyDatePicker.gridy = 6;
		queryPanel.add(studyDatePicker, gbc_studyDatePicker);

		JFormattedTextField studyDateTextField = studyDatePicker.getJFormattedTextField();
		studyDateTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                try {
					String studyDateText = studyDateTextField.getText();
					if (studyDateText != null && !"".equals(studyDateText)) {
						Date studyDateDate = manualFormatter.parse(studyDateText);
						studyDateModel.setValue(studyDateDate);
						studyDate = dicomFormatter.format(studyDateDate);
						queryButton.setEnabled(true);
					} else {
						studyDate = "";
						if (patientNameTF.getText().length() == 0
								&& patientIDTF.getText().length() == 0
								&& studyDescriptionTF.getText().length() == 0
								&& birthDate.length() == 0) {
							queryButton.setEnabled(false);
						}
					}
				} catch (ParseException ex) {
                    logger.error(ex.getMessage(), e);
					studyDate = "";
					if (patientNameTF.getText().length() == 0
							&& patientIDTF.getText().length() == 0
							&& studyDescriptionTF.getText().length() == 0
							&& birthDate.length() == 0) {
						queryButton.setEnabled(false);
					}
                }
            }
        });
		studyDatePicker.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (studyDateModel.getValue() != null) {
					studyDate = dicomFormatter.format(studyDateModel.getValue());
					queryButton.setEnabled(true);
				} else {
					studyDate = "";
					if (patientNameTF.getText().length() == 0
							&& patientIDTF.getText().length() == 0
							&& studyDescriptionTF.getText().length() == 0
							&& birthDate.length() == 0) {
						queryButton.setEnabled(false);
					}
				}
			}
		});

		JLabel modalityLabel = new JLabel(resourceBundle.getString("shanoir.uploader.modalityLabel"));
		GridBagConstraints gbc_modalityLabel = new GridBagConstraints();
		gbc_modalityLabel.anchor = GridBagConstraints.EAST;
		gbc_modalityLabel.insets = new Insets(5, 5, 0, 0);
		gbc_modalityLabel.gridx = 0;
		gbc_modalityLabel.gridy = 7;
		queryPanel.add(modalityLabel, gbc_modalityLabel);
		
		String[] modalityList = { "MR", "CT", "PT", "NM", "XA", "None" };
		JComboBox<String> modalityCB = new JComboBox<String>(modalityList);
		modalityCB.setSelectedIndex(0);
		GridBagConstraints gBC_modality = new GridBagConstraints();
		gBC_modality.anchor = GridBagConstraints.WEST;
		gBC_modality.insets = new Insets(5, 5, 0, 0);
		gBC_modality.gridx = 1;
		gBC_modality.gridy = 7;
		queryPanel.add(modalityCB, gBC_modality);

		modalityCB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// If None is selected we set back to null
				if (modalityList[modalityList.length - 1].equals(modalityCB.getSelectedItem())) {
					modality = null;
				} else {
					modality = (String) modalityCB.getSelectedItem();
				}	
			}
		});
		
		queryButton = new JButton(resourceBundle.getString("shanoir.uploader.queryButton"), searchIcon);
		GridBagConstraints gbc_queryButton = new GridBagConstraints();
		gbc_queryButton.anchor = GridBagConstraints.CENTER;
		gbc_queryButton.insets = new Insets(5, 5, 5, 5);
		gbc_queryButton.weightx = 1.0;
		gbc_queryButton.gridwidth = 8;
		gbc_queryButton.gridx = 0;
		gbc_queryButton.gridy = 8;
		queryPanel.add(queryButton, gbc_queryButton);
		queryButton.setEnabled(false);
		frame.getRootPane().setDefaultButton(queryButton);
		queryButton.addActionListener(fAL);

		JSeparator separator = new JSeparator();
		GridBagConstraints gbc_separator = new GridBagConstraints();
		gbc_separator.insets = new Insets(10, 10, 10, 10);
		gbc_separator.gridx = 1;
		gbc_separator.gridy = 7;
		queryPanel.add(separator, gbc_separator);

		/**
		 * Edit panel
		 */
		JLabel editPanelLabel = new JLabel(resourceBundle.getString("shanoir.uploader.sendBoxMessage"));
		GridBagConstraints gbc_editPanelLabel = new GridBagConstraints();
		gbc_editPanelLabel.anchor = GridBagConstraints.WEST;
		gbc_editPanelLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_editPanelLabel.insets = new Insets(10, 10, 10, 10);
		gbc_editPanelLabel.gridx = 0;
		gbc_editPanelLabel.gridy = 0;
		gbc_editPanelLabel.gridwidth = 4;
		editPanel.add(editPanelLabel, gbc_editPanelLabel);

		lastNameLabel = new JLabel(resourceBundle.getString("shanoir.uploader.lastNameLabel"));
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
		lastNameTF.setColumns(15);
		lastNameTF.setEnabled(false);
		editPanel.add(lastNameTF, gBCLastNameTF);

		lastNameTF.getDocument().addDocumentListener(new RSDocumentListener(this));

		// add a button to copy the last name to the birth name
		ImageIcon copyIcon = new ImageIcon(getClass().getClassLoader()
				.getResource("images/copyLastNameToBirthName.16x16.png"));
		birthNameCopyButton = new JButton(copyIcon);
		GridBagConstraints gBCBithNameCopyButton = new GridBagConstraints();
		gBCBithNameCopyButton.anchor = GridBagConstraints.WEST;
		gBCBithNameCopyButton.insets = new Insets(10, 0, 10, 10);
		gBCBithNameCopyButton.gridx = 3;
		gBCBithNameCopyButton.gridy = 3;
		birthNameCopyButton.setToolTipText(resourceBundle.getString("shanoir.uploader.copyLastNameToBirthName"));
		birthNameCopyButton.setEnabled(false);
		editPanel.add(birthNameCopyButton, gBCBithNameCopyButton);

		firstNameLabel = new JLabel(resourceBundle.getString("shanoir.uploader.firstNameLabel"));
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
		gBCBirthNameLabel.gridy = 3;
		editPanel.add(birthNameLabel, gBCBirthNameLabel);

		birthNameTF = new JTextField();
		GridBagConstraints gBCBirthNameTF = new GridBagConstraints();
		gBCBirthNameTF.insets = new Insets(10, 10, 10, 10);
		gBCBirthNameTF.fill = GridBagConstraints.HORIZONTAL;
		gBCBirthNameTF.gridx = 1;
		gBCBirthNameTF.gridy = 3;
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
		birthDateTF.setEnabled(false);
		birthDateTF.setColumns(15);
		birthDateTF.getDocument().addDocumentListener(
				new RSDocumentListener(this));

		/**
		 * Radio buttons for sex: Female, Male or Other
		 */
		GridBagConstraints sexGBconstraints = new GridBagConstraints();
		sexGBconstraints.insets = new Insets(10, 0, 10, 10);

		JLabel sexLabel = new JLabel(resourceBundle.getString("shanoir.uploader.sexLabel"));
		sexLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		sexGBconstraints.anchor = GridBagConstraints.EAST;
		sexGBconstraints.gridx = 0;
		sexGBconstraints.gridy = 5;
		editPanel.add(sexLabel, sexGBconstraints);

		JPanel sexRadioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
		sexRG = new ButtonGroup();

		fSexR = new JRadioButton(resourceBundle.getString("shanoir.uploader.sex.F"));
		fSexR.setEnabled(false);
		sexRG.add(fSexR);
		sexRadioPanel.add(fSexR);

		mSexR = new JRadioButton(resourceBundle.getString("shanoir.uploader.sex.M"));
		mSexR.setEnabled(false);
		sexRG.add(mSexR);
		sexRadioPanel.add(mSexR);

		oSexR = new JRadioButton(resourceBundle.getString("shanoir.uploader.sex.O"));
		oSexR.setEnabled(false);
		sexRG.add(oSexR);
		sexRadioPanel.add(oSexR);

		sexGBconstraints.gridx = 1;
		sexGBconstraints.gridy = 5;
		sexGBconstraints.gridwidth = 3;
		sexGBconstraints.anchor = GridBagConstraints.WEST;
		sexGBconstraints.fill = GridBagConstraints.HORIZONTAL;
		editPanel.add(sexRadioPanel, sexGBconstraints);

		/**
		 * Last button for download or copy action:
		 */
		ImageIcon downloadIcon = new ImageIcon(getClass().getClassLoader().getResource("images/download.png"));
		downloadOrCopyButton = new JButton(resourceBundle.getString("shanoir.uploader.downloadOrCopyButton"), downloadIcon);
		GridBagConstraints gbc_btnRetrieve = new GridBagConstraints();
		gbc_btnRetrieve.insets = new Insets(0, 0, 5, 0);
		gbc_btnRetrieve.gridwidth = 4;
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
		dOCAL = new DownloadOrCopyActionListener(this, pseudonymizer, dicomServerClient, dicomFileAnalyzer);
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

		/**
		 * Notification panel: copy or download
		 */
		GridBagConstraints gbc_notificationDownloadsOrCopies = new GridBagConstraints();
		gbc_notificationDownloadsOrCopies.insets = new Insets(10, 10, 10, 10);
		gbc_notificationDownloadsOrCopies.fill = GridBagConstraints.WEST;
		gbc_notificationDownloadsOrCopies.gridx = 0;
		gbc_notificationDownloadsOrCopies.gridy = 0;
		JLabel notificationDownloadsOrCopies = new JLabel();
		notificationDownloadsOrCopies.setText(resourceBundle.getString("shanoir.uploader.currentCopyOrDownloadSummary"));
		Font font = new Font("Courier", Font.BOLD, 12);
		notificationDownloadsOrCopies.setFont(font);
		notificationPanel.add(notificationDownloadsOrCopies, gbc_notificationDownloadsOrCopies);

		GridBagConstraints gbc_startedDownloadsLB = new GridBagConstraints();
		gbc_startedDownloadsLB.insets = new Insets(10, 10, 10, 10);
		gbc_startedDownloadsLB.fill = GridBagConstraints.EAST;
		gbc_startedDownloadsLB.gridx = 0;
		gbc_startedDownloadsLB.gridy = 1;
		startedDownloadsLB = new JLabel();
		startedDownloadsLB.setText(resourceBundle.getString("shanoir.uploader.currentCopyOrDownloadStarted"));
		notificationPanel.add(startedDownloadsLB, gbc_startedDownloadsLB);

		downloadProgressBar = new JProgressBar(0, 100);
		downloadProgressBar.setValue(0);
		downloadProgressBar.setStringPainted(true);
		downloadProgressBar.setVisible(true);

		GridBagConstraints gbc_downloadProgressBar = new GridBagConstraints();
		gbc_downloadProgressBar.insets = new Insets(10, 10, 10, 10);
		gbc_downloadProgressBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_downloadProgressBar.gridx = 1;
		gbc_downloadProgressBar.gridy = 1;
		notificationPanel.add(downloadProgressBar, gbc_downloadProgressBar);

		errorDownloadsLB = new JLabel();
		GridBagConstraints gbc_errorDownloadsLB = new GridBagConstraints();
		gbc_errorDownloadsLB.insets = new Insets(10, 10, 10, 10);
		gbc_errorDownloadsLB.fill = GridBagConstraints.EAST;
		gbc_errorDownloadsLB.gridx = 0;
		gbc_errorDownloadsLB.gridy = 2;
		notificationPanel.add(errorDownloadsLB, gbc_errorDownloadsLB);

		downloadErrorAlert = new JLabel();
		GridBagConstraints gbc_downloadErrorAlert = new GridBagConstraints();
		gbc_downloadErrorAlert.insets = new Insets(10, 10, 10, 10);
		gbc_downloadErrorAlert.fill = GridBagConstraints.EAST;
		gbc_downloadErrorAlert.gridx = 1;
		gbc_downloadErrorAlert.gridy = 2;
		gbc_downloadErrorAlert.gridheight = 2;
		notificationPanel.add(downloadErrorAlert, gbc_downloadErrorAlert);

		/**
		 * Notification panel: imports
		 */
		JLabel notificationCurrentUploads = new JLabel();
		GridBagConstraints gbc_notificationCurrentUploads = new GridBagConstraints();
		gbc_notificationCurrentUploads.insets = new Insets(10, 10, 10, 10);
		gbc_notificationCurrentUploads.fill = GridBagConstraints.WEST;
		gbc_notificationCurrentUploads.gridx = 0;
		gbc_notificationCurrentUploads.gridy = 3;
		notificationCurrentUploads.setText(resourceBundle.getString("shanoir.uploader.currentUploadsSummary"));
		notificationPanel.add(notificationCurrentUploads,
				gbc_notificationCurrentUploads);
		notificationCurrentUploads.setFont(font);

		startedUploadsLB = new JLabel();
		GridBagConstraints gbc_startedUploadsLB = new GridBagConstraints();
		gbc_startedUploadsLB.insets = new Insets(10, 10, 10, 10);
		gbc_startedUploadsLB.fill = GridBagConstraints.EAST;
		gbc_startedUploadsLB.gridx = 0;
		gbc_startedUploadsLB.gridy = 4;
		notificationPanel.add(startedUploadsLB, gbc_startedUploadsLB);

		uploadProgressBar = new JProgressBar(0, 100);
		uploadProgressBar.setValue(0);
		uploadProgressBar.setStringPainted(true);
		uploadProgressBar.setVisible(true);

		GridBagConstraints gbc_progressBar = new GridBagConstraints();
		gbc_progressBar.insets = new Insets(10, 10, 10, 10);
		gbc_progressBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_progressBar.gridx = 1;
		gbc_progressBar.gridy = 4;
		notificationPanel.add(uploadProgressBar, gbc_progressBar);

		finishedUploadsLB = new JLabel();
		GridBagConstraints gbc_finishedUploadsLB = new GridBagConstraints();
		gbc_finishedUploadsLB.insets = new Insets(10, 10, 10, 10);
		gbc_finishedUploadsLB.fill = GridBagConstraints.EAST;
		gbc_finishedUploadsLB.gridx = 0;
		gbc_finishedUploadsLB.gridy = 5;
		notificationPanel.add(finishedUploadsLB, gbc_finishedUploadsLB);

		errorUploadsLB = new JLabel();
		GridBagConstraints gbc_errorUploadsLB = new GridBagConstraints();
		gbc_errorUploadsLB.insets = new Insets(10, 10, 10, 10);
		gbc_errorUploadsLB.fill = GridBagConstraints.EAST;
		gbc_errorUploadsLB.gridx = 0;
		gbc_errorUploadsLB.gridy = 6;
		notificationPanel.add(errorUploadsLB, gbc_errorUploadsLB);

		uploadErrorAlert = new JLabel();
		GridBagConstraints gbc_errorAlert = new GridBagConstraints();
		gbc_errorAlert.insets = new Insets(10, 10, 10, 10);
		gbc_errorAlert.fill = GridBagConstraints.EAST;
		gbc_errorAlert.gridx = 1;
		gbc_errorAlert.gridy = 6;
		gbc_errorAlert.gridheight = 2;
		notificationPanel.add(uploadErrorAlert, gbc_errorAlert);
		
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
