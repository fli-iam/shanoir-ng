package org.shanoir.uploader.action;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;

import org.shanoir.ng.exchange.imports.subject.IdentifierCalculator;
import org.shanoir.ng.importer.dicom.DicomDirGeneratorService;
import org.shanoir.ng.importer.dicom.DicomDirToModelService;
import org.shanoir.ng.importer.dicom.ImagesCreatorAndDicomFileAnalyzerService;
import org.shanoir.ng.importer.model.Patient;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.uploader.dicom.IDicomServerClient;
import org.shanoir.uploader.dicom.query.PatientTreeNode;
import org.shanoir.uploader.dicom.query.SerieTreeNode;
import org.shanoir.uploader.dicom.query.StudyTreeNode;
import org.shanoir.uploader.gui.ImportFromFolderWindow;
import org.shanoir.uploader.model.ExaminationImport;
import org.shanoir.uploader.model.FolderImport;
import org.shanoir.uploader.model.rest.AcquisitionEquipment;
import org.shanoir.uploader.model.rest.Subject;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class ImportFromFolderRunner extends SwingWorker<Void, Integer>  {

    private static final String DICOMDIR = "DICOMDIR";

    private static Logger logger = LoggerFactory.getLogger(ImportFromFolderRunner.class);

    private DicomDirGeneratorService dicomDirGeneratorService = new DicomDirGeneratorService();

    private FolderImport folderimport;
    private ResourceBundle resourceBundle;
    private ImportFromFolderWindow importFromFolderWindow;
    private IDicomServerClient dicomServerClient;
    private ImagesCreatorAndDicomFileAnalyzerService dicomFileAnalyzer;
    private ShanoirUploaderServiceClient shanoirUploaderServiceClientNG;
    private IdentifierCalculator identifierCalculator;

    public ImportFromFolderRunner(FolderImport folderimport, ResourceBundle ressourceBundle, ImportFromFolderWindow importFromFolderWindow, ImagesCreatorAndDicomFileAnalyzerService dicomFileAnalyzer, ShanoirUploaderServiceClient shanoirUploaderServiceClientNG, IDicomServerClient dicomServerClient) {
        this.folderimport = folderimport;
        this.resourceBundle = ressourceBundle;
        this.importFromFolderWindow = importFromFolderWindow;
        this.identifierCalculator = new IdentifierCalculator();
        this.dicomFileAnalyzer = dicomFileAnalyzer;
        this.shanoirUploaderServiceClientNG = shanoirUploaderServiceClientNG;
        this.dicomServerClient = dicomServerClient;
    }


    @Override
    protected Void doInBackground() throws Exception {
        boolean success = true;
        int i = 1;

        List<AcquisitionEquipment> acquisitionEquipments = shanoirUploaderServiceClientNG.findAcquisitionEquipments();
        for (Iterator<AcquisitionEquipment> acquisitionEquipmentsIt = acquisitionEquipments.iterator(); acquisitionEquipmentsIt.hasNext();) {
            AcquisitionEquipment acquisitionEquipment = (AcquisitionEquipment) acquisitionEquipmentsIt.next();
            if (acquisitionEquipment.getId().equals(folderimport.getStudyCard().getAcquisitionEquipmentId())) {
                folderimport.getStudyCard().setAcquisitionEquipment(acquisitionEquipment);
                folderimport.getStudyCard().setCenterId(acquisitionEquipment.getCenter().getId());
                break;
            }
        }

        // Load all subjects if necessary
        if (folderimport.getListOfSubjectsForStudy() == null) {
            folderimport.setListOfSubjectsForStudy(this.shanoirUploaderServiceClientNG.findSubjectsByStudy(folderimport.getStudy().getId()));
        }

        for (ExaminationImport importTodo : this.folderimport.getExaminationImports()) {
            success = importData(importTodo) && success;
            i++;
        }

        if (success) {
            // Open current import tab and close csv import panel
            ((JTabbedPane) this.importFromFolderWindow.scrollPaneUpload.getParent().getParent()).setSelectedComponent(this.importFromFolderWindow.scrollPaneUpload.getParent());

            this.importFromFolderWindow.frame.setVisible(false);
            this.importFromFolderWindow.frame.dispose();
        } else {
            this.importFromFolderWindow.displayImports(folderimport);
            importFromFolderWindow.openButton.setEnabled(true);
            importFromFolderWindow.uploadButton.setEnabled(false);
        }
        return null;
    }

    private boolean importData(ExaminationImport importTodo) {
        // 1. Read dicom to retrieve information
        logger.info("1 - Get data from folder's path and copy it to destination folder");
        File selectedRootDir = new File(importTodo.getPath());
        List<Patient> patients = new ArrayList<>();
        try {
            boolean dicomDirGenerated = false;
            File dicomDirFile = new File(selectedRootDir, DICOMDIR);
            if (!dicomDirFile.exists()) {
                logger.info("No DICOMDIR found: generating one.");
                dicomDirGeneratorService.generateDicomDirFromDirectory(dicomDirFile, selectedRootDir);
                dicomDirGenerated = true;
                logger.info("DICOMDIR generated at path: " + dicomDirFile.getAbsolutePath());
            }
            final DicomDirToModelService dicomDirReader = new DicomDirToModelService();
            patients = dicomDirReader.readDicomDirToPatients(dicomDirFile);
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            logger.error(mapper.writeValueAsString(this.folderimport.getStudyCard()));
            String filePathDicomDir = selectedRootDir.toString();

            // clean up in case of dicomdir generated
            if (dicomDirGenerated) {
                dicomDirFile.delete();
            }
        } catch (Exception e) {
            logger.error("Something wrong happened while retrieving data: ", e);
            importTodo.setMessage(resourceBundle.getString("shanoir.uploader.import.folder.error.dicom"));
            importFromFolderWindow.displayImports(folderimport);
            return false;
        }

        logger.error("Loading patients");
        // Create dicom data to be able to move things
        Subject subject = null;
        Set<SerieTreeNode> selectedSeriesNodes = new HashSet<>();

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        PatientTreeNode patNode = new PatientTreeNode(patients.get(0));
        StudyTreeNode stNode = new StudyTreeNode(patients.get(0).getStudies().get(0));
        for (Serie serie: patients.get(0).getStudies().get(0).getSeries()) {
            SerieTreeNode serieNode = new SerieTreeNode(serie);
            serieNode.setSelected(true);
            stNode.addTreeNode(serieNode);
            selectedSeriesNodes.add(serieNode);
        }
        patNode.addTreeNode(stNode);
        String subjectIdentifier = null;
        try {
            subject = new Subject();
            // calculate identifier
//            subjectIdentifier = stNode.getId() +  this.identifierCalculator.calculateIdentifier(subject.getFirstName(), subject.getLastName(), "" + (subject.getBirthDate() != null ? subject.getBirthDate() : LocalDate.of(1990, Month.JANUARY,1)));
            subject.setIdentifier(subjectIdentifier);


            // Change birth date to first day of year
            logger.error("Study date could not be used for import.");
            final LocalDate dicomBirthDate = patNode.getPatient().getPatientBirthDate();
            if (dicomBirthDate != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(Date.from(dicomBirthDate.atStartOfDay().toInstant(ZoneOffset.UTC)));
                cal.set(Calendar.MONTH, Calendar.JANUARY);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                subject.setBirthDate(LocalDate.from(cal.toInstant()));
            }
        } catch (Exception e) {
            logger.error("Something wrong happened while analyzing data from the dicom", e);
            importTodo.setMessage(resourceBundle.getString("shanoir.uploader.import.folder.error.dicom"));
            return false;
        }



        // 8.  Create subject if necessary
        Subject subjectFound = null;

        String subjectName = importTodo.getParent().getStudy().getName() + "_" + importTodo.getSubjectName();
        for (Subject potentialSubject : importTodo.getParent().getListOfSubjectsForStudy()) {
            if (potentialSubject.getName().equals(subjectName)) {
                subjectFound = potentialSubject;
            }
        }
/**
 * 
        Study study = new Study();
        study.setId(importTodo.getParent().getStudy().getId());
        study.setName(importTodo.getParent().getStudy().getName());
        Subject subject;
        if (subjectFound != null) {
            logger.info("8 Subject exists, just use it");
            subject = subjectFound;
        } else {
            logger.info("8 Creating a new subject");
            subject = new org.shanoir.uploader.model.rest.Subject();
            subject.setName(subjectName);
            if (!StringUtils.isEmpty(patients.get(0).getPatientSex())) {
                subject.setSex(Sex.valueOf(patients.get(0).getPatientSex()));
            } else {
                // Force feminine (girl power ?)
                subject.setSex(Sex.F);
            }
            subject.setIdentifier(subjectIdentifier);

            subject.setImagedObjectCategory(ImagedObjectCategory.LIVING_HUMAN_BEING);

            subject.setBirthDate(subject.getBirthDate());

//            ImportUtils.addSubjectStudy(study, subject, SubjectType.PATIENT, true, null);

            // Get center ID from study card
            subject = shanoirUploaderServiceClientNG.createSubject(subject, true, this.folderimport.getStudyCard().getCenterId());
            this.folderimport.getListOfSubjectsForStudy().add(subject);
        }
        if (subject == null) {
//            uploadJob.setUploadState(UploadState.ERROR);
            importTodo.setMessage(resourceBundle.getString("shanoir.uploader.import.folder.error.subject"));
            return false;
        }

        // 9. Create examination
        logger.info("9 Create exam");

        Examination examDTO = new Examination();
        examDTO.setCenterId(importTodo.getParent().getStudyCard().getCenterId());
        examDTO.setComment(importTodo.getExamName());
        examDTO.setExaminationDate(Date.from(patients.get(0).getStudies().get(0).getStudyDate().atStartOfDay().toInstant(ZoneOffset.UTC)));
        examDTO.setPreclinical(false);
        examDTO.setStudyId(Long.valueOf(importTodo.getParent().getStudy().getId()));
        examDTO.setSubjectId(subject.getId());
        Examination createdExam = shanoirUploaderServiceClientNG.createExamination(examDTO);

        if (createdExam == null) {
//            uploadJob.setUploadState(UploadState.ERROR);
            importTodo.setMessage(resourceBundle.getString("shanoir.uploader.import.folder.error.examination"));
            return false;
        }
 */

        logger.info("10 Import.json");

//        ImportJob importJob = ImportUtils.prepareImportJob(uploadJob, subject.getName(), subject.getId(), createdExam.getId(), study, importTodo.getParent().getStudyCard());
//        importJob.setFromShanoirUploader(true);
//        Runnable runnable = new ImportFinishRunnable(uploadJob, uploadFolder, importJob, subject.getName());
//        Thread thread = new Thread(runnable);
//        thread.start();

        return true;
    }
}
