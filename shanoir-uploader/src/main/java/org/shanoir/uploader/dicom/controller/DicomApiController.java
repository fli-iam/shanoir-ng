package org.shanoir.uploader.dicom.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.dicom.dto.ConfigDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/dicom/configuration")
public class DicomApiController {

    private static final Logger logger = LoggerFactory.getLogger(DicomApiController.class);

    @GetMapping
    public ConfigDTO getDicomConfiguration() {
        Integer pacsPort = null;
        Integer localPort = null;

        if (ShUpConfig.dicomServerProperties.getProperty("pacs.port") != null && ShUpConfig.dicomServerProperties.getProperty("local.port") != null) {
            try {
                pacsPort = Integer.parseInt(ShUpConfig.dicomServerProperties.getProperty("pacs.port"));
                localPort = Integer.parseInt(ShUpConfig.dicomServerProperties.getProperty("local.port"));
            } catch (NumberFormatException e) {
                logger.error("Error parsing Dicom port numbers", e);
                return null;
            }
        }
        return new ConfigDTO(
            ShUpConfig.dicomServerProperties.getProperty("pacs.host"),
            pacsPort,
            ShUpConfig.dicomServerProperties.getProperty("pacs.aet.title"),
            ShUpConfig.dicomServerProperties.getProperty("local.host"),
            localPort,
            ShUpConfig.dicomServerProperties.getProperty("local.aet.title")
            );
    }

    @PutMapping
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

}
