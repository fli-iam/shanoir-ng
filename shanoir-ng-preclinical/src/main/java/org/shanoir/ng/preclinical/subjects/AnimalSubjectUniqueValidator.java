package org.shanoir.ng.preclinical.subjects;

import org.shanoir.ng.shared.validation.UniqueConstraintManagerImpl;
import org.springframework.stereotype.Service;

@Service
public class AnimalSubjectUniqueValidator extends UniqueConstraintManagerImpl<AnimalSubject> {

}
