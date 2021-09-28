package org.shanoir.ng.preclinical.references;

import org.shanoir.ng.shared.security.FieldEditionSecurityManagerImpl;
import org.springframework.stereotype.Service;

@Service
public class RefsEditableByManager extends FieldEditionSecurityManagerImpl<Reference> {

}
