package org.shanoir.ng.studycard.service;

import org.shanoir.ng.download.ExaminationAttributes;
import org.shanoir.ng.download.WADODownloaderService;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.studycard.dto.QualityCardResult;
import org.shanoir.ng.studycard.model.ExaminationData;
import org.shanoir.ng.studycard.model.rule.QualityExaminationRule;

public interface QualityExaminationRuleService {

     void apply(QualityExaminationRule qer, ExaminationData examination, ExaminationAttributes<?> examinationDicomAttributes, QualityCardResult result, WADODownloaderService downloader);

     void apply(QualityExaminationRule qer, Examination examination, ExaminationAttributes<?> examinationDicomAttributes, QualityCardResult result, WADODownloaderService downloader);
}
