package org.shanoir.uploader.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentListener;
import javax.xml.datatype.XMLGregorianCalendar;

import org.jdatepicker.JDatePicker;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.shanoir.uploader.action.CancelButtonActionListener;
import org.shanoir.uploader.action.ImportCreateNewExamCBItemListener;
import org.shanoir.uploader.action.ImportSubjectNameDocumentFilter;
import org.shanoir.uploader.gui.customcomponent.JComboBoxMandatory;
import org.shanoir.uploader.gui.customcomponent.JTextFieldMandatory;

/**
 * This is the view class for the Study, StudyCard, Subject and MR Examination
 * selection in order to import the data automatically under Shanoir Webapp.
 * 
 * Part of the MVC Pattern : View Controller is available under package
 * org.shanoir.uploader.action as importDetailsListener.java Model is available
 * under package org.shanoir.uploader.model as ExportData.java
 * 
 * @author mkain
 * @author atouboul
 *
 */
public class ImportDialog extends JDialog {

	public JLabel verticalLabelStudy;
	public JLabel verticalLabelSubject;
	public JLabel verticalLabelMrExamination;

	// Study / StudyCard
	public JLabel studyLabel;
	public JComboBoxMandatory studyCB;
	public JLabel studyCardLabel;
	public JComboBoxMandatory studyCardCB;
	public JLabel studyCardFilterLabel;
	public JTextField studyCardFilterTextField;
	
	// Subject(s)
	public JLabel subjectLabel;
	public JTextFieldMandatory subjectTextField;
	public JLabel existingSubjectsLabel;
	public JComboBox existingSubjectsCB;
	public JLabel subjectImageObjectCategoryLabel;
	public JComboBoxMandatory subjectImageObjectCategoryCB;
	public JLabel subjectStudyIdentifierLabel;
	public JTextField subjectStudyIdentifierTF;
	public JLabel subjectIsPhysicallyInvolvedLabel;
	public JCheckBox subjectIsPhysicallyInvolvedCB;
	public JLabel subjectTypeLabel;
	public JComboBoxMandatory subjectTypeCB;
	public JLabel subjectLanguageHemisphericDominanceLabel;
	public JComboBox subjectLanguageHemisphericDominanceCB;
	public JLabel subjectManualHemisphericDominanceLabel;
	public JComboBox subjectManualHemisphericDominanceCB;
	public JLabel subjectPersonalCommentLabel;
	public JTextArea subjectPersonalCommentTextArea;
	public JTextArea subjectTextArea;

	// MR Examination
	public JLabel mrExaminationExistingExamLabel;
	public JComboBoxMandatory mrExaminationExistingExamCB;
	public JLabel mrExaminationNewExamLabel;
	public JCheckBox mrExaminationNewExamCB;
	public JLabel mrExaminationCenterLabel;
	public JComboBoxMandatory mrExaminationCenterCB;
	public JLabel mrExaminationExamExecutiveLabel;
	public JComboBoxMandatory mrExaminationExamExecutiveCB;
	public JDatePanelImpl datePanel;
	public JLabel mrExaminationDateLabel;
	public JDatePicker mrExaminationDateDP;
	public UtilDateModel mrExaminationNewDateModel;
	public JFormattedTextField mrExaminationDateDPTF;
	public JLabel mrExaminationCommentLabel;
	public JTextFieldMandatory mrExaminationCommentTF;

	public JLabel mriCenterLabel;
	public JTextField mriCenterText;
	public JLabel mriCenterAddressLabel;
	public JTextField mriCenterAddressText;
	public JLabel mriStationNameLabel;
	public JLabel mriStationNameText;
	public JLabel mriManufacturerLabel;
	public JTextField mriManufacturerText;
	public JLabel mriManufacturersModelNameLabel;
	public JTextField mriManufacturersModelNameText;
	public JLabel mriMagneticFieldStrengthLabel;
	public JTextField mriMagneticFieldStrengthText;
	public JLabel mriDeviceSerialNumberLabel;
	public JTextField mriDeviceSerialNumberText;

