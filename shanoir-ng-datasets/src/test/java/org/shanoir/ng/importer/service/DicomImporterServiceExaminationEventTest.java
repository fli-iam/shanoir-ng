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

package org.shanoir.ng.importer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.utils.KeycloakUtil;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * MS Studies counts a study's examinations from CREATE_EXAMINATION_EVENT. A STOW-RS import
 * must send it for every new examination, once, and only if the import transaction commits.
 */
@ExtendWith(MockitoExtension.class)
public class DicomImporterServiceExaminationEventTest {

    private static final Long EXAMINATION_ID = 77L;
    private static final Long STUDY_ID = 6L;
    private static final Long SUBJECT_ID = 12L;
    private static final Long CENTER_ID = 3L;
    private static final Long USER_ID = 9L;

    @Mock
    private ShanoirEventService eventService;

    @InjectMocks
    private DicomImporterService dicomImporterService;

    private Examination examination() {
        Examination examination = new Examination();
        examination.setId(EXAMINATION_ID);
        return examination;
    }

    @Test
    public void theEventCarriesWhatMsStudiesParses() {
        try (MockedStatic<KeycloakUtil> keycloak = Mockito.mockStatic(KeycloakUtil.class)) {
            keycloak.when(KeycloakUtil::getTokenUserId).thenReturn(USER_ID);

            // no surrounding transaction: sent at once
            dicomImporterService.announceNewExamination(examination(), STUDY_ID, SUBJECT_ID, CENTER_ID);
        }

        ArgumentCaptor<ShanoirEvent> captor = ArgumentCaptor.forClass(ShanoirEvent.class);
        verify(eventService).publishEvent(captor.capture());
        ShanoirEvent event = captor.getValue();
        assertEquals(ShanoirEventType.CREATE_EXAMINATION_EVENT, event.getEventType());
        assertEquals(EXAMINATION_ID.toString(), event.getObjectId());
        assertEquals(USER_ID, event.getUserId());
        assertEquals(STUDY_ID, event.getStudyId());
        assertEquals(ShanoirEvent.SUCCESS, event.getStatus());
        // MS Studies reads exactly this: ".*?centerId:(\d+);subjectId:(\d+)"
        assertEquals("centerId:3;subjectId:12", event.getMessage());
    }

    @Test
    public void insideATransactionTheEventWaitsForTheCommit() {
        TransactionSynchronizationManager.initSynchronization();
        try (MockedStatic<KeycloakUtil> keycloak = Mockito.mockStatic(KeycloakUtil.class)) {
            keycloak.when(KeycloakUtil::getTokenUserId).thenReturn(USER_ID);

            dicomImporterService.announceNewExamination(examination(), STUDY_ID, SUBJECT_ID, CENTER_ID);

            verifyNoInteractions(eventService);
            for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
                synchronization.afterCommit();
            }
            verify(eventService).publishEvent(Mockito.any(ShanoirEvent.class));
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    public void aRolledBackImportSendsNothing() {
        TransactionSynchronizationManager.initSynchronization();
        try (MockedStatic<KeycloakUtil> keycloak = Mockito.mockStatic(KeycloakUtil.class)) {
            keycloak.when(KeycloakUtil::getTokenUserId).thenReturn(USER_ID);

            dicomImporterService.announceNewExamination(examination(), STUDY_ID, SUBJECT_ID, CENTER_ID);
            for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
                synchronization.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
            }

            verify(eventService, never()).publishEvent(Mockito.any(ShanoirEvent.class));
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

}
