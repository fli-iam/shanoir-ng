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
import { ModuleWithProviders } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { AcquisitionEquipmentListComponent} from './acquisition-equipments/acquisition-equipment-list/acquisition-equipment-list.component';
import { AcquisitionEquipmentComponent } from './acquisition-equipments/acquisition-equipment/acquisition-equipment.component';
import { ManufacturerModelComponent } from './acquisition-equipments/manufacturer-model/manufacturer-model.component';
import { ManufacturerComponent } from './acquisition-equipments/manufacturer/manufacturer.component';
import { AsyncTasksComponent } from './async-tasks/async-tasks.component';
import { CenterListComponent } from './centers/center-list/center-list.component';
import { CenterComponent } from './centers/center/center.component';
import { CoilListComponent } from './coils/coil-list/coil-list.component';
import { CoilComponent } from './coils/coil/coil.component';
import { DatasetAcquisitionListComponent } from './dataset-acquisitions/dataset-acquisition-list/dataset-acquisition-list.component';
import { DatasetAcquisitionComponent } from './dataset-acquisitions/dataset-acquisition/dataset-acquisition.component';
import { DatasetListComponent } from './datasets/dataset-list/dataset-list.component';
import { DatasetComponent } from './datasets/dataset/dataset.component';
import { EegDatasetComponent } from './datasets/dataset/eeg/dataset.eeg.component';
import { ExaminationListComponent } from './examinations/examination-list/examination-list.component';
import { ExaminationComponent } from './examinations/examination/examination.component';
import { HomeComponent } from './home/home.component';
import { ClinicalContextComponent } from './import/clinical-context/clinical-context.component';
import { EegClinicalContextComponent } from './import/eeg-clinical-context/eeg-clinical-context.component';
import { DicomUploadComponent } from './import/dicom-upload/dicom-upload.component';
import { EegUploadComponent } from './import/eeg-upload/eeg-upload.component';
import { BidsUploadComponent } from './import/bids/bids-upload.component';
import { FinishImportComponent } from './import/finish/finish.component';
import { FinishEegImportComponent } from './import/eeg-finish/eeg-finish.component';
import { ImportComponent } from './import/import.component';
import { QueryPacsComponent } from './import/query-pacs/query-pacs.component';
import { SelectSeriesComponent } from './import/select-series/select-series.component';
import { EegSelectSeriesComponent } from './import/eeg-select-series/eeg-select-series.component';
import { AuthAdminGuard } from './shared/roles/auth-admin-guard';
import { AuthAdminOrExpertGuard } from './shared/roles/auth-admin-or-expert-guard';
import { CanImportFromPACSGuard } from './shared/roles/auth-can-import-from-PACS-guard';
import { StudyListComponent } from './studies/study-list/study-list.component';
import { StudyComponent } from './studies/study/study.component';
import { StudyCardListComponent } from './study-cards/study-card-list/study-card-list.component';
import { StudyCardComponent } from './study-cards/study-card/study-card.component';
import { SubjectListComponent } from './subjects/subject-list/subject-list.component';
import { SubjectComponent } from './subjects/subject/subject.component';
import { InstrumentAssessmentComponent } from './examinations/instrument-assessment/instrument-assessment.component';
import { AccountRequestComponent } from './users/account-request/account-request.component';
import { ExtensionRequestComponent } from './users/extension-request/extension-request.component';
import { UserListComponent } from './users/user-list/user-list.component';
import { UserComponent } from './users/user/user.component';
import { StudyCardForRulesListComponent } from './study-cards/study-card-list/study-card-list-for-rules.component';
import { SolrSearchComponent } from './solr/solr.search.component';
import { BrukerUploadComponent } from './preclinical/importBruker/bruker-upload/bruker-upload.component';
import { BrukerSelectSeriesComponent } from './preclinical/importBruker/select-series/bruker-select-series.component';
import { BrukerFinishImportComponent } from './preclinical/importBruker/finish/bruker-finish.component';
import { ContrastAgentsListComponent } from './preclinical/contrastAgent/list/contrastAgent-list.component';
import { ContrastAgentFormComponent } from './preclinical/contrastAgent/edit/contrastAgent-form.component';
import { ReferenceFormComponent } from './preclinical/reference/edit/reference-form.component';
import { ReferencesListComponent } from './preclinical/reference/list/reference-list.component';
import { AnimalExaminationFormComponent } from './preclinical/examination/edit/animal-examination-form.component';
import { AnimalExaminationListComponent } from './preclinical/examination/list/animal-examination-list.component';
import { TherapiesListComponent } from './preclinical/therapies/therapy/list/therapy-list.component';
import { TherapyFormComponent } from './preclinical/therapies/therapy/edit/therapy-form.component';
import { PathologyFormComponent } from './preclinical/pathologies/pathology/edit/pathology-form.component';
import { PathologiesListComponent } from './preclinical/pathologies/pathology/list/pathology-list.component';
import { PathologyModelFormComponent } from './preclinical/pathologies/pathologyModel/edit/pathologyModel-form.component';
import { AnestheticIngredientFormComponent } from './preclinical/anesthetics/ingredients/edit/anestheticIngredient-form.component';
import { PathologyModelsListComponent } from './preclinical/pathologies/pathologyModel/list/pathologyModel-list.component';
import { AnestheticIngredientsListComponent } from './preclinical/anesthetics/ingredients/list/anestheticIngredient-list.component';
import { AnestheticsListComponent } from './preclinical/anesthetics/anesthetic/list/anesthetic-list.component';
import { AnimalSubjectsListComponent } from './preclinical/animalSubject/list/animalSubject-list.component';
import { AnestheticFormComponent } from './preclinical/anesthetics/anesthetic/edit/anesthetic-form.component';
import { AnimalSubjectFormComponent } from './preclinical/animalSubject/edit/animalSubject-form.component';
import { DownloadStatisticsComponent } from './datasets/download-statistics/download-statistics.component';

