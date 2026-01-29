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

package org.shanoir.ng.vip.output.handler;

import jakarta.ws.rs.NotFoundException;
import org.apache.commons.io.IOUtils;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.data.UID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.shanoir.ng.dataset.modality.ProcessedDatasetType;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetType;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.importer.dto.ProcessedDatasetImportJob;
import org.shanoir.ng.importer.service.ImporterService;
import org.shanoir.ng.importer.service.ProcessedDatasetImporterService;
import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.processing.service.DatasetProcessingService;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.model.Subject;
import org.shanoir.ng.shared.repository.StudyRepository;
import org.shanoir.ng.shared.repository.SubjectRepository;
import org.shanoir.ng.vip.executionMonitoring.model.ExecutionMonitoring;
import org.shanoir.ng.vip.processingResource.repository.ProcessingResourceRepository;
import org.shanoir.ng.vip.output.exception.ResultHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Order(1)
public class DefaultHandler extends OutputHandler {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultHandler.class);

    @Value("${vip.result-file-name}")
    private String resultFileName;

    @Autowired
    private ImporterService importerService;

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private DatasetProcessingService datasetProcessingService;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private DatasetService datasetService;

    @Autowired
    private ProcessingResourceRepository processingResourceRepository;

    @Autowired
    private ProcessedDatasetImporterService processedDatasetImporterService;

    @Override
    public boolean canProcess(ExecutionMonitoring processing) {
        return true;
    }

    @Override
    public void manageTarGzResult(List<File> resultFiles, File parent, ExecutionMonitoring monitoring) throws ResultHandlerException {
        try {
            List<File> outputFiles = new ArrayList<>();
            File resultJson = null;
            for (File file : resultFiles) {
                if (file.getAbsolutePath().endsWith("/" + resultFileName)) {
                    resultJson = file;
                } else {
                    // For all other files that are not a result.json or a folder, create a processed dataset and a dataset processing
                    outputFiles.add(file);
                    LOG.info("Output file [{}] found in archive.", file.getAbsolutePath());
                }
            }

            List<Dataset> inputDatasets = new ArrayList<>(monitoring.getInputDatasets());

            if (inputDatasets.isEmpty()) {
                throw new ResultHandlerException("No input datasets found.", null);
            }
            if (outputFiles.isEmpty()) {
                throw new ResultHandlerException("No processable file found in Tar result.", null);
            }
            DatasetProcessing newProcessing = createProcessedDatasets(outputFiles, monitoring, inputDatasets);
            executionTrackingService.completeTracking(monitoring, newProcessing);
        } catch (Exception e) {
            importerService.createFailedJob(parent.getPath());
            throw new ResultHandlerException("An error occured while extracting result from result archive.", e);
        }
    }

    private List<Dataset> getInputDatasets(File resultJson, String tarName) throws IOException, JSONException {
        List<String> candidates = new ArrayList<>();
        candidates.add(tarName);
        if (resultJson == null) {
            LOG.info("No result JSON found in archive.");
        } else if (resultJson.length() == 0) {
            LOG.warn("Result JSON [{}] is present but empty.", resultJson.getAbsolutePath());
        } else {
            LOG.info("Processing result JSON [{}]...", resultJson.getName());

            JSONObject json;
            try (InputStream is = new FileInputStream(resultJson)) {
                json = new JSONObject(IOUtils.toString(is, StandardCharsets.UTF_8));
            }

            Iterator<String> keys = json.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                JSONArray array = json.optJSONArray(key);
                if (array != null) {
                    // case "["resource_id+XXX+filename.nii", "resource_id+YYY+filename.nii", ...]"
                    for (int i = 0; i < array.length(); i++) {
                        String value = array.optString(i);
                        if (value != null) {
                            candidates.add(array.getString(i));
                        }
                    }
                } else {
                    String value = json.optString(key);
                    if (value != null) {
                        // Case "resource_id+XXX+filename.nii"
                        candidates.add(value);
                    }
                }
            }
        }
        List<Dataset> datasets = new ArrayList<>();
        for (String name : candidates) {
            datasets.addAll(getDatasetFromFilename(name));
        }
        return datasets;
    }

    /**
     * Creates a list of processed dataset and a dataset processing associated to the list of files given in entry.
     */
    private List<Dataset> getDatasetFromFilename(String name) {
        Matcher matcher = Pattern.compile("resource_id\\+(.+)\\+.*").matcher(name);
        if (matcher.matches()) {
            return processingResourceRepository.findDatasetsByResourceId(matcher.group(1));
        }
        return new ArrayList<>();
    }

    /**
     * Creates a list of processed dataset and a dataset processing associated to the list of files given in entry.
     */
    private DatasetProcessing createProcessedDatasets(List<File> processedFiles, ExecutionMonitoring execution, List<Dataset> inputDatasets) throws Exception {

        // Create dataset processing
        DatasetProcessing processing = createProcessing(execution, inputDatasets);

        for (File file : processedFiles) {
            LOG.info("Processing [{}]...", file.getAbsolutePath());

            ProcessedDatasetImportJob processedDataset = new ProcessedDatasetImportJob();
            processedDataset.setDatasetProcessing(processing);
            processedDataset.setProcessedDatasetFilePath(file.getAbsolutePath());
            processedDataset.setProcessedDatasetType(ProcessedDatasetType.EXECUTION_RESULT);
            String datasetName = file.getName();
            if (datasetName.contains("resource_id")) {
                datasetName = datasetName.substring(datasetName.lastIndexOf("+") + 1);
            }
            processedDataset.setProcessedDatasetName(datasetName);

            if (!inputDatasets.isEmpty()) {
                Long studyId = datasetService.getStudyId(inputDatasets.get(0));
                Study study = studyRepository.findById(studyId)
                        .orElseThrow(() -> new NotFoundException("Study [" + studyId + "] not found."));
                processedDataset.setStudyId(studyId);
                processedDataset.setStudyName(study.getName());

                List<Long> subjectIds = inputDatasets.stream().map(Dataset::getSubjectId).toList();
                Predicate<Long> predicate = obj -> Objects.equals(inputDatasets.get(0).getSubjectId(), obj);

                if (subjectIds.stream().allMatch(predicate)) {
                    Subject subject = subjectRepository.findById(inputDatasets.get(0).getSubjectId())
                            .orElseThrow(() -> new NotFoundException("Subject [" + inputDatasets.get(0).getSubjectId() + "] not found"));
                    processedDataset.setSubjectId(subject.getId());
                    processedDataset.setSubjectName(subject.getName());
                }
            }

            // Determine dataset type: DICOM → infer from modality/SOP class, otherwise GENERIC
            String datasetType = determineDatasetType(file);
            processedDataset.setDatasetType(datasetType);
            processedDatasetImporterService.createProcessedDataset(processedDataset);

            LOG.info("Processed dataset [{}] has been created from [{}].", processedDataset.getProcessedDatasetName(), file.getAbsolutePath());
        }
        datasetProcessingService.update(processing);
        return processing;
    }

    private DatasetProcessing createProcessing(ExecutionMonitoring execution, List<Dataset> inputDatasets) {
        DatasetProcessing processing = new DatasetProcessing();
        processing.setParent(execution);
        processing.setComment(execution.getPipelineIdentifier());
        processing.setUsername(execution.getUsername());
        processing.setInputDatasets(inputDatasets);
        processing.setProcessingDate(execution.getProcessingDate());
        processing.setStudyId(execution.getStudyId());
        processing.setDatasetProcessingType(execution.getDatasetProcessingType());
        processing.setOutputDatasets(new ArrayList<>());
        processing = datasetProcessingService.create(processing);
        return processing;
    }

    /**
     * Determines the dataset type based on file content.
     * If DICOM: extracts modality/SOP class and maps to appropriate type
     * If not DICOM: returns GENERIC
     */
    private String determineDatasetType(File file) {
        try {
            Attributes attributes = readDicomAttributes(file);
            if (attributes != null) {
                return mapDicomToDatasetType(attributes);
            }
        } catch (Exception e) {
            LOG.debug("Could not read DICOM attributes from file [{}]: {}", file.getAbsolutePath(), e.getMessage());
        }
        return DatasetType.Names.GENERIC;
    }

    private Attributes readDicomAttributes(File file) {
        if (file.exists()) {
            try (DicomInputStream dIS = new DicomInputStream(file)) {
                return dIS.readDataset();
            } catch (IOException e) {
                // Not a DICOM file or error reading
                return null;
            }
        }
        return null;
    }

    private String mapDicomToDatasetType(Attributes attributes) {
        String sopClassUID = attributes.getString(Tag.SOPClassUID);

        if (sopClassUID == null || sopClassUID.isEmpty()) return DatasetType.Names.GENERIC;
        if (isMr(sopClassUID)) return DatasetType.Names.MR;
        if (isCt(sopClassUID)) return DatasetType.Names.CT;
        if (isXa(sopClassUID)) return DatasetType.Names.XA;
        if (isPet(sopClassUID)) return DatasetType.Names.PET;
        if (isSpect(sopClassUID)) return DatasetType.Names.SPECT;
        if (isSegmentation(sopClassUID)) return DatasetType.Names.SEGMENTATION;
        if (isRegistration(sopClassUID)) return DatasetType.Names.REGISTRATION;
        if (isSr(sopClassUID)) return DatasetType.Names.SR;

        return DatasetType.Names.GENERIC;

    }

    private boolean isMr(String uid) {
        return Set.of(
            UID.MRImageStorage,
            UID.EnhancedMRImageStorage,
            UID.MRSpectroscopyStorage,
            UID.EnhancedMRColorImageStorage
        ).contains(uid);
    }

    private boolean isCt(String uid) {
        return Set.of(
            UID.CTImageStorage,
            UID.EnhancedCTImageStorage
        ).contains(uid);
    }

    private boolean isXa(String uid) {
        return Set.of(
            UID.XRayAngiographicImageStorage,
            UID.EnhancedXAImageStorage,
            UID.XRayRadiofluoroscopicImageStorage,
            UID.EnhancedXRFImageStorage,
            UID.XRay3DAngiographicImageStorage,
            UID.XRay3DCraniofacialImageStorage
        ).contains(uid);
    }

    private boolean isPet(String uid) {
        return Set.of(
            UID.PositronEmissionTomographyImageStorage,
            UID.EnhancedPETImageStorage
        ).contains(uid);
    }

    private boolean isSpect(String uid) {
        return Set.of(
            UID.NuclearMedicineImageStorage
        ).contains(uid);
    }

    private boolean isSegmentation(String uid) {
        return Set.of(
            UID.SegmentationStorage,
            UID.SurfaceSegmentationStorage
        ).contains(uid);
    }

    private boolean isRegistration(String uid) {
        return Set.of(
            UID.SpatialRegistrationStorage,
            UID.DeformableSpatialRegistrationStorage
        ).contains(uid);
    }

    private boolean isSr(String uid) {
        return Set.of(
            UID.BasicTextSRStorage,
            UID.EnhancedSRStorage,
            UID.ComprehensiveSRStorage,
            UID.MammographyCADSRStorage,
            UID.ChestCADSRStorage,
            UID.XRayRadiationDoseSRStorage,
            UID.ColonCADSRStorage,
            UID.ImplantationPlanSRStorage
        ).contains(uid);
    }
}
