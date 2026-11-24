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

package org.shanoir.ng.preclinical;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.shanoir.ng.preclinical.anesthetics.anesthetic.Anesthetic;
import org.shanoir.ng.preclinical.anesthetics.anesthetic.AnestheticApiController;
import org.shanoir.ng.preclinical.anesthetics.anesthetic.AnestheticService;
import org.shanoir.ng.preclinical.anesthetics.examination_anesthetics.ExaminationAnesthetic;
import org.shanoir.ng.preclinical.anesthetics.examination_anesthetics.ExaminationAnestheticService;
import org.shanoir.ng.preclinical.pathologies.Pathology;
import org.shanoir.ng.preclinical.pathologies.PathologyApiController;
import org.shanoir.ng.preclinical.pathologies.PathologyService;
import org.shanoir.ng.preclinical.pathologies.pathology_models.PathologyModel;
import org.shanoir.ng.preclinical.pathologies.pathology_models.PathologyModelApiController;
import org.shanoir.ng.preclinical.pathologies.pathology_models.PathologyModelService;
import org.shanoir.ng.preclinical.pathologies.subject_pathologies.SubjectPathology;
import org.shanoir.ng.preclinical.pathologies.subject_pathologies.SubjectPathologyService;
import org.shanoir.ng.preclinical.therapies.Therapy;
import org.shanoir.ng.preclinical.therapies.TherapyApiController;
import org.shanoir.ng.preclinical.therapies.TherapyService;
import org.shanoir.ng.preclinical.therapies.subject_therapies.SubjectTherapy;
import org.shanoir.ng.preclinical.therapies.subject_therapies.SubjectTherapyService;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.storage.StorageService;
import org.shanoir.ng.utils.KeycloakUtil;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

