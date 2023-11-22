package org.shanoir.ng.dataset.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.shanoir.ng.dataset.modality.EegDataset;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.repository.DatasetRepository;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.ct.CtDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.MrDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.pet.PetDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.repository.DatasetAcquisitionRepository;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.eeg.model.Channel;
import org.shanoir.ng.eeg.model.Event;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.shared.model.*;
import org.shanoir.ng.shared.service.StudyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;


@Service
public class DatasetCopyServiceImpl implements DatasetCopyService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ExaminationService examinationService;
    @Autowired
    private StudyService studyService;
    @Autowired
    private DatasetAcquisitionRepository datasetAcquisitionRepository;
    @Autowired
    private ExaminationRepository examinationRepository;

    @Autowired
    private DatasetRepository datasetRepository;

    private static final Logger LOG = LoggerFactory.getLogger(DatasetCopyServiceImpl.class);

    @Override
    public void moveDataset(Dataset ds, Long studyId, Map<Long, Examination> examMap, Map<Long, DatasetAcquisition> acqMap) {
        try {
            Long oldDsId = ds.getId();
            DatasetAcquisition newAcq = null;
            if (ds.getDatasetAcquisition() != null &&  ds.getDatasetAcquisition().getId() != null) {
                // Check for known DatasetAcquisition in map...
                Long oldAcqId = ds.getDatasetAcquisition().getId();
                if (acqMap.get(ds.getDatasetAcquisition().getId()) != null) {
                    newAcq = acqMap.get(ds.getDatasetAcquisition().getId());
                } else {
                    // ... then in source ...
                    newAcq = datasetAcquisitionRepository.findBySourceIdAndExaminationStudy_Id(oldAcqId, studyId);

                }
                if (newAcq == null) {
                    // ... creating the DatasetAcquisition in case it doesn't exist yet
                    newAcq = moveAcquisition(ds.getDatasetAcquisition(), studyId, examMap);
                }

                List<DatasetExpression> dsExList = ds.getDatasetExpressions();
                datasetCleanup(ds);

                for (DatasetExpression dsEx : dsExList) {
                    this.moveDatasetExpression(dsEx, ds);
                }

                ds.setSubjectId(ds.getSubjectId());
                ds.setId(null);
                entityManager.detach(ds);
                Dataset newDs = datasetRepository.save(ds);
                newDs.setDatasetAcquisition(newAcq);
                newDs.setSourceId(oldDsId);
                entityManager.flush();
                acqMap.put(oldAcqId, newAcq);
                LOG.warn("[CopyDatasets] New dataset created with id = " + newDs.getId());

            } else if (ds.getDatasetProcessing() != null) {
                LOG.error("[CopyDatasets] Dataset selected is a processed dataset, it can't be copied.");
            }
        } catch (Exception e) {
            LOG.error("[CopyDatasets] Error in the Dataset service", e);
            throw e;
        }
    }

    public DatasetAcquisition moveAcquisition(DatasetAcquisition acq, Long studyId, Map<Long, Examination> examMap) {
        Long oldAcqId = acq.getId();
        Examination newExam = null;
        if (acq.getExamination() != null &&  acq.getExamination().getId() != null) {
            if (examMap.get(acq.getExamination().getId()) != null) {
                newExam = examMap.get(acq.getExamination().getId());
            } else {
                newExam = examinationRepository.findBySourceIdAndStudy_Id(acq.getExamination().getId(), studyId);
            }
            if (newExam == null) {
                newExam = moveExamination(acq.getExamination(), studyId);
            }
        }
        acq.setDatasets(null);
        acquisitionCleanup(acq);

        acq.setId(null);
        entityManager.detach(acq);
        DatasetAcquisition newAcquisition = datasetAcquisitionRepository.save(acq);
        newAcquisition.setExamination(newExam);
        newAcquisition.setSourceId(oldAcqId);
        examMap.put(acq.getExamination().getId(), newExam);
        LOG.warn("[CopyDatasets] New dataset acquisition created with id = " + newAcquisition.getId());
        return newAcquisition;
    }

    public Examination moveExamination(Examination examination, Long studyId) {
        Long oldExamId = examination.getId();

        examination.setDatasetAcquisitions(null);
        examination.setExtraDataFilePathList(null);
        examination.setInstrumentBasedAssessmentList(null);
        examination.setStudy(studyService.findById(studyId));
        examination.setId(null);
        entityManager.detach(examination);
        Examination newExamination = examinationService.save(examination);
        newExamination.setSourceId(oldExamId);
        LOG.warn("[CopyDatasets] New examination created with id = " + newExamination.getId());
        return newExamination;
    }

    public void moveDatasetExpression(DatasetExpression expression, Dataset dataset) {
        for (DatasetFile file : expression.getDatasetFiles())
            this.moveFile(file, expression);

        expression.setDataset(dataset);
        expression.setId(null);
        entityManager.detach(expression);
    }

    public void moveFile(DatasetFile file, DatasetExpression expression) {
        file.setDatasetExpression(expression);
        file.setId(null);
        entityManager.detach(file);
    }

    private void datasetCleanup(Dataset ds) {
        if ("Mr".equals(ds.getType())) {
            MrDataset mrDs = (MrDataset) ds;
            if (!CollectionUtils.isEmpty(mrDs.getFlipAngle())) {
                for (FlipAngle element : mrDs.getFlipAngle()) {
                    element.setId(null);
                    element.setMrDataset(mrDs);
                }
            }
            if (mrDs.getDatasetProcessing() != null) {
                mrDs.getDatasetProcessing().setId(null);
            }
            if (mrDs.getOriginMrMetadata() != null) {
                mrDs.getOriginMrMetadata().setId(null);
            }
            if (mrDs.getUpdatedMrMetadata() != null) {
                mrDs.getUpdatedMrMetadata().setId(null);
            }
            if (!CollectionUtils.isEmpty(mrDs.getDiffusionGradients())) {
                for (DiffusionGradient element : mrDs.getDiffusionGradients()) {
                    entityManager.detach(element);
                    element.setId(null);
                    element.setMrDataset(mrDs);
                }
            }
            if (!CollectionUtils.isEmpty(mrDs.getEchoTime())) {
                for (EchoTime element : mrDs.getEchoTime()) {
                    element.setId(null);
                    element.setMrDataset(mrDs);
                }
            }
            if (!CollectionUtils.isEmpty(mrDs.getInversionTime())) {
                for (InversionTime element : mrDs.getInversionTime()) {
                    element.setId(null);
                    element.setMrDataset(mrDs);
                }
            }
            if (!CollectionUtils.isEmpty(mrDs.getRepetitionTime())) {
                for (RepetitionTime element : mrDs.getRepetitionTime()) {
                    element.setId(null);
                    element.setMrDataset(mrDs);
                }
            }
        } else if ("Eeg".equals(ds.getType())) {
            EegDataset eegDs = (EegDataset) ds;
            if (!CollectionUtils.isEmpty(eegDs.getChannels())) {
                for (Channel ch : eegDs.getChannels()) {
                    ch.setId(null);
                    ch.setDataset(eegDs);
                }
            }
            if (!CollectionUtils.isEmpty(eegDs.getEvents())) {
                for (Event ev : eegDs.getEvents()) {
                    ev.setId(null);
                    ev.setDataset(eegDs);
                }
            }
        }
        if (ds.getOriginMetadata() != null) {
            ds.getOriginMetadata().setId(null);
        }
        if (ds.getUpdatedMetadata() != null) {
            ds.getUpdatedMetadata().setId(null);
        }
    }

    private void acquisitionCleanup(DatasetAcquisition acq) {
        switch (acq.getType()) {
            case "Mr":
                MrDatasetAcquisition mrAcq = (MrDatasetAcquisition) acq;

                if (mrAcq.getMrProtocol() != null) {
                    mrAcq.getMrProtocol().setId(null);

                    if (mrAcq.getMrProtocol().getDiffusionGradients() != null) {
                        for (DiffusionGradient element : mrAcq.getMrProtocol().getDiffusionGradients()) {
                            element.setId(null);
                            element.setMrProtocol(mrAcq.getMrProtocol());
                        }
                    }
                    if (mrAcq.getMrProtocol().getOriginMetadata() != null) {
                        mrAcq.getMrProtocol().getOriginMetadata().setId(null);
                    }
                    if (mrAcq.getMrProtocol().getUpdatedMetadata() != null) {
                        mrAcq.getMrProtocol().getUpdatedMetadata().setId(null);
                    }
                }
                mrAcq.setId(null);
                break;
            case "Ct":
                CtDatasetAcquisition ctAcq = (CtDatasetAcquisition) acq;
                if (ctAcq.getCtProtocol() != null) {
                    ctAcq.getCtProtocol().setId(null);
                }
                ctAcq.setId(null);
                break;
            case "Pet":
                PetDatasetAcquisition petAcq = (PetDatasetAcquisition) acq;
                if (petAcq.getPetProtocol() != null) {
                    petAcq.getPetProtocol().setId(null);
                }
                petAcq.setId(null);
                break;
            default:
                // Do nothing for others, no specific objets to migrate
                break;
        }
    }
}