package org.shanoir.ng.processing.carmin.controller;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.processing.carmin.model.CarminDatasetProcessing;
import org.shanoir.ng.processing.carmin.schedule.ExecutionStatusMonitorService;
import org.shanoir.ng.processing.carmin.service.CarminDatasetProcessingService;
import org.shanoir.ng.processing.dto.CarminDatasetProcessingDTO;
import org.shanoir.ng.processing.dto.ParameterResourcesDTO;
import org.shanoir.ng.processing.dto.mapper.CarminDatasetProcessingMapper;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.shared.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author KhalilKes
 */
@Controller
public class CarminDatasetProcessingApiController implements CarminDatasetProcessingApi {


    @Autowired
    private CarminDatasetProcessingService carminDatasetProcessingService;

    @Autowired
    private ExecutionStatusMonitorService executionStatusMonitorService;

    @Autowired
    private CarminDatasetProcessingMapper mapper;

    @Override
    public ResponseEntity<CarminDatasetProcessingDTO> saveNewCarminDatasetProcessing(
            @Valid @RequestBody CarminDatasetProcessingDTO dto, boolean start, BindingResult result)
            throws RestServiceException, EntityNotFoundException, SecurityException {

        /* Validation */
        validate(result);

        /* Save dataset processing in db. */
        final CarminDatasetProcessing createdProcessing = carminDatasetProcessingService
                .createCarminDatasetProcessing(mapper.carminDatasetProcessingDTOToCarminDatasetProcessing(dto));

        List<ParameterResourcesDTO> parametersDatasets = carminDatasetProcessingService.createProcessingResources(createdProcessing, dto.getParametersResources());

        if (start) {
            if (createdProcessing.getIdentifier() == null || createdProcessing.getIdentifier().isEmpty()) {
                throw new RestServiceException(
                        new ErrorModel(HttpStatus.BAD_REQUEST.value(), "Monitoring job has been set to start but processing identifier is null or empty.", null));
            }
            executionStatusMonitorService.startMonitoringJob(createdProcessing.getIdentifier());
        }

        CarminDatasetProcessingDTO createdDTO = mapper.carminDatasetProcessingToCarminDatasetProcessingDTO(createdProcessing);
        createdDTO.setParametersResources(parametersDatasets);

        return new ResponseEntity<>(createdDTO, HttpStatus.OK);
    }


    @Override
    public ResponseEntity<Void> updateCarminDatasetProcessing(
            @Parameter(name = "dataset processing to update", required = true) @Valid @RequestBody CarminDatasetProcessingDTO dto,
            boolean start,
            final BindingResult result) throws RestServiceException, SecurityException {

        validate(result);
        try {
            CarminDatasetProcessing updatedProcessing = carminDatasetProcessingService.updateCarminDatasetProcessing(mapper.carminDatasetProcessingDTOToCarminDatasetProcessing(dto));

            if (start) {
                if (updatedProcessing.getIdentifier() == null || updatedProcessing.getIdentifier().isEmpty()) {
                    throw new RestServiceException(
                            new ErrorModel(HttpStatus.BAD_REQUEST.value(), "Monitoring job has been set to start but processing identifier is null or empty.", null));
                }
                executionStatusMonitorService.startMonitoringJob(updatedProcessing.getIdentifier());
            }

            return new ResponseEntity<>(HttpStatus.OK);

        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public ResponseEntity<CarminDatasetProcessingDTO> findCarminDatasetProcessingById(Long datasetProcessingId) {
        final Optional<CarminDatasetProcessing> processing = carminDatasetProcessingService.findById(datasetProcessingId);
        if (processing.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(mapper.carminDatasetProcessingToCarminDatasetProcessingDTO(processing.get()), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<CarminDatasetProcessingDTO>> getAllCarminDatasetProcessings() {
        final List<CarminDatasetProcessing> processings = carminDatasetProcessingService.findAllAllowed();
        if (processings.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(mapper.carminDatasetProcessingsToCarminDatasetProcessingDTOs(processings), HttpStatus.OK);
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
