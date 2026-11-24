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

package org.shanoir.ng.preclinical.pathologies.subject;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.shanoir.ng.preclinical.pathologies.Pathology;
import org.shanoir.ng.preclinical.pathologies.PathologyService;
import org.shanoir.ng.preclinical.pathologies.models.PathologyModel;
import org.shanoir.ng.preclinical.pathologies.models.PathologyModelService;
import org.shanoir.ng.preclinical.subjects.model.AnimalSubject;
import org.shanoir.ng.preclinical.subjects.service.AnimalSubjectService;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.security.PreclinicalSecurityService;
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

@Controller
public class SubjectPathologyApiController implements SubjectPathologyApi {

    private static final Logger LOG = LoggerFactory.getLogger(SubjectPathologyApiController.class);

    @Autowired
    private SubjectPathologyService pathosService;
    @Autowired
    private PathologyModelService pathosModelService;
    @Autowired
    private AnimalSubjectService subjectService;
    @Autowired
    private PathologyService pathologyService;
    @Autowired
    private SubjectPathologyValidator uniqueValidator;

    @Autowired
    private SubjectPathologyEditableByManager editableOnlyValidator;

    @Autowired
    private PreclinicalSecurityService securityService;

    @Override
    public ResponseEntity<SubjectPathology> addSubjectPathology(
            @Parameter(name = "subject id", required = true) @PathVariable("id") Long id,
            @Parameter(name = "pathology to add to subject", required = true) @RequestBody SubjectPathology pathos,
            BindingResult result) throws RestServiceException {

        // First check if given user exists
        AnimalSubject animalSubject = subjectService.getById(id);
        if (animalSubject == null) {
            throw new RestServiceException(
                    new ErrorModel(HttpStatus.NOT_FOUND.value(), "animalSubject not found", new ErrorDetails()));
        } else {
            final FieldErrorMap accessErrors = this.getCreationRightsErrors(pathos);
            final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
            final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(pathos);
            /* Merge errors. */
            final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
            if (!errors.isEmpty()) {
                throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments",
                        new ErrorDetails(errors)));
            }

            // Guarantees it is a creation, not an update
            pathos.setId(null);

            // Just in case
            try {
                pathos.setAnimalSubject(animalSubject);
            } catch (Exception e) {
                LOG.error("Error while parsing subject id for Long cast " + e.getMessage(), e);
            }

