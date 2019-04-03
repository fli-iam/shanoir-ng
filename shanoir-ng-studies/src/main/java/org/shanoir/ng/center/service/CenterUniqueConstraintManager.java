package org.shanoir.ng.center.service;

import org.shanoir.ng.center.model.Center;
import org.shanoir.ng.shared.validation.UniqueConstraintManager;
import org.shanoir.ng.shared.validation.UniqueConstraintManagerImpl;
import org.springframework.stereotype.Service;

@Service
public class CenterUniqueConstraintManager extends UniqueConstraintManagerImpl<Center> implements UniqueConstraintManager<Center> {

}
