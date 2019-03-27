package org.shanoir.ng.center.controler;

import java.util.List;

import javax.validation.Valid;

import org.shanoir.ng.center.dto.CenterDTO;
import org.shanoir.ng.center.dto.mapper.CenterMapper;
import org.shanoir.ng.center.model.Center;
import org.shanoir.ng.center.service.CenterService;
import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.UndeletableDependenciesException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.annotations.ApiParam;

@Controller
public class CenterApiController implements CenterApi {

	@Autowired
	private CenterMapper centerMapper;

	@Autowired
	private CenterService centerService;

	@Override
	public ResponseEntity<Void> deleteCenter(
			@ApiParam(value = "id of the center", required = true) @PathVariable("centerId") final Long centerId)
			throws RestServiceException {

		try {
			centerService.deleteByIdCheckDependencies(centerId);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			
		} catch (EntityNotFoundException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} catch (UndeletableDependenciesException e) {
			throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Forbidden",
					new ErrorDetails(e.getErrorMap())));
		}
	}

	@Override
	public ResponseEntity<CenterDTO> findCenterById(
			@ApiParam(value = "id of the center", required = true) @PathVariable("centerId") final Long centerId) {
		
		final Center center = centerService.findById(centerId);
		if (center == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(centerMapper.centerToCenterDTO(center), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<CenterDTO>> findCenters() {
		final List<Center> centers = centerService.findAll();
		if (centers.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(centerMapper.centersToCenterDTOs(centers), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<IdNameDTO>> findCentersNames() {
		final List<IdNameDTO> centers = centerService.findIdsAndNames();
		if (centers.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(centers, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<CenterDTO> saveNewCenter(
			@ApiParam(value = "the center to create", required = true) @RequestBody @Valid final Center center,
			final BindingResult result) throws RestServiceException {

		/* Validation */
		final FieldErrorMap errors = new FieldErrorMap()
				.checkFieldAccess(center) 
				.checkBindingContraints(result)
				.checkUniqueConstraints(center, centerService);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors)));
		}

		/* Save center in db. */
		final Center createdCenter = centerService.create(center);
		return new ResponseEntity<>(centerMapper.centerToCenterDTO(createdCenter), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Void> updateCenter(
			@ApiParam(value = "id of the center", required = true) @PathVariable("centerId") final Long centerId,
			@ApiParam(value = "the center to update", required = true) @RequestBody @Valid final Center center,
			final BindingResult result) throws RestServiceException {

		try {
			/* Validation */
			final FieldErrorMap errors = new FieldErrorMap()
					.checkFieldAccess(center, centerService) 
					.checkBindingContraints(result)
					.checkUniqueConstraints(center, centerService);
			if (!errors.isEmpty()) {
				throw new RestServiceException(
						new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors)));
			}
			/* Update center in db. */
			centerService.update(center);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			
		} catch (EntityNotFoundException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
}
