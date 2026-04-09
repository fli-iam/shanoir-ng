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

package org.shanoir.ng.acquisitionequipment.controler;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.shanoir.ng.acquisitionequipment.dto.AcquisitionEquipmentDTO;
import org.shanoir.ng.acquisitionequipment.dto.mapper.AcquisitionEquipmentMapper;
import org.shanoir.ng.acquisitionequipment.model.AcquisitionEquipment;
import org.shanoir.ng.acquisitionequipment.service.AcquisitionEquipmentService;
import org.shanoir.ng.shared.dicom.EquipmentDicom;
import org.shanoir.ng.shared.error.FieldError;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.utils.KeycloakUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.transaction.Transactional;

@Controller
public class AcquisitionEquipmentApiController implements AcquisitionEquipmentApi {

    @Autowired
    private AcquisitionEquipmentMapper acquisitionEquipmentMapper;

    @Autowired
    private AcquisitionEquipmentService acquisitionEquipmentService;

    @Autowired
    private ShanoirEventService eventService;

    @Override
    public ResponseEntity<Void> deleteAcquisitionEquipment(
            @Parameter(description = "id of the acquisition equipment", required = true) @PathVariable("acquisitionEquipmentId") final Long acquisitionEquipmentId) {
        try {
            if (acquisitionEquipmentId.equals(0L)) {
                throw new EntityNotFoundException("Cannot update unknown equipment");
            }
            acquisitionEquipmentService.deleteById(acquisitionEquipmentId);
            eventService.publishEvent(new ShanoirEvent(ShanoirEventType.DELETE_EQUIPEMENT_EVENT, acquisitionEquipmentId.toString(), KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS));
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<AcquisitionEquipmentDTO> findAcquisitionEquipmentById(
            @Parameter(description = "id of the acquisition equipment", required = true) @PathVariable("acquisitionEquipmentId") final Long acquisitionEquipmentId) {
        final AcquisitionEquipment equipment = acquisitionEquipmentService.findById(acquisitionEquipmentId).orElse(null);
        if (equipment == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(acquisitionEquipmentMapper.acquisitionEquipmentToAcquisitionEquipmentDTO(equipment), HttpStatus.OK);
    }

    @Override
    @Transactional
    public ResponseEntity<List<AcquisitionEquipmentDTO>> findAcquisitionEquipments() {
        List<AcquisitionEquipment> equipments = acquisitionEquipmentService.findAll();
        // Remove "unknown" equipment
        equipments = equipments.stream().filter(equipment -> equipment.getId() != 0).collect(Collectors.toList());
        if (equipments.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(
                acquisitionEquipmentMapper.acquisitionEquipmentsToAcquisitionEquipmentDTOs(equipments), HttpStatus.OK);
    }

    @Override
    @Transactional
    public ResponseEntity<List<AcquisitionEquipmentDTO>> findAcquisitionEquipmentsByCenter(@Parameter(description = "id of the center", required = true) @PathVariable("centerId") Long centerId) {
        final List<AcquisitionEquipment> equipments = acquisitionEquipmentService.findAllByCenterId(centerId);
        if (equipments.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(
                acquisitionEquipmentMapper.acquisitionEquipmentsToAcquisitionEquipmentDTOs(equipments), HttpStatus.OK);
    }

    @Override
    @Transactional
    public ResponseEntity<List<AcquisitionEquipmentDTO>> findAcquisitionEquipmentsByStudy(@Parameter(description = "id of the study", required = true) @PathVariable("studyId") Long studyId) {
        List<AcquisitionEquipment> equipments = acquisitionEquipmentService.findAllByStudyId(studyId);
        // Remove "unknown" equipment
        equipments = equipments.stream().filter(equipment -> equipment.getId() != 0).collect(Collectors.toList());
        if (equipments.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(
                acquisitionEquipmentMapper.acquisitionEquipmentsToAcquisitionEquipmentDTOs(equipments), HttpStatus.OK);
    }

    @Override
    @Transactional
    public ResponseEntity<AcquisitionEquipmentDTO> saveNewAcquisitionEquipment(
            @Parameter(description = "acquisition equipment to create", required = true) @RequestBody final AcquisitionEquipment acquisitionEquipment,
            final BindingResult result) throws RestServiceException {

        validate(result);

        /* Save acquisition equipment in db. */
        try {
            AcquisitionEquipment newAcqEquipment = acquisitionEquipmentService.create(acquisitionEquipment);
            AcquisitionEquipmentDTO equipementCreated = acquisitionEquipmentMapper.acquisitionEquipmentToAcquisitionEquipmentDTO(newAcqEquipment);

            eventService.publishEvent(new ShanoirEvent(ShanoirEventType.CREATE_EQUIPEMENT_EVENT, equipementCreated.getId().toString(), KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS));
            return new ResponseEntity<>(equipementCreated, HttpStatus.OK);
        } catch (DataIntegrityViolationException e) {
            checkDataIntegrityException(e, acquisitionEquipment);
            throw e;
        }
    }

    @Override
    public ResponseEntity<Void> updateAcquisitionEquipment(
            @Parameter(description = "id of the acquisition equipment", required = true) @PathVariable("acquisitionEquipmentId") final Long acquisitionEquipmentId,
            @Parameter(description = "acquisition equipment to update", required = true) @RequestBody final AcquisitionEquipment acquisitionEquipment,
            final BindingResult result) throws RestServiceException {

        validate(result);

        /* Update user in db. */
        try {
            if (acquisitionEquipmentId.equals(0L)) {
                throw new EntityNotFoundException("Cannot update unknown equipment");
            }
            acquisitionEquipmentService.update(acquisitionEquipment);
            eventService.publishEvent(new ShanoirEvent(ShanoirEventType.UPDATE_EQUIPEMENT_EVENT, acquisitionEquipmentId.toString(), KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS));
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (DataIntegrityViolationException e) {
            checkDataIntegrityException(e, acquisitionEquipment);
            throw e;
        }
    }

    private void validate(BindingResult result) throws RestServiceException {
        final FieldErrorMap errors = new FieldErrorMap(result);
        if (!errors.isEmpty()) {
            throw new RestServiceException(
                    new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors)));
        }
    }

    private void checkDataIntegrityException(DataIntegrityViolationException e, AcquisitionEquipment acquisitionEquipment) throws RestServiceException {
        if (e.getRootCause() instanceof SQLIntegrityConstraintViolationException) {
            SQLIntegrityConstraintViolationException rootEx = (SQLIntegrityConstraintViolationException) e.getRootCause();
            if (rootEx.getMessage().contains("model_number_idx")) {
                FieldErrorMap errorMap = new FieldErrorMap();
                List<FieldError> errors = new ArrayList<>();
                errors.add(new FieldError("unique", "The given manufModel/serial/center value tuple is already taken, choose another ",
                        acquisitionEquipment.getManufacturerModel().getId() + " / " + acquisitionEquipment.getSerialNumber() + " / " + acquisitionEquipment.getCenter().getId()));
                errorMap.put("manufacturerModel - serialNumber - center", errors);
                ErrorModel error = new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errorMap));
                throw new RestServiceException(error);
            }
        }
    }

    @Override
    @Transactional
    public ResponseEntity<List<AcquisitionEquipmentDTO>> findAcquisitionEquipmentsBySerialNumber(
            @Parameter(description = "serial number of the acquisition equipment", required = true) @PathVariable("serialNumber") final String serialNumber) {
        List<AcquisitionEquipment> equipments = acquisitionEquipmentService.findAllBySerialNumber(serialNumber);
        // Remove "unknown" equipment
        equipments = equipments.stream().filter(equipment -> equipment.getId() != 0).collect(Collectors.toList());
        if (equipments.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(
                acquisitionEquipmentMapper.acquisitionEquipmentsToAcquisitionEquipmentDTOs(equipments), HttpStatus.OK);
    }

    @Override
    @Transactional
    public ResponseEntity<List<AcquisitionEquipmentDTO>> findAcquisitionEquipmentsOrCreateByEquipmentDicom(
            @Parameter(description = "id of the center", required = true) @PathVariable("centerId") Long centerId,
            @Parameter(description = "equipment dicom to find or create an equipment", required = true) @RequestBody final EquipmentDicom equipmentDicom,
            final BindingResult result) {
        List<AcquisitionEquipment> equipments = acquisitionEquipmentService.findAcquisitionEquipmentsOrCreateByEquipmentDicom(centerId, equipmentDicom);
        // Remove "unknown" equipment
        equipments = equipments.stream().filter(equipment -> equipment.getId() != 0).collect(Collectors.toList());
        if (equipments.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(
                acquisitionEquipmentMapper.acquisitionEquipmentsToAcquisitionEquipmentDTOs(equipments), HttpStatus.OK);
    }

}
