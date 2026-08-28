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

package org.shanoir.ng.studycard.service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.dcm4che3.data.Tag;
import org.hibernate.Hibernate;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.service.DatasetAcquisitionService;
import org.shanoir.ng.download.AcquisitionAttributes;
import org.shanoir.ng.download.WADODownloaderService;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.*;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.quality.QualityTag;
import org.shanoir.ng.shared.repository.StudyRepository;
import org.shanoir.ng.shared.validation.UniqueConstraintManager;
import org.shanoir.ng.studycard.dto.QualityCardResult;
import org.shanoir.ng.studycard.dto.QualityCardResultEntry;
import org.shanoir.ng.studycard.model.QualityCard;
import org.shanoir.ng.studycard.model.condition.AcqDICOMConditionOnDatasets;
import org.shanoir.ng.studycard.model.condition.AcqMetadataCondOnAcq;
import org.shanoir.ng.studycard.model.condition.AcqMetadataCondOnDatasets;
import org.shanoir.ng.studycard.model.condition.CardCondition;
import org.shanoir.ng.studycard.model.rule.ConditionComparator;
import org.shanoir.ng.studycard.model.rule.ConditionResult;
import org.shanoir.ng.studycard.model.rule.QualityCardRule;
import org.shanoir.ng.studycard.repository.QualityCardRepository;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.client.RestClientException;

/**
 * Study Card service implementation.
 *
 * @author msimon
 *
 */
@Service
public class QualityCardServiceImpl implements QualityCardService {

    private static final Logger LOG = LoggerFactory.getLogger(QualityCardServiceImpl.class);

    @Autowired
    private QualityCardRepository qualityCardRepository;

    @Autowired
    private UniqueConstraintManager<QualityCard> uniqueConstraintManager;

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private DatasetAcquisitionService datasetAcquisitionService;

    @Autowired
    private WADODownloaderService downloader;

    @Autowired
    private ShanoirEventService eventService;

    @Autowired
    private QualityCardServiceImpl self;

    @Transactional
    public void update(final QualityCard card) throws EntityNotFoundException {
        QualityCard qualityCardDb = qualityCardRepository.findById(card.getId()).orElse(null);
        if (qualityCardDb == null) throw new EntityNotFoundException(QualityCard.class, card.getId());
        updateQualityCardValues(qualityCardDb, card);
        qualityCardRepository.save(qualityCardDb);
    }

