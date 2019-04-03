package org.shanoir.ng.study.security;

import org.shanoir.ng.shared.security.FieldEditionSecurityManager;
import org.shanoir.ng.shared.security.FieldEditionSecurityManagerImpl;
import org.shanoir.ng.study.model.Study;
import org.springframework.stereotype.Service;

@Service
public class StudyFieldEditionSecurityManager extends FieldEditionSecurityManagerImpl<Study> implements FieldEditionSecurityManager<Study>  {

}
