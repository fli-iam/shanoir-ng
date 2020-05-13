package org.shanoir.uploader.gui;

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
import javax.xml.datatype.XMLGregorianCalendar;

import org.jdatepicker.JDatePicker;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;
import org.shanoir.uploader.action.CancelButtonActionListener;
import org.shanoir.uploader.action.ImportCreateNewExamCBItemListener;
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

	// Subject
	public JLabel subjectLabel;
	public JTextFieldMandatory subjectTextField;
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
	public JLabel mriCenterText;
	public JLabel mriCenterAddressLabel;
	public JLabel mriCenterAddressText;
	public JLabel mriStationNameLabel;
	public JLabel mriStationNameText;
	public JLabel mriManufacturerLabel;
	public JLabel mriManufacturerText;
	public JLabel mriManufacturersModelNameLabel;
	public JLabel mriManufacturersModelNameText;
	public JLabel mriDeviceSerialNumberLabel;
	public JLabel mriDeviceSerialNumberText;

	public JButton cancelButton;
	public JButton exportButton;
	public JButton preImportExportOnlyButton;
	public JSeparator separatorStudyStudyCard;
	public JLabel separatorSubjectStudyCardLabel;
	public JLabel separatorSubjectLabel;
	public JSeparator separatorSubject;
	public JLabel separatorSubjectStudyRelLabel;
	public JSeparator separatorSubjectStudyRel;
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
			ItemListener importStudyAndStudyCardCBIL, ActionListener importFinishAL) {
		super(mainWindow, title, trueOrFalse);
		this.mainWindow = mainWindow;
		this.mainWindow.importDialog = this;

		this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		this.setSize(700, 900);
		this.setLocationRelativeTo(mainWindow);

		JPanel container = new JPanel();
		JScrollPane scrPane = new JScrollPane(container);
		add(scrPane);

		container.setLayout(new GridBagLayout());
		GridBagConstraints importDialogGBC = new GridBagConstraints();
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;

		/**
		 * information on site who has made the IRM
		 */
		int style = Font.ITALIC;
		Font font = new Font("about", style, 12);

		mriCenterLabel = new JLabel(resourceBundle.getString("shanoir.uploader.import.dicom.center"));
		mriCenterLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		mriCenterLabel.setFont(font);
		importDialogGBC.weightx = 0.2;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 0;
		importDialogGBC.gridy = 0;
		importDialogGBC.gridwidth = 2;
		container.add(mriCenterLabel, importDialogGBC);

		mriCenterText = new JLabel("");
		mriCenterText.setFont(font);
		importDialogGBC.weightx = 0.2;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 2;
		importDialogGBC.gridy = 0;
		importDialogGBC.gridwidth = 1;
		container.add(mriCenterText, importDialogGBC);

		mriCenterAddressLabel = new JLabel(resourceBundle.getString("shanoir.uploader.import.dicom.address"));
		mriCenterAddressLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		mriCenterAddressLabel.setFont(font);
		importDialogGBC.weightx = 0.2;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 0;
		importDialogGBC.gridy = 1;
		importDialogGBC.gridwidth = 2;
		container.add(mriCenterAddressLabel, importDialogGBC);

		mriCenterAddressText = new JLabel("");
		mriCenterAddressText.setFont(font);
		importDialogGBC.weightx = 0.2;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 2;
		importDialogGBC.gridy = 1;
		importDialogGBC.gridwidth = 1;
		container.add(mriCenterAddressText, importDialogGBC);
		
		mriStationNameLabel = new JLabel(resourceBundle.getString("shanoir.uploader.import.dicom.station.name"));
		mriStationNameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		mriStationNameLabel.setFont(font);
		importDialogGBC.weightx = 0.2;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 0;
		importDialogGBC.gridy = 2;
		importDialogGBC.gridwidth = 2;
		container.add(mriStationNameLabel, importDialogGBC);

		mriStationNameText = new JLabel("");
		mriStationNameText.setFont(font);
		importDialogGBC.weightx = 0.2;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 2;
		importDialogGBC.gridy = 2;
		importDialogGBC.gridwidth = 1;
		container.add(mriStationNameText, importDialogGBC);
		
		mriManufacturerLabel = new JLabel(resourceBundle.getString("shanoir.uploader.import.dicom.manufacturer"));
		mriManufacturerLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		mriManufacturerLabel.setFont(font);
		importDialogGBC.weightx = 0.2;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 0;
		importDialogGBC.gridy = 3;
		importDialogGBC.gridwidth = 2;
		container.add(mriManufacturerLabel, importDialogGBC);

		mriManufacturerText = new JLabel("");
		mriManufacturerText.setFont(font);
		importDialogGBC.weightx = 0.2;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 2;
		importDialogGBC.gridy = 3;
		importDialogGBC.gridwidth = 1;
		container.add(mriManufacturerText, importDialogGBC);
		
		mriManufacturersModelNameLabel = new JLabel(resourceBundle.getString("shanoir.uploader.import.dicom.manufacturer.model.name"));
		mriManufacturersModelNameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		mriManufacturersModelNameLabel.setFont(font);
		importDialogGBC.weightx = 0.2;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 0;
		importDialogGBC.gridy = 4;
		importDialogGBC.gridwidth = 2;
		container.add(mriManufacturersModelNameLabel, importDialogGBC);

		mriManufacturersModelNameText = new JLabel("");
		mriManufacturersModelNameText.setFont(font);
		importDialogGBC.weightx = 0.2;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 2;
		importDialogGBC.gridy = 4;
		importDialogGBC.gridwidth = 1;
		container.add(mriManufacturersModelNameText, importDialogGBC);
		
		mriDeviceSerialNumberLabel = new JLabel(resourceBundle.getString("shanoir.uploader.import.dicom.device.serial.number"));
		mriDeviceSerialNumberLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		mriDeviceSerialNumberLabel.setFont(font);
		importDialogGBC.weightx = 0.2;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 0;
		importDialogGBC.gridy = 5;
		importDialogGBC.gridwidth = 2;
		container.add(mriDeviceSerialNumberLabel, importDialogGBC);

		mriDeviceSerialNumberText = new JLabel("");
		mriDeviceSerialNumberText.setFont(font);
		importDialogGBC.weightx = 0.2;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 2;
		importDialogGBC.gridy = 5;
		importDialogGBC.gridwidth = 1;
		container.add(mriDeviceSerialNumberText, importDialogGBC);

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
		importDialogGBC.gridy = 6;
		importDialogGBC.gridwidth = 1;
		container.add(separatorSubjectStudyCardLabel, importDialogGBC);

		separatorStudyStudyCard = new JSeparator();
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 0;
		importDialogGBC.gridy = 6;
		importDialogGBC.weightx = 1;
		importDialogGBC.gridwidth = 3;
		container.add(separatorStudyStudyCard, importDialogGBC);

		studyLabel = new JLabel(resourceBundle.getString("shanoir.uploader.studyLabel") + " *");
		studyLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		importDialogGBC.weightx = 0.2;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 0;
		importDialogGBC.gridy = 7;
		importDialogGBC.gridwidth = 2;
		importDialogGBC.gridheight = 1;
		container.add(studyLabel, importDialogGBC);

		studyCB = new JComboBoxMandatory();
		studyCB.setBackground(Color.WHITE);
		importDialogGBC.weightx = 0.7;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 2;
		importDialogGBC.gridy = 7;
		importDialogGBC.gridwidth = 1;
		importDialogGBC.gridheight = 1;
		container.add(studyCB, importDialogGBC);
		studyCB.addItemListener(importStudyAndStudyCardCBIL);

		studyCardLabel = new JLabel(resourceBundle.getString("shanoir.uploader.studycardLabel") + " *");
		studyCardLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		importDialogGBC.weightx = 0.2;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 0;
		importDialogGBC.gridy = 8;
		importDialogGBC.gridwidth = 2;
		importDialogGBC.gridheight = 1;
		container.add(studyCardLabel, importDialogGBC);

		studyCardCB = new JComboBoxMandatory();
		studyCardCB.setBackground(Color.WHITE);
		importDialogGBC.weightx = 0.7;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 2;
		importDialogGBC.gridy = 8;
		importDialogGBC.gridwidth = 1;
		importDialogGBC.gridheight = 1;
		container.add(studyCardCB, importDialogGBC);

		studyCardCB.addItemListener(importStudyAndStudyCardCBIL);

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
		importDialogGBC.gridy = 9;
		importDialogGBC.gridwidth = 1;
		container.add(separatorSubjectLabel, importDialogGBC);

		separatorSubject = new JSeparator();
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 0;
		importDialogGBC.gridy = 9;
		importDialogGBC.weightx = 1;
		importDialogGBC.gridwidth = 3;
		container.add(separatorSubject, importDialogGBC);

		subjectLabel = new JLabel(resourceBundle.getString("shanoir.uploader.subjectLabel") + " *");

		subjectLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		importDialogGBC.weightx = 0.2;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 0;
		importDialogGBC.gridy = 10;
		importDialogGBC.gridwidth = 2;
		importDialogGBC.gridheight = 1;
		container.add(subjectLabel, importDialogGBC);

		subjectTextField = new JTextFieldMandatory();
		subjectTextField.setBackground(Color.LIGHT_GRAY);
		subjectTextField.setEnabled(false);
		subjectTextField.setEditable(false);
		importDialogGBC.weightx = 0.7;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 2;
		importDialogGBC.gridy = 10;
		importDialogGBC.gridwidth = 1;
		container.add(subjectTextField, importDialogGBC);

		subjectImageObjectCategoryLabel = new JLabel(
				resourceBundle.getString("shanoir.uploader.subjectImageObjectCategoryLabel") + " *");
		subjectImageObjectCategoryLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		importDialogGBC.weightx = 0.2;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 0;
		importDialogGBC.gridy = 11;
		importDialogGBC.gridwidth = 2;
		container.add(subjectImageObjectCategoryLabel, importDialogGBC);

		subjectImageObjectCategoryCB = new JComboBoxMandatory();
		subjectImageObjectCategoryCB.setBackground(Color.LIGHT_GRAY);
		importDialogGBC.weightx = 0.7;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 2;
		importDialogGBC.gridy = 11;
		importDialogGBC.gridwidth = 1;
		container.add(subjectImageObjectCategoryCB, importDialogGBC);

		subjectLanguageHemisphericDominanceLabel = new JLabel(
				resourceBundle.getString("shanoir.uploader.subjectLanguageHemisphericDominanceLabel"));
		subjectLanguageHemisphericDominanceLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		importDialogGBC.weightx = 0.2;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 0;
		importDialogGBC.gridy = 12;
		importDialogGBC.gridwidth = 2;
		container.add(subjectLanguageHemisphericDominanceLabel, importDialogGBC);

		subjectLanguageHemisphericDominanceCB = new JComboBox();
		subjectLanguageHemisphericDominanceCB.setBackground(Color.LIGHT_GRAY);
		importDialogGBC.weightx = 0.7;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 2;
		importDialogGBC.gridy = 12;
		importDialogGBC.gridwidth = 1;
		container.add(subjectLanguageHemisphericDominanceCB, importDialogGBC);

		subjectManualHemisphericDominanceLabel = new JLabel(
				resourceBundle.getString("shanoir.uploader.subjectManualHemisphericDominanceLabel"));
		subjectManualHemisphericDominanceLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		importDialogGBC.weightx = 0.2;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 0;
		importDialogGBC.gridy = 13;
		importDialogGBC.gridwidth = 2;
		container.add(subjectManualHemisphericDominanceLabel, importDialogGBC);

		subjectManualHemisphericDominanceCB = new JComboBox();
		subjectManualHemisphericDominanceCB.setBackground(Color.LIGHT_GRAY);
		importDialogGBC.weightx = 0.7;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 2;
		importDialogGBC.gridy = 13;
		importDialogGBC.gridwidth = 1;
		container.add(subjectManualHemisphericDominanceCB, importDialogGBC);

		subjectPersonalCommentLabel = new JLabel(
				resourceBundle.getString("shanoir.uploader.subjectPersonalCommentLabel"));
		subjectPersonalCommentLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		importDialogGBC.weightx = 0.2;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 0;
		importDialogGBC.gridy = 14;
		importDialogGBC.gridwidth = 2;
		container.add(subjectPersonalCommentLabel, importDialogGBC);

		subjectPersonalCommentTextArea = new JTextArea(5, 60);
		subjectPersonalCommentTextArea.setMargin(new Insets(5, 5, 5, 5));
		subjectPersonalCommentTextArea.setBackground(Color.LIGHT_GRAY);
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.weightx = 0.5;
		importDialogGBC.gridx = 2;
		importDialogGBC.gridy = 14;
		importDialogGBC.gridwidth = 2;
		container.add(subjectPersonalCommentTextArea, importDialogGBC);

		/**
		 * Subject / Study
		 */
		separatorSubjectStudyRelLabel = new JLabel(resourceBundle.getString("shanoir.uploader.subjectStudySeparator"));
		separatorSubjectStudyRelLabel.setHorizontalAlignment(SwingConstants.CENTER);
		separatorSubjectStudyRelLabel.setOpaque(true);
		separatorSubjectStudyRelLabel.setFont(new Font("Cracked", Font.BOLD, 14));
		importDialogGBC.weightx = 0.2;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 1;
		importDialogGBC.gridy = 15;
		importDialogGBC.gridwidth = 1;
		container.add(separatorSubjectStudyRelLabel, importDialogGBC);

		separatorSubjectStudyRel = new JSeparator();
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 0;
		importDialogGBC.gridy = 15;
		importDialogGBC.weightx = 1;
		importDialogGBC.gridwidth = 3;
		container.add(separatorSubjectStudyRel, importDialogGBC);
		
		subjectStudyIdentifierLabel = new JLabel(
				resourceBundle.getString("shanoir.uploader.subjectStudyIdentifierLabel"));
		subjectStudyIdentifierLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		importDialogGBC.weightx = 0.2;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 0;
		importDialogGBC.gridy = 16;
		importDialogGBC.gridwidth = 2;
		container.add(subjectStudyIdentifierLabel, importDialogGBC);

		subjectStudyIdentifierTF = new JTextField();
		subjectStudyIdentifierTF.setBackground(Color.LIGHT_GRAY);
		subjectStudyIdentifierTF.setEnabled(false);
		subjectStudyIdentifierTF.setEditable(false);
		importDialogGBC.weightx = 0.7;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 2;
		importDialogGBC.gridy = 16;
		importDialogGBC.gridwidth = 1;
		container.add(subjectStudyIdentifierTF, importDialogGBC);

		subjectIsPhysicallyInvolvedLabel = new JLabel(
				resourceBundle.getString("shanoir.uploader.isPhysicallyInvolvedLabel") + " *");
		subjectIsPhysicallyInvolvedLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		importDialogGBC.weightx = 0.2;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 0;
		importDialogGBC.gridy = 17;
		importDialogGBC.gridwidth = 2;
		container.add(subjectIsPhysicallyInvolvedLabel, importDialogGBC);

		subjectIsPhysicallyInvolvedCB = new JCheckBox();
		importDialogGBC.weightx = 0.7;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 2;
		importDialogGBC.gridy = 17;
		importDialogGBC.gridwidth = 1;
		container.add(subjectIsPhysicallyInvolvedCB, importDialogGBC);

		subjectTypeLabel = new JLabel(resourceBundle.getString("shanoir.uploader.subjectTypeLabel") + " *");
		subjectTypeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		importDialogGBC.weightx = 0.2;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 0;
		importDialogGBC.gridy = 18;
		importDialogGBC.gridwidth = 2;
		container.add(subjectTypeLabel, importDialogGBC);

		subjectTypeCB = new JComboBoxMandatory();
		subjectTypeCB.setBackground(Color.LIGHT_GRAY);
		subjectTypeCB.setEditable(false);
		importDialogGBC.weightx = 0.7;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 2;
		importDialogGBC.gridy = 18;
		importDialogGBC.gridwidth = 1;
		container.add(subjectTypeCB, importDialogGBC);

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
		importDialogGBC.gridy = 19;
		importDialogGBC.gridwidth = 1;
		container.add(separatorMrExaminationLabel, importDialogGBC);

		separatorMrExamination = new JSeparator();
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 0;
		importDialogGBC.gridy = 19;
		importDialogGBC.weightx = 1;
		importDialogGBC.gridwidth = 3;
		container.add(separatorMrExamination, importDialogGBC);

		mrExaminationExistingExamLabel = new JLabel(
				resourceBundle.getString("shanoir.uploader.mrExaminationExitingExamLabel"));
		mrExaminationExistingExamLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		importDialogGBC.weightx = 0.2;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 0;
		importDialogGBC.gridy = 20;
		importDialogGBC.gridwidth = 2;
		container.add(mrExaminationExistingExamLabel, importDialogGBC);

		mrExaminationExistingExamCB = new JComboBoxMandatory();
		mrExaminationExistingExamCB.setBackground(Color.WHITE);
		importDialogGBC.weightx = 0.7;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 2;
		importDialogGBC.gridy = 20;
		importDialogGBC.gridwidth = 1;
		container.add(mrExaminationExistingExamCB, importDialogGBC);

		mrExaminationNewExamLabel = new JLabel(resourceBundle.getString("shanoir.uploader.mrExaminationNewExamLabel"));
		mrExaminationNewExamLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		importDialogGBC.weightx = 0.2;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 0;
		importDialogGBC.gridy = 21;
		importDialogGBC.gridwidth = 2;
		container.add(mrExaminationNewExamLabel, importDialogGBC);

		mrExaminationNewExamCB = new JCheckBox();
		importDialogGBC.weightx = 0.7;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 2;
		importDialogGBC.gridy = 21;
		importDialogGBC.gridwidth = 1;
		container.add(mrExaminationNewExamCB, importDialogGBC);

		ImportCreateNewExamCBItemListener createNewExamCBItemListener = new ImportCreateNewExamCBItemListener(this);
		mrExaminationNewExamCB.addItemListener(createNewExamCBItemListener);

		mrExaminationCenterLabel = new JLabel(
				resourceBundle.getString("shanoir.uploader.mrExaminationCenterLabel") + " *");
		mrExaminationCenterLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		importDialogGBC.weightx = 0.2;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 0;
		importDialogGBC.gridy = 22;
		importDialogGBC.gridwidth = 2;
		container.add(mrExaminationCenterLabel, importDialogGBC);

		mrExaminationCenterCB = new JComboBoxMandatory();
		mrExaminationCenterCB.setBackground(Color.LIGHT_GRAY);
		importDialogGBC.weightx = 0.7;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 2;
		importDialogGBC.gridy = 22;
		importDialogGBC.gridwidth = 1;
		container.add(mrExaminationCenterCB, importDialogGBC);

		mrExaminationExamExecutiveLabel = new JLabel(
				resourceBundle.getString("shanoir.uploader.mrExaminationExamExecutiveLabel") + " *");
		mrExaminationExamExecutiveLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		importDialogGBC.weightx = 0.2;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 0;
		importDialogGBC.gridy = 23;
		importDialogGBC.gridwidth = 2;
		container.add(mrExaminationExamExecutiveLabel, importDialogGBC);

		mrExaminationExamExecutiveCB = new JComboBoxMandatory();
		mrExaminationExamExecutiveCB.setBackground(Color.LIGHT_GRAY);
		importDialogGBC.weightx = 0.7;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 2;
		importDialogGBC.gridy = 23;
		importDialogGBC.gridwidth = 1;
		container.add(mrExaminationExamExecutiveCB, importDialogGBC);

		mrExaminationDateLabel = new JLabel(resourceBundle.getString("shanoir.uploader.mrExaminationDateLabel") + " *");
		mrExaminationDateLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		importDialogGBC.weightx = 0.2;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 0;
		importDialogGBC.gridy = 24;
		importDialogGBC.gridwidth = 2;
		container.add(mrExaminationDateLabel, importDialogGBC);

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
		importDialogGBC.gridy = 24;
		importDialogGBC.gridwidth = 1;
		container.add((Component) mrExaminationDateDP, importDialogGBC);

		mrExaminationCommentLabel = new JLabel(
				resourceBundle.getString("shanoir.uploader.mrExaminationCommentLabel") + " *");
		mrExaminationCommentLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		importDialogGBC.weightx = 0.2;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 0;
		importDialogGBC.gridy = 25;
		importDialogGBC.gridwidth = 2;
		importDialogGBC.gridheight = 1;
		container.add(mrExaminationCommentLabel, importDialogGBC);

		mrExaminationCommentTF = new JTextFieldMandatory();
		importDialogGBC.weightx = 0.7;
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.insets = new Insets(5, 5, 5, 5);
		importDialogGBC.gridx = 2;
		importDialogGBC.gridy = 25;
		importDialogGBC.gridwidth = 1;
		container.add(mrExaminationCommentTF, importDialogGBC);

		cancelButton = new JButton(resourceBundle.getString("shanoir.uploader.cancel"));
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.weightx = 0;
		importDialogGBC.gridx = 1;
		importDialogGBC.gridy = 26;
		importDialogGBC.gridwidth = 1;
		container.add(cancelButton, importDialogGBC);

		CancelButtonActionListener cBAL = new CancelButtonActionListener(this);
		cancelButton.addActionListener(cBAL);
		
		exportButton = new JButton(resourceBundle.getString("shanoir.uploader.exportimport"));
		importDialogGBC.fill = GridBagConstraints.HORIZONTAL;
		importDialogGBC.weightx = 0;
		importDialogGBC.gridx = 2;
		importDialogGBC.gridy = 26;
		importDialogGBC.gridwidth = 2;
		container.add(exportButton, importDialogGBC);
		exportButton.addActionListener(importFinishAL);
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