    public void validate(QualityCard qualityCard, BindingResult result) throws RestServiceException {
        final FieldErrorMap errors = new FieldErrorMap()
                .add(new FieldErrorMap(result))
                .add(uniqueConstraintManager.validate(qualityCard));
        if (!errors.isEmpty()) {
            ErrorModel error = new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors));
            throw new RestServiceException(error);
        }
    }

    public QualityCardResult checkQuality(DatasetAcquisition datasetAcquisition, AcquisitionAttributes<?> acquisitionAttributes, List<QualityCard> qualityCards) throws ShanoirException {
        QualityCardResult qualityResult = new QualityCardResult();
        for (QualityCard qualityCard : qualityCards) {
            // In case multiple quality cards are used with different roles, we check them all
            qualityResult.merge(applyQualityCardOnAcquisition(qualityCard, datasetAcquisition, acquisitionAttributes));
            LOG.info("Quality Card {} applied on dataset acquisition {} with result: {}.", qualityCard.getName(),
                    acquisitionAttributes.getFirstDatasetAttributes().getString(Tag.SeriesDescription), datasetAcquisition.getQualityTag());
        }
        return qualityResult;
    }

    private void resetDatasetAcquisitions(List<DatasetAcquisition> datasetAcquisitions) {
        if (datasetAcquisitions != null) {
            for (DatasetAcquisition datasetAcquisition : datasetAcquisitions) {
                datasetAcquisition.setQualityTag(null);
            }
        }
    }

    private void updateQualityCardValues(final QualityCard qualityCardDb, final QualityCard qualityCard) {
        qualityCardDb.setName(qualityCard.getName());
        qualityCardDb.setId(qualityCard.getId());
        qualityCardDb.setStudyId(qualityCard.getStudyId());
        qualityCardDb.setToCheckAtImport(qualityCard.isToCheckAtImport());
        if (qualityCardDb.getRules() == null) qualityCardDb.setRules(new ArrayList<QualityCardRule>());
        else qualityCardDb.getRules().clear();
        if (qualityCard.getRules() != null) {
            resetIdsForFreshInsert(qualityCard.getRules());
            qualityCardDb.getRules().addAll(qualityCard.getRules());
        }
    }

    @Override
    public void resetIdsForFreshInsert(QualityCard qualityCard) {
        if (qualityCard.getRules() != null) {
            resetIdsForFreshInsert(qualityCard.getRules());
        }
    }

    /**
     * Due to the way the cards are saved, we need to (AbstractEntity.id -> @GeneratedValue(strategy = GenerationType.IDENTITY))
     * we need to reset the id at update
     */
    private void resetIdsForFreshInsert(List<QualityCardRule> rules) {
        for (QualityCardRule rule : rules) {
            rule.setId(null);
            if (rule.getConditions() != null) {
                for (CardCondition condition : rule.getConditions()) {
                    condition.setId(null);
                }
            }
        }
    }

    /**
     * Quality Cards : apply on one DICOM serie/acquisition.
     */
    private QualityCardResult applyQualityCardOnDatasetAcquisition(QualityCard qualityCard, DatasetAcquisition acquisition) throws PacsException {
        long startTs = new Date().getTime();
        AcquisitionAttributes<Long> dicomAttributes = null;
        QualityCardResult result = new QualityCardResult();

        if (qualityCard == null)
            throw new IllegalArgumentException("qualityCard can't be null");
        if (acquisition == null)
            throw new IllegalArgumentException("dataset acquisition can't be null");
        LOG.debug("Quality check for dataset acquisition " + acquisition.getId() + " started");

        if (CollectionUtils.isEmpty(qualityCard.getRules())) {
            throw new RestClientException("Quality card used with empty rules.");
        }

        LOG.debug(qualityCard.getRules().size() + " rules found for quality card with id: " + qualityCard.getId()
                + " and name: " + qualityCard.getName());

        // We retrieve dicom attributes for this dataset acquisition/DICOM serie
        try {
            dicomAttributes = downloader.getDicomAttributesForAcquisition(acquisition);
            // in case of error during PACS communication, we log the error in the result entry and continue to apply quality card
        } catch (PacsException e) {
            LOG.error("Error during PACS communication while applying quality card on dataset acquisition " + acquisition.getId(), e);
            addErrorEntryForAcquisition(acquisition, result, e);
            return result;
        }

        result = applyQualityCardOnAcquisition(qualityCard, acquisition, dicomAttributes);

        LOG.debug("Quality check for acquisition " + acquisition.getId() + " finished in "
                + (new Date().getTime() - startTs) + " ms");

        return result;
    }

    /**
     * Apply a quality card's rules on a single dataset acquisition, given its already-fetched dicom attributes.
     * Used during import, when dicoms are present in tmp directory.
     */
    private QualityCardResult applyQualityCardOnAcquisition(QualityCard qualityCard, DatasetAcquisition acquisition, AcquisitionAttributes<?> dicomAttributes) throws PacsException {
        QualityCardResult result = new QualityCardResult();
        if (qualityCard.getRules() != null) {
            for (QualityCardRule rule : qualityCard.getRules()) {
                apply(rule, acquisition, dicomAttributes, result, downloader);
            }
        }
        return result;
    }

    private void loadRulesLazyCollections(List<QualityCardRule> rules, ShanoirEvent event) {
        event.setMessage("Loading rules");
        event.setProgress(0.5f);
        eventService.publishEvent(event);
        if (rules != null) {
            for (QualityCardRule rule : rules) {
                if (rule.getConditions() != null) {
                    for (CardCondition condition : rule.getConditions()) {
                        Hibernate.initialize(condition.getValues());
                    }
                }
            }
        }
    }

    private void addErrorEntryForAcquisition(DatasetAcquisition acquisition, QualityCardResult result, Exception e) {
        QualityCardResultEntry errorEntry = new QualityCardResultEntry();
        errorEntry.setSubjectName(acquisition.getExamination().getSubject() != null ? acquisition.getExamination().getSubject().getName() : null);
        errorEntry.setDatasetAcquisitionId(acquisition.getId());
        errorEntry.setExaminationDate(acquisition.getExamination().getExaminationDate());
        errorEntry.setExaminationComment(acquisition.getExamination().getComment());
        errorEntry.setMessage("Error during PACS communication: " + e.getCause().getMessage());
        result.add(errorEntry);
    }

    public QualityCardResult applyQualityCardOnStudy(QualityCard qualityCard, boolean updateTags) throws PacsException {
        long startTs = new Date().getTime();
        if (qualityCard == null)
            throw new IllegalArgumentException("qualityCard can't be null");
        ShanoirEvent event = new ShanoirEvent(ShanoirEventType.CHECK_QUALITY_EVENT, null, KeycloakUtil.getTokenUserId(),
                "Quality check started on study " + qualityCard.getStudyId(), 4, qualityCard.getStudyId());
        eventService.publishEvent(event);
        Study study = studyRepository.findByIdWithDatasetsAndDatasetFilePaths(qualityCard.getStudyId()).orElse(null);
        if (study == null)
            throw new IllegalArgumentException("study can't be null");
        if (!Objects.equals(qualityCard.getStudyId(), study.getId()))
            throw new IllegalStateException("study and qualityCard study ids don't match");

        if (CollectionUtils.isEmpty(qualityCard.getRules())) {
            event.setStatus(-1);
            event.setMessage("Quality card used with empty rules.");
            event.setProgress(1f);
            eventService.publishEvent(event);
            throw new RestClientException("Quality card used with empty rules.");
        }

        // Load lazy data
        loadRulesLazyCollections(qualityCard.getRules(), event);

        List<Examination> examinations = study.getExaminations();

        QualityCardResult result = new QualityCardResult();
        AtomicInteger examinationIndex = new AtomicInteger(0);
        int totalExaminations = examinations.size();

        for (Examination examination : examinations) {
            event.setMessage("Processing examination " + examination.getComment());
            event.setProgress(examinationIndex.floatValue() / totalExaminations);
            eventService.publishEvent(event);

            // We only load the DatasetAcquisitions from one examination at a time
            List<DatasetAcquisition> datasetAcquisitions = examination.getDatasetAcquisitions();

            if (updateTags) {
                resetDatasetAcquisitions(datasetAcquisitions);
            }
            // We apply the quality card on DatasetAcquisitions for one
            // examination only
            List<DatasetAcquisition> updatedAcquisitions = new ArrayList<>();
            try {
                datasetAcquisitions.stream().forEach(datasetAcquisition -> {
                    event.setStatus(2);
                    event.setMessage("Checking quality for acquisition " + datasetAcquisition.getId()
                            + " in examination " + examination.getComment());
                    eventService.publishEvent(event);
                    try {
                        QualityCardResult acquisitionResult = applyQualityCardOnDatasetAcquisition(
                                qualityCard, datasetAcquisition);
                        result.merge(acquisitionResult);
                        updatedAcquisitions.addAll(acquisitionResult.getUpdatedDatasetAcquisitions());
                    } catch (PacsException e) {
                        throw new StreamExceptionWrapper(e);
                    }
                });
            } catch (StreamExceptionWrapper e) {
                throw (PacsException) (e.getCause());
            }
            if (updateTags && !updatedAcquisitions.isEmpty()) {
                try {
                    datasetAcquisitionService.update(updatedAcquisitions);
                } catch (EntityNotFoundException e) {
                    throw new IllegalStateException(
                            "Could not update dataset acquisitions for examination " + examination.getComment(), e);
                }
            }
            datasetAcquisitions.clear();
            updatedAcquisitions.clear();
            examinationIndex.incrementAndGet();
        }
        event.setProgress(1f);
        event.setStatus(1);
        event.setMessage("Quality card applied on study " + study.getName() + " in " + (new Date().getTime() - startTs)
                + " ms.");
        event.setReport(result.toString());
        eventService.publishEvent(event);
        return result;
    }

    /**
     *
     * @param acquisitionDicomAttributes if null conditions will be checked on the
     *                                   acquisition data and dicom data will be
     *                                   fetched from pacs.
     *                                   Else conditions will be checked on the
     *                                   looping on the given dicom attributes
     * @param datasetAcquisition
     * @param result
     * @param downloader
     * @throws PacsException
     */
    private void apply(QualityCardRule rule, DatasetAcquisition datasetAcquisition, AcquisitionAttributes<?> acquisitionDicomAttributes,
                      QualityCardResult result, WADODownloaderService downloader) throws PacsException {
        // if applied at import and not from ShUp then acquisitionDicomAttributes should
        // not be null, otherwise we fetch DICOM acquisition attributes.
        if (acquisitionDicomAttributes == null) {
            acquisitionDicomAttributes = downloader.getDicomAttributesForAcquisition(datasetAcquisition);
        }

        // In case a rule was added without condition (= set as Always in gui)
        if (rule.getConditions() == null || rule.getConditions().isEmpty()) {
            // Several concurrently applicable rules must never let evaluation order decide the
            // final tag: only apply this rule's tag if it's at least as severe as whatever is
            // already set (ERROR > WARNING > VALID wins regardless of rule order).
            boolean applied = rule.getQualityTag().isMoreSevereThan(datasetAcquisition.getQualityTag());
            QualityCardResultEntry resultEntry = initResult(datasetAcquisition);
            resultEntry.setTagSet(rule.getQualityTag());
            if (applied) {
                resultEntry.setMessage("Tag " + rule.getQualityTag().name() + " was set by the quality card rule without any condition.");
                datasetAcquisition.setQualityTag(rule.getQualityTag());
                result.addUpdatedDatasetAcquisition(datasetAcquisition);
            } else {
                resultEntry.setMessage("Tag " + rule.getQualityTag().name() + " was not set by the quality card rule without any condition, "
                        + "because the more severe tag " + datasetAcquisition.getQualityTag().name() + " already applies.");
            }
            result.add(resultEntry);
        } else {
            ConditionResult conditionResult = conditionsfulfilled(rule, acquisitionDicomAttributes, datasetAcquisition);
            boolean applied = conditionResult.isFulfilled() && rule.getQualityTag().isMoreSevereThan(datasetAcquisition.getQualityTag());
            if (applied) {
                datasetAcquisition.setQualityTag(rule.getQualityTag());
                result.addUpdatedDatasetAcquisition(datasetAcquisition);
            }
            // if conditions not fulfilled for a VALID tag
            // or if conditions fulfilled for an ERROR or WARNING tag
            // then add an entry to the report
            if ((conditionResult.isFulfilled() && !rule.getQualityTag().equals(QualityTag.VALID))
                    || (!conditionResult.isFulfilled() && rule.getQualityTag().equals(QualityTag.VALID))) {
                QualityCardResultEntry resultEntry = initResult(datasetAcquisition);
                resultEntry.setFailedValid(QualityTag.VALID.equals(rule.getQualityTag()) && !conditionResult.isFulfilled());
                resultEntry.setTagSet(rule.getQualityTag());
                // Here we use the seriesDescription attribute or the dataset acquisition ID to clearly identify the dataset acquisition concerned by the quality card result message.
                String seriesDescription = acquisitionDicomAttributes.getFirstDatasetAttributes() != null ? acquisitionDicomAttributes.getFirstDatasetAttributes().getString(Tag.SeriesDescription) : datasetAcquisition.getId().toString();
                if (conditionResult.isFulfilled() && applied) {
                    resultEntry.setMessage("Tag " + rule.getQualityTag().name() + " was set on acquisition " + seriesDescription
                            + " because those conditions were fulfilled : " + StringUtils.join(conditionResult.getFulfilledConditionsMsgList(), ", "));
                } else if (conditionResult.isFulfilled()) {
                    resultEntry.setMessage("Tag " + rule.getQualityTag().name() + " conditions were fulfilled on acquisition " + seriesDescription
                            + " but the more severe tag " + datasetAcquisition.getQualityTag().name() + " already applies : "
                            + StringUtils.join(conditionResult.getFulfilledConditionsMsgList(), ", "));
                } else {
                    resultEntry.setMessage("Tag " + rule.getQualityTag().name() + " could not be set on acquisition " + seriesDescription
                            + " because those conditions failed : " + StringUtils.join(conditionResult.getUnfulfilledConditionsMsgList(), ", "));
                }
                result.add(resultEntry);
            }
        }
    }

    /**
     * init a line for the quality card result test grid
     *
     * @param datasetAcquisition
     */
    private QualityCardResultEntry initResult(DatasetAcquisition datasetAcquisition) {
        QualityCardResultEntry result = new QualityCardResultEntry();
        result.setSubjectName(datasetAcquisition.getExamination().getSubject() != null ? datasetAcquisition.getExamination().getSubject().getName() : null);
        result.setDatasetAcquisitionId(datasetAcquisition.getId());
        result.setExaminationDate(datasetAcquisition.getExamination().getExaminationDate());
        result.setExaminationComment(datasetAcquisition.getExamination().getComment());
        return result;
    }

    /**
     * Update the quality card result test line with the result of the apply try
     *
     * @param rule the tested rule of the qualityCard
     * @param dicomAttributes if null conditions will be checked on the examination data and dicom data will be fetched from pacs.
     * Else conditions will be checked on the looping on the given dicom attributes
     * @param da the tested datasetAcquisition
     *
     * @return the updated quality card result test line
     */
    private ConditionResult conditionsfulfilled(QualityCardRule rule, AcquisitionAttributes<?> dicomAttributes, DatasetAcquisition da) {
        boolean allFulfilled = true;
        ConditionResult condResult = new ConditionResult();
        Collections.sort(rule.getConditions(), new ConditionComparator()); // sort by level
        for (CardCondition condition : rule.getConditions()) {
            StringBuffer msg = new StringBuffer();
            boolean fulfilled = true;
            if (condition instanceof AcqDICOMConditionOnDatasets) {
                fulfilled = ((AcqDICOMConditionOnDatasets) condition).fulfilled(dicomAttributes, msg);
            } else if (condition instanceof AcqMetadataCondOnAcq) {
                fulfilled = ((AcqMetadataCondOnAcq) condition).fulfilled(da, msg);
            } else if (condition instanceof AcqMetadataCondOnDatasets) {
                fulfilled = ((AcqMetadataCondOnDatasets) condition).fulfilled(da.getDatasets(), msg);
            } else {
                throw new IllegalStateException("There might be an unimplemented condition type here. Condition class : " + condition.getClass());
            }

            if (fulfilled) {
                condResult.addFulfilledConditionsMsg(msg.toString());
            } else {
                condResult.addUnfulfilledConditionsMsg(msg.toString());
            }

            if (rule.isOrConditions() && fulfilled) {
                allFulfilled = true;
                break;
            } else {
                allFulfilled &= fulfilled;
            }
        }
        condResult.setFulfilled(allFulfilled);
        return condResult;
    }


}
