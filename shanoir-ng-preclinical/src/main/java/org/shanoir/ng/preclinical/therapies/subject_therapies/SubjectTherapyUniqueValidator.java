package org.shanoir.ng.preclinical.therapies.subject_therapies;

import org.shanoir.ng.shared.validation.UniqueConstraintManagerImpl;
import org.springframework.stereotype.Service;

@Service
public class SubjectTherapyUniqueValidator extends UniqueConstraintManagerImpl<SubjectTherapy> {

}
