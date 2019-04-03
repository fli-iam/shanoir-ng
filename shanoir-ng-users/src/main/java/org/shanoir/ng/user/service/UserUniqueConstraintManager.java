package org.shanoir.ng.user.service;

import org.shanoir.ng.shared.validation.UniqueConstraintManager;
import org.shanoir.ng.shared.validation.UniqueConstraintManagerImpl;
import org.shanoir.ng.user.model.User;
import org.springframework.stereotype.Service;

@Service
public class UserUniqueConstraintManager extends UniqueConstraintManagerImpl<User> implements UniqueConstraintManager<User>  {

} 
