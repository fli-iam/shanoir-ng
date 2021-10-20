package org.shanoir.ng.preclinical.references;

import org.shanoir.ng.shared.validation.UniqueConstraintManagerImpl;
import org.springframework.stereotype.Service;

@Service
public class ReferenceUniqueValidator extends UniqueConstraintManagerImpl<Reference> {

}
