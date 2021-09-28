package org.shanoir.ng.preclinical.therapies;

import org.shanoir.ng.shared.security.FieldEditionSecurityManagerImpl;
import org.springframework.stereotype.Service;

@Service
public class TherapyEditableByManager extends FieldEditionSecurityManagerImpl<Therapy>{

}
