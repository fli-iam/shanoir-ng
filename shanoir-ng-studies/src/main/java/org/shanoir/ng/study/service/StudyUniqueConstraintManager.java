package org.shanoir.ng.study.service;

import org.shanoir.ng.shared.validation.UniqueConstraintManager;
import org.shanoir.ng.shared.validation.UniqueConstraintManagerImpl;
import org.shanoir.ng.study.model.Study;
import org.springframework.stereotype.Service;

@Service
public class StudyUniqueConstraintManager extends UniqueConstraintManagerImpl<Study> implements UniqueConstraintManager<Study>  {

} 
