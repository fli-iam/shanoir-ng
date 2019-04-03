package org.shanoir.ng.subject.service;

import org.shanoir.ng.shared.validation.UniqueConstraintManager;
import org.shanoir.ng.shared.validation.UniqueConstraintManagerImpl;
import org.shanoir.ng.subject.model.Subject;
import org.springframework.stereotype.Service;

@Service
public class SubjectUniqueConstraintManager extends UniqueConstraintManagerImpl<Subject> implements UniqueConstraintManager<Subject> {

}
