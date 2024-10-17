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

package org.shanoir.ng.examination.controler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.examination.dto.ExaminationDTO;
import org.shanoir.ng.examination.dto.SubjectExaminationDTO;
import org.shanoir.ng.examination.dto.mapper.ExaminationMapper;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.*;
import org.shanoir.ng.shared.model.Subject;
import org.shanoir.ng.shared.repository.CenterRepository;
import org.shanoir.ng.shared.repository.SubjectRepository;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Controller
public class ExaminationApiController implements ExaminationApi {

	private static final Logger LOG = LoggerFactory.getLogger(ExaminationApiController.class);

	@Autowired
	private ExaminationMapper examinationMapper;

	@Autowired
	private ExaminationService examinationService;
	
	@Autowired
	private SubjectRepository subjectRepository;
	
	@Autowired
	private CenterRepository centerRepository;

	@Autowired
	private ShanoirEventService eventService;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	private final HttpServletRequest request;

	@org.springframework.beans.factory.annotation.Autowired
	public ExaminationApiController(final HttpServletRequest request) {
		this.request = request;
	}

	@Override
	public ResponseEntity<Void> deleteExamination(
			@Parameter(description = "id of the examination", required = true) @PathVariable("examinationId") final Long examinationId) {
		LOG.error("delete examination id : " + examinationId);
		Long studyId = examinationService.findById(examinationId).getStudyId();

		ShanoirEvent event = null;
		event = new ShanoirEvent(
				ShanoirEventType.DELETE_EXAMINATION_EVENT,
				String.valueOf(examinationId),
				KeycloakUtil.getTokenUserId(),
				"Starting delete of examination with id : " + examinationId,
				ShanoirEvent.IN_PROGRESS,
				0,
				studyId);

		eventService.publishEvent(event);

		examinationService.deleteExaminationAsync(examinationId, studyId, event);

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<ExaminationDTO> findExaminationById(
			@Parameter(description = "id of the examination", required = true) @PathVariable("examinationId") final Long examinationId) {
		Examination examination = examinationService.findById(examinationId);
		orderDatasetAcquisitions(examination);
		if (examination == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(examinationMapper.examinationToExaminationDTO(examination), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Page<ExaminationDTO>> findExaminations(final Pageable pageable, String searchStr, String searchField) {
		Page<Examination> examinations = examinationService.findPage(pageable, false, searchStr, searchField);
		if (examinations == null || examinations.getContent().isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(examinationMapper.examinationsToExaminationDTOs(examinations), HttpStatus.OK);
	}

	
	@Override
	public ResponseEntity<Page<ExaminationDTO>> findPreclinicalExaminations(
			@Parameter(description = "preclinical", required = true) @PathVariable("isPreclinical") Boolean isPreclinical, Pageable pageable) {
		Page<Examination> examinations;

		// Get examinations reachable by connected user
		examinations = examinationService.findPage(pageable, isPreclinical, null, null);
		if (examinations.getContent().isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(examinationMapper.examinationsToExaminationDTOs(examinations), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<SubjectExaminationDTO>> findExaminationsBySubjectIdStudyId(
			@Parameter(description = "id of the subject", required = true) @PathVariable("subjectId") Long subjectId,
			@Parameter(description = "id of the study", required = true) @PathVariable("studyId") Long studyId) {
		final List<Examination> examinations = examinationService.findBySubjectIdStudyId(subjectId, studyId);
		for (Examination exam : examinations) {
			orderDatasetAcquisitions(exam);
		}
		if (examinations.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(examinationMapper.examinationsToSubjectExaminationDTOs(examinations), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<Long>> findExaminationsByStudyId(
			@Parameter(description = "id of the study", required = true) @PathVariable("studyId") Long studyId) {
		final List<Long> examinations = examinationService.findIdsByStudyId(studyId);
		if (examinations.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(examinations, HttpStatus.OK);
	}

	// Attention: this method is used by ShanoirUploader!!!
	@Override
	public ResponseEntity<ExaminationDTO> saveNewExamination(
			@Parameter(description = "the examination to create", required = true) @RequestBody @Valid final ExaminationDTO examinationDTO,
			final BindingResult result) throws RestServiceException {
		validate(result);
        final Examination createdExamination = examinationService.save(examinationMapper.examinationDTOToExamination(examinationDTO));
		// NB: Message as centerId / subjectId is important in RabbitMQStudiesService
		eventService.publishEvent(new ShanoirEvent(ShanoirEventType.CREATE_EXAMINATION_EVENT, createdExamination.getId().toString(), KeycloakUtil.getTokenUserId(), "centerId:" + createdExamination.getCenterId() + ";subjectId:" + (createdExamination.getSubject() != null ? createdExamination.getSubject().getId() : null), ShanoirEvent.SUCCESS, createdExamination.getStudyId()));
		return new ResponseEntity<>(examinationMapper.examinationToExaminationDTO(createdExamination), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Void> updateExamination(
			@Parameter(description = "id of the examination", required = true) @PathVariable("examinationId") final Long examinationId,
			@Parameter(description = "the examination to update", required = true) @RequestBody @Valid final ExaminationDTO examination,
			final BindingResult result) throws RestServiceException {
		/* Update examination in db. */
		try {
			examinationService.update(examinationMapper.examinationDTOToExamination(examination));
			eventService.publishEvent(new ShanoirEvent(ShanoirEventType.UPDATE_EXAMINATION_EVENT, examination.getId().toString(), KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS, examination.getStudyId()));
			rabbitTemplate.convertAndSend(RabbitMQConfiguration.RELOAD_BIDS, objectMapper.writeValueAsString(examination.getStudyId()));
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (JsonProcessingException | EntityNotFoundException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} catch (ShanoirException e) {
			throw new RestServiceException(new ErrorModel(e.getErrorCode(), e.getMessage()));
		}
	}

	@Override
	public ResponseEntity<List<ExaminationDTO>> findExaminationsBySubjectId(@Parameter(description = "id of the subject", required = true) @PathVariable("subjectId") Long subjectId) {
		final List<Examination> examinations = examinationService.findBySubjectId(subjectId);
		if (examinations.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(examinationMapper.examinationsToExaminationDTOs(examinations),
				HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Void> addExtraData(
			@Parameter(description = "id of the examination", required = true) @PathVariable("examinationId") Long examinationId,
			@Parameter(description = "file to upload", required = true) @Valid @RequestBody MultipartFile file) throws RestServiceException {
		if (examinationService.addExtraData(examinationId, file) != null) {
			return new ResponseEntity<>(HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
	}
	
	@Override
	public ResponseEntity<Void> createExaminationAndAddExtraData(
			@Parameter(description = "name of the subject", required = true) @PathVariable("subjectName") String subjectName,
			@Parameter(description = "id of the center", required = true) @PathVariable("centerId") Long centerId,
			@Parameter(description = "file to upload", required = true) @Valid @RequestBody MultipartFile file) throws RestServiceException {
		
		Subject subject = subjectRepository.findByName(subjectName);
		if (subject == null) {
			ErrorModel error = new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Couldn't find subject with name " + subjectName);
			throw new RestServiceException(error);
		}
		
		if (centerRepository.findById(centerId).isEmpty()) {
			ErrorModel error = new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Couldn't find center with id " + centerId);
			throw new RestServiceException(error);
		}

		Examination examination = new Examination();
		examination.setComment(file.getOriginalFilename());
		examination.setCenterId(centerId);
		examination.setSubject(subject);
		examination.setStudy(subject.getSubjectStudyList().get(0).getStudy());
		examination.setExaminationDate(LocalDate.now());
		List<String> pathList = new ArrayList<>();
		pathList.add(file.getOriginalFilename());
		examination.setExtraDataFilePathList(pathList);
		Examination dbExamination = examinationService.save(examination);
		
		String path = examinationService.addExtraData(dbExamination.getId(), file);
		
		if (path != null) {
			return new ResponseEntity<>(HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}		
	}
	

	@Override
	public void downloadExtraData(
			@Parameter(description = "id of the examination", required = true) @PathVariable("examinationId") Long examinationId,
			@Parameter(description = "file to download", required = true) @PathVariable("fileName") String fileName, HttpServletResponse response) throws RestServiceException, IOException {
		String filePath = this.examinationService.getExtraDataFilePath(examinationId, fileName);
		LOG.info("Retrieving file : {}", filePath);
		File fileToDownLoad = new File(filePath);
		if (!fileToDownLoad.exists()) {
			response.sendError(HttpStatus.NO_CONTENT.value());
			return;
		}

		String contentType = request.getServletContext().getMimeType(fileToDownLoad.getAbsolutePath());

		try (InputStream is = new FileInputStream(fileToDownLoad);) {
			response.setHeader("Content-Disposition", "attachment;filename=" + fileToDownLoad.getName());
			response.setContentType(contentType);
			response.setContentLengthLong(fileToDownLoad.length());
			org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
			response.flushBuffer();
		}
	}

	/**
	 * Validate a dataset
	 * 
	 * @param result
	 * @throws RestServiceException
	 */
	private void validate(BindingResult result) throws RestServiceException {
		final FieldErrorMap errors = new FieldErrorMap(result);
		if (!errors.isEmpty()) {
			ErrorModel error = new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors));
			throw new RestServiceException(error);
		}
	}

	private void orderDatasetAcquisitions(Examination exam) {
		if (exam == null || exam.getDatasetAcquisitions() == null || exam.getDatasetAcquisitions().isEmpty()) {
			return;
		}
		exam.getDatasetAcquisitions().sort(new Comparator<DatasetAcquisition>() {
			@Override
			public int compare(DatasetAcquisition o1, DatasetAcquisition o2) {
				// Rank is never null
				Integer aIndex = o1.getSortingIndex() != null ? o1.getSortingIndex() : o1.getRank();
				Integer bIndex = o2.getSortingIndex() != null ? o2.getSortingIndex() : o2.getRank();
				if (aIndex == null) {
					aIndex = 0;
				}
				if (bIndex == null) {
					bIndex = 0;
				}
				return aIndex - bIndex;
			}
		});
	}
}
