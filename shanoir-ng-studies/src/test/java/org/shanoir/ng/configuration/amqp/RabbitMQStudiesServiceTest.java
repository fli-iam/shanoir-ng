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

package org.shanoir.ng.configuration.amqp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shanoir.ng.profile.model.Profile;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.repository.StudyRepository;

/**
 * Study name and anonymization profile replies of RabbitMQStudiesService.
 */
@ExtendWith(MockitoExtension.class)
public class RabbitMQStudiesServiceTest {

    private static final long STUDY_ID = 1L;

    private static final long UNKNOWN_STUDY_ID = 999999L;

    @Mock
    private StudyRepository studyRepo;

    @InjectMocks
    private RabbitMQStudiesService service;

    @Test
    public void testGetStudyName() {
        given(studyRepo.findById(STUDY_ID)).willReturn(Optional.of(createStudy(createProfile())));
        assertEquals("study", service.getStudyName(STUDY_ID));
    }

    @Test
    public void testGetStudyNameOfUnknownStudyIsNull() {
        given(studyRepo.findById(UNKNOWN_STUDY_ID)).willReturn(Optional.empty());
        assertNull(service.getStudyName(UNKNOWN_STUDY_ID));
    }

    @Test
    public void testGetStudyAnonymisationProfile() {
        given(studyRepo.findById(STUDY_ID)).willReturn(Optional.of(createStudy(createProfile())));
        assertEquals("Profile Neurinfo", service.getStudyAnonymisationProfile(STUDY_ID));
    }

    @Test
    public void testGetStudyAnonymisationProfileOfUnknownStudyIsNull() {
        given(studyRepo.findById(UNKNOWN_STUDY_ID)).willReturn(Optional.empty());
        assertNull(service.getStudyAnonymisationProfile(UNKNOWN_STUDY_ID));
    }

    @Test
    public void testGetStudyAnonymisationProfileOfStudyWithoutProfileIsNull() {
        given(studyRepo.findById(STUDY_ID)).willReturn(Optional.of(createStudy(null)));
        assertNull(service.getStudyAnonymisationProfile(STUDY_ID));
    }

    private Study createStudy(Profile profile) {
        Study study = new Study();
        study.setId(STUDY_ID);
        study.setName("study");
        study.setProfile(profile);
        return study;
    }

    private Profile createProfile() {
        Profile profile = new Profile();
        profile.setProfileName("Profile Neurinfo");
        return profile;
    }

}
