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
