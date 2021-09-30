package org.shanoir.ng.preclinical.extra_data.physiological_data;

import org.shanoir.ng.shared.validation.UniqueConstraintManagerImpl;
import org.springframework.stereotype.Service;

@Service
public class PhysioDataUniqueConstraintManager extends UniqueConstraintManagerImpl<PhysiologicalData> {

}
