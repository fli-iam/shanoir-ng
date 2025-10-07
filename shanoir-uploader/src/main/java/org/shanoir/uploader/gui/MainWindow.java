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
import java.awt.event.ItemEvent;
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
import javax.swing.JCheckBoxMenuItem;
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
import org.shanoir.uploader.service.rest.UrlConfig;
import org.shanoir.uploader.utils.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.formdev.flatlaf.FlatLightLaf;


/**
 * The MainWindow of the ShanoirUploader.
 *
 * @author mkain
 *
 */
@SuppressWarnings("serial")
public class MainWindow extends JFrame {

    private static final Logger LOG = LoggerFactory.getLogger(MainWindow.class);

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
    public DownloadOrCopyActionListener dOCAL;
    private SelectionActionListener sAL;

    public JMenu mnAutoimport;
    public JCheckBoxMenuItem checkOnServerMenuItem;
    public boolean isFromPACS;
    public boolean isDicomObjectSelected = false;

    public UtilDateModel birthDateModel;
    public UtilDateModel studyDateModel;
    public String birthDate = "";
    public String studyDate = "";
    public String modality;
    JScrollPane scrollPaneUpload;
    public JButton deleteFinishedUploads;

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

    public ImagesCreatorAndDicomFileAnalyzerService dicomFileAnalyzer;

    private ImportDialogOpener importDialogOpener;

