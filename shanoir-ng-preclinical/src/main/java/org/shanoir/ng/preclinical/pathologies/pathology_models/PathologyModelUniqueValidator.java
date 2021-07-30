package org.shanoir.ng.preclinical.pathologies.pathology_models;

import org.shanoir.ng.shared.validation.UniqueConstraintManagerImpl;
import org.springframework.stereotype.Service;

@Service
public class PathologyModelUniqueValidator extends UniqueConstraintManagerImpl<PathologyModel> {

}
