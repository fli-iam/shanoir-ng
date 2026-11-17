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

package org.shanoir.ng.study.rights.ampq;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.rights.StudyUser;
import org.shanoir.ng.study.rights.StudyUserRightsRepository;
import org.shanoir.ng.study.rights.command.CommandType;
import org.shanoir.ng.study.rights.command.StudyUserCommand;

@ExtendWith(MockitoExtension.class)
public class StudyUserUpdateServiceTest {

    private static final Long STUDY_USER_ID = 1L;

    @Mock
    private StudyUserRightsRepository studyUserRepository;

    @InjectMocks
    private StudyUserUpdateService service;

    @Test
    public void updateCommandCopiesAllReplicatedFields() {
        StudyUser existing = new StudyUser();
        existing.setId(STUDY_USER_ID);
        existing.setReceiveStudyUserReport(false);
        existing.setReceiveNewImportReport(false);
        existing.setConfirmed(false);
        existing.setExpirationDate(LocalDate.of(2026, 9, 5));
        existing.setStudyUserRights(List.of(StudyUserRight.CAN_SEE_ALL));
        existing.setCenterIds(List.of(1L));

        LocalDate extendedDate = LocalDate.of(2029, 9, 5);
        StudyUser replacing = new StudyUser();
        replacing.setId(STUDY_USER_ID);
        replacing.setReceiveStudyUserReport(true);
        replacing.setReceiveNewImportReport(true);
        replacing.setConfirmed(true);
        replacing.setExpirationDate(extendedDate);
        replacing.setStudyUserRights(List.of(StudyUserRight.CAN_IMPORT, StudyUserRight.CAN_SEE_ALL));
        replacing.setCenterIds(List.of(1L, 2L));

        when(studyUserRepository.findAllById(anySet())).thenReturn(List.of(existing));

        service.processCommands(List.of(new StudyUserCommand(CommandType.UPDATE, replacing)));

        assertThat(existing.isReceiveStudyUserReport()).isTrue();
        assertThat(existing.isReceiveNewImportReport()).isTrue();
        assertThat(existing.isConfirmed()).isTrue();
        assertThat(existing.getExpirationDate()).isEqualTo(extendedDate);
        assertThat(existing.getStudyUserRights())
                .containsExactlyInAnyOrder(StudyUserRight.CAN_IMPORT, StudyUserRight.CAN_SEE_ALL);
        assertThat(existing.getCenterIds()).containsExactlyInAnyOrder(1L, 2L);
        verify(studyUserRepository).saveAll(List.of(existing));
    }

    @Test
    public void updateCommandCanRemoveExpirationDate() {
        StudyUser existing = new StudyUser();
        existing.setId(STUDY_USER_ID);
        existing.setExpirationDate(LocalDate.of(2026, 9, 5));

        // e.g. the user became study administrator: no expiration date anymore
        StudyUser replacing = new StudyUser();
        replacing.setId(STUDY_USER_ID);
        replacing.setExpirationDate(null);

        when(studyUserRepository.findAllById(anySet())).thenReturn(List.of(existing));

        service.processCommands(List.of(new StudyUserCommand(CommandType.UPDATE, replacing)));

        assertThat(existing.getExpirationDate()).isNull();
    }
}