    /**
     * Create the frame.
     */
    public MainWindow(final IDicomServerClient dicomServerClient, final File shanoirUploaderFolder,
            final UrlConfig urlConfig, final ResourceBundle resourceBundle) {
        this.dicomServerClient = dicomServerClient;
        this.dicomFileAnalyzer = new ImagesCreatorAndDicomFileAnalyzerService();
        this.shanoirUploaderFolder = shanoirUploaderFolder;
        this.resourceBundle = resourceBundle;
        String jframeTitle = "ShanoirUploader " + ShUpConfig.SHANOIR_UPLOADER_VERSION;
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                LOG.error(ex.getMessage());
            }
        }

        setTitle(jframeTitle);
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
                ImportFromTableWindow importTable = new ImportFromTableWindow(shanoirUploaderFolder, resourceBundle, scrollPaneUpload, dicomServerClient, dicomFileAnalyzer, ShUpOnloadConfig.getShanoirUploaderServiceClient(), ShUpOnloadConfig.getPseudonymizer());
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

        checkOnServerMenuItem = new JCheckBoxMenuItem(
            resourceBundle.getString("shanoir.uploader.configurationMenu.checkOnServer"));
        mnConfiguration.add(checkOnServerMenuItem);
        String filePath = ShUpConfig.shanoirUploaderFolder + File.separator + ShUpConfig.BASIC_PROPERTIES;
        checkOnServerMenuItem.addItemListener(e -> {
            boolean selected = (e.getStateChange() == ItemEvent.SELECTED);
            if (selected) {
                LOG.info("Saving check.on.server true in basic.properties file.");
                PropertiesUtil.storePropertyToFile(filePath, ShUpConfig.basicProperties, ShUpConfig.CHECK_ON_SERVER, Boolean.TRUE.toString());
            } else {
                LOG.info("Saving check.on.server false in basic.properties file.");
                PropertiesUtil.storePropertyToFile(filePath, ShUpConfig.basicProperties, ShUpConfig.CHECK_ON_SERVER, Boolean.FALSE.toString());
            }
        });

        JMenuItem mntmLanguage = new JMenuItem(resourceBundle.getString("shanoir.uploader.configurationMenu.language"));
        mnConfiguration.add(mntmLanguage);
        mntmLanguage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                LanguageConfigurationWindow sscw = new LanguageConfigurationWindow(shanoirUploaderFolder,
                        resourceBundle);
            }
        });

        JCheckBoxMenuItem pseudonymizeAfterCopyOrDownloadMenuItem = new JCheckBoxMenuItem(
                resourceBundle.getString("shanoir.uploader.configurationMenu.pseudonymizeAfterCopyOrDownload"));
        mnConfiguration.add(pseudonymizeAfterCopyOrDownloadMenuItem);

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
        menuBar.add(Box.createRigidArea(new Dimension(200, 5)));
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
        gBLPanel.columnWidths = new int[] {0, 0, 0};
        gBLPanel.rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0};
        gBLPanel.columnWeights = new double[] {1.0, 1.0, Double.MIN_VALUE};
        gBLPanel.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                Double.MIN_VALUE};
        queryPanel.setLayout(gBLPanel);
        editPanel.setLayout(gBLPanel);

        /**
         * Query panel elements
         */
        JLabel queryPanelLabel = new JLabel(
                resourceBundle.getString("shanoir.uploader.queryBoxMessage"));
        GridBagConstraints gbcQueryPanelLabel = new GridBagConstraints();
        gbcQueryPanelLabel.anchor = GridBagConstraints.WEST;
        gbcQueryPanelLabel.fill = GridBagConstraints.HORIZONTAL;
        gbcQueryPanelLabel.insets = new Insets(10, 10, 10, 10);
        gbcQueryPanelLabel.gridwidth = 3;
        gbcQueryPanelLabel.gridx = 0;
        gbcQueryPanelLabel.gridy = 0;
        queryPanel.add(queryPanelLabel, gbcQueryPanelLabel);

        ImageIcon infoIcon = new ImageIcon(getClass().getResource("/images/info.png"));

        JLabel queryLevelLabel = new JLabel();
        queryLevelLabel.setText("<html>" + resourceBundle.getString("shanoir.uploader.queryLevelLabel") + " <img src='" + infoIcon + "'> :</html>");
        queryLevelLabel.setToolTipText(resourceBundle.getString("shanoir.uploader.queryLevel.tooltip"));
        GridBagConstraints gbcQueryLevelLabel = new GridBagConstraints();
        gbcQueryLevelLabel.anchor = GridBagConstraints.EAST;
        gbcQueryLevelLabel.insets = new Insets(5, 5, 2, 0);
        gbcQueryLevelLabel.gridx = 0;
        gbcQueryLevelLabel.gridy = 1;
        queryPanel.add(queryLevelLabel, gbcQueryLevelLabel);

        JPanel queryRadioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        queryLevelRG = new ButtonGroup();

        // "Patient" Radio Button
        pRB = new JRadioButton(resourceBundle.getString("shanoir.uploader.queryLevelPatient"));
        pRB.setSelected(true);
        queryLevelRG.add(pRB);
        queryRadioPanel.add(pRB);

        // "Study" Radio Button
        sRB = new JRadioButton(resourceBundle.getString("shanoir.uploader.queryLevelStudy"));
        queryLevelRG.add(sRB);
        queryRadioPanel.add(sRB);

        GridBagConstraints gbcRadioPanel = new GridBagConstraints();
        gbcRadioPanel.insets = new Insets(2, 0, 0, 0);
        gbcRadioPanel.gridx = 1;
        gbcRadioPanel.gridy = 1;
        gbcRadioPanel.gridwidth = 2;
        gbcRadioPanel.anchor = GridBagConstraints.WEST;
        gbcRadioPanel.fill = GridBagConstraints.HORIZONTAL;
        queryPanel.add(queryRadioPanel, gbcRadioPanel);

        JLabel patientNameLabel = new JLabel();
        patientNameLabel.setText("<html>" + resourceBundle.getString("shanoir.uploader.patientNameLabel") + " <img src='" + infoIcon + "'> :</html>");
        patientNameLabel.setToolTipText(resourceBundle.getString("shanoir.uploader.patientNameLabel.tooltip"));
        patientNameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        GridBagConstraints gbcPatientNameLabel = new GridBagConstraints();
        gbcPatientNameLabel.anchor = GridBagConstraints.EAST;
        gbcPatientNameLabel.insets = new Insets(5, 5, 0, 0);
        gbcPatientNameLabel.gridx = 0;
        gbcPatientNameLabel.gridy = 2;
        queryPanel.add(patientNameLabel, gbcPatientNameLabel);

        patientNameTF = new JTextField();
        GridBagConstraints gbcPatientNameTF = new GridBagConstraints();
        gbcPatientNameTF.insets = new Insets(5, 5, 0, 10);
        gbcPatientNameTF.fill = GridBagConstraints.HORIZONTAL;
        gbcPatientNameTF.gridwidth = 6;
        gbcPatientNameTF.gridx = 1;
        gbcPatientNameTF.gridy = 2;
        queryPanel.add(patientNameTF, gbcPatientNameTF);
        patientNameTF.setColumns(15);
        patientNameTF.setText("");

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

        JLabel patientIDLabel = new JLabel(resourceBundle.getString("shanoir.uploader.patientIDLabel"));
        patientIDLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        GridBagConstraints gbcPatientIDLabel = new GridBagConstraints();
        gbcPatientIDLabel.anchor = GridBagConstraints.EAST;
        gbcPatientIDLabel.insets = new Insets(5, 5, 0, 0);
        gbcPatientIDLabel.gridx = 0;
        gbcPatientIDLabel.gridy = 3;
        queryPanel.add(patientIDLabel, gbcPatientIDLabel);

        patientIDTF = new JTextField();
        GridBagConstraints gbcPatientIDTF = new GridBagConstraints();
        gbcPatientIDTF.insets = new Insets(5, 5, 0, 10);
        gbcPatientIDTF.fill = GridBagConstraints.HORIZONTAL;
        gbcPatientIDTF.gridwidth = 6;
        gbcPatientIDTF.gridx = 1;
        gbcPatientIDTF.gridy = 3;
        queryPanel.add(patientIDTF, gbcPatientIDTF);
        patientIDTF.setColumns(15);
        patientIDTF.setText("");
        patientIDTF.addCaretListener(caretQueryPACSfields);

        // Add Birth Date field
        JLabel birthDateReasearchLabel = new JLabel(resourceBundle.getString("shanoir.uploader.patientBirthDateLabel"));
        GridBagConstraints gbcBirthDateResearchLabel = new GridBagConstraints();
        gbcBirthDateResearchLabel.anchor = GridBagConstraints.EAST;
        gbcBirthDateResearchLabel.insets = new Insets(5, 5, 0, 0);
        gbcBirthDateResearchLabel.gridx = 0;
        gbcBirthDateResearchLabel.gridy = 4;
        queryPanel.add(birthDateReasearchLabel, gbcBirthDateResearchLabel);

        birthDateModel = new UtilDateModel();

        Properties p = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");
        JDatePanelImpl birthDatePanel = new JDatePanelImpl(birthDateModel, p);
        DateLabelFormatter dLP = new DateLabelFormatter();
        final JDatePickerImpl birthDatePicker = new JDatePickerImpl(birthDatePanel, dLP);
        birthDatePicker.setTextEditable(true);

        GridBagConstraints gbcBirthDateResearchTF = new GridBagConstraints();
        gbcBirthDateResearchTF.insets = new Insets(5, 5, 0, 10);
        gbcBirthDateResearchTF.fill = GridBagConstraints.HORIZONTAL;
        gbcBirthDateResearchTF.gridwidth = 6;
        gbcBirthDateResearchTF.gridx = 1;
        gbcBirthDateResearchTF.gridy = 4;
        queryPanel.add(birthDatePicker, gbcBirthDateResearchTF);

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
                    LOG.error(ex.getMessage(), e);
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

        JLabel studyDescriptionLabel = new JLabel();
        studyDescriptionLabel.setText("<html>" + resourceBundle.getString("shanoir.uploader.studyDescriptionLabel") + " <img src='" + infoIcon + "'> :</html>");
        studyDescriptionLabel.setToolTipText(resourceBundle.getString("shanoir.uploader.studyDescription.tooltip"));
        GridBagConstraints gbcStudyDescriptionLabel = new GridBagConstraints();
        gbcStudyDescriptionLabel.anchor = GridBagConstraints.EAST;
        gbcStudyDescriptionLabel.insets = new Insets(5, 5, 0, 0);
        gbcStudyDescriptionLabel.gridx = 0;
        gbcStudyDescriptionLabel.gridy = 5;
        queryPanel.add(studyDescriptionLabel, gbcStudyDescriptionLabel);

        studyDescriptionTF = new JTextField();
        GridBagConstraints gbcStudyDescriptionTF = new GridBagConstraints();
        gbcStudyDescriptionTF.insets = new Insets(5, 5, 0, 10);
        gbcStudyDescriptionTF.fill = GridBagConstraints.HORIZONTAL;
        gbcStudyDescriptionTF.gridwidth = 6;
        gbcStudyDescriptionTF.gridx = 1;
        gbcStudyDescriptionTF.gridy = 5;
        queryPanel.add(studyDescriptionTF, gbcStudyDescriptionTF);
        studyDescriptionTF.setColumns(15);
        studyDescriptionTF.setText("");
        studyDescriptionTF.addCaretListener(caretQueryPACSfields);

        // Add Examination date field
        // 0008,0020 StudyDate
        JLabel studyDateLabel = new JLabel(resourceBundle.getString("shanoir.uploader.studyDateLabel"));
        GridBagConstraints gbcStudyDateLabel = new GridBagConstraints();
        gbcStudyDateLabel.anchor = GridBagConstraints.EAST;
        gbcStudyDateLabel.insets = new Insets(5, 5, 0, 0);
        gbcStudyDateLabel.gridx = 0;
        gbcStudyDateLabel.gridy = 6;
        queryPanel.add(studyDateLabel, gbcStudyDateLabel);

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

        GridBagConstraints gbcStudyDatePicker = new GridBagConstraints();
        gbcStudyDatePicker.insets = new Insets(5, 5, 0, 10);
        gbcStudyDatePicker.fill = GridBagConstraints.HORIZONTAL;
        gbcStudyDatePicker.gridwidth = 6;
        gbcStudyDatePicker.gridx = 1;
        gbcStudyDatePicker.gridy = 6;
        queryPanel.add(studyDatePicker, gbcStudyDatePicker);

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
                    LOG.error(ex.getMessage(), e);
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
        GridBagConstraints gbcModalityLabel = new GridBagConstraints();
        gbcModalityLabel.anchor = GridBagConstraints.EAST;
        gbcModalityLabel.insets = new Insets(5, 5, 0, 0);
        gbcModalityLabel.gridx = 0;
        gbcModalityLabel.gridy = 7;
        queryPanel.add(modalityLabel, gbcModalityLabel);

        String[] modalityList = {"MR", "CT", "PT", "NM", "XA", "None"};
        JComboBox<String> modalityCB = new JComboBox<String>(modalityList);
        // We set by default the modality to None because some PACS do not support modality at Patient or even Study root query
        modalityCB.setSelectedIndex(modalityList.length - 1);
        GridBagConstraints gbcModality = new GridBagConstraints();
        gbcModality.anchor = GridBagConstraints.WEST;
        gbcModality.insets = new Insets(5, 5, 0, 0);
        gbcModality.gridx = 1;
        gbcModality.gridy = 7;
        queryPanel.add(modalityCB, gbcModality);

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
        GridBagConstraints gbcQueryButton = new GridBagConstraints();
        gbcQueryButton.anchor = GridBagConstraints.CENTER;
        gbcQueryButton.insets = new Insets(5, 5, 5, 5);
        gbcQueryButton.weightx = 1.0;
        gbcQueryButton.gridwidth = 8;
        gbcQueryButton.gridx = 0;
        gbcQueryButton.gridy = 8;
        queryPanel.add(queryButton, gbcQueryButton);
        queryButton.setEnabled(false);
        frame.getRootPane().setDefaultButton(queryButton);
        queryButton.addActionListener(fAL);

        JSeparator separator = new JSeparator();
        GridBagConstraints gbcSeparator = new GridBagConstraints();
        gbcSeparator.insets = new Insets(10, 10, 10, 10);
        gbcSeparator.gridx = 1;
        gbcSeparator.gridy = 7;
        queryPanel.add(separator, gbcSeparator);

        /**
         * Edit panel
         */
        JLabel editPanelLabel = new JLabel(resourceBundle.getString("shanoir.uploader.sendBoxMessage"));
        GridBagConstraints gbcEditPanelLabel = new GridBagConstraints();
        gbcEditPanelLabel.anchor = GridBagConstraints.WEST;
        gbcEditPanelLabel.fill = GridBagConstraints.HORIZONTAL;
        gbcEditPanelLabel.insets = new Insets(10, 10, 10, 10);
        gbcEditPanelLabel.gridx = 0;
        gbcEditPanelLabel.gridy = 0;
        gbcEditPanelLabel.gridwidth = 4;
        editPanel.add(editPanelLabel, gbcEditPanelLabel);

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
        GridBagConstraints gbcBtnRetrieve = new GridBagConstraints();
        gbcBtnRetrieve.insets = new Insets(0, 0, 5, 0);
        gbcBtnRetrieve.gridwidth = 4;
        gbcBtnRetrieve.gridx = 0;
        gbcBtnRetrieve.gridy = 6;
        editPanel.add(downloadOrCopyButton, gbcBtnRetrieve);

        menuBar.add(Box.createHorizontalGlue());

        dOCAL = new DownloadOrCopyActionListener(this, ShUpOnloadConfig.getPseudonymizer(), dicomServerClient, dicomFileAnalyzer);
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
        GridBagConstraints gbcNotificationDownloadsOrCopies = new GridBagConstraints();
        gbcNotificationDownloadsOrCopies.insets = new Insets(10, 10, 10, 10);
        gbcNotificationDownloadsOrCopies.fill = GridBagConstraints.WEST;
        gbcNotificationDownloadsOrCopies.gridx = 0;
        gbcNotificationDownloadsOrCopies.gridy = 0;
        JLabel notificationDownloadsOrCopies = new JLabel();
        notificationDownloadsOrCopies.setText(resourceBundle.getString("shanoir.uploader.currentCopyOrDownloadSummary"));
        Font font = new Font("Courier", Font.BOLD, 12);
        notificationDownloadsOrCopies.setFont(font);
        notificationPanel.add(notificationDownloadsOrCopies, gbcNotificationDownloadsOrCopies);

        GridBagConstraints gbcStartedDownloadsLB = new GridBagConstraints();
        gbcStartedDownloadsLB.insets = new Insets(10, 10, 10, 10);
        gbcStartedDownloadsLB.fill = GridBagConstraints.EAST;
        gbcStartedDownloadsLB.gridx = 0;
        gbcStartedDownloadsLB.gridy = 1;
        startedDownloadsLB = new JLabel();
        startedDownloadsLB.setText(resourceBundle.getString("shanoir.uploader.currentCopyOrDownloadStarted"));
        notificationPanel.add(startedDownloadsLB, gbcStartedDownloadsLB);

        downloadProgressBar = new JProgressBar(0, 100);
        downloadProgressBar.setValue(0);
        downloadProgressBar.setStringPainted(true);
        downloadProgressBar.setVisible(true);

        GridBagConstraints gbcDownloadProgressBar = new GridBagConstraints();
        gbcDownloadProgressBar.insets = new Insets(10, 10, 10, 10);
        gbcDownloadProgressBar.fill = GridBagConstraints.HORIZONTAL;
        gbcDownloadProgressBar.gridx = 1;
        gbcDownloadProgressBar.gridy = 1;
        notificationPanel.add(downloadProgressBar, gbcDownloadProgressBar);

        errorDownloadsLB = new JLabel();
        GridBagConstraints gbcErrorDownloadsLB = new GridBagConstraints();
        gbcErrorDownloadsLB.insets = new Insets(10, 10, 10, 10);
        gbcErrorDownloadsLB.fill = GridBagConstraints.EAST;
        gbcErrorDownloadsLB.gridx = 0;
        gbcErrorDownloadsLB.gridy = 2;
        notificationPanel.add(errorDownloadsLB, gbcErrorDownloadsLB);

        downloadErrorAlert = new JLabel();
        GridBagConstraints gbcDownloadErrorAlert = new GridBagConstraints();
        gbcDownloadErrorAlert.insets = new Insets(10, 10, 10, 10);
        gbcDownloadErrorAlert.fill = GridBagConstraints.EAST;
        gbcDownloadErrorAlert.gridx = 1;
        gbcDownloadErrorAlert.gridy = 2;
        gbcDownloadErrorAlert.gridheight = 2;
        notificationPanel.add(downloadErrorAlert, gbcDownloadErrorAlert);

        /**
         * Notification panel: imports
         */
        JLabel notificationCurrentUploads = new JLabel();
        GridBagConstraints gbcNotificationCurrentUploads = new GridBagConstraints();
        gbcNotificationCurrentUploads.insets = new Insets(10, 10, 10, 10);
        gbcNotificationCurrentUploads.fill = GridBagConstraints.WEST;
        gbcNotificationCurrentUploads.gridx = 0;
        gbcNotificationCurrentUploads.gridy = 3;
        notificationCurrentUploads.setText(resourceBundle.getString("shanoir.uploader.currentUploadsSummary"));
        notificationPanel.add(notificationCurrentUploads,
                gbcNotificationCurrentUploads);
        notificationCurrentUploads.setFont(font);

        startedUploadsLB = new JLabel();
        GridBagConstraints gbcStartedUploadsLB = new GridBagConstraints();
        gbcStartedUploadsLB.insets = new Insets(10, 10, 10, 10);
        gbcStartedUploadsLB.fill = GridBagConstraints.EAST;
        gbcStartedUploadsLB.gridx = 0;
        gbcStartedUploadsLB.gridy = 4;
        notificationPanel.add(startedUploadsLB, gbcStartedUploadsLB);

        uploadProgressBar = new JProgressBar(0, 100);
        uploadProgressBar.setValue(0);
        uploadProgressBar.setStringPainted(true);
        uploadProgressBar.setVisible(true);

        GridBagConstraints gbcProgressBar = new GridBagConstraints();
        gbcProgressBar.insets = new Insets(10, 10, 10, 10);
        gbcProgressBar.fill = GridBagConstraints.HORIZONTAL;
        gbcProgressBar.gridx = 1;
        gbcProgressBar.gridy = 4;
        notificationPanel.add(uploadProgressBar, gbcProgressBar);

        finishedUploadsLB = new JLabel();
        GridBagConstraints gbcFinishedUploadsLB = new GridBagConstraints();
        gbcFinishedUploadsLB.insets = new Insets(10, 10, 10, 10);
        gbcFinishedUploadsLB.fill = GridBagConstraints.EAST;
        gbcFinishedUploadsLB.gridx = 0;
        gbcFinishedUploadsLB.gridy = 5;
        notificationPanel.add(finishedUploadsLB, gbcFinishedUploadsLB);

        errorUploadsLB = new JLabel();
        GridBagConstraints gbcErrorUploadsLB = new GridBagConstraints();
        gbcErrorUploadsLB.insets = new Insets(10, 10, 10, 10);
        gbcErrorUploadsLB.fill = GridBagConstraints.EAST;
        gbcErrorUploadsLB.gridx = 0;
        gbcErrorUploadsLB.gridy = 6;
        notificationPanel.add(errorUploadsLB, gbcErrorUploadsLB);

        uploadErrorAlert = new JLabel();
        GridBagConstraints gbcErrorAlert = new GridBagConstraints();
        gbcErrorAlert.insets = new Insets(10, 10, 10, 10);
        gbcErrorAlert.fill = GridBagConstraints.EAST;
        gbcErrorAlert.gridx = 1;
        gbcErrorAlert.gridy = 6;
        gbcErrorAlert.gridheight = 2;
        notificationPanel.add(uploadErrorAlert, gbcErrorAlert);

        menuBar.add(Box.createRigidArea(new Dimension(8, 0)));

        // add main split pane here
        tabbedPane.addTab(resourceBundle.getString("shanoir.uploader.mainWindowTab"), null, mainSplitPane,
                resourceBundle.getString("shanoir.uploader.mainWindowTab.tooltip"));
        JPanel currentUploadsPanel = new JPanel(new BorderLayout());
        // and below the current uploads panel
        tabbedPane.addTab(resourceBundle.getString("shanoir.uploader.currentUploadsTab"), null, currentUploadsPanel,
                resourceBundle.getString("shanoir.uploader.currentUploadsTab.tooltip"));
        scrollPaneUpload = new JScrollPane();
        scrollPaneUpload.setBounds(0, 0, MAXIMIZED_HORIZ, MAXIMIZED_VERT);
        scrollPaneUpload.setPreferredSize(new Dimension(898, 600));
        currentUploadsPanel.add(scrollPaneUpload, BorderLayout.CENTER);

        // Add delete all finished uploads button
        deleteFinishedUploads = new JButton(resourceBundle.getString("shanoir.uploader.currentUploads.Action.deleteAll"));
        deleteFinishedUploads.setFont(new Font("SansSerif", Font.BOLD, 14));
        deleteFinishedUploads.setBackground(new Color(220, 53, 69));
        deleteFinishedUploads.setForeground(Color.WHITE);
        deleteFinishedUploads.setFocusPainted(false);
        deleteFinishedUploads.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        deleteFinishedUploads.setToolTipText(resourceBundle.getString("shanoir.uploader.currentUploads.Action.deleteAll.tooltip"));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(deleteFinishedUploads);

        currentUploadsPanel.add(buttonPanel, BorderLayout.SOUTH);

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
