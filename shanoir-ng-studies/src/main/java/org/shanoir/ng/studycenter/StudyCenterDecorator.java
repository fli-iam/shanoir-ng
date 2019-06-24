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

/**
 * 
 */
package org.shanoir.ng.studycenter;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yyao
 *
 */
public class StudyCenterDecorator implements StudyCenterMapper{
	
	@Autowired 
	private StudyCenterMapper delegate;
	
	@Override
	public StudyCenterDTO studyCenterToStudyCenterDTO(StudyCenter studyCenter) {
		final StudyCenterDTO studyCenterDTO = delegate.studyCenterToStudyCenterDTO(studyCenter);
		
		// Investigator
		return studyCenterDTO;
	}

	@Override
	public List<StudyCenterDTO> studyCenterListToStudyCenterDTOList(List<StudyCenter> studyCenterList) {
		final List<StudyCenterDTO> studyCenterDTOs = new ArrayList<>();
		if (studyCenterList != null) {
			for (StudyCenter studyCenter : studyCenterList) {
				studyCenterDTOs.add(studyCenterToStudyCenterDTO(studyCenter));
			}
		}
		return studyCenterDTOs;
	}

}
