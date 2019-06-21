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

package org.shanoir.ng.subject.dto.mapper;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.shared.service.MicroserviceRequestsService;
import org.shanoir.ng.study.service.StudyService;
import org.shanoir.ng.subject.dto.SubjectFromShupDTO;
import org.shanoir.ng.subject.model.HemisphericDominance;
import org.shanoir.ng.subject.model.ImagedObjectCategory;
import org.shanoir.ng.subject.model.Sex;
import org.shanoir.ng.subject.model.Subject;
import org.shanoir.ng.subject.model.SubjectType;
import org.shanoir.ng.subject.service.SubjectService;
import org.shanoir.ng.subjectstudy.model.SubjectStudy;
import org.shanoir.ng.utils.KeycloakUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

// TODO : Implement those mappings into SubjectMapper ?
@Service
public class SubjectMappingUtilsService {
	
	@Autowired
	StudyService studyService;
	
	@Autowired
	SubjectService subjectService;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private MicroserviceRequestsService microservicesRequestsService;
	
	/**
	 * Map a SubjectFromShupDTO to a Subject
	 * 
	 * @param subjectFromShupDTO the dto to convert
	 * @return a Subject
	 * @throws MicroServiceCommunicationException when the attempt to get a center id from the studycard microservice fails.
	 */
	public Subject toSubject(SubjectFromShupDTO subjectFromShupDTO) throws MicroServiceCommunicationException {
		
		Subject subject = new Subject();
		subject.setName(subjectFromShupDTO.getName());
		subject.setBirthDate(subjectFromShupDTO.getBirthDate());
		subject.setIdentifier(subjectFromShupDTO.getIdentifier());
		subject.setImagedObjectCategory(ImagedObjectCategory.getCategory(subjectFromShupDTO.getImagedObjectCategory()));
		subject.setLanguageHemisphericDominance(HemisphericDominance.getDominance(subjectFromShupDTO.getLanguageHemisphericDominance()));
		subject.setManualHemisphericDominance(HemisphericDominance.getDominance(subjectFromShupDTO.getManualHemisphericDominance()));
		subject.setPseudonymusHashValues(subjectFromShupDTO.getPseudonymusHashValues());
		subject.setSex(Sex.getSex(subjectFromShupDTO.getSex()));
		
		SubjectStudy subjectStudy = new SubjectStudy();
		subjectStudy.setStudy(studyService.findById(subjectFromShupDTO.getStudyId()));
		subjectStudy.setPhysicallyInvolved(subjectFromShupDTO.getPhysicallyInvolved());
		subjectStudy.setSubject(subject);
		subjectStudy.setSubjectStudyIdentifier(subjectFromShupDTO.getSubjectStudyIdentifier());
		subjectStudy.setSubjectType(SubjectType.getType(subjectFromShupDTO.getSubjectType()));
		
		List<SubjectStudy> subjectStudyList = new ArrayList<SubjectStudy>();
		subjectStudyList.add(subjectStudy);
		subject.setSubjectStudyList(subjectStudyList);

		String commonName = getOfsepCommonName(subjectFromShupDTO.getStudyCardId());
		if (commonName == null || commonName.equals(""))
			subject.setName("NoCommonName");
		else
			subject.setName(commonName);
		subjectStudy.setSubject(subject);
		
		return subject;	
	}
	
