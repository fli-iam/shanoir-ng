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

package org.shanoir.ng.dataset.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.repository.DatasetExpressionRepository;
import org.shanoir.ng.dataset.repository.DatasetRepository;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.MrDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.repository.DatasetAcquisitionRepository;
import org.shanoir.ng.datasetfile.DatasetFileRepository;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.model.Subject;
import org.shanoir.ng.shared.repository.StudyRepository;
import org.shanoir.ng.shared.repository.SubjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;

import jakarta.transaction.Transactional;


@Service
public class DatasetCopyServiceImpl implements DatasetCopyService {

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private DatasetAcquisitionRepository datasetAcquisitionRepository;

    @Autowired
    private ExaminationRepository examinationRepository;

    @Autowired
    private DatasetRepository datasetRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private DatasetExpressionRepository datasetExpressionRepository;

    @Autowired
    private DatasetFileRepository datasetFileRepository;

    private static final Logger LOG = LoggerFactory.getLogger(DatasetCopyServiceImpl.class);

    @Override
    @Transactional
    public DatasetCopyService.DatasetCopyResult moveDataset(Long dsId, Long studyId, Map<Long, IdName> subjectMap, Map<Long, Examination> examMap, Map<Long, DatasetAcquisition> acqMap, Long userId) throws DatasetCopyService.NotFoundSubjectIdException, DatasetCopyService.NotFoundDatasetIdException, JsonProcessingException {
        DatasetCopyService.DatasetCopyResult result = new DatasetCopyService.DatasetCopyResult();
        try {
            Dataset ds = datasetRepository.findById(dsId).orElseThrow(() -> new DatasetCopyService.NotFoundDatasetIdException(dsId));
            if (ds.getSource() != null) {
                LOG.info("[CopyDatasets] Selected dataset is a copy, please pick the original dataset.");
                result.incrementCopy();
                return result;
            }
            Long oldDsId = ds.getId();
            IdName targetSubject = subjectMap.get(ds.getSubjectId());
            if (targetSubject == null) {
                LOG.error("[CopyDatasets] No mapping found for subject with id = " + ds.getSubjectId() + ". Dataset with id = " + oldDsId + " cannot be copied.");
                throw new DatasetCopyService.NotFoundSubjectIdException(ds.getSubjectId());
            }
            result.setSubjectName(targetSubject.getName());
            Subject targetSubjectRef = subjectRepository.getReferenceById(targetSubject.getId());
            LOG.info("[CopyDatasets] moveDataset : " + oldDsId + " to study : " + studyId);

            // Creation of new dataset according to its type
            Dataset newDs = null;
            if (ds.getDatasetAcquisition() != null &&  ds.getDatasetAcquisition().getId() != null) {

                newDs = DatasetUtils.copyDatasetFromDataset(ds);
                newDs.setSource(ds);
                newDs.setCopies(new ArrayList<>());
                newDs.setSubjectId(targetSubjectRef.getId());

                // Handling of DatasetAcquisition and Examination
                DatasetAcquisition newDsAcq = null;
                Long oldAcqId = null;
                if (ds.getDatasetAcquisition() != null) {
                    oldAcqId = ds.getDatasetAcquisition().getId();
                    if (acqMap.get(oldAcqId) != null) {
                        newDsAcq = acqMap.get(oldAcqId);
                    } else {
                        newDsAcq = datasetAcquisitionRepository.findBySourceIdAndExaminationStudy_Id(oldAcqId, studyId);
                    }
                    if (newDsAcq == null) {
                        newDsAcq = moveAcquisition(ds.getDatasetAcquisition(), newDs, studyId, targetSubjectRef, examMap, userId);
                    }
                }
                // Create the DatasetExpression for the new Dataset
                newDs.setDatasetAcquisition(newDsAcq);
                List<DatasetExpression> dexpList = new ArrayList<>(ds.getDatasetExpressions().size());
                Map<Long, DatasetExpression> oldToNewDsExp = new LinkedHashMap<>();
                for (DatasetExpression dexp : ds.getDatasetExpressions()) {
                    DatasetExpression newDexp = new DatasetExpression(dexp, newDs, false);
                    dexpList.add(newDexp);
                    oldToNewDsExp.put(dexp.getId(), newDexp);
                }
                newDs.setDatasetExpressions(dexpList);
                // Set dataset.subjectId
                newDs.setSubjectId(newDs.getDatasetAcquisition().getExamination().getSubject().getId());
                // Save dataset without files to get an id for the dataset expressions
                datasetRepository.saveAndFlush(newDs);
                result.setNewDsId(newDs.getId());
                // Copy dataset files in a super fast single query for each dataset expression
                for (DatasetExpression dexp : ds.getDatasetExpressions()) {
                    DatasetExpression newDexp = oldToNewDsExp.get(dexp.getId());
                    datasetFileRepository.copyDatasetFiles(dexp.getId(), newDexp.getId());
                }
                acqMap.put(oldAcqId, newDsAcq);
                result.incrementSuccess();
                if (newDsAcq != null && newDsAcq.getExamination() != null) {
                    result.setExaminationId(newDsAcq.getExamination().getId());
                    result.setCenterId(newDsAcq.getExamination().getCenterId());
                }
                result.setSubjectId(targetSubject.getId());
        // userId,
        // newExamination.getCenterId()
        // (newExamination.getSubject() != null ? newExamination.getSubject().getId() : null),
            } else if (ds.getDatasetProcessing() != null) {
                LOG.error("[CopyDatasets] Dataset selected is a processed dataset, it can't be copied.");
                result.incrementProcessed();
            }

            return result;
        } catch (Exception e) {
            LOG.error("[CopyDatasets] Error during the copy of dataset [" + dsId + "] to study [" + studyId + "].");
            throw e;
        }
    }