let appRoutes: Routes = [
    {
        path: '',
        redirectTo: '/home',
        pathMatch: 'full'
    }, {
        path: 'account-request',
        component: AccountRequestComponent,
    }, {
        path: 'extension-request',
        component: ExtensionRequestComponent,
    }, {
        path: 'home',
        component: HomeComponent
    }, {
        path: 'solr-search',
        component: SolrSearchComponent
    }, {
        path: 'imports',
        component: ImportComponent,
        children: [
            {
                path: '',
                pathMatch: 'full',
                redirectTo: '/home'
            }, {   
                path: 'upload',
                component: DicomUploadComponent,
                data: {importMode: 'DICOM'}
            }, {   
                path: 'bruker',
                component: BrukerUploadComponent,
                data: {importMode: 'BRUKER'}
            }, {   
                path: 'eeg',
                component: EegUploadComponent,
                data: {importMode: 'EEG'}
            }, {   
                path: 'bids',
                component: BidsUploadComponent,
                data: {importMode: 'BIDS'}
            }, {
                path: 'pacs',
                component: QueryPacsComponent,
                canActivate: [CanImportFromPACSGuard]
            }, {
                path: 'series',
                component: SelectSeriesComponent
            }, {
                path: 'eegseries',
                component: EegSelectSeriesComponent
            }, {
                path: 'context',
                component: ClinicalContextComponent
            }, {
                path: 'eegcontext',
                component: EegClinicalContextComponent
            }, {
                path: 'finish',
                component: FinishImportComponent
            }
            , {
                path: 'eegfinish',
                component: FinishEegImportComponent
            }
            , {
                path: 'bruker',
                component: BrukerUploadComponent
            }, {
                path: 'brukerseries',
                component: BrukerSelectSeriesComponent
            }, {
                path: 'brukerfinish',
                component: BrukerFinishImportComponent
            }
        ]
    }, {
        path: 'task',
        component: AsyncTasksComponent
    }, {
        path: 'study-card/select-rule',
        children: [
            {
                path: 'list/:id',
                component: StudyCardForRulesListComponent,
            }, {
                path: 'select/:id',
                component: StudyCardComponent,
                data: { mode: 'view', select: true }
            }
        ]
    },{ 
        path: 'preclinical-contrastagents', 
        component: ContrastAgentsListComponent
    },{ 
        path: 'preclinical-contrastagent', 
        component: ContrastAgentFormComponent
    },{ 
        path: 'download-statistics', 
        component: DownloadStatisticsComponent
    },
];

