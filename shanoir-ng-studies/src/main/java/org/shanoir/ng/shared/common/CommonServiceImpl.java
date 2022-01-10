/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.shared.common;

import org.shanoir.ng.acquisitionequipment.model.AcquisitionEquipment;
import org.shanoir.ng.acquisitionequipment.repository.AcquisitionEquipmentRepository;
import org.shanoir.ng.center.model.Center;
import org.shanoir.ng.center.repository.CenterRepository;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.repository.StudyRepository;
import org.shanoir.ng.subject.model.Subject;
import org.shanoir.ng.subject.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of shared service.
 * 
 * @author ifakhfakh
 *
 */
@Service
public class CommonServiceImpl implements CommonService {

	@Autowired
	private StudyRepository studyRepository;

	@Autowired
	private SubjectRepository subjectRepository;

	@Autowired
	private CenterRepository centerRepository;

	@Autowired
	private AcquisitionEquipmentRepository equipementRepository;

	@Override
	public CommonIdNamesDTO findByIds(final CommonIdsDTO commonIdsDTO) {
		CommonIdNamesDTO names = new CommonIdNamesDTO();
		if (commonIdsDTO.getStudyId() != null) {
			final Study study = studyRepository.findById(commonIdsDTO.getStudyId()).orElse(null);
			if (study != null) {
				names.setStudy(new IdName(commonIdsDTO.getStudyId(), study.getName()));
			}
		}
		if (commonIdsDTO.getCenterId() != null) {
			final Center center = centerRepository.findById(commonIdsDTO.getCenterId()).orElse(null);
			if (center != null) {
				names.setCenter(new IdName(commonIdsDTO.getCenterId(), center.getName()));
			}
		}
		if (commonIdsDTO.getSubjectId() != null) {
			final Subject subject = subjectRepository.findById(commonIdsDTO.getSubjectId()).orElse(null);
			if (subject != null) {
				names.setSubject(new IdName(commonIdsDTO.getSubjectId(), subject.getName()));
			}
		}
		if (commonIdsDTO.getEquipementId() != null) {
			final AcquisitionEquipment equipement = equipementRepository.findById(commonIdsDTO.getEquipementId()).orElse(null);
			if (equipement != null) {
				names.setEquipement(new IdName(commonIdsDTO.getEquipementId(), equipement.getSerialNumber()));
			}
		}
		return names;
	}

}
