package org.shanoir.ng.dataset.service;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.EntityOrigin;
import org.shanoir.ng.dataset.repository.DatasetRepository;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.MrDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.repository.DatasetAcquisitionRepository;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.model.*;
import org.shanoir.ng.shared.repository.SubjectRepository;
import org.shanoir.ng.shared.service.StudyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.shanoir.ng.dataset.model.DatasetType;


@Service
public class DatasetCopyServiceImpl implements DatasetCopyService {

    @Autowired
    private StudyService studyService;
    @Autowired
    private DatasetAcquisitionRepository datasetAcquisitionRepository;
    @Autowired
    private ExaminationRepository examinationRepository;
    @Autowired
    private DatasetRepository datasetRepository;
    @Autowired
    private SubjectRepository subjectRepository;
    @Autowired
    ShanoirEventService eventService;

    private static final Logger LOG = LoggerFactory.getLogger(DatasetCopyServiceImpl.class);

    @Override
    public Object[] moveDataset(Dataset ds, Long studyId, Map<Long, Examination> examMap, Map<Long, DatasetAcquisition> acqMap, Long userId) throws JsonProcessingException {
        try {
            int countProcessed = 0;
            int countSuccess = 0;
            Long oldDsId = ds.getId();
            LOG.info("[CopyDatasets] moveDataset : " + oldDsId + " to study : " + studyId);

            // Creation of new dataset according to its type
            DatasetType dsType = ds.getType();
            Dataset newDs = null;
            if (ds.getDatasetAcquisition() != null &&  ds.getDatasetAcquisition().getId() != null) {
                if (DatasetType.Mr.equals(dsType)) {
                    newDs = new MrDataset(ds);
                } else {
                    newDs = DatasetUtils.copyDatasetFromDataset(ds);
                }
                ds.getCopies().add(newDs);
                ds.setOrigin(EntityOrigin.SOURCE);
                newDs.setSource(ds);
                newDs.setOrigin(EntityOrigin.COPY);
                newDs.setSubjectId(ds.getSubjectId());

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
                        newDsAcq = moveAcquisition(ds.getDatasetAcquisition(), newDs, studyId, examMap, userId);
                    }
                }
                // Create the DatasetExpression for the new Dataset
                newDs.setDatasetAcquisition(newDsAcq);
                List<DatasetExpression> dexpList = new ArrayList<>(ds.getDatasetExpressions().size());
                for (DatasetExpression dexp : ds.getDatasetExpressions()) {
                    dexpList.add(new DatasetExpression(dexp, newDs));
                }
                newDs.setDatasetExpressions(dexpList);


                datasetRepository.save(newDs);
                acqMap.put(oldAcqId, newDsAcq);
                countSuccess++;

            } else if (ds.getDatasetProcessing() != null) {
                LOG.error("[CopyDatasets] Dataset selected is a processed dataset, it can't be copied.");
                countProcessed++;
            }

            return new Object[]{newDs != null ? newDs.getId() : null, countProcessed, countSuccess};
        } catch (Exception e) {
            LOG.error("[CopyDatasets] Error during the copy of dataset [" + ds.getId() + "] to study [" + studyId + "].");
            throw e;
        }
    }

    public DatasetAcquisition moveAcquisition(DatasetAcquisition oldAcq, Dataset newDs, Long studyId, Map<Long, Examination> examMap, Long userId) {
        Examination newExam = null;
        // Get existing examination...
        if (oldAcq.getExamination() != null &&  oldAcq.getExamination().getId() != null) {
            if (examMap.get(oldAcq.getExamination().getId()) != null) {
                newExam = examMap.get(oldAcq.getExamination().getId());
            }  else {
                newExam = examinationRepository.findBySourceIdAndStudy_Id(oldAcq.getExamination().getId(), studyId);
            }
            if (newExam == null) {
                newExam = moveExamination(oldAcq, studyId, userId);
            }
        }
        // Create new DatasetAcquisition according to its type
        DatasetAcquisition newDsAcq = null;
        if ("Mr".equals(oldAcq.getType())) {
            newDsAcq = new MrDatasetAcquisition(oldAcq, (MrDataset) newDs);
        } else {
            newDsAcq = DatasetAcquisitionUtils.copyDatasetAcquisitionFromDatasetAcquisition(oldAcq);
        }

        oldAcq.setOrigin(EntityOrigin.SOURCE);
        oldAcq.getCopies().add(newDsAcq);
        newDsAcq.setOrigin(EntityOrigin.COPY);
        newDsAcq.setExamination(newExam);
        newDsAcq.setSource(oldAcq);

        datasetAcquisitionRepository.save(newDsAcq);
        examMap.put(oldAcq.getExamination().getId(), newExam);
        LOG.info("[CopyDatasets] New dataset acquisition created with id = " + newDsAcq.getId());
        return newDsAcq;
    }

    public Examination moveExamination(DatasetAcquisition acq, Long studyId, Long userId) {
        Examination oldExam = acq.getExamination();
        Study newStudy = studyService.findById(studyId);
        Subject subject = subjectRepository.findById(oldExam.getSubject().getId()).orElse(null);

        Examination newExamination = new Examination(oldExam, newStudy, subject);

        oldExam.setOrigin(EntityOrigin.SOURCE);
        oldExam.getCopies().add(newExamination);
        newExamination.setSource(oldExam);
        newExamination.setOrigin(EntityOrigin.COPY);

        examinationRepository.save(newExamination);
        eventService.publishEvent(
                new ShanoirEvent(
                        ShanoirEventType.CREATE_EXAMINATION_EVENT,
                        newExamination.getId().toString(),
                        userId,
                        "centerId:" + newExamination.getCenterId() + ";subjectId:" + (newExamination.getSubject() != null ? newExamination.getSubject().getId() : null),
                        ShanoirEvent.SUCCESS,
                        newExamination.getStudyId()));

        LOG.info("[CopyDatasets] New examination created with id = " + newExamination.getId());
        return newExamination;
    }
}