appRoutes = appRoutes.concat(
    getRoutesFor('study', StudyComponent, StudyListComponent, {create: AuthAdminOrExpertGuard, update: AuthAdminOrExpertGuard}),
    getRoutesFor('subject', SubjectComponent, SubjectListComponent, {update: AuthAdminOrExpertGuard}),
    getRoutesFor('examination', ExaminationComponent, ExaminationListComponent, {update: AuthAdminGuard}),
    getRoutesFor('dataset', DatasetComponent, DatasetListComponent, {update: AuthAdminOrExpertGuard}),
    getRoutesFor('center', CenterComponent, CenterListComponent, {create: AuthAdminOrExpertGuard, update: AuthAdminOrExpertGuard}),
    getRoutesFor('acquisition-equipment', AcquisitionEquipmentComponent, AcquisitionEquipmentListComponent, {create: AuthAdminOrExpertGuard, update: AuthAdminOrExpertGuard}),
    getRoutesFor('coil', CoilComponent, CoilListComponent, {create: AuthAdminOrExpertGuard, update: AuthAdminOrExpertGuard}),
    getRoutesFor('user', UserComponent, UserListComponent, {create: AuthAdminGuard, update: AuthAdminGuard}),
    getRoutesFor('manufacturer', ManufacturerComponent, null, {create: AuthAdminOrExpertGuard, update: AuthAdminOrExpertGuard}),
    getRoutesFor('manufacturer-model', ManufacturerModelComponent, null, {create: AuthAdminOrExpertGuard, update: AuthAdminOrExpertGuard}),
    getRoutesFor('study-card', StudyCardComponent, StudyCardListComponent, {create: AuthAdminOrExpertGuard, update: AuthAdminOrExpertGuard}),
    getRoutesFor('dataset-acquisition', DatasetAcquisitionComponent, DatasetAcquisitionListComponent, {update: AuthAdminOrExpertGuard}),
    getRoutesFor('instrument', InstrumentAssessmentComponent, null, {create: AuthAdminOrExpertGuard, update: AuthAdminOrExpertGuard}),
    getRoutesFor('preclinical-reference', ReferenceFormComponent, ReferencesListComponent, {create: AuthAdminOrExpertGuard, update: AuthAdminOrExpertGuard}), 
    getRoutesFor('preclinical-examination', AnimalExaminationFormComponent, AnimalExaminationListComponent, {create: AuthAdminOrExpertGuard, update: AuthAdminOrExpertGuard}),
    getRoutesFor('preclinical-therapy', TherapyFormComponent, TherapiesListComponent, {create: AuthAdminOrExpertGuard, update: AuthAdminOrExpertGuard}),
    getRoutesFor('preclinical-pathology', PathologyFormComponent, PathologiesListComponent, {create: AuthAdminOrExpertGuard, update: AuthAdminOrExpertGuard}), 
    getRoutesFor('preclinical-pathology-model', PathologyModelFormComponent, PathologyModelsListComponent, {create: AuthAdminOrExpertGuard, update: AuthAdminOrExpertGuard}),
    getRoutesFor('preclinical-anesthetic-ingredient', AnestheticIngredientFormComponent, AnestheticIngredientsListComponent, {create: AuthAdminOrExpertGuard, update: AuthAdminOrExpertGuard}),
    getRoutesFor('preclinical-anesthetic', AnestheticFormComponent, AnestheticsListComponent, {create: AuthAdminOrExpertGuard, update: AuthAdminOrExpertGuard}),
    getRoutesFor('preclinical-subject', AnimalSubjectFormComponent, AnimalSubjectsListComponent, {create: AuthAdminOrExpertGuard, update: AuthAdminOrExpertGuard})
);

export const routing: ModuleWithProviders = RouterModule.forRoot(appRoutes); 

export function getRoutesFor(entityName: string, entityComponent, listComponent, 
        auth: {read?: any, create?: any, update?: any} ): Routes {

    let routes = [];
    routes.push(
        {
            path: entityName,
            redirectTo: entityName + '/list',
            pathMatch: 'full',
        }
    );
    if (entityComponent) {
        routes.push(
            {
                path: entityName + '/details/:id',
                component: entityComponent,
                data: { mode: 'view' },
                canActivate: auth.read ? [auth.read] : undefined,
            }, {
                path: entityName + '/edit/:id',
                component: entityComponent,
                data: { mode: 'edit' },
                canActivate: auth.update ? [auth.update] : undefined,
            }, {
                path: entityName + '/create',
                component: entityComponent,
                data: { mode: 'create' },
                canActivate: auth.create ? [auth.create] : undefined,
            }
        );
    }
    if (listComponent) {
        routes.push(
            {
                path: entityName + '/list',
                component: listComponent,
                canActivate: auth.read ? [auth.read] : undefined,
            }
        );
    }
    return routes;
};