	/**
	 * Update some values of subject to save them in database.
	 * EXCEPT FOR Attribute UserPersonalCommentList
	 *
	 * @param subjectDB is subject found in DB prior the update
	 * @param SubjectFromShupDTO contains the new values.
	 * @return a Subject with new values.
	 */
	public Subject updateSubjectValues(final Subject subjectDb, final SubjectFromShupDTO subjectFromShupDTO) {

		subjectDb.setName(subjectFromShupDTO.getName());
		subjectDb.setBirthDate(subjectFromShupDTO.getBirthDate());
		subjectDb.setIdentifier(subjectFromShupDTO.getIdentifier());
		subjectDb.setPseudonymusHashValues(subjectFromShupDTO.getPseudonymusHashValues());
		subjectDb.setSex(Sex.getSex(subjectFromShupDTO.getSex()));
		boolean foundStudy = false;
		for (SubjectStudy ss : subjectDb.getSubjectStudyList()) {
			if (ss.getStudy().getId() == subjectFromShupDTO.getStudyId()) {
				ss.setSubjectType(SubjectType.getType(subjectFromShupDTO.getSubjectType()));
				ss.setPhysicallyInvolved(subjectFromShupDTO.getPhysicallyInvolved());
				foundStudy = true;
			}
		}
		if (!foundStudy) {
			SubjectStudy subjectStudy =  new SubjectStudy();
			subjectStudy.setSubjectType(SubjectType.getType(subjectFromShupDTO.getSubjectType()));
			subjectStudy.setPhysicallyInvolved(subjectFromShupDTO.getPhysicallyInvolved());
			subjectStudy.setStudy(studyService.findById(subjectFromShupDTO.getStudyId()));
			subjectStudy.setSubject(subjectDb);
			if (subjectDb.getSubjectStudyList() == null) {
				List<SubjectStudy> subjectStudyList = new ArrayList<SubjectStudy>();
				subjectStudyList.add(subjectStudy);
				subjectDb.setSubjectStudyList(subjectStudyList);
			} else {
				subjectDb.getSubjectStudyList().add(subjectStudy);
			}
		}

		subjectDb.setManualHemisphericDominance(HemisphericDominance.getDominance(subjectFromShupDTO.getManualHemisphericDominance()));
		subjectDb.setLanguageHemisphericDominance(HemisphericDominance.getDominance(subjectFromShupDTO.getLanguageHemisphericDominance()));
		subjectDb.setImagedObjectCategory(ImagedObjectCategory.getCategory(subjectFromShupDTO.getImagedObjectCategory()));
		
		/**
		 *  the following line is commented because R/O in shanoir uploader. 
		 *  If in future version, this is enable in Shanoir Uploader remove the comment
		 *  on the following line..
		 */
				
		// subjectDb.setUserPersonalCommentList(subjectFromShupDTO.getUserPersonalCommentList());
		return subjectDb;
	}
	
	/**
	 * Get the OFSEP name from a study card
	 * 
	 * @param studyCardId
	 * @return
	 * @throws MicroServiceCommunicationException
	 */
	public String getOfsepCommonName(Long studyCardId) throws MicroServiceCommunicationException {
		Long idCenter = getCenterIdFromStudyCard(studyCardId);
		DecimalFormat formatterCenter = new DecimalFormat("000");
		String commonNameCenter = formatterCenter.format(idCenter);

		Subject subjectOfsepCommonNameMaxFoundByCenter = subjectService.findSubjectFromCenterCode(commonNameCenter);
		int maxCommonNameNumber = 0;

		if (subjectOfsepCommonNameMaxFoundByCenter != null) {
			String maxNameToIncrement = subjectOfsepCommonNameMaxFoundByCenter.getName().substring(3);
			maxCommonNameNumber = Integer.parseInt(maxNameToIncrement);
		}
		maxCommonNameNumber += 1;
		DecimalFormat formatterSubject = new DecimalFormat("0000");
		return commonNameCenter + formatterSubject.format(maxCommonNameNumber);
	}
	
	private Long getCenterIdFromStudyCard(Long studyCardId) throws MicroServiceCommunicationException {
		HttpEntity<Long> entity = new HttpEntity<>(KeycloakUtil.getKeycloakHeader());
		// Request to studycard MS to get center id
		ResponseEntity<Long> centerIdResponse = restTemplate.exchange(microservicesRequestsService.getStudycardsMsUrl()
					+ MicroserviceRequestsService.CENTERID + "/" + studyCardId, HttpMethod.GET, entity, Long.class);

		Long centerId = null;
		if (HttpStatus.OK.equals(centerIdResponse.getStatusCode())
				|| HttpStatus.NO_CONTENT.equals(centerIdResponse.getStatusCode())) {
			centerId = centerIdResponse.getBody();
		} else {
			throw new MicroServiceCommunicationException(
					"Cannot get the center id from the study card microservice (response status : " 
					+ centerIdResponse.getStatusCode() + " )");
		}
		return centerId;
	}
}
