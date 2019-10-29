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

import java.util.List;

import javax.validation.Valid;

import org.shanoir.ng.examination.dto.ExaminationDTO;
import org.shanoir.ng.examination.dto.SubjectExaminationDTO;
import org.shanoir.ng.examination.dto.mapper.ExaminationMapper;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.annotations.ApiParam;

@Controller
public class ExaminationApiController implements ExaminationApi {

	private static final Logger LOG = LoggerFactory.getLogger(ExaminationApiController.class);

	@Autowired
	private ExaminationMapper examinationMapper;

	@Autowired
	private ExaminationService examinationService;
	

	@Override
	public ResponseEntity<Void> deleteExamination(
			@ApiParam(value = "id of the examination", required = true) @PathVariable("examinationId") final Long examinationId)
			throws RestServiceException {
		
		try {
			// Check if user rights needed
			examinationService.deleteById(examinationId);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (EntityNotFoundException e) {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		}
	}

	@Override
	public ResponseEntity<ExaminationDTO> findExaminationById(
			@ApiParam(value = "id of the examination", required = true) @PathVariable("examinationId") final Long examinationId)
			throws RestServiceException {
		
		Examination examination = examinationService.findById(examinationId);
		if (examination == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(examinationMapper.examinationToExaminationDTO(examination), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Page<ExaminationDTO>> findExaminations(final Pageable pageable) {
		Page<Examination> examinations = examinationService.findPage(pageable);
		if (examinations.getContent().size() == 0) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<Page<ExaminationDTO>>(examinationMapper.examinationsToExaminationDTOs(examinations), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<SubjectExaminationDTO>> findExaminationsBySubjectIdStudyId(
			@ApiParam(value = "id of the subject", required = true) @PathVariable("subjectId") Long subjectId,
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId) {
		
		final List<Examination> examinations = examinationService.findBySubjectIdStudyId(subjectId, studyId);
		if (examinations.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(examinationMapper.examinationsToSubjectExaminationDTOs(examinations), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<ExaminationDTO> saveNewExamination(
			@ApiParam(value = "the examination to create", required = true) @RequestBody @Valid final ExaminationDTO examinationDTO,
			final BindingResult result) throws RestServiceException {

		validate(result);

		final Examination createdExamination = examinationService.save(examinationMapper.examinationDTOToExamination(examinationDTO));
		return new ResponseEntity<ExaminationDTO>(examinationMapper.examinationToExaminationDTO(createdExamination), HttpStatus.OK);

	}

	@Override
	public ResponseEntity<Void> updateExamination(
			@ApiParam(value = "id of the examination", required = true) @PathVariable("examinationId") final Long examinationId,
			@ApiParam(value = "the examination to update", required = true) @RequestBody @Valid final ExaminationDTO examination,
			final BindingResult result) throws RestServiceException {

		validate(result);
		
		/* Update examination in db. */
		try {
			examinationService.update(examinationMapper.examinationDTOToExamination(examination));
			return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
		} catch (EntityNotFoundException e) {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		}
	}

	@Override
	public ResponseEntity<List<ExaminationDTO>> findExaminationsBySubjectId(@ApiParam(value = "id of the subject", required = true) @PathVariable("subjectId") Long subjectId) {
		final List<Examination> examinations = examinationService.findBySubjectId(subjectId);
		if (examinations.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(examinationMapper.examinationsToExaminationDTOs(examinations),
				HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Long> saveNewExaminationFromShup(@ApiParam(value = "examination to create", required = true) @Valid @RequestBody ExaminationDTO examinationDTO,
			final BindingResult result) throws RestServiceException {
		
		validate(result);

		final Examination createdExamination = examinationService.save(examinationMapper.examinationDTOToExamination(examinationDTO));
		LOG.warn("Subject service completed");
		return new ResponseEntity<Long>(createdExamination.getId(), HttpStatus.OK);

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

}
