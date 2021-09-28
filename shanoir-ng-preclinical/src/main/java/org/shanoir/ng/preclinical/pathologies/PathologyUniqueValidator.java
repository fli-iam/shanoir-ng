package org.shanoir.ng.preclinical.pathologies;

import org.shanoir.ng.shared.validation.UniqueConstraintManagerImpl;
import org.springframework.stereotype.Service;

@Service
public class PathologyUniqueValidator extends UniqueConstraintManagerImpl<Pathology> {

}
