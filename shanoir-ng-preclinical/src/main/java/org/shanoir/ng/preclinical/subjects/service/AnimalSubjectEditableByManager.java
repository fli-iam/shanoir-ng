package org.shanoir.ng.preclinical.subjects.service;

import org.shanoir.ng.preclinical.subjects.model.AnimalSubject;
import org.shanoir.ng.shared.security.FieldEditionSecurityManagerImpl;
import org.springframework.stereotype.Service;

@Service
public class AnimalSubjectEditableByManager  extends FieldEditionSecurityManagerImpl<AnimalSubject> {

}
