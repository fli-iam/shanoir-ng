package org.shanoir.ng.center.security;

import org.shanoir.ng.center.model.Center;
import org.shanoir.ng.shared.security.FieldEditionSecurityManager;
import org.shanoir.ng.shared.security.FieldEditionSecurityManagerImpl;
import org.springframework.stereotype.Service;

@Service
public class CenterFieldEditionSecurityManager extends FieldEditionSecurityManagerImpl<Center> implements FieldEditionSecurityManager<Center>  {

}
