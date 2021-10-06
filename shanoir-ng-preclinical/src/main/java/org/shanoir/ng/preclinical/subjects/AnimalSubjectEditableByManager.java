package org.shanoir.ng.preclinical.subjects;

import org.shanoir.ng.shared.security.FieldEditionSecurityManagerImpl;
import org.springframework.stereotype.Service;

@Service
public class AnimalSubjectEditableByManager  extends FieldEditionSecurityManagerImpl<AnimalSubject> {

}
