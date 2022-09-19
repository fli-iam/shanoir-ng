package org.shanoir.ng.processing.carmin.controller;

import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.shanoir.ng.processing.carmin.model.CarminDatasetProcessing;
import org.shanoir.ng.processing.carmin.schedule.ExecutionStatusMonitorService;
import org.shanoir.ng.processing.carmin.service.CarminDatasetProcessingService;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.annotations.ApiParam;

/**
 * @author KhalilKes
 */
@Controller
public class CarminDatasetProcessingApiController implements CarminDatasetProcessingApi {


    @Autowired
    private CarminDatasetProcessingService carminDatasetProcessingService;

    @Autowired
    private ExecutionStatusMonitorService executionStatusMonitorService;

    @Override
    public ResponseEntity<CarminDatasetProcessing> saveNewCarminDatasetProcessing(
            @Valid @RequestBody CarminDatasetProcessing carminDatasetProcessing, BindingResult result)
            throws RestServiceException {

        /* Validation */
        validate(result);
        
        /**
         * run monitoring job
         */

        executionStatusMonitorService.startJob(carminDatasetProcessing.getIdentifier());

        /* Save dataset processing in db. */
        final CarminDatasetProcessing createdDatasetProcessing = carminDatasetProcessingService
                .createCarminDatasetProcessing(carminDatasetProcessing);

        return new ResponseEntity<>(createdDatasetProcessing, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CarminDatasetProcessing> findCarminDatasetProcessingById(Long datasetProcessingId) {
        final Optional<CarminDatasetProcessing> carminDatasetProcessing = carminDatasetProcessingService.findById(datasetProcessingId);
		if (!carminDatasetProcessing.isPresent()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(carminDatasetProcessing.get(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CarminDatasetProcessing> findCarminDatasetProcessingByIdentifier(
            @ApiParam(value = "id of the dataset processing", required = true) @RequestParam("identifier") String identifier) {

        final Optional<CarminDatasetProcessing> carminDatasetProcessing = carminDatasetProcessingService.findByIdentifier(identifier);
        if (!carminDatasetProcessing.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(carminDatasetProcessing.get(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> updateCarminDatasetProcessing(
            @ApiParam(value = "id of the dataset processing", required = true) @PathVariable("datasetProcessingId") Long datasetProcessingId,
            @ApiParam(value = "dataset processing to update", required = true) @Valid @RequestBody CarminDatasetProcessing carminDatasetProcessing,
            final BindingResult result) throws RestServiceException {

        validate(result);
        try {
            carminDatasetProcessingService.updateCarminDatasetProcessing(carminDatasetProcessing);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);

        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public ResponseEntity<List<CarminDatasetProcessing>> findCarminDatasetProcessings() {
        final List<CarminDatasetProcessing> carminDatasetProcessings = carminDatasetProcessingService.findAll();
		if (carminDatasetProcessings.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(carminDatasetProcessings, HttpStatus.OK);
    }


    private void validate(BindingResult result) throws RestServiceException {
        final FieldErrorMap errors = new FieldErrorMap(result);
        if (!errors.isEmpty()) {
            ErrorModel error = new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments",
                    new ErrorDetails(errors));
            throw new RestServiceException(error);
        }
    }

    
}