            /* Save pathos in db. */
            try {
                final SubjectPathology createdPathos = pathosService.save(pathos);
                return new ResponseEntity<SubjectPathology>(createdPathos, HttpStatus.OK);
            } catch (ShanoirException e) {
                throw new RestServiceException(e,
                        new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
            }
        }

    }

    @Override
    public ResponseEntity<Void> deleteSubjectPathology(
            @Parameter(name = "Animal Subject id", required = true) @PathVariable("id") Long id,
            @Parameter(name = "pathology id to delete", required = true) @PathVariable("pid") Long pid)
            throws RestServiceException {

        // First check if given subject exists
        if (subjectService.getById(id) == null) {
            throw new RestServiceException(
                    new ErrorModel(HttpStatus.NOT_FOUND.value(), "Subject not found", new ErrorDetails()));
        } else {
            if (!belongsToSubject(pathosService.findById(pid), id)) {
                return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
            }
            try {
                pathosService.deleteById(pid);
            } catch (ShanoirException e) {
                return new ResponseEntity<Void>(HttpStatus.NOT_ACCEPTABLE);
            }
            return new ResponseEntity<Void>(HttpStatus.OK);
        }

    }

    @Override
    public ResponseEntity<Void> deleteSubjectPathologies(
            @Parameter(name = "animal subject id", required = true) @PathVariable("id") Long id)
            throws RestServiceException {
        AnimalSubject animalSubject = subjectService.getById(id);
        if (animalSubject == null) {
            return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
        } else {
            try {
                pathosService.deleteByAnimalSubject(animalSubject);
            } catch (ShanoirException e) {
                LOG.error("ERROR while deleting pathologies for subject " + animalSubject.getId(), e);
                return new ResponseEntity<Void>(HttpStatus.NOT_ACCEPTABLE);
            }
            return new ResponseEntity<Void>(HttpStatus.OK);
        }
    }

    @Override
    public ResponseEntity<SubjectPathology> getSubjectPathologyById(
            @Parameter(name = "subject id", required = true) @PathVariable("id") Long id,
            @Parameter(name = "ID of subject pathology that needs to be fetched", required = true) @PathVariable("pid") Long pid)
            throws RestServiceException {
        if (subjectService.getById(id) == null) {
            throw new RestServiceException(
                    new ErrorModel(HttpStatus.NOT_FOUND.value(), "Subject not found", new ErrorDetails()));
        } else {
            final SubjectPathology pathos = pathosService.findById(pid);
            if (!belongsToSubject(pathos, id)) {
                return new ResponseEntity<SubjectPathology>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<SubjectPathology>(pathos, HttpStatus.OK);
        }
    }

    @Override
    public ResponseEntity<List<SubjectPathology>> getSubjectPathologies(
            @Parameter(name = "animalSubject id", required = true) @PathVariable("id") Long id)
            throws RestServiceException {
        AnimalSubject animalSubject = subjectService.getById(id);
        if (animalSubject == null) {
            return new ResponseEntity<List<SubjectPathology>>(HttpStatus.NOT_FOUND);
        } else {
            final List<SubjectPathology> pathos = pathosService.findByAnimalSubject(animalSubject);
            return new ResponseEntity<List<SubjectPathology>>(pathos, HttpStatus.OK);
        }
    }

    @Override
    public ResponseEntity<List<SubjectPathology>> getSubjectPathologiesByPathology(
            @Parameter(name = "pathology id", required = true) @PathVariable("pid") Long pid)
            throws RestServiceException {
        Pathology pathology = pathologyService.findById(pid);
        if (pathology == null) {
            return new ResponseEntity<List<SubjectPathology>>(HttpStatus.NOT_FOUND);
        } else {
            final List<SubjectPathology> pathos = keepVisibleSubjects(pathosService.findAllByPathology(pathology));
            if (pathos.isEmpty()) {
                return new ResponseEntity<List<SubjectPathology>>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<List<SubjectPathology>>(pathos, HttpStatus.OK);
        }
    }

    @Override
    public ResponseEntity<List<SubjectPathology>> getSubjectPathologiesByPathologyModel(
            @Parameter(name = "pathology model id", required = true) @PathVariable("pathoModelId") Long pathoModelId) {
        PathologyModel patMod = pathosModelService.findById(pathoModelId);
        List<SubjectPathology> subPathology = keepVisibleSubjects(pathosService.findByPathologyModel(patMod));
        if (subPathology == null || subPathology.isEmpty()) {
            return new ResponseEntity<List<SubjectPathology>>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<List<SubjectPathology>>(subPathology, HttpStatus.OK);
        }
    }

    @Override
    public ResponseEntity<Void> updateSubjectPathology(
            @Parameter(name = "subject id", required = true) @PathVariable("id") Long id,
            @Parameter(name = "ID of subject pathology that needs to be updated", required = true) @PathVariable("pid") Long pid,
            @Parameter(name = "Subject pathology object that needs to be updated", required = true) @RequestBody SubjectPathology pathos,
            final BindingResult result) throws RestServiceException {

        AnimalSubject animalSubject = subjectService.getById(id);
        if (animalSubject == null) {
            throw new RestServiceException(
                    new ErrorModel(HttpStatus.NOT_FOUND.value(), "animalSubject not found", new ErrorDetails()));
        } else if (!belongsToSubject(pathosService.findById(pid), id)) {
            return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
        } else {
            pathos.setId(pid);

            final FieldErrorMap accessErrors = this.getUpdateRightsErrors(pathos);
            final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
            final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(pathos);
            /* Merge errors. */
            final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
            if (!errors.isEmpty()) {
                throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments",
                        new ErrorDetails(errors)));
            }
            try {
                pathosService.update(pathos);
            } catch (ShanoirException e) {
                LOG.error("Error while trying to update subject pathology " + pid + " : ", e);
                throw new RestServiceException(e,
                        new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
            }
            return new ResponseEntity<Void>(HttpStatus.OK);
        }
    }

    /**
     * Keeps the entries of the subjects of the studies the user can see (all of them for an admin):
     * the lists across all the subjects must not show the data of the other studies.
     */
    private List<SubjectPathology> keepVisibleSubjects(List<SubjectPathology> entries) {
        if (entries == null || entries.isEmpty()) {
            return entries;
        }
        List<Long> subjectIds = entries.stream()
                .filter(pathos -> pathos.getAnimalSubject() != null)
                .map(pathos -> pathos.getAnimalSubject().getId())
                .distinct()
                .toList();
        Set<Long> visibleSubjectIds = new HashSet<>(securityService.filterSubjectIdsHasRight(subjectIds, "CAN_SEE_ALL"));
        return entries.stream()
                .filter(pathos -> pathos.getAnimalSubject() != null && visibleSubjectIds.contains(pathos.getAnimalSubject().getId()))
                .toList();
    }

    /**
     * The rights are checked on the subject of the path: the subject pathology must belong to it,
     * otherwise any subject pathology could be read or changed through a subject of the user.
     */
    private static boolean belongsToSubject(SubjectPathology pathos, Long subjectId) {
        return pathos != null && pathos.getAnimalSubject() != null
                && subjectId != null && subjectId.equals(pathos.getAnimalSubject().getId());
    }

    private FieldErrorMap getUpdateRightsErrors(final SubjectPathology pathos) {
        return editableOnlyValidator.validate(pathos);
    }

    private FieldErrorMap getCreationRightsErrors(final SubjectPathology pathos) {
        return editableOnlyValidator.validate(pathos);
    }

    private FieldErrorMap getUniqueConstraintErrors(final SubjectPathology pathos) {
        final FieldErrorMap uniqueErrors = uniqueValidator.validate(pathos);
        return uniqueErrors;
    }

}
