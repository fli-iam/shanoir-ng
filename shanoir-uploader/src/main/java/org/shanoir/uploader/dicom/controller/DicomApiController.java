package org.shanoir.uploader.dicom.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

import org.shanoir.ng.importer.model.Patient;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.ng.importer.model.Study;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.action.event.DicomClientReadyEvent;
import org.shanoir.uploader.dicom.DicomServerClient;
import org.shanoir.uploader.dicom.DicomTreeNode;
import org.shanoir.uploader.dicom.dto.ConfigDTO;
import org.shanoir.uploader.dicom.query.Media;
import org.shanoir.uploader.dicom.query.PatientTreeNode;
import org.shanoir.uploader.dicom.query.SerieTreeNode;
import org.shanoir.uploader.dicom.query.StudyTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;


@RestController
@RequestMapping("/dicom")
public class DicomApiController implements ApplicationListener<DicomClientReadyEvent> {

    public ShUpOnloadConfig shUpOnloadConfig = ShUpOnloadConfig.getInstance();

    private volatile DicomServerClient dicomServerClient;

    private static final Logger logger = LoggerFactory.getLogger(DicomApiController.class);

    @Override
    public void onApplicationEvent(DicomClientReadyEvent event) {
        logger.debug(">>> DicomClientReadyEvent received, client = {}", event.getDicomServerClient());
        this.dicomServerClient = event.getDicomServerClient();
        logger.debug("DicomApiController: DicomServerClient ready.");
    }

    // Securize endpoints if called before initialization of DicomServerClient
    private DicomServerClient getClient() {
        if (dicomServerClient == null) {
            try {
                dicomServerClient = shUpOnloadConfig.getDicomServerClient();
                if (dicomServerClient == null) {
                    throw new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "DICOM client not yet initialized"
                    );
                }
            } catch (Exception e) {
                logger.error("Error retrieving DicomServerClient: " + e.getMessage(), e);
            }
        }
        return dicomServerClient;
    }

    @GetMapping("/echo")
    public HashMap<String, Boolean> echoDicomServer() {
        return new HashMap<String, Boolean>() {
            {
                put("success", getClient().echoDicomServer());
            }
        };
    }

    @GetMapping("/configuration")
    public ConfigDTO getDicomConfiguration() {
        Integer pacsDicomPort = null;
        Integer localDicomPort = null;

        if (ShUpConfig.dicomServerProperties.getProperty("dicom.server.port") != null && ShUpConfig.dicomServerProperties.getProperty("local.dicom.server.port") != null) {
            try {
                pacsDicomPort = Integer.valueOf(ShUpConfig.dicomServerProperties.getProperty("dicom.server.port"));
                localDicomPort = Integer.valueOf(ShUpConfig.dicomServerProperties.getProperty("local.dicom.server.port"));
            } catch (NumberFormatException e) {
                logger.error("Error parsing Dicom port numbers", e);
                return null;
            }
        }
        return new ConfigDTO(
            ShUpConfig.dicomServerProperties.getProperty("dicom.server.host"),
            pacsDicomPort,
            ShUpConfig.dicomServerProperties.getProperty("dicom.server.aet.called"),
            ShUpConfig.dicomServerProperties.getProperty("local.dicom.server.host"),
            localDicomPort,
            ShUpConfig.dicomServerProperties.getProperty("local.dicom.server.aet.calling")
            );
    }

    @PutMapping("/configuration")
    public void updateDicomConfiguration(@RequestBody ConfigDTO config) {
        ShUpConfig.dicomServerProperties.setProperty("dicom.server.host", config.getDistantDicomServer().getHost());
        ShUpConfig.dicomServerProperties.setProperty("dicom.server.port", String.valueOf(config.getDistantDicomServer().getPort()));
        ShUpConfig.dicomServerProperties.setProperty("dicom.server.aet.called", config.getDistantDicomServer().getAet());
        ShUpConfig.dicomServerProperties.setProperty("local.dicom.server.host", config.getLocalDicomServer().getHost());
        ShUpConfig.dicomServerProperties.setProperty("local.dicom.server.port", String.valueOf(config.getLocalDicomServer().getPort()));
        ShUpConfig.dicomServerProperties.setProperty("local.dicom.server.aet.calling", config.getLocalDicomServer().getAet());

        Properties props = ShUpConfig.dicomServerProperties;
        props.setProperty("dicom.server.host", config.getDistantDicomServer().getHost());
        props.setProperty("dicom.server.port", String.valueOf(config.getDistantDicomServer().getPort()));
        props.setProperty("dicom.server.aet.called", config.getDistantDicomServer().getAet());
        props.setProperty("local.dicom.server.host", config.getLocalDicomServer().getHost());
        props.setProperty("local.dicom.server.port", String.valueOf(config.getLocalDicomServer().getPort()));
        props.setProperty("local.dicom.server.aet.calling", config.getLocalDicomServer().getAet());

        try (FileOutputStream fos = new FileOutputStream(ShUpConfig.shanoirUploaderFolder + File.separator + ShUpConfig.DICOM_SERVER_PROPERTIES)) {
            props.store(fos, "Updated by user");
            logger.info("Dicom server properties updated by user");
        } catch (Exception e) {
            logger.error("Error updating Dicom configuration", e);
        }
    }

    @PostMapping("/query")
    public Object queryDicomServer(@RequestBody HashMap<String, String> queryParameters) throws Exception {
        logger.info("Querying Dicom server with parameters: {}", queryParameters);

        List<Patient> patients = getClient().queryDicomServer(Objects.equals(queryParameters.get("studyRootQuery"), "true"), queryParameters.get("modality"), queryParameters.get("patientName"), queryParameters.get("patientID"), queryParameters.get("studyDescription"), queryParameters.get("patientBirthDate"), queryParameters.get("studyDate"));

        Media media = new Media();

        logger.info(patients.toString());

        // content of function fillMediaWithPatients(Media media, final List<Patient> patients)
        if (patients != null) {
            for (Iterator patientsIt = patients.iterator(); patientsIt.hasNext();) {
                Patient patient = (Patient) patientsIt.next();
                final PatientTreeNode patientTreeNode = media.initChildTreeNode(patient);
                logger.info("Patient info read: " + patient.toString());
                // add patients
                media.addTreeNode(patientTreeNode);
                List<Study> studies = patient.getStudies();
                for (Iterator studiesIt = studies.iterator(); studiesIt.hasNext();) {
                    Study study = (Study) studiesIt.next();
                    final StudyTreeNode studyTreeNode = patientTreeNode.initChildTreeNode(study);
                    // add studies
                    patientTreeNode.addTreeNode(studyTreeNode);
                    List<Serie> series = study.getSeries();
                    for (Iterator seriesIt = series.iterator(); seriesIt.hasNext();) {
                        Serie serie = (Serie) seriesIt.next();
                        if (!serie.isErroneous() && !serie.isIgnored()) {
                            final SerieTreeNode serieTreeNode = studyTreeNode.initChildTreeNode(serie);
                            // add series
                            studyTreeNode.addTreeNode(serieTreeNode);
                        }
                    }
                }
            }
            logger.info(media.getTreeNodes().size() + " patients read from DICOM server.");
            logger.info("Patients read from DICOM server: " + media.getTreeNodes().toString());
        }

        return media.getData();
    }

}
