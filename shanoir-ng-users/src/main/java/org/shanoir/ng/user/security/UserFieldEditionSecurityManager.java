package org.shanoir.ng.user.security;

import org.shanoir.ng.shared.security.FieldEditionSecurityManager;
import org.shanoir.ng.shared.security.FieldEditionSecurityManagerImpl;
import org.shanoir.ng.user.model.User;
import org.springframework.stereotype.Service;

@Service
public class UserFieldEditionSecurityManager extends FieldEditionSecurityManagerImpl<User> implements FieldEditionSecurityManager<User>  {

}
