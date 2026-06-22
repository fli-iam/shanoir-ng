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

package org.shanoir.ng.center.controler;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.shanoir.ng.center.dto.CenterDTO;
import org.shanoir.ng.center.dto.mapper.CenterMapper;
import org.shanoir.ng.center.model.Center;
import org.shanoir.ng.center.security.CenterFieldEditionSecurityManager;
import org.shanoir.ng.center.service.CenterService;
import org.shanoir.ng.center.service.CenterUniqueConstraintManager;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.dicom.InstitutionDicom;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.UndeletableDependenciesException;
import org.shanoir.ng.studycenter.StudyCenter;
import org.shanoir.ng.utils.KeycloakUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@Controller
public class CenterApiController implements CenterApi {

    @Autowired
    private CenterMapper centerMapper;

    @Autowired
    private CenterService centerService;

    @Autowired
    private CenterFieldEditionSecurityManager fieldEditionSecurityManager;

    @Autowired
    private CenterUniqueConstraintManager uniqueConstraintManager;

    @Autowired
    private ShanoirEventService eventService;

    @Override
    public ResponseEntity<Void> deleteCenter(
            @Parameter(description = "id of the center", required = true) @PathVariable("centerId") final Long centerId)
                    throws RestServiceException {
        try {
            if (centerId.equals(0L)) {
                throw new EntityNotFoundException("Cannot update unknown center");
            }
            centerService.deleteByIdCheckDependencies(centerId);
            eventService.publishEvent(new ShanoirEvent(ShanoirEventType.DELETE_CENTER_EVENT, centerId.toString(), KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS));
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (UndeletableDependenciesException e) {
            throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Forbidden",
                    new ErrorDetails(e.getErrorMap())));
        }
    }

    @Override
    @Transactional
    public ResponseEntity<CenterDTO> findCenterById(
            @Parameter(description = "id of the center", required = true) @PathVariable("centerId") final Long centerId) {
        final Optional<Center> center = centerService.findById(centerId);
        if (center.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(centerMapper.centerToCenterDTOStudyCenters(center.orElseThrow()), HttpStatus.OK);
    }

    /**
     * This method is used by ShanoirUploader, during mass imports (Excel) and imports for studies
     * without study cards. We could add a check here to only allow this method on studies, that have
     * no study cards, but this would block existing mass imports into today's studies, that is why
     * we do not add this restriction today.
     */
    @Override
    @Transactional
    public ResponseEntity<CenterDTO> findOrCreateOrAddCenterByInstitutionDicom(
            @Parameter(description = "id of the study", required = true) @PathVariable("studyId") Long studyId,
            @Parameter(description = "institution dicom to find or create a center", required = true)
            @RequestBody InstitutionDicom institutionDicom, BindingResult result) throws RestServiceException {
        if (institutionDicom.getInstitutionName() == null || institutionDicom.getInstitutionName().isBlank()) {
            return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        }
        try {
            final Center center = centerService.findOrCreateOrAddCenterByInstitutionDicom(studyId, institutionDicom, true);
            eventService.publishEvent(new ShanoirEvent(ShanoirEventType.CREATE_CENTER_EVENT, center.getId().toString(), KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS));
            return new ResponseEntity<>(centerMapper.centerToCenterDTOFlat(center), HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<List<CenterDTO>> findCenters() {
        List<Center> centers = centerService.findAll();
        // Remove "unknown" center
        centers = centers.stream().filter(center -> center.getId() != 0).collect(Collectors.toList());
        if (centers.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(centerMapper.centersToCenterDTOsFull(centers), HttpStatus.OK);
    }

    @Override
    @Transactional
    public ResponseEntity<List<CenterDTO>> findCentersByStudy(
            @Parameter(description = "id of the study", required = true) @PathVariable("studyId") Long studyId) {
        final List<Center> centers = centerService.findByStudy(studyId);
        if (centers.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(centerMapper.centersToCenterDTOsEquipments(centers), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<IdName>> findCentersNames() {
        List<IdName> centers = centerService.findIdsAndNames();
        // Remove "unknown" center
        centers = centers.stream().filter(center -> center.getId() != 0).collect(Collectors.toList());
        if (centers.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(centers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<IdName>> findCentersNames(
            @Parameter(description = "id of the study", required = true) @PathVariable("studyId") Long studyId) {
        final List<IdName> centers = centerService.findIdsAndNames(studyId);
        if (centers.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(centers, HttpStatus.OK);
    }

    @Override
    @Transactional
    public ResponseEntity<CenterDTO> saveNewCenter(
            @Parameter(description = "the center to create", required = true) @RequestBody @Valid final Center center,
            final BindingResult result) throws RestServiceException {
        forceCentersOfStudyCenterList(center);
        validate(center, result);
        /* Save center in db. */
        final Center createdCenter = centerService.create(center, true);
        eventService.publishEvent(new ShanoirEvent(ShanoirEventType.CREATE_CENTER_EVENT, createdCenter.getId().toString(), KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS));
        return new ResponseEntity<>(centerMapper.centerToCenterDTOFlat(createdCenter), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> updateCenter(
            @Parameter(description = "id of the center", required = true) @PathVariable("centerId") final Long centerId,
            @Parameter(description = "the center to update", required = true) @RequestBody @Valid final Center center,
            final BindingResult result) throws RestServiceException {
        try {
            if (centerId.equals(0L)) {
                throw new EntityNotFoundException("Cannot update unknown center");
            }
            forceCentersOfStudyCenterList(center);
            validate(center, result);

            /* Update center in db. */
            centerService.update(center, true);
            eventService.publishEvent(new ShanoirEvent(ShanoirEventType.UPDATE_CENTER_EVENT, centerId.toString(), KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS));
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    private void validate(Center center, BindingResult result) throws RestServiceException {
        final FieldErrorMap errors = new FieldErrorMap()
                .add(fieldEditionSecurityManager.validate(center))
                .add(new FieldErrorMap(result))
                .add(uniqueConstraintManager.validate(center));
        if (!errors.isEmpty()) {
            ErrorModel error = new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors));
            throw new RestServiceException(error);
        }
    }

    private void forceCentersOfStudyCenterList(Center center) {
        if (center.getStudyCenterList() != null) {
            for (StudyCenter sc : center.getStudyCenterList()) {
                sc.setCenter(center);
            }
        }
    }

}
