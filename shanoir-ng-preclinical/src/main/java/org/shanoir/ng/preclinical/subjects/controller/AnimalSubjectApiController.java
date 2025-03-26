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

package org.shanoir.ng.preclinical.subjects.controller;

import java.util.ArrayList;
import java.util.List;
import org.shanoir.ng.preclinical.pathologies.subject_pathologies.SubjectPathologyService;
import org.shanoir.ng.preclinical.references.RefsService;
import org.shanoir.ng.preclinical.subjects.dto.AnimalSubjectDto;
import org.shanoir.ng.preclinical.subjects.dto.PreclinicalSubjectDto;
import org.shanoir.ng.preclinical.subjects.dto.PreclinicalSubjectDtoService;
import org.shanoir.ng.preclinical.subjects.dto.SubjectDto;
import org.shanoir.ng.preclinical.subjects.model.AnimalSubject;
import org.shanoir.ng.preclinical.subjects.service.AnimalSubjectEditableByManager;
import org.shanoir.ng.preclinical.subjects.service.AnimalSubjectService;
import org.shanoir.ng.preclinical.subjects.service.AnimalSubjectUniqueValidator;
import org.shanoir.ng.preclinical.therapies.subject_therapies.SubjectTherapyService;
import org.shanoir.ng.shared.error.FieldError;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.validation.RefValueExistsValidator;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;

@Controller
public class AnimalSubjectApiController implements AnimalSubjectApi {

    private static final String BAD_ARGUMENTS = "Bad arguments";

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(AnimalSubjectApiController.class);

    @Autowired
    private AnimalSubjectService subjectService;
    @Autowired
    private RefsService refsService;
    @Autowired
    private SubjectPathologyService subjectPathologyService;
    @Autowired
    private SubjectTherapyService subjectTherapyService;
    @Autowired
    private ShanoirEventService eventService;

    @Autowired
    private AnimalSubjectUniqueValidator uniqueValidator;

    @Autowired
    private AnimalSubjectEditableByManager editableOnlyValidator;

    @Autowired
    private PreclinicalSubjectDtoService dtoService;

