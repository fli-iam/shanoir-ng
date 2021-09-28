package org.shanoir.ng.preclinical.pathologies.subject_pathologies;

import org.shanoir.ng.shared.security.FieldEditionSecurityManagerImpl;
import org.springframework.stereotype.Service;

@Service
public class SubjectPathologyEditableByManager extends FieldEditionSecurityManagerImpl<SubjectPathology> {

}
