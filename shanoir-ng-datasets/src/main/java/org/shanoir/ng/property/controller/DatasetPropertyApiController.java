package org.shanoir.ng.property.controller;

import org.shanoir.ng.dataset.controler.DatasetApiController;
import org.shanoir.ng.dataset.security.DatasetSecurityService;
import org.shanoir.ng.dataset.service.DatasetService;
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
    private DatasetService dsSrv;

    @Autowired
    private DatasetPropertyService propertyService;

    @Autowired
    private DatasetSecurityService securityService;

    @Autowired
    private DatasetPropertyMapper mapper;


    @Override
    public ResponseEntity<List<DatasetPropertyDTO>> getPropertiesByDatasetId(Long datasetId) {

        if(!dsSrv.existsById(datasetId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<DatasetProperty> properties = this.propertyService.getByDatasetId(datasetId);

        if(properties.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(
                mapper.datasetPropertiesToDatasetPropertyDTOs(properties),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<DatasetPropertyDTO>> getPropertiesByProcessingId(Long processingId) throws EntityNotFoundException {

        if(!propertyService.existsById(processingId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<DatasetProperty> filteredProperties = new ArrayList<>();

        for(DatasetProperty property : this.propertyService.getByDatasetProcessingId(processingId)) {
            if(securityService.hasRightOnDataset(property.getDataset().getId(), "CAN_SEE_ALL")) {
                filteredProperties.add(property);
            }
        }

        if(filteredProperties.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(
                mapper.datasetPropertiesToDatasetPropertyDTOs(filteredProperties),
                HttpStatus.OK);
    }

}
