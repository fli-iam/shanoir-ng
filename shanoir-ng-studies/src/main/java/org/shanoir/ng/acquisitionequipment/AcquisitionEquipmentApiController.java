package org.shanoir.ng.acquisitionequipment;

import java.util.List;

import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.ErrorModelCode;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirStudiesException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.annotations.ApiParam;

@Controller
public class AcquisitionEquipmentApiController implements AcquisitionEquipmentApi {

	@Autowired
	private AcquisitionEquipmentService acquisitionEquipmentService;

	public ResponseEntity<Void> deleteAcquisitionEquipment(
			@ApiParam(value = "id of the acquisition equipment", required = true) @PathVariable("acquisitionEquipmentId") final Long acquisitionEquipmentId) {
		try {
			acquisitionEquipmentService.deleteById(acquisitionEquipmentId);
		} catch (ShanoirStudiesException e) {
			if (ErrorModelCode.ACQ_EQPT_NOT_FOUND.equals(e.getErrorCode())) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<Void>(HttpStatus.OK);
	}

	public ResponseEntity<AcquisitionEquipment> findAcquisitionEquipmentById(
			@ApiParam(value = "id of the acquisition equipment", required = true) @PathVariable("acquisitionEquipmentId") final Long acquisitionEquipmentId) {
		final AcquisitionEquipment equipment = acquisitionEquipmentService.findById(acquisitionEquipmentId);
		if (equipment == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(equipment, HttpStatus.OK);
	}

	public ResponseEntity<List<AcquisitionEquipment>> findAcquisitionEquipments() {
		final List<AcquisitionEquipment> equipments = acquisitionEquipmentService.findAll();
		if (equipments.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(equipments, HttpStatus.OK);
	}

	public ResponseEntity<AcquisitionEquipment> saveNewAcquisitionEquipment(
			@ApiParam(value = "acquisition equipment to create", required = true) @RequestBody final AcquisitionEquipment acquisitionEquipment,
			final BindingResult result) throws RestServiceException {
		/* Validation */
		// Check hibernate validation
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(hibernateErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(new ErrorModel(422, "Bad arguments", new ErrorDetails(errors)));
		}

		// Guarantees it is a creation, not an update
		acquisitionEquipment.setId(null);

		/* Save acquisition equipment in db. */
		try {
			return new ResponseEntity<>(acquisitionEquipmentService.save(acquisitionEquipment), HttpStatus.OK);
		} catch (final ShanoirStudiesException e) {
			throw new RestServiceException(new ErrorModel(422, "Bad arguments", null));
		}
	}

	public ResponseEntity<Void> updateAcquisitionEquipment(
			@ApiParam(value = "id of the acquisition equipment", required = true) @PathVariable("acquisitionEquipmentId") final Long acquisitionEquipmentId,
			@ApiParam(value = "acquisition equipment to update", required = true) @RequestBody final AcquisitionEquipment acquisitionequipment,
			final BindingResult result) throws RestServiceException {
		
		// IMPORTANT : avoid any confusion that could lead to security breach
		acquisitionequipment.setId(acquisitionEquipmentId);
				
		// Check hibernate validation
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(hibernateErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(new ErrorModel(422, "Bad arguments", new ErrorDetails(errors)));
		}

		/* Update user in db. */
		try {
			acquisitionEquipmentService.update(acquisitionequipment);
		} catch (final ShanoirStudiesException e) {
			if (ErrorModelCode.ACQ_EQPT_NOT_FOUND.equals(e.getErrorCode())) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			throw new RestServiceException(new ErrorModel(422, "Bad arguments", null));
		}

		return new ResponseEntity<>(HttpStatus.OK);
	}

}