/**
 * An anesthetic still given in examinations, or a therapy, a pathology or a pathology model still used by subjects,
 * of any study, cannot be deleted: the deletion is refused (409) without telling where it is used, as the user
 * may not see those studies.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CatalogueDeleteInUseTest {

    private static final Long ID = 1L;

    @Mock
    private TherapyService therapyService;

    @Mock
    private SubjectTherapyService subjectTherapyService;

    @Mock
    private PathologyService pathologyService;

    @Mock
    private PathologyModelService pathologyModelService;

    @Mock
    private SubjectPathologyService subjectPathologyService;

    @Mock
    private StorageService storageService;

    @Mock
    private ShanoirEventService eventService;

    @Mock
    private AnestheticService anestheticService;

    @Mock
    private ExaminationAnestheticService examAnestheticService;

    @InjectMocks
    private AnestheticApiController anesthetics;

    @InjectMocks
    private TherapyApiController therapies;

    @InjectMocks
    private PathologyApiController pathologies;

    @InjectMocks
    private PathologyModelApiController models;

    private MockedStatic<KeycloakUtil> keycloakUtil;

    private final Anesthetic anesthetic = new Anesthetic();

    private final Therapy therapy = new Therapy();

    private final Pathology pathology = new Pathology();

    private final PathologyModel model = new PathologyModel();

    @BeforeEach
    void setup() {
        keycloakUtil = Mockito.mockStatic(KeycloakUtil.class);
        keycloakUtil.when(KeycloakUtil::getTokenUserId).thenReturn(2L);
        model.setId(ID);
        when(anestheticService.findById(ID)).thenReturn(anesthetic);
        when(examAnestheticService.findByAnesthetic(anesthetic)).thenReturn(List.of());
        when(therapyService.findById(ID)).thenReturn(therapy);
        when(pathologyService.findById(ID)).thenReturn(pathology);
        when(pathologyModelService.findById(ID)).thenReturn(model);
        when(subjectTherapyService.findAllByTherapy(therapy)).thenReturn(List.of());
        when(subjectPathologyService.findAllByPathology(pathology)).thenReturn(List.of());
        when(subjectPathologyService.findAllByPathologyModel(model)).thenReturn(List.of());
        when(pathologyModelService.findByPathology(pathology)).thenReturn(List.of());
    }

    @AfterEach
    void closeUserMock() {
        keycloakUtil.close();
    }

    @Test
    void anestheticGivenInExaminationsIsNotDeleted() throws Exception {
        when(examAnestheticService.findByAnesthetic(anesthetic)).thenReturn(List.of(new ExaminationAnesthetic()));

        assertEquals(HttpStatus.CONFLICT, anesthetics.deleteAnesthetic(ID).getStatusCode());
        verify(anestheticService, never()).deleteById(anyLong());
    }

    @Test
    void unusedAnestheticIsDeleted() throws Exception {
        assertEquals(HttpStatus.OK, anesthetics.deleteAnesthetic(ID).getStatusCode());
        verify(anestheticService).deleteById(ID);
    }

    @Test
    void anestheticDeletionRefusedByTheDatabaseIsAConflict() throws Exception {
        doThrow(new DataIntegrityViolationException("fk")).when(anestheticService).deleteById(ID);

        assertEquals(HttpStatus.CONFLICT, anesthetics.deleteAnesthetic(ID).getStatusCode());
    }

    @Test
    void therapyGivenToSubjectsIsNotDeleted() throws Exception {
        when(subjectTherapyService.findAllByTherapy(therapy)).thenReturn(List.of(new SubjectTherapy()));

        assertEquals(HttpStatus.CONFLICT, therapies.deleteTherapy(ID).getStatusCode());
        verify(therapyService, never()).deleteById(anyLong());
    }

    @Test
    void unusedTherapyIsDeleted() throws Exception {
        assertEquals(HttpStatus.OK, therapies.deleteTherapy(ID).getStatusCode());
        verify(therapyService).deleteById(ID);
    }

    @Test
    void therapyDeletionRefusedByTheDatabaseIsAConflict() throws Exception {
        doThrow(new DataIntegrityViolationException("fk")).when(therapyService).deleteById(ID);

        assertEquals(HttpStatus.CONFLICT, therapies.deleteTherapy(ID).getStatusCode());
    }

    @Test
    void pathologyGivenToSubjectsIsNotDeleted() throws Exception {
        when(subjectPathologyService.findAllByPathology(pathology)).thenReturn(List.of(new SubjectPathology()));

        assertEquals(HttpStatus.CONFLICT, pathologies.deletePathology(ID).getStatusCode());
        verify(pathologyService, never()).deleteById(anyLong());
    }

    @Test
    void pathologyUsedByAModelIsNotDeleted() throws Exception {
        when(pathologyModelService.findByPathology(pathology)).thenReturn(List.of(new PathologyModel()));

        assertEquals(HttpStatus.CONFLICT, pathologies.deletePathology(ID).getStatusCode());
        verify(pathologyService, never()).deleteById(anyLong());
    }

    @Test
    void unusedPathologyIsDeleted() throws Exception {
        assertEquals(HttpStatus.OK, pathologies.deletePathology(ID).getStatusCode());
        verify(pathologyService).deleteById(ID);
    }

    @Test
    void pathologyModelGivenToSubjectsIsNotDeletedAndKeepsItsFiles() throws Exception {
        when(subjectPathologyService.findAllByPathologyModel(model)).thenReturn(List.of(new SubjectPathology()));

        assertEquals(HttpStatus.CONFLICT, models.deletePathologyModel(ID).getStatusCode());
        verify(pathologyModelService, never()).deleteById(anyLong());
        verify(storageService, never()).deletePathologyModelData(any(), any());
    }

    @Test
    void pathologyModelDeletionRefusedByTheDatabaseKeepsItsFiles() throws Exception {
        doThrow(new DataIntegrityViolationException("fk")).when(pathologyModelService).deleteById(ID);

        assertEquals(HttpStatus.CONFLICT, models.deletePathologyModel(ID).getStatusCode());
        verify(storageService, never()).deletePathologyModelData(any(), any());
    }

    @Test
    void unusedPathologyModelIsDeletedBeforeItsFiles() throws Exception {
        assertEquals(HttpStatus.OK, models.deletePathologyModel(ID).getStatusCode());
        InOrder order = inOrder(pathologyModelService, storageService);
        order.verify(pathologyModelService).deleteById(ID);
        order.verify(storageService).deletePathologyModelData(any(), any());
    }

}
