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

package org.shanoir.ng.dataset.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.repository.DatasetRepository;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.rights.StudyRightsService;
import org.shanoir.ng.utils.KeycloakUtil;

/**
 * Tests for the per-dataset visualization rule in DatasetSecurityService:
 * an unowned dataset is visible to anyone who reached the check, while an owned
 * annotation is visible only to its owner (with CAN_ANNOTATE) or to a reviewer.
 *
 * @author afragkia
 */
@ExtendWith(MockitoExtension.class)
public class DatasetSecurityServiceTest {

    private static final Long DATASET_ID = 100L;

    private static final Long STUDY_ID = 1L;

    private static final String OWNER = "annotatorUser";

    private static final String OTHER_USER = "otherUser";

    @Mock
    private DatasetRepository datasetRepository;

    @Mock
    private StudyRightsService studyRightsService;

    @InjectMocks
    private DatasetSecurityService datasetSecurityService;

    private Dataset ownedDataset() {
        Dataset dataset = new MrDataset();
        dataset.setStudyId(STUDY_ID);
        dataset.setUsername(OWNER);
        return dataset;
    }

    private Dataset unownedDataset() {
        Dataset dataset = new MrDataset();
        dataset.setStudyId(STUDY_ID);
        dataset.setUsername(null);
        return dataset;
    }

    @Test
    public void adminCanAlwaysVisualize() {
        try (MockedStatic<KeycloakUtil> keycloakUtilMock = Mockito.mockStatic(KeycloakUtil.class)) {
            keycloakUtilMock.when(KeycloakUtil::isAdmin).thenReturn(true);
            assertTrue(datasetSecurityService.hasRightToVisualizeDataset(DATASET_ID));
        }
        verifyNoInteractions(datasetRepository);
        verifyNoInteractions(studyRightsService);
    }

    @Test
    public void returnsFalseWhenDatasetNotFound() {
        when(datasetRepository.findById(DATASET_ID)).thenReturn(Optional.empty());
        try (MockedStatic<KeycloakUtil> keycloakUtilMock = Mockito.mockStatic(KeycloakUtil.class)) {
            keycloakUtilMock.when(KeycloakUtil::isAdmin).thenReturn(false);
            assertFalse(datasetSecurityService.hasRightToVisualizeDataset(DATASET_ID));
        }
        verifyNoInteractions(studyRightsService);
    }

    @Test
    public void unownedDatasetIsVisible() {
        when(datasetRepository.findById(DATASET_ID)).thenReturn(Optional.of(unownedDataset()));
        try (MockedStatic<KeycloakUtil> keycloakUtilMock = Mockito.mockStatic(KeycloakUtil.class)) {
            keycloakUtilMock.when(KeycloakUtil::isAdmin).thenReturn(false);
            assertTrue(datasetSecurityService.hasRightToVisualizeDataset(DATASET_ID));
        }
        // no ownership: the study rights are not consulted here
        verifyNoInteractions(studyRightsService);
    }

    @Test
    public void ownerWithCanAnnotateCanVisualize() {
        when(datasetRepository.findById(DATASET_ID)).thenReturn(Optional.of(ownedDataset()));
        when(studyRightsService.hasRightOnStudy(STUDY_ID, StudyUserRight.CAN_ANNOTATE.name())).thenReturn(true);
        try (MockedStatic<KeycloakUtil> keycloakUtilMock = Mockito.mockStatic(KeycloakUtil.class)) {
            keycloakUtilMock.when(KeycloakUtil::isAdmin).thenReturn(false);
            keycloakUtilMock.when(KeycloakUtil::getTokenUserName).thenReturn(OWNER);
            assertTrue(datasetSecurityService.hasRightToVisualizeDataset(DATASET_ID));
        }
    }

    @Test
    public void ownerWithoutCanAnnotateCannotVisualizeAsOwnerButCanAsReviewer() {
        when(datasetRepository.findById(DATASET_ID)).thenReturn(Optional.of(ownedDataset()));
        when(studyRightsService.hasRightOnStudy(STUDY_ID, StudyUserRight.CAN_ANNOTATE.name())).thenReturn(false);
        when(studyRightsService.hasRightOnStudy(STUDY_ID, StudyUserRight.CAN_REVIEW.name())).thenReturn(true);
        try (MockedStatic<KeycloakUtil> keycloakUtilMock = Mockito.mockStatic(KeycloakUtil.class)) {
            keycloakUtilMock.when(KeycloakUtil::isAdmin).thenReturn(false);
            keycloakUtilMock.when(KeycloakUtil::getTokenUserName).thenReturn(OWNER);
            assertTrue(datasetSecurityService.hasRightToVisualizeDataset(DATASET_ID));
        }
    }

    @Test
    public void ownerWithoutCanAnnotateNorCanReviewCannotVisualize() {
        when(datasetRepository.findById(DATASET_ID)).thenReturn(Optional.of(ownedDataset()));
        when(studyRightsService.hasRightOnStudy(STUDY_ID, StudyUserRight.CAN_ANNOTATE.name())).thenReturn(false);
        when(studyRightsService.hasRightOnStudy(STUDY_ID, StudyUserRight.CAN_REVIEW.name())).thenReturn(false);
        try (MockedStatic<KeycloakUtil> keycloakUtilMock = Mockito.mockStatic(KeycloakUtil.class)) {
            keycloakUtilMock.when(KeycloakUtil::isAdmin).thenReturn(false);
            keycloakUtilMock.when(KeycloakUtil::getTokenUserName).thenReturn(OWNER);
            assertFalse(datasetSecurityService.hasRightToVisualizeDataset(DATASET_ID));
        }
    }

    @Test
    public void reviewerWhoIsNotOwnerCanVisualize() {
        when(datasetRepository.findById(DATASET_ID)).thenReturn(Optional.of(ownedDataset()));
        when(studyRightsService.hasRightOnStudy(STUDY_ID, StudyUserRight.CAN_REVIEW.name())).thenReturn(true);
        try (MockedStatic<KeycloakUtil> keycloakUtilMock = Mockito.mockStatic(KeycloakUtil.class)) {
            keycloakUtilMock.when(KeycloakUtil::isAdmin).thenReturn(false);
            keycloakUtilMock.when(KeycloakUtil::getTokenUserName).thenReturn(OTHER_USER);
            assertTrue(datasetSecurityService.hasRightToVisualizeDataset(DATASET_ID));
        }
    }

    @Test
    public void nonOwnerWithoutCanReviewCannotVisualize() {
        when(datasetRepository.findById(DATASET_ID)).thenReturn(Optional.of(ownedDataset()));
        when(studyRightsService.hasRightOnStudy(STUDY_ID, StudyUserRight.CAN_REVIEW.name())).thenReturn(false);
        try (MockedStatic<KeycloakUtil> keycloakUtilMock = Mockito.mockStatic(KeycloakUtil.class)) {
            keycloakUtilMock.when(KeycloakUtil::isAdmin).thenReturn(false);
            keycloakUtilMock.when(KeycloakUtil::getTokenUserName).thenReturn(OTHER_USER);
            assertFalse(datasetSecurityService.hasRightToVisualizeDataset(DATASET_ID));
        }
    }

}
