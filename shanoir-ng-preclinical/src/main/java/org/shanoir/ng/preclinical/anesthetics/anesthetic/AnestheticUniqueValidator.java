package org.shanoir.ng.preclinical.anesthetics.anesthetic;

import org.shanoir.ng.shared.validation.UniqueConstraintManagerImpl;
import org.springframework.stereotype.Service;

@Service
public class AnestheticUniqueValidator extends UniqueConstraintManagerImpl<Anesthetic> {

}