    // Save dataset and dataset files in batch to avoid memory overflow
    @Transactional
    public void saveDatasetWithDatasetFileBatch(Dataset dataset) {
        List<DatasetExpression> datasetExpressions = dataset.getDatasetExpressions(); // save list
        dataset.setDatasetExpressions(List.of()); // empty it
        Dataset savedDataset = datasetRepository.save(dataset); // save dataset without dataset expressions
        for (DatasetExpression dexp : datasetExpressions) { // for each dataset expression
            dexp.setDataset(savedDataset); // attach the saved dataset to the dataset expression
            dexp.setDatasetFiles(List.of()); // empty the list of dataset files
            DatasetExpression savedDexp = datasetExpressionRepository.save(dexp); // save the dataset expression without dataset files
            datasetFileRepository.copyDatasetFiles(dexp.getId(), savedDexp.getId()); // copy dataset files in a super fast single query
        }
    }

    private DatasetAcquisition moveAcquisition(DatasetAcquisition oldAcq, Dataset newDs, Long studyId,
            Subject targetSubject, Map<Long, Examination> examMap, Long userId) {
        Examination newExam = null;
        // Get existing examination...
        if (oldAcq.getExamination() != null &&  oldAcq.getExamination().getId() != null) {
            if (examMap.get(oldAcq.getExamination().getId()) != null) {
                newExam = examMap.get(oldAcq.getExamination().getId());
            }  else {
                newExam = examinationRepository.findBySourceIdAndStudy_Id(oldAcq.getExamination().getId(), studyId);
            }
            if (newExam == null) {
                newExam = moveExamination(oldAcq, studyId, targetSubject, userId);
            }
        }
        // Create new DatasetAcquisition according to its type
        DatasetAcquisition newDsAcq = null;
        if ("Mr".equals(oldAcq.getType())) {
            newDsAcq = new MrDatasetAcquisition(oldAcq, (MrDataset) newDs);
        } else {
            newDsAcq = DatasetAcquisitionUtils.copyDatasetAcquisitionFromDatasetAcquisition(oldAcq);
        }

        oldAcq.getCopies().add(newDsAcq);
        newDsAcq.setExamination(newExam);
        newDsAcq.setCopies(new ArrayList<>());
        newDsAcq.setSource(oldAcq);

        datasetAcquisitionRepository.save(newDsAcq);
        examMap.put(oldAcq.getExamination().getId(), newExam);
        LOG.info("[CopyDatasets] New dataset acquisition created with id = " + newDsAcq.getId());
        return newDsAcq;
    }

    private Examination moveExamination(DatasetAcquisition acq, Long studyId, Subject targetSubject, Long userId) {
        Examination oldExam = acq.getExamination();
        Study newStudy = studyRepository.getReferenceById(studyId);
        Examination newExamination = new Examination(oldExam, newStudy, targetSubject);
        oldExam.getCopies().add(newExamination);
        newExamination.setSource(oldExam);
        newExamination.setCopies(new ArrayList<>());
        examinationRepository.save(newExamination);
        LOG.info("[CopyDatasets] New examination created with id = " + newExamination.getId());
        return newExamination;
    }

}
