package org.shanoir.ng.preclinical.therapies;

import org.shanoir.ng.shared.validation.UniqueConstraintManagerImpl;
import org.springframework.stereotype.Service;

@Service
public class TherapyUniqueValidator extends UniqueConstraintManagerImpl<Therapy> {

}
