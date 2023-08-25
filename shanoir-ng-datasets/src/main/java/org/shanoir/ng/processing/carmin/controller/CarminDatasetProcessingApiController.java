package org.shanoir.ng.processing.carmin.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.processing.carmin.model.CarminDatasetProcessing;
import org.shanoir.ng.processing.carmin.schedule.ExecutionStatusMonitorService;
import org.shanoir.ng.processing.carmin.service.CarminDatasetProcessingService;
import org.shanoir.ng.processing.dto.CarminDatasetProcessingDTO;
import org.shanoir.ng.processing.dto.ParameterResourcesDTO;
import org.shanoir.ng.processing.dto.mapper.CarminDatasetProcessingMapper;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.SecurityException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
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

        if(start){
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
            @ApiParam(value = "id of the dataset processing", required = true) @PathVariable("datasetProcessingId") Long datasetProcessingId,
            @ApiParam(value = "dataset processing to update", required = true) @Valid @RequestBody CarminDatasetProcessingDTO dto,
            boolean start,
            final BindingResult result) throws RestServiceException, SecurityException {

        validate(result);
        try {
            CarminDatasetProcessing updatedProcessing = carminDatasetProcessingService.updateCarminDatasetProcessing(mapper.carminDatasetProcessingDTOToCarminDatasetProcessing(dto));

            if(start){
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
		// Avoid infinite loop error -> We should be using datasetDTO here in a general matter.
		for (Dataset dataset : processing.get().getInputDatasets()) {
			dataset.setDatasetAcquisition(null);
		}
		return new ResponseEntity<>(mapper.carminDatasetProcessingToCarminDatasetProcessingDTO(processing.get()), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CarminDatasetProcessingDTO> findCarminDatasetProcessingByIdentifier(
            @ApiParam(value = "id of the dataset processing", required = true) @RequestParam("identifier") String identifier) {

        final Optional<CarminDatasetProcessing> processing = carminDatasetProcessingService.findByIdentifier(identifier);

        if (processing.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(mapper.carminDatasetProcessingToCarminDatasetProcessingDTO(processing.get()), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<CarminDatasetProcessingDTO>> findCarminDatasetProcessings() {
        final List<CarminDatasetProcessing> processings = carminDatasetProcessingService.findAllAllowed();
		if (processings.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(mapper.carminDatasetProcessingsToCarminDatasetProcessingDTOs(processings), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<CarminDatasetProcessingDTO>> findCarminDatasetProcessingsByStudyIdAndSubjectId(
            @ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId,
            @ApiParam(value = "id of the subject", required = true) @PathVariable("subjectId") Long subjectId) {
        List<CarminDatasetProcessing> processings = carminDatasetProcessingService.findAll();
        if (processings.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        processings = processings.stream()
                .filter(processing ->
                        !CollectionUtils.isEmpty(processing.getInputDatasets())
                && processing.getInputDatasets().get(0).getStudyId().equals(studyId)
                && processing.getInputDatasets().get(0).getSubjectId().equals(subjectId))
                .collect(Collectors.toList());

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