	public JButton cancelButton;
	public JButton exportButton;
	public JButton preImportExportOnlyButton;
	public JSeparator separatorStudyStudyCard;
	public JLabel separatorSubjectStudyCardLabel;
	public JLabel separatorSubjectLabel;
	public JSeparator separatorSubject;
	public JLabel separatorMrExaminationLabel;
	public JSeparator separatorMrExamination;
	
	public MainWindow mainWindow;
	
	/**
	 * On injecting both listeners the ImportDialog becomes invisible of
	 * the differences between sh-old and sh-ng. ImportDialog is a clean
	 * view component, no dependency to the models.
	 * 
	 * @param mainWindow
	 * @param title
	 * @param trueOrFalse
	 * @param resourceBundle
	 * @param importStudyAndStudyCardCBIL
	 * @param importFinishAL
	 */
	public ImportDialog(MainWindow mainWindow, String title, Boolean trueOrFalse, ResourceBundle resourceBundle,
			ItemListener importStudyAndStudyCardCBIL, ActionListener importFinishAL, DocumentListener studyCardFilterItemListener) {
		super(mainWindow, title, trueOrFalse);
		this.mainWindow = mainWindow;
		this.mainWindow.importDialog = this;

		this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		this.setSize(1020, 900);
		this.setLocationRelativeTo(mainWindow);

		// Main Panel
		JPanel container = new JPanel(new BorderLayout());
		JScrollPane scrPane = new JScrollPane(container);
		add(scrPane);

		// DICOM Infos Panel - information on site who has made the IRM
        JPanel dicomPanel = new JPanel(new GridBagLayout());
        dicomPanel.setBorder(BorderFactory.createTitledBorder("Informations DICOM"));
        GridBagConstraints dicomPanelGBC = new GridBagConstraints();
        dicomPanelGBC.insets = new Insets(5, 5, 5, 5);
		
		int style = Font.ITALIC;
		Font font = new Font("about", style, 12);

		// Left column for Center information
		mriCenterLabel = new JLabel(resourceBundle.getString("shanoir.uploader.import.dicom.center"));
		mriCenterLabel.setFont(font);
		dicomPanelGBC.gridx = 0;
		dicomPanelGBC.gridy = 0;
		dicomPanel.add(mriCenterLabel, dicomPanelGBC);

		mriCenterText = new JTextField(15);
		mriCenterText.setFont(font);
		mriCenterText.setHorizontalAlignment(SwingConstants.LEFT);
		dicomPanelGBC.gridx = 1;
		dicomPanelGBC.gridy = 0;
		dicomPanel.add(mriCenterText, dicomPanelGBC);

		mriCenterAddressLabel = new JLabel(resourceBundle.getString("shanoir.uploader.import.dicom.address"));
		mriCenterAddressLabel.setFont(font);
		dicomPanelGBC.gridx = 0;
		dicomPanelGBC.gridy = 1;
		dicomPanel.add(mriCenterAddressLabel, dicomPanelGBC);

		mriCenterAddressText = new JTextField(15);
		mriCenterAddressText.setFont(font);
		mriCenterAddressText.setHorizontalAlignment(SwingConstants.LEFT);
		dicomPanelGBC.gridx = 1;
		dicomPanelGBC.gridy = 1;
		dicomPanel.add(mriCenterAddressText, dicomPanelGBC);

		mriStationNameLabel = new JLabel(resourceBundle.getString("shanoir.uploader.import.dicom.station.name"));
		mriStationNameLabel.setFont(font);
		dicomPanelGBC.gridx = 0;
		dicomPanelGBC.gridy = 2;
		dicomPanel.add(mriStationNameLabel, dicomPanelGBC);

		mriStationNameText = new JLabel("");
		mriStationNameText.setFont(font);
		mriStationNameText.setHorizontalAlignment(SwingConstants.LEFT);
		dicomPanelGBC.gridx = 1;
		dicomPanelGBC.gridy = 2;
		dicomPanel.add(mriStationNameText, dicomPanelGBC);

		// Right column for Acquisition equipment information
		mriManufacturerLabel = new JLabel(resourceBundle.getString("shanoir.uploader.import.dicom.manufacturer"));
		mriManufacturerLabel.setFont(font);
		dicomPanelGBC.insets = new Insets(5, 50, 5, 5);
		dicomPanelGBC.gridx = 2;
		dicomPanelGBC.gridy = 0;
		dicomPanel.add(mriManufacturerLabel, dicomPanelGBC);

		mriManufacturerText = new JTextField(15);
		mriManufacturerText.setFont(font);
		mriManufacturerText.setHorizontalAlignment(SwingConstants.LEFT);
		dicomPanelGBC.insets = new Insets(5, 5, 5, 5);
		dicomPanelGBC.gridx = 3;
		dicomPanelGBC.gridy = 0;
		dicomPanel.add(mriManufacturerText, dicomPanelGBC);

		mriManufacturersModelNameLabel = new JLabel(resourceBundle.getString("shanoir.uploader.import.dicom.manufacturer.model.name"));
		mriManufacturersModelNameLabel.setFont(font);
		dicomPanelGBC.insets = new Insets(5, 50, 5, 5);
		dicomPanelGBC.gridx = 2;
		dicomPanelGBC.gridy = 1;
		dicomPanel.add(mriManufacturersModelNameLabel, dicomPanelGBC);

		mriManufacturersModelNameText = new JTextField(15);
		mriManufacturersModelNameText.setFont(font);
		mriManufacturersModelNameText.setHorizontalAlignment(SwingConstants.LEFT);
		dicomPanelGBC.insets = new Insets(5, 5, 5, 5);
		dicomPanelGBC.gridx = 3;
		dicomPanelGBC.gridy = 1;
		dicomPanel.add(mriManufacturersModelNameText, dicomPanelGBC);

		mriMagneticFieldStrengthLabel = new JLabel(resourceBundle.getString("shanoir.uploader.import.dicom.magnetic.field.strength"));
		mriMagneticFieldStrengthLabel.setFont(font);
		dicomPanelGBC.insets = new Insets(5, 50, 5, 5);
		dicomPanelGBC.gridx = 2;
		dicomPanelGBC.gridy = 2;
		dicomPanel.add(mriMagneticFieldStrengthLabel, dicomPanelGBC);

		mriMagneticFieldStrengthText = new JTextField(4);
		mriMagneticFieldStrengthText.setFont(font);
		mriMagneticFieldStrengthText.setHorizontalAlignment(SwingConstants.LEFT);
		dicomPanelGBC.insets = new Insets(5, 5, 5, 5);
		dicomPanelGBC.gridx = 3;
		dicomPanelGBC.gridy = 2;
		dicomPanel.add(mriMagneticFieldStrengthText, dicomPanelGBC);

		mriDeviceSerialNumberLabel = new JLabel(resourceBundle.getString("shanoir.uploader.import.dicom.device.serial.number"));
		mriDeviceSerialNumberLabel.setFont(font);
		dicomPanelGBC.insets = new Insets(5, 50, 5, 5);
		dicomPanelGBC.gridx = 2;
		dicomPanelGBC.gridy = 3;
		dicomPanel.add(mriDeviceSerialNumberLabel, dicomPanelGBC);

		mriDeviceSerialNumberText = new JTextField(15);
		mriDeviceSerialNumberText.setFont(font);
		mriDeviceSerialNumberText.setHorizontalAlignment(SwingConstants.LEFT);
		dicomPanelGBC.insets = new Insets(5, 5, 5, 5);
		dicomPanelGBC.gridx = 3;
		dicomPanelGBC.gridy = 3;
		dicomPanel.add(mriDeviceSerialNumberText, dicomPanelGBC);

		container.add(dicomPanel, BorderLayout.NORTH);

		// DICOM Form Panel - informations to be filled or selected by user
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints importDialogGBC = new GridBagConstraints();
        importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;

		/**
		 * Study/StudyCard
		 */
		separatorSubjectStudyCardLabel = new JLabel(
				resourceBundle.getString("shanoir.uploader.studyStudyCardSeparator"));
		separatorSubjectStudyCardLabel.setHorizontalAlignment(SwingConstants.CENTER);
		separatorSubjectStudyCardLabel.setOpaque(true);
		separatorSubjectStudyCardLabel.setFont(new Font("Cracked", Font.BOLD, 14));
		importDialogGBC.weightx = 0.2;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 1;
		importDialogGBC.gridy = 4;
		importDialogGBC.gridwidth = 1;
		formPanel.add(separatorSubjectStudyCardLabel, importDialogGBC);

		separatorStudyStudyCard = new JSeparator();
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 0;
		importDialogGBC.gridy = 4;
		importDialogGBC.weightx = 1;
		importDialogGBC.gridwidth = 3;
		formPanel.add(separatorStudyStudyCard, importDialogGBC);

		studyLabel = new JLabel(resourceBundle.getString("shanoir.uploader.studyLabel") + " *");
		studyLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		importDialogGBC.weightx = 0.2;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 0;
		importDialogGBC.gridy = 5;
		importDialogGBC.gridwidth = 2;
		importDialogGBC.gridheight = 1;
		formPanel.add(studyLabel, importDialogGBC);

		studyCB = new JComboBoxMandatory();
		studyCB.setBackground(Color.WHITE);
		studyCB.setToolTipText(resourceBundle.getString("shanoir.uploader.autoFillTooltip"));
		importDialogGBC.weightx = 0.7;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 2;
		importDialogGBC.gridy = 5;
		importDialogGBC.gridwidth = 1;
		importDialogGBC.gridheight = 1;
		formPanel.add(studyCB, importDialogGBC);
		studyCB.addItemListener(importStudyAndStudyCardCBIL);
		AutoCompleteDecorator.decorate(studyCB);

		studyCardLabel = new JLabel(resourceBundle.getString("shanoir.uploader.studycardLabel") + " *");
		studyCardLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		importDialogGBC.weightx = 0.2;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 0;
		importDialogGBC.gridy = 6;
		importDialogGBC.gridwidth = 2;
		importDialogGBC.gridheight = 1;
		formPanel.add(studyCardLabel, importDialogGBC);

		studyCardCB = new JComboBoxMandatory();
		studyCardCB.setBackground(Color.WHITE);
		importDialogGBC.weightx = 0.7;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 2;
		importDialogGBC.gridy = 6;
		importDialogGBC.gridwidth = 1;
		importDialogGBC.gridheight = 1;
		formPanel.add(studyCardCB, importDialogGBC);
		studyCardCB.addItemListener(importStudyAndStudyCardCBIL);

		studyCardFilterLabel = new JLabel(resourceBundle.getString("shanoir.uploader.studycardFilter"));
 		studyCardFilterLabel.setHorizontalAlignment(SwingConstants.RIGHT);
 		importDialogGBC.weightx = 0.2;
 		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
 		importDialogGBC.insets = new Insets(5, 5, 5, 5);
 		importDialogGBC.gridx = 0;
 		importDialogGBC.gridy = 7;
 		importDialogGBC.gridwidth = 2;
 		importDialogGBC.gridheight = 1;
 		formPanel.add(studyCardFilterLabel, importDialogGBC);

 		studyCardFilterTextField = new JTextField(15);
 		studyCardFilterTextField.setBackground(Color.WHITE);
 		importDialogGBC.weightx = 0.7;
 		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
 		importDialogGBC.insets = new Insets(5, 5, 5, 5);
 		importDialogGBC.gridx = 2;
 		importDialogGBC.gridy = 7;
 		importDialogGBC.gridwidth = 1;
 		importDialogGBC.gridheight = 1;
 		formPanel.add(studyCardFilterTextField, importDialogGBC);
 		studyCardFilterTextField.getDocument().addDocumentListener(studyCardFilterItemListener);

		/**
		 * Subject
		 */
		separatorSubjectLabel = new JLabel(resourceBundle.getString("shanoir.uploader.subjectSeparator"));
		separatorSubjectLabel.setHorizontalAlignment(SwingConstants.CENTER);
		separatorSubjectLabel.setOpaque(true);
		separatorSubjectLabel.setFont(new Font("Cracked", Font.BOLD, 14));
		importDialogGBC.weightx = 0.2;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 1;
		importDialogGBC.gridy = 8;
		importDialogGBC.gridwidth = 1;
		formPanel.add(separatorSubjectLabel, importDialogGBC);

		separatorSubject = new JSeparator();
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 0;
		importDialogGBC.gridy = 8;
		importDialogGBC.weightx = 1;
		importDialogGBC.gridwidth = 3;
		formPanel.add(separatorSubject, importDialogGBC);

		subjectLabel = new JLabel(resourceBundle.getString("shanoir.uploader.subjectLabel") + " *");

		subjectLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		importDialogGBC.weightx = 0.2;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 0;
		importDialogGBC.gridy = 9;
		importDialogGBC.gridwidth = 2;
		importDialogGBC.gridheight = 1;
		formPanel.add(subjectLabel, importDialogGBC);

		subjectTextField = new JTextFieldMandatory();
		subjectTextField.setBackground(Color.LIGHT_GRAY);
		subjectTextField.setEnabled(false);
		subjectTextField.setEditable(false);
		importDialogGBC.weightx = 0.7;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 2;
		importDialogGBC.gridy = 9;
		importDialogGBC.gridwidth = 1;
		formPanel.add(subjectTextField, importDialogGBC);
		ImportSubjectNameDocumentFilter subjectNameFilter = new ImportSubjectNameDocumentFilter(mainWindow);
		subjectTextField.getDocument().addDocumentListener(subjectNameFilter);

		existingSubjectsLabel = new JLabel(resourceBundle.getString("shanoir.uploader.existingSubjectsLabel") + " *");

		existingSubjectsLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		importDialogGBC.weightx = 0.2;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 0;
		importDialogGBC.gridy = 10;
		importDialogGBC.gridwidth = 2;
		importDialogGBC.gridheight = 1;
		formPanel.add(existingSubjectsLabel, importDialogGBC);

		existingSubjectsCB = new JComboBox();
		existingSubjectsCB.setBackground(Color.LIGHT_GRAY);
		existingSubjectsCB.setToolTipText(resourceBundle.getString("shanoir.uploader.autoFillTooltip"));
		importDialogGBC.weightx = 0.7;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 2;
		importDialogGBC.gridy = 10;
		importDialogGBC.gridwidth = 1;
		formPanel.add(existingSubjectsCB, importDialogGBC);
		existingSubjectsCB.addItemListener(importStudyAndStudyCardCBIL);
		AutoCompleteDecorator.decorate(existingSubjectsCB);
		
		subjectImageObjectCategoryLabel = new JLabel(
				resourceBundle.getString("shanoir.uploader.subjectImageObjectCategoryLabel") + " *");
		subjectImageObjectCategoryLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		importDialogGBC.weightx = 0.2;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 0;
		importDialogGBC.gridy = 11;
		importDialogGBC.gridwidth = 2;
		formPanel.add(subjectImageObjectCategoryLabel, importDialogGBC);

		subjectImageObjectCategoryCB = new JComboBoxMandatory();
		subjectImageObjectCategoryCB.setBackground(Color.LIGHT_GRAY);
		importDialogGBC.weightx = 0.7;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 2;
		importDialogGBC.gridy = 11;
		importDialogGBC.gridwidth = 1;
		formPanel.add(subjectImageObjectCategoryCB, importDialogGBC);

		subjectLanguageHemisphericDominanceLabel = new JLabel(
				resourceBundle.getString("shanoir.uploader.subjectLanguageHemisphericDominanceLabel"));
		subjectLanguageHemisphericDominanceLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		importDialogGBC.weightx = 0.2;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 0;
		importDialogGBC.gridy = 12;
		importDialogGBC.gridwidth = 2;
		formPanel.add(subjectLanguageHemisphericDominanceLabel, importDialogGBC);

		subjectLanguageHemisphericDominanceCB = new JComboBox();
		subjectLanguageHemisphericDominanceCB.setBackground(Color.LIGHT_GRAY);
		importDialogGBC.weightx = 0.7;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 2;
		importDialogGBC.gridy = 12;
		importDialogGBC.gridwidth = 1;
		formPanel.add(subjectLanguageHemisphericDominanceCB, importDialogGBC);

		subjectManualHemisphericDominanceLabel = new JLabel(
				resourceBundle.getString("shanoir.uploader.subjectManualHemisphericDominanceLabel"));
		subjectManualHemisphericDominanceLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		importDialogGBC.weightx = 0.2;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 0;
		importDialogGBC.gridy = 13;
		importDialogGBC.gridwidth = 2;
		formPanel.add(subjectManualHemisphericDominanceLabel, importDialogGBC);

		subjectManualHemisphericDominanceCB = new JComboBox();
		subjectManualHemisphericDominanceCB.setBackground(Color.LIGHT_GRAY);
		importDialogGBC.weightx = 0.7;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 2;
		importDialogGBC.gridy = 13;
		importDialogGBC.gridwidth = 1;
		formPanel.add(subjectManualHemisphericDominanceCB, importDialogGBC);

		subjectPersonalCommentLabel = new JLabel(
				resourceBundle.getString("shanoir.uploader.subjectPersonalCommentLabel"));
		subjectPersonalCommentLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		importDialogGBC.weightx = 0.2;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 0;
		importDialogGBC.gridy = 14;
		importDialogGBC.gridwidth = 2;
		formPanel.add(subjectPersonalCommentLabel, importDialogGBC);

		subjectPersonalCommentTextArea = new JTextArea(1, 60);
		subjectPersonalCommentTextArea.setMargin(new Insets(5, 5, 5, 5));
		subjectPersonalCommentTextArea.setBackground(Color.LIGHT_GRAY);
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.weightx = 0.5;
		importDialogGBC.gridx = 2;
		importDialogGBC.gridy = 14;
		importDialogGBC.gridwidth = 2;
		formPanel.add(subjectPersonalCommentTextArea, importDialogGBC);

		subjectStudyIdentifierLabel = new JLabel(
				resourceBundle.getString("shanoir.uploader.subjectStudyIdentifierLabel"));
		subjectStudyIdentifierLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		importDialogGBC.weightx = 0.2;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 0;
		importDialogGBC.gridy = 15;
		importDialogGBC.gridwidth = 2;
		formPanel.add(subjectStudyIdentifierLabel, importDialogGBC);

		subjectStudyIdentifierTF = new JTextField();
		subjectStudyIdentifierTF.setBackground(Color.LIGHT_GRAY);
		subjectStudyIdentifierTF.setEnabled(false);
		subjectStudyIdentifierTF.setEditable(false);
		importDialogGBC.weightx = 0.7;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 2;
		importDialogGBC.gridy = 15;
		importDialogGBC.gridwidth = 1;
		formPanel.add(subjectStudyIdentifierTF, importDialogGBC);

		subjectIsPhysicallyInvolvedLabel = new JLabel(
				resourceBundle.getString("shanoir.uploader.isPhysicallyInvolvedLabel") + " *");
		subjectIsPhysicallyInvolvedLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		importDialogGBC.weightx = 0.2;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 0;
		importDialogGBC.gridy = 16;
		importDialogGBC.gridwidth = 2;
		formPanel.add(subjectIsPhysicallyInvolvedLabel, importDialogGBC);

		subjectIsPhysicallyInvolvedCB = new JCheckBox();
		importDialogGBC.weightx = 0.7;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 2;
		importDialogGBC.gridy = 16;
		importDialogGBC.gridwidth = 1;
		formPanel.add(subjectIsPhysicallyInvolvedCB, importDialogGBC);

		subjectTypeLabel = new JLabel(resourceBundle.getString("shanoir.uploader.subjectTypeLabel") + " *");
		subjectTypeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		importDialogGBC.weightx = 0.2;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 0;
		importDialogGBC.gridy = 17;
		importDialogGBC.gridwidth = 2;
		formPanel.add(subjectTypeLabel, importDialogGBC);

		subjectTypeCB = new JComboBoxMandatory();
		subjectTypeCB.setBackground(Color.LIGHT_GRAY);
		subjectTypeCB.setEditable(false);
		importDialogGBC.weightx = 0.7;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 2;
		importDialogGBC.gridy = 17;
		importDialogGBC.gridwidth = 1;
		formPanel.add(subjectTypeCB, importDialogGBC);

		/**
		 * MrExamination
		 */
		separatorMrExaminationLabel = new JLabel(
				resourceBundle.getString("shanoir.uploader.separatorMrExaminationLabel"));
		separatorMrExaminationLabel.setHorizontalAlignment(SwingConstants.CENTER);
		separatorMrExaminationLabel.setOpaque(true);
		separatorMrExaminationLabel.setFont(new Font("Cracked", Font.BOLD, 14));
		importDialogGBC.weightx = 0.2;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 1;
		importDialogGBC.gridy = 18;
		importDialogGBC.gridwidth = 1;
		formPanel.add(separatorMrExaminationLabel, importDialogGBC);

		separatorMrExamination = new JSeparator();
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 0;
		importDialogGBC.gridy = 18;
		importDialogGBC.weightx = 1;
		importDialogGBC.gridwidth = 3;
		formPanel.add(separatorMrExamination, importDialogGBC);

		mrExaminationExistingExamLabel = new JLabel(
				resourceBundle.getString("shanoir.uploader.mrExaminationExitingExamLabel"));
		mrExaminationExistingExamLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		importDialogGBC.weightx = 0.2;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 0;
		importDialogGBC.gridy = 19;
		importDialogGBC.gridwidth = 2;
		formPanel.add(mrExaminationExistingExamLabel, importDialogGBC);

		mrExaminationExistingExamCB = new JComboBoxMandatory();
		mrExaminationExistingExamCB.setBackground(Color.WHITE);
		importDialogGBC.weightx = 0.7;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 2;
		importDialogGBC.gridy = 19;
		importDialogGBC.gridwidth = 1;
		formPanel.add(mrExaminationExistingExamCB, importDialogGBC);

		mrExaminationNewExamLabel = new JLabel(resourceBundle.getString("shanoir.uploader.mrExaminationNewExamLabel"));
		mrExaminationNewExamLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		importDialogGBC.weightx = 0.2;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 0;
		importDialogGBC.gridy = 20;
		importDialogGBC.gridwidth = 2;
		formPanel.add(mrExaminationNewExamLabel, importDialogGBC);

		mrExaminationNewExamCB = new JCheckBox();
		importDialogGBC.weightx = 0.7;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 2;
		importDialogGBC.gridy = 20;
		importDialogGBC.gridwidth = 1;
		formPanel.add(mrExaminationNewExamCB, importDialogGBC);

		ImportCreateNewExamCBItemListener createNewExamCBItemListener = new ImportCreateNewExamCBItemListener(this);
		mrExaminationNewExamCB.addItemListener(createNewExamCBItemListener);

		mrExaminationCenterLabel = new JLabel(
				resourceBundle.getString("shanoir.uploader.mrExaminationCenterLabel") + " *");
		mrExaminationCenterLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		importDialogGBC.weightx = 0.2;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 0;
		importDialogGBC.gridy = 21;
		importDialogGBC.gridwidth = 2;
		formPanel.add(mrExaminationCenterLabel, importDialogGBC);

		mrExaminationCenterCB = new JComboBoxMandatory();
		mrExaminationCenterCB.setBackground(Color.LIGHT_GRAY);
		importDialogGBC.weightx = 0.7;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 2;
		importDialogGBC.gridy = 21;
		importDialogGBC.gridwidth = 1;
		formPanel.add(mrExaminationCenterCB, importDialogGBC);

		mrExaminationExamExecutiveLabel = new JLabel(
				resourceBundle.getString("shanoir.uploader.mrExaminationExamExecutiveLabel") + " *");
		mrExaminationExamExecutiveLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		importDialogGBC.weightx = 0.2;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 0;
		importDialogGBC.gridy = 22;
		importDialogGBC.gridwidth = 2;
		formPanel.add(mrExaminationExamExecutiveLabel, importDialogGBC);

		mrExaminationExamExecutiveCB = new JComboBoxMandatory();
		mrExaminationExamExecutiveCB.setBackground(Color.LIGHT_GRAY);
		importDialogGBC.weightx = 0.7;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 2;
		importDialogGBC.gridy = 22;
		importDialogGBC.gridwidth = 1;
		formPanel.add(mrExaminationExamExecutiveCB, importDialogGBC);

		mrExaminationDateLabel = new JLabel(resourceBundle.getString("shanoir.uploader.mrExaminationDateLabel") + " *");
		mrExaminationDateLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		importDialogGBC.weightx = 0.2;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 0;
		importDialogGBC.gridy = 23;
		importDialogGBC.gridwidth = 2;
		formPanel.add(mrExaminationDateLabel, importDialogGBC);

		Properties p = new Properties();
		p.put("text.today", "Today");
		p.put("text.month", "Month");
		p.put("text.year", "Year");
		mrExaminationNewDateModel = new UtilDateModel();
		datePanel = new JDatePanelImpl(mrExaminationNewDateModel, p);
		datePanel.setBackground(Color.BLUE);

		mrExaminationDateDP = new JDatePickerImpl(datePanel, new DateLabelFormatter());
		mrExaminationDateDPTF = ((JDatePickerImpl) mrExaminationDateDP)
				.getJFormattedTextField();
		importDialogGBC.weightx = 0.7;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 2;
		importDialogGBC.gridy = 23;
		importDialogGBC.gridwidth = 1;
		formPanel.add((Component) mrExaminationDateDP, importDialogGBC);

		mrExaminationCommentLabel = new JLabel(
				resourceBundle.getString("shanoir.uploader.mrExaminationCommentLabel") + " *");
		mrExaminationCommentLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		importDialogGBC.weightx = 0.2;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 0;
		importDialogGBC.gridy = 24;
		importDialogGBC.gridwidth = 2;
		importDialogGBC.gridheight = 1;
		formPanel.add(mrExaminationCommentLabel, importDialogGBC);

		mrExaminationCommentTF = new JTextFieldMandatory();
		importDialogGBC.weightx = 0.7;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 2;
		importDialogGBC.gridy = 24;
		importDialogGBC.gridwidth = 1;
		formPanel.add(mrExaminationCommentTF, importDialogGBC);

		cancelButton = new JButton(resourceBundle.getString("shanoir.uploader.cancel"));
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.weightx = 0;
		importDialogGBC.gridx = 1;
		importDialogGBC.gridy = 25;
		importDialogGBC.gridwidth = 1;
		formPanel.add(cancelButton, importDialogGBC);

		CancelButtonActionListener cBAL = new CancelButtonActionListener(this);
		cancelButton.addActionListener(cBAL);
		
		exportButton = new JButton(resourceBundle.getString("shanoir.uploader.exportimport"));
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.weightx = 0;
		importDialogGBC.gridx = 2;
		importDialogGBC.gridy = 25;
		importDialogGBC.gridwidth = 2;
		formPanel.add(exportButton, importDialogGBC);
		exportButton.addActionListener(importFinishAL);

		container.add(formPanel, BorderLayout.CENTER);
	}

	public boolean isExaminationFilledCorrectly(XMLGregorianCalendar dateMrExam, boolean skip) {
		if (!skip) {
			if (!mrExaminationNewExamCB.isSelected()) {
				if (mrExaminationExistingExamCB.isValueSet()) {
					return true;
				} else {
					return false;
				}
			} else {
				if (mrExaminationCenterCB.isValueSet() && mrExaminationCommentTF.isValueSet() && !dateMrExam.equals("")
						&& dateMrExam != null && mrExaminationExamExecutiveCB.isValueSet()) {
					return true;
				} else {
					return false;
				}
			}
		} else {
			return false;
		}
	}

}