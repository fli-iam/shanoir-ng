package org.shanoir.ng.preclinical.pathologies;

import org.shanoir.ng.shared.security.FieldEditionSecurityManagerImpl;
import org.springframework.stereotype.Service;

@Service
public class PathologyEditableByManager extends FieldEditionSecurityManagerImpl<Pathology> {

}
