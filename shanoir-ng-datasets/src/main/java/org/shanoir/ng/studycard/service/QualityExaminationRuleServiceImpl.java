package org.shanoir.ng.studycard.service;

import org.apache.commons.lang3.StringUtils;
import org.shanoir.ng.download.ExaminationAttributes;
import org.shanoir.ng.download.WADODownloaderService;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.shared.model.Subject;
import org.shanoir.ng.shared.quality.QualityTag;
import org.shanoir.ng.studycard.dto.QualityCardResult;
import org.shanoir.ng.studycard.dto.QualityCardResultEntry;
import org.shanoir.ng.studycard.model.ExaminationData;
import org.shanoir.ng.studycard.model.condition.ExamMetadataCondOnAcq;
import org.shanoir.ng.studycard.model.condition.ExamMetadataCondOnDatasets;
import org.shanoir.ng.studycard.model.condition.StudyCardCondition;
import org.shanoir.ng.studycard.model.condition.StudyCardDICOMConditionOnDatasets;
import org.shanoir.ng.studycard.model.rule.QualityExaminationRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class QualityExaminationRuleServiceImpl implements QualityExaminationRuleService {

    public void apply(QualityExaminationRule qer, Examination examination, ExaminationAttributes<?> examinationDicomAttributes, QualityCardResult result, WADODownloaderService downloader) {
        ExaminationData examData = new ExaminationData(examination);
        if (examData.getSubjectId() == null) {
            Logger log = LoggerFactory.getLogger(QualityExaminationRule.class);
            log.warn("No subject in exam " + examination.getId());
        } else {
            apply(qer, examData, examinationDicomAttributes, result, downloader);
        }
    }

    public void apply(QualityExaminationRule qer, ExaminationData examination, ExaminationAttributes<?> examinationDicomAttributes, QualityCardResult result, WADODownloaderService downloader) {
        // In case a rule was added without condition (= set as Always in gui)
        if (qer.getConditions() == null || qer.getConditions().isEmpty()) {
            QualityCardResultEntry resultEntry = initResult(examination);
            resultEntry.setTagSet(qer.getQualityTag());
            resultEntry.setMessage("Tag " + qer.getQualityTag().name() + " was set by the quality card rule without any condition.");
            result.add(resultEntry);
            result.addUpdatedSubject(
                    setTagToSubject(qer, examination.getSubjectId()));
        } else {
            ConditionResult conditionResult = conditionsfulfilled(qer, examinationDicomAttributes, examination, downloader);
            if (conditionResult.isFulfilled()) {
                result.addUpdatedSubject(
                        setTagToSubject(qer, examination.getSubjectId()));
            }
            // if conditions not fulfilled for a VALID tag
            // or if conditions fulfilled for a ERROR or WARNING tag
            // then add an entry to the report
            if ((conditionResult.isFulfilled() && !qer.getQualityTag().equals(QualityTag.VALID))
                    || (!conditionResult.isFulfilled() && qer.getQualityTag().equals(QualityTag.VALID))) {
                QualityCardResultEntry resultEntry = initResult(examination);
                resultEntry.setFailedValid(QualityTag.VALID.equals(qer.getQualityTag()) && !conditionResult.isFulfilled());
                resultEntry.setTagSet(qer.getQualityTag());
                if (conditionResult.isFulfilled()) {
                    resultEntry.setMessage("Tag " + qer.getQualityTag().name() + " was set because those conditions were fulfilled : " + StringUtils.join(conditionResult.getFulfilledConditionsMsgList(), ", "));
                } else {
                    resultEntry.setMessage("Tag " + qer.getQualityTag().name() + " could not be set because those conditions failed : " + StringUtils.join(conditionResult.getUnfulfilledConditionsMsgList(), ", "));
                }
                result.add(resultEntry);
            }
        }
    }

    /**
     *
     * @return list of updated subject studies
     */
    private Subject setTagToSubject(QualityExaminationRule qer, Long subjectId) {
        // don't touch subjectStudy as we later will compare the original with the updated
        Subject subjectCopy = new Subject();
        subjectCopy.setId(subjectId);
        subjectCopy.setQualityTag(qer.getQualityTag());
        return subjectCopy;
    }

    /**
     *
     * @param dicomAttributes if null conditions will be checked on the examination data and dicom data will be fetched from pacs.
     * Else conditions will be checked on the looping on the given dicom attributes
     * @param examination
     * @return
     */
    private ConditionResult conditionsfulfilled(QualityExaminationRule qer, ExaminationAttributes<?> dicomAttributes, ExaminationData examination, WADODownloaderService downloader) {
        boolean allFulfilled = true;
        ConditionResult condResult = new ConditionResult();
        Collections.sort(qer.getConditions(), new ConditionComparator()); // sort by level
        boolean pilotedByDicomAttributes;
        ExaminationAttributes<Long> examinationAttributesCache = null;
        if (dicomAttributes != null) {
            pilotedByDicomAttributes = true;
        } else {
            pilotedByDicomAttributes = false;
            examinationAttributesCache = new ExaminationAttributes<>(downloader.getWadoURLHandler());
        }
        for (StudyCardCondition condition : qer.getConditions()) {
            StringBuffer msg = new StringBuffer();
            boolean fulfilled = true;
            if (condition instanceof StudyCardDICOMConditionOnDatasets) {
                if (pilotedByDicomAttributes) {
                    fulfilled = ((StudyCardDICOMConditionOnDatasets) condition).fulfilled(dicomAttributes, msg);
                } else {
                    fulfilled = ((StudyCardDICOMConditionOnDatasets) condition).fulfilled(examination.getDatasetAcquisitions(), examinationAttributesCache, downloader, msg);
                }
            } else if (condition instanceof ExamMetadataCondOnAcq) {
                fulfilled = ((ExamMetadataCondOnAcq) condition).fulfilled(examination.getDatasetAcquisitions(), msg);
            } else if (condition instanceof ExamMetadataCondOnDatasets) {
                fulfilled = ((ExamMetadataCondOnDatasets) condition).fulfilled(examination.getDatasetAcquisitions(), msg);
            } else {
                throw new IllegalStateException("There might be an unimplemented condition type here. Condition class : " + condition.getClass());
            }

            if (fulfilled) {
                condResult.addFulfilledConditionsMsg(msg.toString());
            } else {
                condResult.addUnfulfilledConditionsMsg(msg.toString());
            }

            if (qer.isOrConditions() && fulfilled) {
                allFulfilled = true;
                break;
            } else {
                allFulfilled &= fulfilled;
            }
        }
        condResult.setFulfilled(allFulfilled);
        return condResult;
    }

    private QualityCardResultEntry initResult(ExaminationData examination) {
        QualityCardResultEntry result = new QualityCardResultEntry();
        result.setSubjectName(examination.getSubjectName());
        result.setExaminationDate(examination.getExaminationDate());
        result.setExaminationComment(examination.getExaminationComment());
        return result;
    }

    public class ConditionResult {

        private boolean fulfilled;
        private List<String> fulfilledConditionsMsgList = new ArrayList<>();
        private List<String> unfulfilledConditionsMsgList = new ArrayList<>();

        public boolean isFulfilled() {
            return fulfilled;
        }

        public void setFulfilled(boolean fulfilled) {
            this.fulfilled = fulfilled;
        }

        public List<String> getFulfilledConditionsMsgList() {
            return fulfilledConditionsMsgList;
        }

        public void addFulfilledConditionsMsg(String msg) {
            this.fulfilledConditionsMsgList.add(msg);
        }

        public List<String> getUnfulfilledConditionsMsgList() {
            return unfulfilledConditionsMsgList;
        }

        public void addUnfulfilledConditionsMsg(String msg) {
            this.unfulfilledConditionsMsgList.add(msg);
        }
    }

    public class ConditionComparator implements Comparator<StudyCardCondition> {
        @Override
        public int compare(StudyCardCondition cond1, StudyCardCondition cond2) {
            return priority(cond1) - priority(cond2);
        }
        /**
         * the higher the priority, the higher is the returned number.
         */
        private int priority(StudyCardCondition condition) {
            if (condition instanceof StudyCardDICOMConditionOnDatasets) {
                return 1;
            } else if (condition instanceof ExamMetadataCondOnAcq) {
                return 3;
            } else if (condition instanceof ExamMetadataCondOnDatasets) {
                return 2;
            } else {
                return 0;
            }
        }
    }
}
