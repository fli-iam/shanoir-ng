package org.shanoir.ng.studycard.service;

import org.shanoir.ng.shared.validation.UniqueConstraintManager;
import org.shanoir.ng.shared.validation.UniqueConstraintManagerImpl;
import org.shanoir.ng.studycard.model.StudyCard;
import org.springframework.stereotype.Service;

@Service
public class StudyCardUniqueConstraintManager extends UniqueConstraintManagerImpl<StudyCard> implements UniqueConstraintManager<StudyCard>  {

} 
