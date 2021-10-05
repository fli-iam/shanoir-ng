package org.shanoir.ng.preclinical.pathologies.subject_pathologies;

import org.shanoir.ng.shared.validation.UniqueConstraintManagerImpl;
import org.springframework.stereotype.Service;

@Service
public class SubjectPathologyValidator extends UniqueConstraintManagerImpl<SubjectPathology> {

}
