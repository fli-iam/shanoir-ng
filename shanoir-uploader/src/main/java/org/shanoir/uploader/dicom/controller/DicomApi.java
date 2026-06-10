package org.shanoir.uploader.dicom.controller;

import java.io.IOException;
import java.net.MalformedURLException;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/dicom")
public interface DicomApi {

    @GetMapping(value = "/configuration", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ConfigDto> getDicomConfiguration() throws MalformedURLException, IOException;
    
}
