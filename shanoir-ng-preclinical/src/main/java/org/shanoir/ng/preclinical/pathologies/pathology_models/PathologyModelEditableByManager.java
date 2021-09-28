package org.shanoir.ng.preclinical.pathologies.pathology_models;

import org.shanoir.ng.shared.security.FieldEditionSecurityManagerImpl;
import org.springframework.stereotype.Service;

@Service
public class PathologyModelEditableByManager extends FieldEditionSecurityManagerImpl<PathologyModel> {

}
