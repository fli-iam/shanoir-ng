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
import { RouterModule, Routes, CanActivate } from '@angular/router';
import { AcquisitionEquipmentListComponent } from './acquisition-equipments/acquisition-equipment-list/acquisition-equipment-list.component';
import { AcquisitionEquipmentComponent } from './acquisition-equipments/acquisition-equipment/acquisition-equipment.component';
import { ManufacturerModelComponent } from './acquisition-equipments/manufacturer-model/manufacturer-model.component';
import { ManufacturerComponent } from './acquisition-equipments/manufacturer/manufacturer.component';
import { CenterListComponent } from './centers/center-list/center-list.component';
import { CenterComponent } from './centers/center/center.component';
import { CoilListComponent } from './coils/coil-list/coil-list.component';
import { CoilComponent } from './coils/coil/coil.component';
import { DatasetListComponent } from './datasets/dataset-list/dataset-list.component';
import { DatasetComponent } from './datasets/dataset/dataset.component';
import { EegDatasetComponent } from './datasets/dataset/eeg/dataset.eeg.component';
import { ExaminationListComponent } from './examinations/examination-list/examination-list.component';
import { ExaminationComponent } from './examinations/examination/examination.component';
import { NewInstrumentComponent } from './examinations/instrument-assessment/new-instrument.component';
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
import { CanImportFromPACSGuard } from './shared/roles/auth-can-import-from-PACS-guard';
import { AuthAdminOrExpertGuard } from './shared/roles/auth-admin-or-expert-guard';
import { StudyListComponent } from './studies/study-list/study-list.component';
import { StudyComponent } from './studies/study/study.component';
import { SubjectListComponent } from './subjects/subject-list/subject-list.component';
import { SubjectComponent } from './subjects/subject/subject.component';
import { AccountRequestComponent } from './users/account-request/account-request.component';
import { ExtensionRequestComponent } from './users/extension-request/extension-request.component';
import { UserListComponent } from './users/user-list/user-list.component';
import { UserComponent } from './users/user/user.component';
import { AsyncTasksComponent } from './async-tasks/async-tasks.component';
import { SolrSearchComponent } from './solr/solr.search.component';

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
                data: {importMode: 'PACS'},
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
        ]
    }, {
        path: 'new-instrument',
        component: NewInstrumentComponent
    }, {
        path: 'task',
        component: AsyncTasksComponent
    }
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
    getRoutesFor('manufacturer', ManufacturerComponent, HomeComponent, {create: AuthAdminOrExpertGuard, update: AuthAdminOrExpertGuard}),
    getRoutesFor('manufacturer-model', ManufacturerModelComponent, HomeComponent, {create: AuthAdminOrExpertGuard, update: AuthAdminOrExpertGuard})
);

export const routing: ModuleWithProviders = RouterModule.forRoot(appRoutes); 

export function getRoutesFor(entityName: string, entityComponent, listComponent, 
        auth: {read?: any, create?: any, update?: any} ): Routes {

    return [
        {
            path: entityName,
            redirectTo: entityName + '/list',
            pathMatch: 'full',
        }, {
            path: entityName + '/list',
            component: listComponent,
            canActivate: auth.read ? [auth.read] : undefined,
        }, {
            path: entityName+'/details/:id',
            component: entityComponent,
            data: { mode: 'view' },
            canActivate: auth.read ? [auth.read] : undefined,
        }, {
            path: entityName+'/edit/:id',
            component: entityComponent,
            data: { mode: 'edit' },
            canActivate: auth.update ? [auth.update] : undefined,
        }, {
            path: entityName+'/create',
            component: entityComponent,
            data: { mode: 'create' },
            canActivate: auth.create ? [auth.create] : undefined,
        }
    ];
};