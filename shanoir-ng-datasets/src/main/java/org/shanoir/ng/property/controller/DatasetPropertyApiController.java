package org.shanoir.ng.property.controller;

import org.shanoir.ng.dataset.controler.DatasetApiController;
import org.shanoir.ng.dataset.security.DatasetSecurityService;
import org.shanoir.ng.property.model.DatasetProperty;
import org.shanoir.ng.property.model.DatasetPropertyDTO;
import org.shanoir.ng.property.model.DatasetPropertyMapper;
import org.shanoir.ng.property.service.DatasetPropertyService;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;

@Controller
public class DatasetPropertyApiController implements DatasetPropertyApi {

    @Autowired
    private DatasetPropertyService propertyService;

    @Autowired
    private DatasetSecurityService securityService;

    @Autowired
    private DatasetPropertyMapper mapper;


    @Override
    public ResponseEntity<List<DatasetPropertyDTO>> getPropertiesByDatasetId(Long datasetId) {
        return new ResponseEntity<>(
                mapper.datasetPropertiesToDatasetPropertyDTOs(this.propertyService.getByDatasetId(datasetId)),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<DatasetPropertyDTO>> getPropertiesByProcessingId(Long processingId) throws EntityNotFoundException {
        List<DatasetProperty> filteredProperties = new ArrayList<>();

        for(DatasetProperty property : this.propertyService.getByDatasetProcessingId(processingId)){
            if(securityService.hasRightOnDataset(property.getDataset().getId(), "CAN_SEE_ALL")){
                filteredProperties.add(property);
            }
        }
        return new ResponseEntity<>(
                mapper.datasetPropertiesToDatasetPropertyDTOs(filteredProperties),
                HttpStatus.OK);
    }

}
