package org.shanoir.ng.preclinical.extra_data;

import org.shanoir.ng.preclinical.extra_data.examination_extra_data.ExaminationExtraData;
import org.shanoir.ng.shared.validation.UniqueConstraintManagerImpl;
import org.springframework.stereotype.Service;

@Service
public class ExtraDataUniqueConstraintManager extends UniqueConstraintManagerImpl<ExaminationExtraData> {

}
