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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.shanoir.ng.bids.model.BidsElement;
import org.shanoir.ng.bids.model.BidsFolder;
import org.shanoir.ng.bids.service.StudyBIDSService;
import org.shanoir.ng.bids.utils.BidsDeserializer;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.dto.IdNameCenterStudyDTO;
import org.shanoir.ng.study.dto.StudyDTO;
import org.shanoir.ng.study.dto.mapper.StudyMapper;
import org.shanoir.ng.study.dua.DataUserAgreement;
import org.shanoir.ng.study.dua.DataUserAgreementService;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.security.StudyFieldEditionSecurityManager;
import org.shanoir.ng.study.service.StudyService;
import org.shanoir.ng.study.service.StudyUniqueConstraintManager;
import org.shanoir.ng.study.service.StudyUserService;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

	@Value("${studies-data}")
	private String dataDir;

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
	private DataUserAgreementService dataUserAgreementService;

	@Autowired
	private StudyBIDSService bidsService;

	@Autowired
	private BidsDeserializer bidsDeserializer;

	@Autowired
	private ShanoirEventService eventService;

	private static final Logger LOG = LoggerFactory.getLogger(StudyApiController.class);

	private final HttpServletRequest request;

	@org.springframework.beans.factory.annotation.Autowired
	public StudyApiController(final HttpServletRequest request) {
		this.request = request;
	}

	@Override
	public ResponseEntity<Void> deleteStudy(@PathVariable("studyId") Long studyId) {
		try {
			this.deleteProtocolFile(studyId);
			bidsService.deleteBids(studyId);
			studyService.deleteById(studyId);
			eventService.publishEvent(new ShanoirEvent(ShanoirEventType.DELETE_STUDY_EVENT, studyId.toString(),
					KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS));
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

		Study createdStudy;
		try {
			createdStudy = studyService.create(study);
			bidsService.createBidsFolder(createdStudy);
			eventService.publishEvent(new ShanoirEvent(ShanoirEventType.CREATE_STUDY_EVENT,
					createdStudy.getId().toString(), KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS));
		} catch (MicroServiceCommunicationException e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Microservice communication error", null));
		}
		return new ResponseEntity<>(studyMapper.studyToStudyDTO(createdStudy), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Void> updateStudy(@PathVariable("studyId") final Long studyId, @RequestBody final Study study,
			final BindingResult result) throws RestServiceException {

		validate(study, result);

		try {
			bidsService.updateBidsFolder(study);
			studyService.update(study);
			eventService.publishEvent(new ShanoirEvent(ShanoirEventType.UPDATE_STUDY_EVENT, studyId.toString(),
					KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS));
		} catch (EntityNotFoundException e) {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		} catch (MicroServiceCommunicationException e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Microservice communication error", null));
		}
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<List<StudyUserRight>> rights(@PathVariable("studyId") final Long studyId)
			throws RestServiceException {
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
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId)
			throws IOException {
		Study study = studyService.findById(studyId);
		if (study.getProtocolFilePaths() == null || study.getProtocolFilePaths().isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		String filePath = getStudyFilePath(studyId, study.getProtocolFilePaths().get(0));
		File fileToDelete = new File(filePath);
		if (!fileToDelete.exists()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		Files.delete(Paths.get(filePath));
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@Override
	public void downloadProtocolFile(
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId,
			@ApiParam(value = "file to download", required = true) @PathVariable("fileName") String fileName,
			HttpServletResponse response) throws RestServiceException, IOException {
		String filePath = getStudyFilePath(studyId, fileName);
		LOG.info("Retrieving file : {}", filePath);
		File fileToDownLoad = new File(filePath);
		if (!fileToDownLoad.exists()) {
			response.sendError(HttpStatus.NO_CONTENT.value());
			return;
		}
		try (InputStream is = new FileInputStream(fileToDownLoad);) {
			response.setHeader("Content-Disposition", "attachment;filename=" + fileToDownLoad.getName());
			response.setContentType(MediaType.APPLICATION_PDF_VALUE);
			org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
			response.flushBuffer();
		}
	}

	@Override
	public ResponseEntity<Void> uploadProtocolFile(
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId,
			@ApiParam(value = "file to upload", required = true) @Valid @RequestBody MultipartFile file)
			throws RestServiceException {
		try {
			// TODO: Add check on challenge
			/*
			if (!(file.getOriginalFilename().endsWith(".pdf") || file.getOriginalFilename().endsWith(".zip")) || file.getSize() > 50000000) {
				LOG.error("Could not upload the file: {}", file.getOriginalFilename());
				Study study = studyService.findById(studyId);
				if (study.getProtocolFilePaths() != null) {
					study.getProtocolFilePaths().remove(file.getName());
				}
				studyService.update(study);
				return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
			}
			*/
			String filePath = getStudyFilePath(studyId, file.getOriginalFilename());
			File fileToCreate = new File(filePath);
			fileToCreate.getParentFile().mkdirs();
			LOG.info("Saving file {} to destination: {}", file.getOriginalFilename(), filePath);
			file.transferTo(new File(filePath));
		} catch (Exception e) {
			LOG.error("Error while loading files on examination: {}. File not uploaded. {}", studyId, e);
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	/**
	 * Gets the protocol or data user agreement file path
	 * 
	 * @param studyId
	 *            id of the study
	 * @param fileName
	 *            name of the file
	 * @return the file path of the file
	 */
	private String getStudyFilePath(Long studyId, String fileName) {
		return dataDir + "/study-" + studyId + "/" + fileName;
	}

	private void validate(Study study, BindingResult result) throws RestServiceException {
		final FieldErrorMap errors = new FieldErrorMap().add(fieldEditionSecurityManager.validate(study))
				.add(new FieldErrorMap(result)).add(uniqueConstraintManager.validate(study));
		if (!errors.isEmpty()) {
			ErrorModel error = new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments",
					new ErrorDetails(errors));
			throw new RestServiceException(error);
		}
	}

	@Override
	public void exportBIDSByStudyId(
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId,
			HttpServletResponse response) throws RestServiceException, IOException {
		Study study = studyService.findById(studyId);
		File workFolder = bidsService.exportAsBids(study);

		// Copy / zip it (and by the way filter only folder that we are interested in)
		String userDir = getUserDir(System.getProperty(JAVA_IO_TMPDIR)).getAbsolutePath();

		// Add timestamp to get a "random" difference
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		File tmpFile = new File(userDir + File.separator + formatter.format(new DateTime().toDate()) + File.separator);
		tmpFile.mkdirs();
		File zipFile = new File(tmpFile.getAbsolutePath() + File.separator + workFolder.getName() + ZIP);
		zipFile.createNewFile();

		// Zip it
		zip(workFolder.getAbsolutePath(), zipFile.getAbsolutePath());

		// Try to determine file's content type
		String contentType = request.getServletContext().getMimeType(zipFile.getAbsolutePath());

		try (InputStream is = new FileInputStream(zipFile);) {
			response.setHeader("Content-Disposition", "attachment;filename=" + zipFile.getName());
			response.setContentType(contentType);
			org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
			response.flushBuffer();
		} finally {
			FileUtils.deleteQuietly(zipFile);
		}
	}

	public static File getUserDir(String importDir) {
		final Long userId = KeycloakUtil.getTokenUserId();
		final String userImportDirFilePath = importDir + File.separator + Long.toString(userId);
		final File userImportDir = new File(userImportDirFilePath);
		if (!userImportDir.exists()) {
			userImportDir.mkdirs(); // create if not yet existing
		} // else is wanted case, user has already its import directory
		return userImportDir;
	}

	@Override
	public ResponseEntity<BidsElement> getBIDSStructureByStudyId(
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId)
			throws RestServiceException, IOException {

		BidsElement studyBidsElement = new BidsFolder(
				"Error while retrieving the study bids structure, please contact an administrator.");
		Study study = studyService.findById(studyId);
		if (study != null) {
			studyBidsElement = bidsDeserializer.deserialize(study);
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
			try (Stream<Path> walker = Files.walk(pp)) {
				walker.filter(path -> !path.toFile().isDirectory()).forEach(path -> {
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

	@Override
	public ResponseEntity<List<DataUserAgreement>> getDataUserAgreements() throws RestServiceException, IOException {
		Long userId = KeycloakUtil.getTokenUserId();
		List<DataUserAgreement> dataUserAgreements = this.dataUserAgreementService.getDataUserAgreementsByUserId(userId);
		if (!dataUserAgreements.isEmpty()) {
			return new ResponseEntity<>(dataUserAgreements, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
	}
	
	@Override
	public ResponseEntity<Void> acceptDataUserAgreement(
		@ApiParam(value = "id of the dua", required = true) @PathVariable("duaId") Long duaId)
		throws RestServiceException, MicroServiceCommunicationException {
		try {
			this.dataUserAgreementService.acceptDataUserAgreement(duaId);
		} catch (ShanoirException e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), null));
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Void> uploadDataUserAgreement(
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId,
			@ApiParam(value = "dua to upload", required = true) @Valid @RequestBody MultipartFile file) throws RestServiceException {
		try {
			if (!file.getOriginalFilename().endsWith(".pdf")  || file.getSize() > 50000000) {
				LOG.error("Could not upload the file: {}", file.getOriginalFilename());
				// Clean up: delete from study in case of same file existed before and upload not allowed
				Study study = studyService.findById(studyId);
				if (study.getDataUserAgreementPaths() != null) {
					study.getDataUserAgreementPaths().remove(file.getName());
				}
				studyService.update(study);
				return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
			}
			String filePath = getStudyFilePath(studyId, file.getOriginalFilename());
			File fileToCreate = new File(filePath);
			fileToCreate.getParentFile().mkdirs();
			LOG.info("Saving file {} to destination: {}", file.getOriginalFilename(), filePath);
			file.transferTo(new File(filePath));
		} catch (Exception e) {
			LOG.error("Error while loading files on study: {}. File not uploaded. {}", studyId, e);
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Override
	public void downloadDataUserAgreement(
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId,
			@ApiParam(value = "file to download", required = true) @PathVariable("fileName") String fileName, HttpServletResponse response) throws RestServiceException, IOException {
		String filePath = getStudyFilePath(studyId, fileName);
		LOG.info("Retrieving file : {}", filePath);
		File fileToDownLoad = new File(filePath);
		if (!fileToDownLoad.exists()) {
			response.sendError(HttpStatus.NO_CONTENT.value());
			return;
		}
		try (InputStream is = new FileInputStream(fileToDownLoad);) {
			response.setHeader("Content-Disposition", "attachment;filename=" + fileToDownLoad.getName());
			response.setContentType(MediaType.APPLICATION_PDF_VALUE);
			org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
			response.flushBuffer();
		}
	}
		
	@Override
	public ResponseEntity<Void> deleteDataUserAgreement (
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId) throws IOException {
		Study study = studyService.findById(studyId);
		if (study.getDataUserAgreementPaths() == null || study.getDataUserAgreementPaths().isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		String filePath = getStudyFilePath(studyId, study.getDataUserAgreementPaths().get(0));
		File fileToDelete = new File(filePath);
		if (!fileToDelete.exists()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		Files.delete(Paths.get(filePath));
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
