package org.shanoir.ng.coil.controler;

import java.util.List;

import javax.validation.Valid;

import org.shanoir.ng.coil.dto.CoilDTO;
import org.shanoir.ng.coil.dto.mapper.CoilMapper;
import org.shanoir.ng.coil.model.Coil;
import org.shanoir.ng.coil.service.CoilService;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.annotations.ApiParam;

@Controller
public class CoilApiController implements CoilApi {

	@Autowired
	private CoilMapper coilMapper;

	@Autowired
	private CoilService coilService;

	public ResponseEntity<Void> deleteCoil(
			@ApiParam(value = "id of the coil", required = true) @PathVariable("coilId") Long coilId)
			throws RestServiceException {

		try {
			coilService.deleteById(coilId);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			
		} catch (EntityNotFoundException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	public ResponseEntity<CoilDTO> findCoilById(
			@ApiParam(value = "id of the coil", required = true) @PathVariable("coilId") Long coilId) {
		
		final Coil coil = coilService.findById(coilId);
		if (coil == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(coilMapper.coilToCoilDTO(coil), HttpStatus.OK);
	}

	public ResponseEntity<List<CoilDTO>> findCoils() {
		final List<Coil> coils = coilService.findAll();
		if (coils.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(coilMapper.coilsToCoilDTOs(coils), HttpStatus.OK);
	}

	public ResponseEntity<CoilDTO> saveNewCoil(
			@ApiParam(value = "coil to create", required = true) @Valid @RequestBody Coil coil,
			final BindingResult result) throws RestServiceException {
		
		/* Validation */
		validate(result);

		/* Save coil in db. */
		final Coil createdCoil = coilService.create(coil);
		return new ResponseEntity<>(coilMapper.coilToCoilDTO(createdCoil), HttpStatus.OK);
	}

	public ResponseEntity<Void> updateCoil(
			@ApiParam(value = "id of the coil", required = true) @PathVariable("coilId") Long coilId,
			@ApiParam(value = "coil to update", required = true) @Valid @RequestBody Coil coil,
			final BindingResult result) throws RestServiceException {

		validate(result);
		try {
			coilService.update(coil);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		
		} catch (EntityNotFoundException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
	
	
	private void validate(BindingResult result) throws RestServiceException {
		final FieldErrorMap errors = new FieldErrorMap(result);
		if (!errors.isEmpty()) {
			ErrorModel error = new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors));
			throw new RestServiceException(error);
		} 
	}
}
