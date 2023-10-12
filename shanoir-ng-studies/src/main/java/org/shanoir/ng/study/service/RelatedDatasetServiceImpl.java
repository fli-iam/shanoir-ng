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

package org.shanoir.ng.study.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.shanoir.ng.center.model.Center;
import org.shanoir.ng.center.repository.CenterRepository;
import org.shanoir.ng.messaging.StudyUserUpdateBroadcastService;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.model.StudyUser;
import org.shanoir.ng.study.repository.StudyUserRepository;
import org.shanoir.ng.studycenter.StudyCenter;
import org.shanoir.ng.utils.KeycloakUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Implementation of study service.
 * 
 * @author msimon
 *
 */
@Component
public class RelatedDatasetServiceImpl implements RelatedDatasetService {

	@Autowired
	private StudyUserRepository studyUserRepository;

	@Autowired
	private StudyUserService studyUserService;
	@Autowired
	private StudyService studyService;
	@Autowired
	private StudyUserUpdateBroadcastService studyUserUpdateBroadcastService;

	@Autowired
	private CenterRepository centerRepository;
	@Autowired
	private ObjectMapper mapper;

	public void copyDatasetToStudy(String datasetId, String id, String centerIds) {
		System.out.println("copyToStudy : " + datasetId + " / " + id + " / " + centerIds);
		Long studyId = Long.valueOf(id);
		// List<StudyUserRight> rights = studyUserService.getRightsForStudy(studyId);

		Long userId = KeycloakUtil.getTokenUserId();
		StudyUser studyUser = studyUserRepository.findByUserIdAndStudy_Id(userId, studyId);
		List<StudyUserRight> rights = studyUser.getStudyUserRights();

		if (rights.contains(StudyUserRight.CAN_ADMINISTRATE) || rights.contains(StudyUserRight.CAN_IMPORT)) {
			System.out.println("study " + id + " has ADMIN or IMPORT right");

			for (String centerId : centerIds.split(",")) {
				System.out.println("centerId : " + centerId);
				if (!centerRepository.findByStudy(studyId).contains(centerId)) {
					System.out.println("study " + studyId + " does not have center " + centerId + ". Let's add it");

					Study study = studyService.findById(studyId);
					List<StudyCenter> studyCenterList = study.getStudyCenterList();
					Center center = centerRepository.findById(Long.valueOf(centerId)).orElse(null);
					if (center != null && !studyCenterList.contains(center)) {
						StudyCenter centerToAdd =	 new StudyCenter();
						centerToAdd.setStudy(study);
						centerToAdd.setCenter(center);
						studyCenterList.add(centerToAdd);
						study.setMonoCenter(false);
						study.setStudyCenterList(studyCenterList);
						try {
							studyService.update(study);
						} catch (EntityNotFoundException e) {
							throw new RuntimeException(e);
						} catch (MicroServiceCommunicationException e) {
							throw new RuntimeException(e);
						}
						System.out.println("Center " + center.getName() + " added to study " + study.getName());
					}

				} else {
					System.out.println("study " + studyId + " already contains center " + centerId);
				}
			}
		} else {
			// TODO if we don't have the rights on this study, return message to be displayed in modal
		}
	}
}
