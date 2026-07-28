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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
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
import org.shanoir.ng.dicom.web.SeriesInstanceUIDHandler;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.rights.StudyRightsService;
import org.shanoir.ng.study.rights.UserRights;
import org.shanoir.ng.utils.KeycloakUtil;

/**
 * Tests for the per-dataset visualization rule in DatasetSecurityService:
 * an unowned dataset is visible to anyone who reached the check, while an owned
 * annotation is visible only to its owner (with CAN_ANNOTATE) or to a reviewer.
 *
 * @author Adam Fragkiadakis
 */
@ExtendWith(MockitoExtension.class)
public class DatasetSecurityServiceTest {

    private static final Long DATASET_ID = 100L;

    private static final Long STUDY_ID = 1L;

    private static final String OWNER = "annotatorUser";

    private static final String OTHER_USER = "otherUser";

    private static final String DATASET_UID = "1.4.9.12.34.1.8527.0.100";

    private static final String ACQUISITION_UID = "1.4.9.12.34.1.8527.42";

    @Mock
    private DatasetRepository datasetRepository;

    @Mock
    private StudyRightsService studyRightsService;

    @Mock
    private SeriesInstanceUIDHandler seriesInstanceUIDHandler;

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

    private Dataset datasetOwnedBy(String owner) {
        Dataset dataset = new MrDataset();
        dataset.setStudyId(STUDY_ID);
        dataset.setUsername(owner);
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

    @Test
    public void seriesVisualizationAllowedForAdmin() {
        try (MockedStatic<KeycloakUtil> keycloakUtilMock = Mockito.mockStatic(KeycloakUtil.class)) {
            keycloakUtilMock.when(KeycloakUtil::isAdmin).thenReturn(true);
            assertTrue(datasetSecurityService.hasRightToVisualizeSeries(DATASET_UID));
        }
        // admin bypasses before any UID resolution
        verifyNoInteractions(seriesInstanceUIDHandler);
        verifyNoInteractions(datasetRepository);
    }

    @Test
    public void seriesVisualizationAllowedForNonDatasetUID() {
        // an ordinary acquisition series carries no per-dataset ownership rule
        when(seriesInstanceUIDHandler.isDatasetUID(ACQUISITION_UID)).thenReturn(false);
        try (MockedStatic<KeycloakUtil> keycloakUtilMock = Mockito.mockStatic(KeycloakUtil.class)) {
            keycloakUtilMock.when(KeycloakUtil::isAdmin).thenReturn(false);
            assertTrue(datasetSecurityService.hasRightToVisualizeSeries(ACQUISITION_UID));
        }
        verifyNoInteractions(datasetRepository);
        verifyNoInteractions(studyRightsService);
    }

    @Test
    public void seriesVisualizationDelegatesToDatasetRuleWhenAllowed() {
        // a dataset-level UID is resolved to its dataset and follows the dataset rule
        when(seriesInstanceUIDHandler.isDatasetUID(DATASET_UID)).thenReturn(true);
        when(seriesInstanceUIDHandler.extractDatasetId(DATASET_UID)).thenReturn(DATASET_ID);
        when(datasetRepository.findById(DATASET_ID)).thenReturn(Optional.of(unownedDataset()));
        try (MockedStatic<KeycloakUtil> keycloakUtilMock = Mockito.mockStatic(KeycloakUtil.class)) {
            keycloakUtilMock.when(KeycloakUtil::isAdmin).thenReturn(false);
            assertTrue(datasetSecurityService.hasRightToVisualizeSeries(DATASET_UID));
        }
    }

    @Test
    public void seriesVisualizationDelegatesToDatasetRuleWhenDenied() {
        // owned annotation, requester is neither the owner nor a reviewer
        when(seriesInstanceUIDHandler.isDatasetUID(DATASET_UID)).thenReturn(true);
        when(seriesInstanceUIDHandler.extractDatasetId(DATASET_UID)).thenReturn(DATASET_ID);
        when(datasetRepository.findById(DATASET_ID)).thenReturn(Optional.of(ownedDataset()));
        when(studyRightsService.hasRightOnStudy(STUDY_ID, StudyUserRight.CAN_REVIEW.name())).thenReturn(false);
        try (MockedStatic<KeycloakUtil> keycloakUtilMock = Mockito.mockStatic(KeycloakUtil.class)) {
            keycloakUtilMock.when(KeycloakUtil::isAdmin).thenReturn(false);
            keycloakUtilMock.when(KeycloakUtil::getTokenUserName).thenReturn(OTHER_USER);
            assertFalse(datasetSecurityService.hasRightToVisualizeSeries(DATASET_UID));
        }
    }

    @Test
    public void filterKeepsEveryAnnotationForAdmin() {
        List<Dataset> datasets = new ArrayList<>(List.of(datasetOwnedBy(OWNER), datasetOwnedBy(OTHER_USER)));
        try (MockedStatic<KeycloakUtil> keycloakUtilMock = Mockito.mockStatic(KeycloakUtil.class)) {
            keycloakUtilMock.when(KeycloakUtil::isAdmin).thenReturn(true);
            assertTrue(datasetSecurityService.filterAnnotationDatasetList(datasets));
        }
        assertEquals(2, datasets.size());
        verifyNoInteractions(studyRightsService);
    }

    @Test
    public void filterKeepsUnownedDatasets() {
        UserRights userRights = Mockito.mock(UserRights.class);
        when(studyRightsService.getUserRights()).thenReturn(userRights);
        List<Dataset> datasets = new ArrayList<>(List.of(unownedDataset(), unownedDataset()));
        try (MockedStatic<KeycloakUtil> keycloakUtilMock = Mockito.mockStatic(KeycloakUtil.class)) {
            keycloakUtilMock.when(KeycloakUtil::isAdmin).thenReturn(false);
            keycloakUtilMock.when(KeycloakUtil::getTokenUserName).thenReturn(OWNER);
            assertTrue(datasetSecurityService.filterAnnotationDatasetList(datasets));
        }
        // no owner: the per-study rights are never consulted
        assertEquals(2, datasets.size());
    }

    @Test
    public void filterKeepsEveryAnnotationForReviewer() {
        UserRights userRights = Mockito.mock(UserRights.class);
        when(studyRightsService.getUserRights()).thenReturn(userRights);
        when(userRights.hasStudyRights(STUDY_ID, StudyUserRight.CAN_REVIEW.name())).thenReturn(true);
        List<Dataset> datasets = new ArrayList<>(List.of(datasetOwnedBy(OWNER), datasetOwnedBy(OTHER_USER)));
        try (MockedStatic<KeycloakUtil> keycloakUtilMock = Mockito.mockStatic(KeycloakUtil.class)) {
            keycloakUtilMock.when(KeycloakUtil::isAdmin).thenReturn(false);
            keycloakUtilMock.when(KeycloakUtil::getTokenUserName).thenReturn(OTHER_USER);
            assertTrue(datasetSecurityService.filterAnnotationDatasetList(datasets));
        }
        assertEquals(2, datasets.size());
    }

    @Test
    public void filterKeepsOwnAndUnownedRemovesOthersForAnnotator() {
        UserRights userRights = Mockito.mock(UserRights.class);
        when(studyRightsService.getUserRights()).thenReturn(userRights);
        when(userRights.hasStudyRights(STUDY_ID, StudyUserRight.CAN_REVIEW.name())).thenReturn(false);
        when(userRights.hasStudyRights(STUDY_ID, StudyUserRight.CAN_ANNOTATE.name())).thenReturn(true);
        Dataset own = datasetOwnedBy(OWNER);
        Dataset other = datasetOwnedBy(OTHER_USER);
        Dataset unowned = unownedDataset();
        List<Dataset> datasets = new ArrayList<>(List.of(own, other, unowned));
        try (MockedStatic<KeycloakUtil> keycloakUtilMock = Mockito.mockStatic(KeycloakUtil.class)) {
            keycloakUtilMock.when(KeycloakUtil::isAdmin).thenReturn(false);
            keycloakUtilMock.when(KeycloakUtil::getTokenUserName).thenReturn(OWNER);
            assertTrue(datasetSecurityService.filterAnnotationDatasetList(datasets));
        }
        // the annotator keeps his own annotation and the unowned one, loses the other's
        assertEquals(2, datasets.size());
        assertTrue(datasets.contains(own));
        assertTrue(datasets.contains(unowned));
        assertFalse(datasets.contains(other));
    }

    @Test
    public void filterRemovesOwnedAnnotationForUserWithoutAnnotateNorReview() {
        UserRights userRights = Mockito.mock(UserRights.class);
        when(studyRightsService.getUserRights()).thenReturn(userRights);
        when(userRights.hasStudyRights(STUDY_ID, StudyUserRight.CAN_REVIEW.name())).thenReturn(false);
        when(userRights.hasStudyRights(STUDY_ID, StudyUserRight.CAN_ANNOTATE.name())).thenReturn(false);
        Dataset own = datasetOwnedBy(OWNER);
        Dataset unowned = unownedDataset();
        List<Dataset> datasets = new ArrayList<>(List.of(own, unowned));
        try (MockedStatic<KeycloakUtil> keycloakUtilMock = Mockito.mockStatic(KeycloakUtil.class)) {
            keycloakUtilMock.when(KeycloakUtil::isAdmin).thenReturn(false);
            keycloakUtilMock.when(KeycloakUtil::getTokenUserName).thenReturn(OWNER);
            assertTrue(datasetSecurityService.filterAnnotationDatasetList(datasets));
        }
        // even his own annotation is removed without the CAN_ANNOTATE right; unowned stays
        assertEquals(1, datasets.size());
        assertTrue(datasets.contains(unowned));
        assertFalse(datasets.contains(own));
    }

}
