package org.shanoir.uploader.dicom.controller;

import java.io.FileOutputStream;
import java.util.Properties;

import org.shanoir.uploader.ShUpConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class DicomApiController implements DicomApi {

    @Override
    public ResponseEntity<ConfigDto> getDicomConfiguration() {
        ConfigDto configDto = new ConfigDto();
        configDto.setAetTitle(ShUpConfig.dicomServerProperties.getProperty("pacs.aet.title"));
        configDto.setHost(ShUpConfig.dicomServerProperties.getProperty("pacs.host"));
        configDto.setPort(Integer.parseInt(ShUpConfig.dicomServerProperties.getProperty("pacs.port")));
        return ResponseEntity.ok(configDto);
    }

    @Override
    public void setDicomConfiguration(ConfigDto config) throws Exception {
        Properties props = ShUpConfig.dicomServerProperties;
        props.setProperty("pacs.aet.title", config.getAetTitle());
        props.setProperty("pacs.host", config.getHost());
        props.setProperty("pacs.port", String.valueOf(config.getPort()));

        try (FileOutputStream fos = new FileOutputStream("src/main/resources/basic.properties")) {
            props.store(fos, "Updated by user");
        }
        // Mettre à jour le bean en mémoire sans redémarrage
        pacsConfig.setAetTitle(config.getAetTitle());
        pacsConfig.setHost(config.getHost());
        pacsConfig.setPort(config.getPort());
    }
    
}
