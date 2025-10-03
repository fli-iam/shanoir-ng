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

package org.shanoir.ng.service;

import java.io.File;
import java.util.Collections;

import org.apache.commons.lang3.ArrayUtils;
import org.shanoir.ng.model.Dataset;
import org.shanoir.ng.model.NiftiConverter;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This service allows to trigger the conversion of dicom to nifti files.
 * @author jcome
 *
 */
@Service
public class RabbitMqNiftiConversionService {

    @Autowired
    NIfTIConverterService converterService;

    private static final Logger LOG = LoggerFactory.getLogger(RabbitMqNiftiConversionService.class);

    /**
     * Converts BRUKER to DICOM data
     * @param message the string containing the workfolder where the dicom
     * @return true if the conversion is a success, false otherwise
     */
    @RabbitListener(queues = RabbitMQConfiguration.BRUKER_CONVERSION_QUEUE, containerFactory = "multipleConsumersFactory")
    @RabbitHandler
    public boolean convertBrukerToDicom(String message) {
        try {
            String[] messageSplit = message.split(";");
            String inputFolder = messageSplit[0];
            String outputFolder = messageSplit[1];
            this.converterService.brukerToDicomExec(inputFolder, outputFolder);
        } catch (Exception e) {
            LOG.error("Could not convert data from bruker to dicom: ", e);
            return false;
        }
        return true;
    }

    /**
     * Converts ANIMA to NIFTI data
     * @param message the string containing the image path of anima file
     * @return true if the conversion is a success, false otherwise
     */
    @RabbitListener(queues = RabbitMQConfiguration.ANIMA_CONVERSION_QUEUE, containerFactory = "singleConsumerFactory")
    @RabbitHandler
    public boolean convertAnimaToNifti(String message) {
        try {
            return this.converterService.animaToNiftiExec(message);
        } catch (Exception e) {
            LOG.error("Could not convert data from bruker to dicom: ", e);
            return false;
        }
    }

    /**
     * Converts some data
     * @param message the string containing the converter ID + the workfolder where the dicom are
     * @return true if the conversion is a success, false otherwise
     */
    @RabbitListener(queues = RabbitMQConfiguration.NIFTI_CONVERSION_QUEUE, containerFactory = "multipleConsumersFactory")
    @RabbitHandler
    public boolean convertData(String message) {
        try {
            String[] messageSplit = message.split(";");
            int converterId = Integer.parseInt(messageSplit[0]);
            LOG.error("Starting conversion with converter: " + converterId);

            String workFolder = messageSplit[1];
            String workFolderResult = workFolder + File.separator + "result";

            // If we're coming from BIDS, we need to convert directly in the given folder
            if (messageSplit.length > 2) {
                workFolderResult = messageSplit[2];
            }

            NiftiConverter converter = NiftiConverter.getType(converterId);

            File result = new File(workFolderResult);
            if (!result.exists()) {
                result.mkdirs();
                result.setReadable(true, false);
                result.setExecutable(true, false);
                result.setWritable(true, false);
            }

            boolean conversionResult = converterService.convertToNiftiExec(Long.valueOf(converterId), workFolder, workFolderResult);

            if (!conversionResult || ArrayUtils.isEmpty(result.listFiles())) {
                return false;
            }

            if (NiftiConverter.DICOMIFIER.equals(converter)) {
                Dataset dataset = new Dataset();
                dataset.setName("name");
                converterService.niftiFileSortingDicom2Nifti(Collections.emptyList(), result, dataset);
            } else {
                converterService.niftiFileSorting(Collections.emptyList(), result, new File("serieId"));
            }
        } catch (Exception e) {
            LOG.error("Could not convert data to nifti", e);
            return false;
        }
        return true;
    }
}