    @Override
    public ResponseEntity<PreclinicalSubjectDto> createAnimalSubject(
            @Parameter(name = "AnimalSubject object to add", required = true) @RequestBody @Valid final PreclinicalSubjectDto dto,
            final BindingResult result) throws RestServiceException {

        try {

            SubjectDto createdSubjectDto = this.createSubject(dto.getSubject());

            AnimalSubject animalSubject = dtoService.getAnimalSubjectFromPreclinicalDto(dto);
            animalSubject.setSubjectId(createdSubjectDto.getId());

            this.validateAnimalSubjectCreation(animalSubject, result);

            final AnimalSubject createdAnimal = subjectService.save(animalSubject);

            eventService.publishEvent(new ShanoirEvent(ShanoirEventType.CREATE_PRECLINICAL_SUBJECT_EVENT, createdAnimal.getId().toString(), KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS));

            PreclinicalSubjectDto preclinicalDto = dtoService.getPreclinicalDtoFromAnimalSubject(createdAnimal);
            preclinicalDto.setSubject(createdSubjectDto);

            return new ResponseEntity<>(preclinicalDto, HttpStatus.OK);

        } catch (ShanoirException e) {
            throw new RestServiceException(e,
                    new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), BAD_ARGUMENTS, null));
        }
    }

    private SubjectDto createSubject(SubjectDto dto) throws ShanoirException, RestServiceException {

        if(subjectService.isSubjectNameAlreadyUsed(dto.getName())) {
            FieldErrorMap errorMap = new FieldErrorMap();
            List<FieldError> errors = new ArrayList();
            errors.add(new FieldError("unique", "The given value is already taken for this field, choose another", dto.getName()));
            errorMap.put("name", errors);
            throw new RestServiceException(
                    new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), BAD_ARGUMENTS, new ErrorDetails(errorMap)));
        }

        dto.setPreclinical(true);

        Long subjectId;
        try {
            subjectId = subjectService.createSubject(dto);
        } catch (Exception ex) {
            String msg = "Failed to create subject. Animal subject can't be created.";
            LOG.error(msg, ex);
            throw new ShanoirException(msg, ex);
        }
        dto.setId(subjectId);

        return dto;
    }

    private void validateAnimalSubjectCreation(AnimalSubject animalSubject, BindingResult result) throws RestServiceException {
                // A basic user can only update certain fields, check that
        final FieldErrorMap accessErrors = editableOnlyValidator.validate(animalSubject);
        // Check hibernate validation
        final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
        // Check unique constraint
        final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(animalSubject);
        // Check if given reference values exist
        final FieldErrorMap refValuesExistsErrors = new RefValueExistsValidator<AnimalSubject>(refsService).validate(animalSubject);

        /* Merge errors. */
        final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors,
                refValuesExistsErrors);
        if (!errors.isEmpty()) {
            LOG.error("ERROR while creating AnimalSubject - error in fields :{}", errors.size());
            throw new RestServiceException(
                    new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), BAD_ARGUMENTS, new ErrorDetails(errors)));
        }
    }

    @Override
    public ResponseEntity<AnimalSubjectDto> getAnimalSubjectBySubjectId(
            @Parameter(name = "subject id of animalSubject that needs to be fetched", required = true) @PathVariable("id") Long id) {
        final AnimalSubjectDto subject = dtoService.getPreclinicalDtoFromAnimalSubject(subjectService.getBySubjectId(id)).getAnimalSubject();
        if (subject == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(subject, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<AnimalSubjectDto>> findBySubjectIds(List<Long> subjectIds) {
        final List<AnimalSubjectDto> subjects = dtoService.getAnimalSubjectDtoListFromAnimalSubjectList(subjectService.findBySubjectIds(subjectIds));
        if (subjects.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(subjects, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> updateAnimalSubject(
            @Parameter(name = "subject id of animalSubject that needs to be updated", required = true) @PathVariable("id") Long subjectId,
            @Parameter(name = "Subject object that needs to be updated", required = true) @RequestBody AnimalSubjectDto dto,
            final BindingResult result) throws RestServiceException {

        // IMPORTANT : avoid any confusion that could lead to security breach

        Long id = subjectService.getIdBySubjectId(subjectId);

        if(id == null) {
            throw new RestServiceException(new ErrorModel(HttpStatus.NOT_FOUND.value(), "No animal subject found for this subject id", null));
        }

        AnimalSubject subject = dtoService.getAnimalSubjectFromAnimalSubjectDto(dto);
        subject.setId(id);

        this.validateAnimalSubjectUpdate(subject, result);

        /* Update template in db. */
        try {
            subjectService.update(subject);
            eventService.publishEvent(new ShanoirEvent(ShanoirEventType.UPDATE_PRECLINICAL_SUBJECT_EVENT, id.toString(), KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS));
        } catch (ShanoirException e) {
            LOG.error("Error while trying to update animal subject [{}]", id, e);
            throw new RestServiceException(e,
                    new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), BAD_ARGUMENTS, null));
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    private void validateAnimalSubjectUpdate(AnimalSubject animalSubject, BindingResult result) throws RestServiceException {
        // A basic template can only update certain fields, check that
        final FieldErrorMap accessErrors =  editableOnlyValidator.validate(animalSubject);
        // Check hibernate validation
        final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
        // Check unique constrainte
        final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(animalSubject);
        /* Merge errors. */
        final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
        if (!errors.isEmpty()) {
            throw new RestServiceException(
                    new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), BAD_ARGUMENTS, new ErrorDetails(errors)));
        }
    }
    private FieldErrorMap getUniqueConstraintErrors(final AnimalSubject subject) {
        return uniqueValidator.validate(subject);
    }

}
