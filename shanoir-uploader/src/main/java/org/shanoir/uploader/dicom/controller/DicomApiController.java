package org.shanoir.uploader.dicom.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

import org.shanoir.ng.importer.dicom.query.DicomQuery;
import org.shanoir.ng.importer.model.Patient;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.ng.importer.model.Study;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.dicom.DicomServerClient;
import org.shanoir.uploader.dicom.dto.ConfigDTO;
import org.shanoir.uploader.dicom.query.Media;
import org.shanoir.uploader.dicom.query.PatientTreeNode;
import org.shanoir.uploader.dicom.query.SerieTreeNode;
import org.shanoir.uploader.dicom.query.StudyTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/dicom")
@Controller
public class DicomApiController {
    @Autowired
    DicomServerClient dicomServerClient;

    public DicomApiController() throws Exception {
//        try {
//            dicomServerClient = new DicomServerClient(ShUpConfig.dicomServerProperties, new File(ShUpConfig.WORK_FOLDER));
//        } catch (Exception e) {
//            logger.error("Error initializing DicomServerClient", e);
//        }
    }

    private static final Logger logger = LoggerFactory.getLogger(DicomApiController.class);

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

    @GetMapping("/echo")
    public HashMap<String, Boolean> echoDicomServer() {
        try {
            //dicomServerClient;
        } catch (Exception e) {
            logger.error("Error creating DicomServerClient", e);
            return new HashMap<String, Boolean>() {{ put("success", false); }};
        }
        return new HashMap<String, Boolean>() {
            {
                put("success", dicomServerClient.echoDicomServer());
            }
        };
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
    public List<Patient> queryDicomServer(@RequestBody HashMap<String, String> queryParameters) throws Exception {
        logger.info("Querying Dicom server with parameters: {}", queryParameters);

        List<Patient> patients = dicomServerClient.queryDicomServer(Objects.equals(queryParameters.get("studyRootQuery"), "true"), queryParameters.get("modality"), queryParameters.get("patientName"), queryParameters.get("patientID"), queryParameters.get("studyDescription"), queryParameters.get("patientBirthDate"), queryParameters.get("studyDate"));

//        Media media = new Media();
//        if (patients != null) {
//            for (Iterator patientsIt = patients.iterator(); patientsIt.hasNext();) {
//                Patient patient = (Patient) patientsIt.next();
//                final PatientTreeNode patientTreeNode = media.initChildTreeNode(patient);
//                // add patients
//                media.addTreeNode(patientTreeNode);
//                List<Study> studies = patient.getStudies();
//                for (Iterator studiesIt = studies.iterator(); studiesIt.hasNext();) {
//                    Study study = (Study) studiesIt.next();
//                    final StudyTreeNode studyTreeNode = patientTreeNode.initChildTreeNode(study);
//                    // add studies
//                    patientTreeNode.addTreeNode(studyTreeNode);
//                    List<Serie> series = study.getSeries();
//                    for (Iterator seriesIt = series.iterator(); seriesIt.hasNext();) {
//                        Serie serie = (Serie) seriesIt.next();
//                        if (!serie.isErroneous() && !serie.isIgnored()) {
//                            final SerieTreeNode serieTreeNode = studyTreeNode.initChildTreeNode(serie);
//                            // add series
//                            studyTreeNode.addTreeNode(serieTreeNode);
//                        }
//                    }
//                }
//            }
//        }

        return patients;
    }

}
