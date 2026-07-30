package org.shanoir.uploader.action;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.shanoir.ng.importer.model.ImportJobBase;
import org.shanoir.ng.shared.dicom.EquipmentDicom;
import org.shanoir.ng.shared.dicom.InstitutionDicom;
import org.shanoir.ng.studycard.dto.QualityCardResult;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.gui.MainWindow;
import org.shanoir.uploader.model.rest.AcquisitionEquipment;
import org.shanoir.uploader.model.rest.Center;
import org.shanoir.uploader.model.rest.Examination;
import org.shanoir.uploader.model.rest.ImagedObjectCategory;
import org.shanoir.uploader.model.rest.Study;
import org.shanoir.uploader.model.rest.StudyCard;
import org.shanoir.uploader.model.rest.Subject;
import org.shanoir.uploader.model.rest.SubjectType;
import org.shanoir.uploader.upload.UploadState;
import org.shanoir.uploader.utils.ImportUtils;
import org.shanoir.uploader.utils.QualityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportFinishActionListener implements ActionListener {

    private static final Logger logger = LoggerFactory.getLogger(ImportFinishActionListener.class);

    private static final ExecutorService IMPORT_FINISH_EXECUTOR =
            Executors.newFixedThreadPool(Math.max(2, Runtime.getRuntime().availableProcessors() / 2));

    /**
     * Guards against the same uploadFolder being processed twice
     * concurrently (e.g. two queued ActionEvents from a fast double-click
     * both getting dispatched before the button is disabled/import dialog
     * is hidden).
     */
    private static final Set<String> FOLDERS_IN_PROGRESS = ConcurrentHashMap.newKeySet();

    private MainWindow mainWindow;

    private File uploadFolder;
    
    private Subject subjectREST;
    
    public ImportFinishActionListener(final MainWindow mainWindow, File uploadFolder, Subject subjectREST) {
        this.mainWindow = mainWindow;
        this.uploadFolder = uploadFolder;
        this.subjectREST = subjectREST;
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
        final JButton startButton = (JButton) event.getSource();
        if (!startButton.isEnabled()) {
            return;
        }
        startButton.setEnabled(false);

        final String folderKey = uploadFolder.getAbsolutePath();
        if (!FOLDERS_IN_PROGRESS.add(folderKey)) {
            logger.warn("Import for folder {} already in progress, ignoring duplicate trigger.", folderKey);
            startButton.setEnabled(true);
            return;
        }

        final Study study = (Study) mainWindow.importDialog.studyCB.getSelectedItem();
        if (study == null || study.getId() == null) {
            showErrorAndReset(startButton, folderKey,
                    "shanoir.uploader.systemErrorDialog.error.import.study");
            return;
        }

        ImportJobBase importJob = null;
        try {
            importJob = ImportUtils.readImportJob(uploadFolder);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            showErrorAndReset(startButton, folderKey,
                    "shanoir.uploader.systemErrorDialog.error.import.study");
            return;
        }

        Long centerId = null;
        AcquisitionEquipment equipment = null;
        if (study.isWithStudyCards()) {
            final StudyCard studyCard = (StudyCard) mainWindow.importDialog.studyCardCB.getSelectedItem();
            if (studyCard == null || studyCard.getName() == null) {
                showErrorAndReset(startButton, folderKey,
                        "shanoir.uploader.systemErrorDialog.error.import.study");
                return;
            }
            equipment = studyCard.getAcquisitionEquipment();
            centerId = studyCard.getAcquisitionEquipment().getCenter().getId();
        } else {
            if (mainWindow.importDialog.mriCenterText.getText().isBlank()
                    || mainWindow.importDialog.mriCenterAddressText.getText().isBlank()) {
                showErrorAndReset(startButton, folderKey,
                        "shanoir.uploader.systemErrorDialog.error.import.institution");
                return;
            }
            if (mainWindow.importDialog.mriManufacturerText.getText().isBlank()
                    || mainWindow.importDialog.mriManufacturersModelNameText.getText().isBlank()
                    || mainWindow.importDialog.mriMagneticFieldStrengthText.getText().isBlank()
                    || mainWindow.importDialog.mriDeviceSerialNumberText.getText().isBlank()) {
                showErrorAndReset(startButton, folderKey,
                        "shanoir.uploader.systemErrorDialog.error.import.equipment");
                return;
            }
            String magneticFieldStrength = mainWindow.importDialog.mriMagneticFieldStrengthText.getText();
            // Check that magnetic field strength is a number value if modality is not CT or XA
            if (importJob.getFirstSerie().getModality() != null
                    && !importJob.getFirstSerie().getModality().equals("CT")
                    && !importJob.getFirstSerie().getModality().equals("XA")) {
                String regex = "\\d+(\\.\\d+)?";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(magneticFieldStrength);
                if (!matcher.find()) {
                    showErrorAndReset(startButton, folderKey,
                            "shanoir.uploader.systemErrorDialog.error.import.equipment.magnetic.field");
                    return;
                }
            }
            InstitutionDicom institutionDicom = new InstitutionDicom();
            institutionDicom.setInstitutionName(mainWindow.importDialog.mriCenterText.getText());
            institutionDicom.setInstitutionAddress(mainWindow.importDialog.mriCenterAddressText.getText());
            Center center = ImportUtils.findOrCreateCenterWithInstitutionDicom(institutionDicom, study.getId());
            if (center != null) {
                centerId = center.getId();
                EquipmentDicom equipmentDicom = importJob.getFirstSerieWithInstitutionAndEquipment().getEquipment();
                equipmentDicom.setManufacturer(mainWindow.importDialog.mriManufacturerText.getText());
                equipmentDicom.setManufacturerModelName(mainWindow.importDialog.mriManufacturersModelNameText.getText());
                equipmentDicom.setMagneticFieldStrength(mainWindow.importDialog.mriMagneticFieldStrengthText.getText());
                equipmentDicom.setDeviceSerialNumber(mainWindow.importDialog.mriDeviceSerialNumberText.getText());
                equipment = ImportUtils.findOrCreateEquipmentWithEquipmentDicom(equipmentDicom, centerId);
                if (equipment == null) {
                    logger.error("No study card: equipment not found or created.");
                    showErrorAndReset(startButton, folderKey, null, "Equipment not found or created.");
                    return;
                }
            } else {
                logger.error("No study card: center not found or created.");
                showErrorAndReset(startButton, folderKey, null, "Center not found or created.");
                return;
            }
        }

        mainWindow.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        /**
         * In case of Neurinfo: the user can either enter a new common name to create a new subject
         * or select an existing subject from the combo box. This is not possible for OFSEP profile.
         */
        boolean useExistingSubjectInStudy = false;
        if (ShUpConfig.isModeSubjectNameManual()) {
            // minimal length for subject common name is 1, same for subject study identifier
            // if nothing is entered, use existing subject selected
            if (mainWindow.importDialog.existingSubjectsCB.isEnabled()) {
                subjectREST = (Subject) mainWindow.importDialog.existingSubjectsCB.getSelectedItem();
                if (subjectREST != null) {
                    logger.info("Existing subject used from server with ID: " + subjectREST.getId() + ", name: " + subjectREST.getName());
                    useExistingSubjectInStudy = true;
                } else {
                    showErrorAndReset(startButton, folderKey,
                            "shanoir.uploader.systemErrorDialog.error.subject.creation");
                    return;
                }
            }
        }

        // In case user selects existing subject from study, just use it
        if (!useExistingSubjectInStudy) {
            // Subject name: entered by the user in the GUI
            String subjectName = mainWindow.importDialog.subjectTextField.getText();
            ImagedObjectCategory category = (ImagedObjectCategory) mainWindow.importDialog.subjectImageObjectCategoryCB.getSelectedItem();
            String languageHemDom = (String) mainWindow.importDialog.subjectLanguageHemisphericDominanceCB.getSelectedItem();
            String manualHemDom = (String) mainWindow.importDialog.subjectManualHemisphericDominanceCB.getSelectedItem();
            String subjectStudyIdentifier = mainWindow.importDialog.subjectStudyIdentifierTF.getText();
            SubjectType subjectType = (SubjectType) mainWindow.importDialog.subjectTypeCB.getSelectedItem();
            boolean isPhysicallyInvolved = mainWindow.importDialog.subjectIsPhysicallyInvolvedCB.isSelected();
            subjectREST = ImportUtils.manageSubject(
                    subjectREST, importJob.getSubject(), subjectName, category, languageHemDom, manualHemDom,
                    subjectType, useExistingSubjectInStudy, isPhysicallyInvolved, subjectStudyIdentifier,
                    study, equipment);
            if (subjectREST == null) {
                showErrorAndReset(startButton, folderKey,
                        "shanoir.uploader.systemErrorDialog.error.wsdl.subjectcreator.createSubjectFromShup");
                return;
            }
        }

        Examination examination = null;
        // If the user wants to create a new examination
        if (mainWindow.importDialog.mrExaminationNewExamCB.isSelected()) {
            Date examinationDate = (Date) mainWindow.importDialog.mrExaminationDateDP.getModel().getValue();
            String examinationComment = mainWindow.importDialog.mrExaminationCommentTF.getText();
            boolean agreeWithDataReuse = mainWindow.importDialog.mrExaminationDataReuseAgreementCB.isSelected();
            examination = ImportUtils.createExamination(study, subjectREST, examinationDate, examinationComment, centerId, agreeWithDataReuse);
            if (examination == null) {
                showErrorAndReset(startButton, folderKey,
                        "shanoir.uploader.systemErrorDialog.error.wsdl.createmrexamination");
                return;
            } else {
                logger.info("Examination created on server with ID: " + examination.getId());
            }
        // If the user wants to use an existing examination
        } else {
            examination = (Examination) mainWindow.importDialog.mrExaminationExistingExamCB.getSelectedItem();
            logger.info("Examination used on server with ID: " + examination.getId());
        }

        ImportUtils.prepareImportJob(importJob, subjectREST.getName(), subjectREST.getId(), examination.getId(), examination.getStudyInstanceUID(),
                (Study) mainWindow.importDialog.studyCB.getSelectedItem(), (StudyCard) mainWindow.importDialog.studyCardCB.getSelectedItem(), equipment);

        // Quality Check if the Study selected has Quality Cards to be checked at import
        try {
            QualityCardResult qualityControlResult = QualityUtils.checkQualityAtImport(importJob, mainWindow.isFromPACS);
            if (!qualityControlResult.isEmpty() && (qualityControlResult.hasError())) {
                JOptionPane.showMessageDialog(mainWindow.frame, QualityUtils.getQualityControlreportScrollPane(qualityControlResult),
                        ShUpConfig.resourceBundle.getString("shanoir.uploader.import.quality.check.window.title"), JOptionPane.ERROR_MESSAGE);
                ShUpOnloadConfig.getCurrentNominativeDataController().updateNominativeDataPercentage(uploadFolder, UploadState.ERROR.toString());
                logger.error("The upload for the patient {} failed due to quality control errors.", importJob.getSubject().getName());
            } else {
                // If quality control condition is VALID we do not set a quality card result entry but we update the subjectStudy qualityTag
                if (!qualityControlResult.isEmpty() || !qualityControlResult.getUpdatedSubjects().isEmpty()) {
                    // If quality control has one warning or failed valid condition fulfilled we inform the user and allow import to continue
                    if (qualityControlResult.hasWarning() || qualityControlResult.hasFailedValid()) {
                        JOptionPane.showMessageDialog(mainWindow.frame, QualityUtils.getQualityControlreportScrollPane(qualityControlResult),
                                ShUpConfig.resourceBundle.getString("shanoir.uploader.import.quality.check.window.title"), JOptionPane.WARNING_MESSAGE);
                    }
                    // If Failed Valid No updated subject studies exist in the qualityControlResult
                    // For Now if Failed Valid then the quality tag of the subject on server side is not updated with an empty value
                    if (!qualityControlResult.hasFailedValid()) {
                        //Set qualityTag to the importJob in order to update subjectStudy qualityTag on server side
                        importJob.setQualityTag(qualityControlResult.getUpdatedSubjects().get(0).getQualityTag());
                    }
                }
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            JOptionPane.showMessageDialog(mainWindow.frame,
                    ShUpConfig.resourceBundle.getString("shanoir.uploader.import.quality.check.exception.message") + ex.getMessage(),
                    ShUpConfig.resourceBundle.getString("shanoir.uploader.select.error.title"), JOptionPane.ERROR_MESSAGE);
        }

        final ImportJobBase finalImportJob = importJob;
        final String finalSubjectName = subjectREST.getName();

        // Submit to the bounded pool. The completion callback (always run,
        // success or failure) is where we release the guard and restore
        // the UI -- NOT immediately after submission.
        IMPORT_FINISH_EXECUTOR.submit(new ImportFinishRunnable(uploadFolder, finalImportJob, finalSubjectName,
                () -> onImportFinishDone(folderKey)));

        JOptionPane.showMessageDialog(mainWindow.frame,
                ShUpConfig.resourceBundle.getString("shanoir.uploader.import.start.auto.import.message"),
                "Import", JOptionPane.INFORMATION_MESSAGE);

        mainWindow.importDialog.setVisible(false);
        mainWindow.importDialog.mrExaminationExamExecutiveLabel.setVisible(true);
        mainWindow.importDialog.mrExaminationExamExecutiveCB.setVisible(true);
        mainWindow.setCursor(null);
    }

    /**
     * Called from the background thread when ImportFinishRunnable completes
     * (success or failure). Marshals back to the EDT only for UI touches.
     */
    private void onImportFinishDone(String folderKey) {
        FOLDERS_IN_PROGRESS.remove(folderKey);
    }

    private void showErrorAndReset(JButton startButton, String folderKey, String bundleKey) {
        showErrorAndReset(startButton, folderKey, bundleKey, null);
    }

    private void showErrorAndReset(JButton startButton, String folderKey, String bundleKey, String rawMessage) {
        String message = bundleKey != null ? mainWindow.resourceBundle.getString(bundleKey) : rawMessage;
        JOptionPane.showMessageDialog(mainWindow.frame, message, "Error", JOptionPane.ERROR_MESSAGE);
        mainWindow.setCursor(null);
        startButton.setEnabled(true);
        FOLDERS_IN_PROGRESS.remove(folderKey);
    }

}
