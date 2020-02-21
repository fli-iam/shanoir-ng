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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;

import org.shanoir.ng.bids.model.BidsElement;
import org.shanoir.ng.bids.model.BidsFolder;
import org.shanoir.ng.bids.service.StudyBIDSService;
import org.shanoir.ng.bids.utils.BidsDeserializer;
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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.annotations.ApiParam;

@Controller
public class StudyApiController implements StudyApi {

	private static final String ATTACHMENT_FILENAME = "attachment;filename=";

	private static final String ZIP = ".zip";

	private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";

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
	
	@Autowired
	private StudyBIDSService bidsService;

	@Autowired
	private BidsDeserializer bidsDeserializer;

	private final HttpServletRequest request;
	
	private static final Logger LOG = LoggerFactory.getLogger(StudyApiController.class);

	@org.springframework.beans.factory.annotation.Autowired
	public StudyApiController(final HttpServletRequest request) {
		this.request = request;
	}

	@Override
	public ResponseEntity<Void> deleteStudy(@PathVariable("studyId") Long studyId) {
		try {
			bidsService.deleteBids(studyId);
			studyService.deleteById(studyId);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			
		} catch (EntityNotFoundException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
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
		bidsService.createBidsFolder(createdStudy);

		return new ResponseEntity<>(studyMapper.studyToStudyDTO(createdStudy), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Void> updateStudy(@PathVariable("studyId") final Long studyId, @RequestBody final Study study,
			final BindingResult result) throws RestServiceException {

		validate(study, result);
		
		try {
			bidsService.updateBidsFolder(study);
			studyService.update(study);
		} catch (EntityNotFoundException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
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

    @Override
	public ResponseEntity<ByteArrayResource> exportBIDSByStudyId(
    		@ApiParam(value = "id of the study", required=true) @PathVariable("studyId") Long studyId) throws RestServiceException, IOException {
		Study study = studyService.findById(studyId);
		File workFolder = bidsService.exportAsBids(study);

		// Create zip file in /tmp folder
		String tmpDir = System.getProperty(JAVA_IO_TMPDIR);
		File zipFile = new File(tmpDir + File.separator + workFolder.getName() + ZIP);
		// Zip it
		zip(workFolder.getAbsolutePath(), zipFile.getAbsolutePath());

		// Determine file's content type and return it for download
		String contentType = request.getServletContext().getMimeType(zipFile.getAbsolutePath());
		byte[] data = Files.readAllBytes(zipFile.toPath());
		ByteArrayResource resource = new ByteArrayResource(data);

		return ResponseEntity.ok()
				// Content-Disposition
				.header(HttpHeaders.CONTENT_DISPOSITION, ATTACHMENT_FILENAME + zipFile.getName())
				// Content-Type
				.contentType(MediaType.parseMediaType(contentType)) //
				// Content-Length
				.contentLength(data.length) //
				.body(resource);
	}

    @Override
	public ResponseEntity<BidsElement> getBIDSStructureByStudyId(
    		@ApiParam(value = "id of the study", required=true) @PathVariable("studyId") Long studyId) throws RestServiceException, IOException {

    	BidsElement studyBidsElement = new BidsFolder("Error while retrieving the study bids structure, please contact an administrator.");
    	Study study = studyService.findById(studyId);
		if (study != null) {
			studyBidsElement =  bidsDeserializer.deserialize(study);
		}

		return new ResponseEntity<>(studyBidsElement, HttpStatus.OK);
    }

	/**
	 * Zip
	 * 
	 * @param sourceDirPath
	 * @param zipFilePath
	 * @throws IOException
	 */
	private void zip(final String sourceDirPath, final String zipFilePath) throws IOException {
		Path p = Paths.get(zipFilePath);
		try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(p))) {
			Path pp = Paths.get(sourceDirPath);
			try(Stream<Path> walker = Files.walk(pp)) {
				walker.filter(path -> !path.toFile().isDirectory())
				.forEach(path -> {
					ZipEntry zipEntry = new ZipEntry(pp.relativize(path).toString());
					try {
						zos.putNextEntry(zipEntry);
						Files.copy(path, zos);
						zos.closeEntry();
					} catch (IOException e) {
						LOG.error(e.getMessage(), e);
					}
				});
			}
			zos.finish();
		}
	}
	
}
