/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.study.controler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.dto.IdNameCenterStudyDTO;
import org.shanoir.ng.study.dto.StudyDTO;
import org.shanoir.ng.study.dto.mapper.StudyMapper;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.security.StudyFieldEditionSecurityManager;
import org.shanoir.ng.study.service.StudyService;
import org.shanoir.ng.study.service.StudyUniqueConstraintManager;
import org.shanoir.ng.study.service.StudyUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.ApiParam;

@Controller
public class StudyApiController implements StudyApi {

	private static final Logger LOG = LoggerFactory.getLogger(StudyApiController.class);

	@Value("${study-data}")
	private String dataDir;

	@Autowired
	private StudyService studyService;

	@Autowired
	private StudyMapper studyMapper;
	
	@Autowired
	private StudyFieldEditionSecurityManager fieldEditionSecurityManager;
	
	@Autowired
	private StudyUniqueConstraintManager uniqueConstraintManager;
	
	@Autowired
	private StudyUserService studyUserService;

	@Override
	public ResponseEntity<Void> deleteStudy(@PathVariable("studyId") Long studyId) {
		try {
			studyService.deleteById(studyId);
			this.deleteProtocolFile(studyId);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			
		} catch (EntityNotFoundException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} catch (IOException e) {
			LOG.error("Error while deleting protocol file {}", e);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
	}
	
	@Override
	public ResponseEntity<List<StudyDTO>> findStudies() {
		List<Study> studies = studyService.findAll();
		if (studies.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(studyMapper.studiesToStudyDTOs(studies), HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<List<IdName>> findStudiesNames() throws RestServiceException {
		List<IdName> studiesDTO = new ArrayList<>();
		final List<Study> studies = studyService.findAll();
		if (studies.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		for (Study study : studies) {
			studiesDTO.add(studyMapper.studyToIdNameDTO(study));
		}
		return new ResponseEntity<>(studiesDTO, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<IdNameCenterStudyDTO>> findStudiesNamesAndCenters() throws RestServiceException {
		List<IdNameCenterStudyDTO> studiesDTO = new ArrayList<>();
		final List<Study> studies = studyService.findAll();
		if (studies.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		for (Study study : studies) {
			studiesDTO.add(studyMapper.studyToExtendedIdNameDTO(study));
		}
		return new ResponseEntity<>(studiesDTO, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<StudyDTO> findStudyById(@PathVariable("studyId") final Long studyId) {
		Study study = studyService.findById(studyId);
		if (study == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<>(studyMapper.studyToStudyDTO(study), HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<StudyDTO> saveNewStudy(@RequestBody final Study study, final BindingResult result)
			throws RestServiceException {

		validate(study, result);

		final Study createdStudy = studyService.create(study);
		return new ResponseEntity<>(studyMapper.studyToStudyDTO(createdStudy), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Void> updateStudy(@PathVariable("studyId") final Long studyId, @RequestBody final Study study,
			final BindingResult result) throws RestServiceException {

		validate(study, result);
		
		try {
			studyService.update(study);
		} catch (EntityNotFoundException e) {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
	

	@Override
	public ResponseEntity<List<StudyUserRight>> rights(@PathVariable("studyId") final Long studyId) throws RestServiceException {
		List<StudyUserRight> rights = this.studyUserService.getRightsForStudy(studyId);
		if (!rights.isEmpty()) {
			return new ResponseEntity<>(rights, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
	}
	

	@Override
	public ResponseEntity<Boolean> hasOneStudyToImport() throws RestServiceException {
		boolean hasOneStudy = this.studyUserService.hasOneStudyToImport();
		return new ResponseEntity<>(hasOneStudy, HttpStatus.OK);
	}
	
	@Override
	public ResponseEntity<Void> deleteProtocolFile(
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId) throws IOException {
		Study study = studyService.findById(studyId);
		if (study.getProtocolFilePaths() == null || study.getProtocolFilePaths().isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		String filePath = getProtocolFilePath(studyId, study.getProtocolFilePaths().get(0));
		File fileToDelete = new File(filePath);
		if (!fileToDelete.exists()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		Files.delete(Paths.get(filePath));
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Override
	public ResponseEntity<ByteArrayResource> downloadProtocolFile(
			@ApiParam(value = "id of the examination", required = true) @PathVariable("studyId") Long examinationId,
			@ApiParam(value = "file to download", required = true) @PathVariable("fileName") String fileName) throws RestServiceException, IOException {
		String filePath = getProtocolFilePath(examinationId, fileName);
		LOG.info("Retrieving file : {}", filePath);
		File fileToDownLoad = new File(filePath);
		if (!fileToDownLoad.exists()) {
			return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
		}

		// Try to determine file's content type
		String contentType = "application/pdf";

		byte[] data = Files.readAllBytes(fileToDownLoad.toPath());
		ByteArrayResource resource = new ByteArrayResource(data);

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileToDownLoad.getName())
				.contentType(MediaType.parseMediaType(contentType))
				.contentLength(data.length)
				.body(resource);
	}

	@Override
	public ResponseEntity<Void> uploadProtocolFile(
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId,
			@ApiParam(value = "file to upload", required = true) @Valid @RequestBody MultipartFile file) throws RestServiceException {
		if (!file.getOriginalFilename().endsWith(".pdf")) {
			return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
		}
		String filePath = getProtocolFilePath(studyId, file.getOriginalFilename());
		File fileToCreate = new File(filePath);
		fileToCreate.getParentFile().mkdirs();
		try {
			LOG.info("Saving file {} to destination: {}", file.getOriginalFilename(), filePath);
			file.transferTo(new File(filePath));
		} catch (Exception e) {
			LOG.error("Error while loading files on examination: {}. File not uploaded. {}", studyId, e);
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	/**
	 * Gets the protocol file path
	 * @param studyId id of the study
	 * @param fileName name of the file
	 * @return the file path of the file
	 */
	private String getProtocolFilePath(Long studyId, String fileName) {
		return dataDir + "/study-" + studyId + "/" + fileName;
	}
	
	private void validate(Study study, BindingResult result) throws RestServiceException {
		final FieldErrorMap errors = new FieldErrorMap()
				.add(fieldEditionSecurityManager.validate(study))
				.add(new FieldErrorMap(result))
				.add(uniqueConstraintManager.validate(study));
		if (!errors.isEmpty()) {
			ErrorModel error = new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors));
			throw new RestServiceException(error);
		}
	}
	
}
