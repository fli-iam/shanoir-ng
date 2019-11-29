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


import { NgModule } from '@angular/core';
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
import { BoutiquesDatasetListComponent } from './boutiques/dataset-list/dataset-list.component';
import { BoutiquesDatasetComponent } from './boutiques/dataset/dataset.component';
import { InvocationExecutionComponent } from './boutiques/invocation-execution/invocation-execution.component';
import { DatasetComponent } from './datasets/dataset/dataset.component';
import { ExaminationListComponent } from './examinations/examination-list/examination-list.component';
import { ExaminationComponent } from './examinations/examination/examination.component';
import { NewInstrumentComponent } from './examinations/instrument-assessment/new-instrument.component';
import { HomeComponent } from './home/home.component';
import { ClinicalContextComponent } from './import/clinical-context/clinical-context.component';
import { DicomUploadComponent } from './import/dicom-upload/dicom-upload.component';
import { FinishImportComponent } from './import/finish/finish.component';
import { ImportComponent } from './import/import.component';
import { QueryPacsComponent } from './import/query-pacs/query-pacs.component';
import { SelectSeriesComponent } from './import/select-series/select-series.component';
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
import { BoutiquesComponent } from './boutiques/boutiques.component';

let routes: Routes = [
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
                path: 'pacs',
                component: QueryPacsComponent,
                data: {importMode: 'PACS'},
                canActivate: [CanImportFromPACSGuard]
            }, {
                path: 'series',
                component: SelectSeriesComponent
            }, {
                path: 'context',
                component: ClinicalContextComponent
            }, {
                path: 'finish',
                component: FinishImportComponent
            }
        ]
    }, {
        path: 'new-instrument',
        component: NewInstrumentComponent
    }, {
        path: 'task',
        component: AsyncTasksComponent
    }, {
        path: 'boutiques',
        component: BoutiquesComponent
    }, {
        path: 'boutiques/:toolId',
        component: InvocationExecutionComponent
    },


    // Automatically generated routes from:
    // https://www.typescriptlang.org/play/#code/LAKAZgrgdgxgLgSwPZQAQHMCmcBKSJyYDOAYkgE4AUmUicAngHICGAtpgFypFzkJToANKhp16AYSSsADilEdmUesIA2CHpJlzaCpcNCpDRw8wIALLgG9ymZgBMA-F0XLUMG80JPUL4RGl2npjeLgC+qACUzkqolqAGRjZwEORoANoJxoZxIFl5qNKeFiK0CAws7IKZ+YY2dgg28AAqSFyiZUxsmKgA1KgA5AD0ajz9Vbk1RoVwZgCynjDF-ZAqKmPVxqHCOZNTRW2l5V29A8PqcOsTuzBSslDyqCNwmnei47uGMIoAgvAIAG5BZzmAB0HjsqAcqDSphmYNsdgAuqguNA7JgwPxMHZ3vktrENvlpsV2kd2D0hui4MwECoiIMOAg7JcPm5bto4AcxC8ObjdoFqVZUKwkOiuP1-ghMAB3fqoLaEvJfKC-RCAwjAuHgyHQ2FmeH2ZGoqDozH3HGK+XbS3GYlcjoVTAUwbYsoMpksj43LT3HQlbns31wPmTAXMIUisUDV0XK02ozK1UAoE+UH+AXdKEwtMBIJG1BojFYi1XLL4nYfO3+h1dZ3uWyET3XQMPUkSFu0EM1MMR0WcAb1oJyhWlmqJv7q-t6kGDwg67Nw2eYfOFs3YrtGULVREAblAoT3IFAKmwqHI+EIRGi9DSyIAvNDd-EQOeCMRUA-X5eZygvnBKNUWC4BexBkFQ-Q8BAdj0GMAwAMrJNBPJBrB-QIVB9AADLnMhoiwZYS7it85jfHYrD8AA8uQACiAAe0iYOQcAAOIQMw5DMn4uYagMxEzKR5FQFRdEMUxrHscyoQRCGQF4G+pAUJQEEQAARgAVpg8CoXBqkafAuG0NpumaXA2EaB2FzbOmKb9HxZgCfw4kcf0UkydgcmXmBSmYLRbD8J4yBQKhdF+VAAUoAZlkDCFgnhVAZnPBZ+HWTxtkkWRjlsc5rmAe5IEKeBYZENgqEACKeMwxWJT6eHCP05XUlVCWRcl3H9ml-EZUJNH0YxLFZZJ0m5cB8lef0MCiIxqHiJN5AtXVM20IxzVJdshG8elgnCb1YkDbBKXtXZDndSJfVOYNbkjZ5in9MwMAAI4QOoZSBQAtJgj0INI7CGXVvyfUQL0oNRn3faI828Q9T2A4gwOgz9pk4atsTrR19lddton9RJ+1tURm2UT1WPnS5Q2lrJ+VjTctLTUgtIQ-0ki0itNW-SjHipUdGNE2de1cRm+OdVtPO7TjOXk3lo03RAxXkKhACqssM4ry1I6zUUERzh0E1AJP8zZXOCST4vGBTUvgawigQGAd3JDYct1fMUDW7bKSMQzAASUiYBDmsNtrQuE6dovOfrnM65jvNi2TpuS9dFtWzb8Bu+Qr2RpgayO4nrv27MfYqJ73u+6jhtBzt2OhwWeMbYHJ3l8bESgBEh6gDcUBEEgJ4gioSDoJQX7EI3R4gCecCoBe0gEB+AxpAAOkFh5gBQqCUKPZ4gePYDr-JEQEqWE9Tz0D79LPcCWPP-TVAfY9HwMp+n1Ws-9HKfQDyCxInCfYwX4exgIFv-cQLwnqI0OALRd4VjyNfXox9768FdKAloXAn4v23oQYBDQTItE-k-QQP9qhblLP-Feb9vSvFoBAy00Db4nzgKfMhHJxQnFIRZT+eCF4EOqMQwBb4QRhkoaOIw1DYF0LgD2WIwo+zIOfswoBYYQTpxwcOdh-Rf6bi4QA0hPwJxBAETUYRd9RHjjVCmNIqCtEqh0YQNIAAGZEfR+iIhUWowwhDjAGNoVsC++5W4oA7l3Hufdr4RCAA
    // See code below
    {
        path: 'study',
        redirectTo: 'study/list',
    },
    {
        path: 'study/list',
        component: StudyListComponent,
    },
    {
        path: 'study/details/:id',
        component: StudyComponent,
        data: { mode: 'view' },
    },
    {
        path: 'study/edit/:id',
        component: StudyComponent,
        data: { mode: 'edit' },
        canActivate: [AuthAdminOrExpertGuard],
    },
    {
        path: 'study/create',
        component: StudyComponent,
        data: { mode: 'create' },
        canActivate: [AuthAdminOrExpertGuard],
    },
    {
        path: 'subject',
        redirectTo: 'subject/list',
    },
    {
        path: 'subject/list',
        component: SubjectListComponent,
    },
    {
        path: 'subject/details/:id',
        component: SubjectComponent,
        data: { mode: 'view' },
    },
    {
        path: 'subject/edit/:id',
        component: SubjectComponent,
        data: { mode: 'edit' },
        canActivate: [AuthAdminGuard],
    },
    {
        path: 'subject/create',
        component: SubjectComponent,
        data: { mode: 'create' },
    },
    {
        path: 'examination',
        redirectTo: 'examination/list',
    },
    {
        path: 'examination/list',
        component: ExaminationListComponent,
    },
    {
        path: 'examination/details/:id',
        component: ExaminationComponent,
        data: { mode: 'view' },
    },
    {
        path: 'examination/edit/:id',
        component: ExaminationComponent,
        data: { mode: 'edit' },
        canActivate: [AuthAdminGuard],
    },
    {
        path: 'examination/create',
        component: ExaminationComponent,
        data: { mode: 'create' },
    },
    {
        path: 'dataset',
        redirectTo: 'dataset/list',
    },
    {
        path: 'dataset/list',
        component: DatasetListComponent,
    },
    {
        path: 'boutiques/dataset/list',
        component: BoutiquesDatasetListComponent,
    },
    {
        path: 'boutiques/dataset/details/:id',
        component: BoutiquesDatasetComponent,
        data: { mode: 'view' },
    },
    {
        path: 'dataset/details/:id',
        component: DatasetComponent,
        data: { mode: 'view' },
    },
    {
        path: 'dataset/edit/:id',
        component: DatasetComponent,
        data: { mode: 'edit' },
        canActivate: [AuthAdminOrExpertGuard],
    },
    {
        path: 'dataset/create',
        component: DatasetComponent,
        data: { mode: 'create' },
    },
    {
        path: 'center',
        redirectTo: 'center/list',
    },
    {
        path: 'center/list',
        component: CenterListComponent,
    },
    {
        path: 'center/details/:id',
        component: CenterComponent,
        data: { mode: 'view' },
    },
    {
        path: 'center/edit/:id',
        component: CenterComponent,
        data: { mode: 'edit' },
        canActivate: [AuthAdminOrExpertGuard],
    },
    {
        path: 'center/create',
        component: CenterComponent,
        data: { mode: 'create' },
        canActivate: [AuthAdminOrExpertGuard],
    },
    {
        path: 'acquisition-equipment',
        redirectTo: 'acquisition-equipment/list',
    },
    {
        path: 'acquisition-equipment/list',
        component: AcquisitionEquipmentListComponent,
    },
    {
        path: 'acquisition-equipment/details/:id',
        component: AcquisitionEquipmentComponent,
        data: { mode: 'view' },
    },
    {
        path: 'acquisition-equipment/edit/:id',
        component: AcquisitionEquipmentComponent,
        data: { mode: 'edit' },
        canActivate: [AuthAdminOrExpertGuard],
    },
    {
        path: 'acquisition-equipment/create',
        component: AcquisitionEquipmentComponent,
        data: { mode: 'create' },
        canActivate: [AuthAdminOrExpertGuard],
    },
    {
        path: 'coil',
        redirectTo: 'coil/list',
    },
    {
        path: 'coil/list',
        component: CoilListComponent,
    },
    {
        path: 'coil/details/:id',
        component: CoilComponent,
        data: { mode: 'view' },
    },
    {
        path: 'coil/edit/:id',
        component: CoilComponent,
        data: { mode: 'edit' },
        canActivate: [AuthAdminOrExpertGuard],
    },
    {
        path: 'coil/create',
        component: CoilComponent,
        data: { mode: 'create' },
        canActivate: [AuthAdminOrExpertGuard],
    },
    {
        path: 'user',
        redirectTo: 'user/list',
    },
    {
        path: 'user/list',
        component: UserListComponent,
    },
    {
        path: 'user/details/:id',
        component: UserComponent,
        data: { mode: 'view' },
    },
    {
        path: 'user/edit/:id',
        component: UserComponent,
        data: { mode: 'edit' },
        canActivate: [AuthAdminGuard],
    },
    {
        path: 'user/create',
        component: UserComponent,
        data: { mode: 'create' },
        canActivate: [AuthAdminGuard],
    },
    {
        path: 'manufacturer',
        redirectTo: 'manufacturer/list',
    },
    {
        path: 'manufacturer/list',
        component: HomeComponent,
    },
    {
        path: 'manufacturer/details/:id',
        component: ManufacturerComponent,
        data: { mode: 'view' },
    },
    {
        path: 'manufacturer/edit/:id',
        component: ManufacturerComponent,
        data: { mode: 'edit' },
        canActivate: [AuthAdminOrExpertGuard],
    },
    {
        path: 'manufacturer/create',
        component: ManufacturerComponent,
        data: { mode: 'create' },
        canActivate: [AuthAdminOrExpertGuard],
    },
    {
        path: 'manufacturer-model',
        redirectTo: 'manufacturer-model/list',
    },
    {
        path: 'manufacturer-model/list',
        component: HomeComponent,
    },
    {
        path: 'manufacturer-model/details/:id',
        component: ManufacturerModelComponent,
        data: { mode: 'view' },
    },
    {
        path: 'manufacturer-model/edit/:id',
        component: ManufacturerModelComponent,
        data: { mode: 'edit' },
        canActivate: [AuthAdminOrExpertGuard],
    },
    {
        path: 'manufacturer-model/create',
        component: ManufacturerModelComponent,
        data: { mode: 'create' },
        canActivate: [AuthAdminOrExpertGuard],
    },

];

// Angular AOT compilation requires routes to be static, the following code would not work (see below for static routes generation code):

// routes = routes.concat(
//     getRoutesFor('study', StudyComponent, StudyListComponent, {create: AuthAdminOrExpertGuard, update: AuthAdminOrExpertGuard}),
//     getRoutesFor('subject', SubjectComponent, SubjectListComponent, {update: AuthAdminGuard}),
//     getRoutesFor('examination', ExaminationComponent, ExaminationListComponent, {update: AuthAdminGuard}),
//     // getRoutesFor('dataset', DatasetComponent, DatasetListComponent, {update: AuthAdminOrExpertGuard}),
//     getRoutesFor('center', CenterComponent, CenterListComponent, {create: AuthAdminOrExpertGuard, update: AuthAdminOrExpertGuard}),
//     getRoutesFor('acquisition-equipment', AcquisitionEquipmentComponent, AcquisitionEquipmentListComponent, {create: AuthAdminOrExpertGuard, update: AuthAdminOrExpertGuard}),
//     getRoutesFor('coil', CoilComponent, CoilListComponent, {create: AuthAdminOrExpertGuard, update: AuthAdminOrExpertGuard}),
//     getRoutesFor('user', UserComponent, UserListComponent, {create: AuthAdminGuard, update: AuthAdminGuard}),
//     getRoutesFor('manufacturer', ManufacturerComponent, HomeComponent, {create: AuthAdminOrExpertGuard, update: AuthAdminOrExpertGuard}),
//     getRoutesFor('manufacturer-model', ManufacturerModelComponent, HomeComponent, {create: AuthAdminOrExpertGuard, update: AuthAdminOrExpertGuard})
// );

// function getRoutesFor(entityName: string, entityComponent, listComponent, 
//        // auth): Routes {
//         auth: {read?: any, create?: any, update?: any} ): Routes {

//     return [
//         {
//             path: entityName,
//             redirectTo: entityName + '/list',
//             pathMatch: 'full',
//         }, {
//             path: entityName + '/list',
//             component: listComponent,
//             canActivate: auth.read ? [auth.read] : undefined,
//         }, {
//             path: entityName+'/details/:id',
//             component: entityComponent,
//             data: { mode: 'view' },
//             canActivate: auth.read ? [auth.read] : undefined,
//         }, {
//             path: entityName+'/edit/:id',
//             component: entityComponent,
//             data: { mode: 'edit' },
//             canActivate: auth.update ? [auth.update] : undefined,
//         }, {
//             path: entityName+'/create',
//             component: entityComponent,
//             data: { mode: 'create' },
//             canActivate: auth.create ? [auth.create] : undefined,
//         }
//     ];
// };

// function getRoutesFor(entityName: string, entityComponent:any, listComponent:any, 
//         auth: {read?: any, create?: any, update?: any} ): any {

//     return [
//         {
//             path: entityName,
//             redirectTo: entityName + '/list',
//             pathMatch: 'full',
//         }, {
//             path: entityName + '/list',
//             component: listComponent,
//             canActivate: auth.read ? [auth.read] : undefined,
//         }, {
//             path: entityName+'/details/:id',
//             component: entityComponent,
//             data: { mode: 'view' },
//             canActivate: auth.read ? [auth.read] : undefined,
//         }, {
//             path: entityName+'/edit/:id',
//             component: entityComponent,
//             data: { mode: 'edit' },
//             canActivate: auth.update ? [auth.update] : undefined,
//         }, {
//             path: entityName+'/create',
//             component: entityComponent,
//             data: { mode: 'create' },
//             canActivate: auth.create ? [auth.create] : undefined,
//         }
//     ];
// };

// Code to generated the routes statically
//  (the function getRoutesFor does not change)

// let routes: any[] = [];

// routes = routes.concat(
//     getRoutesFor('study', 'StudyComponent', 'StudyListComponent', {create: 'AuthAdminOrExpertGuard', update: 'AuthAdminOrExpertGuard'}),
//     getRoutesFor('subject', 'SubjectComponent', 'SubjectListComponent', {update: 'AuthAdminGuard'}),
//     getRoutesFor('examination', 'ExaminationComponent', 'ExaminationListComponent', {update: 'AuthAdminGuard'}),
//     getRoutesFor('dataset', 'DatasetComponent', 'DatasetListComponent', {update: 'AuthAdminOrExpertGuard'}),
//     getRoutesFor('center', 'CenterComponent', 'CenterListComponent', {create: 'AuthAdminOrExpertGuard', update: 'AuthAdminOrExpertGuard'}),
//     getRoutesFor('acquisition-equipment', 'AcquisitionEquipmentComponent', 'AcquisitionEquipmentListComponent', {create: 'AuthAdminOrExpertGuard', update: 'AuthAdminOrExpertGuard'}),
//     getRoutesFor('coil', 'CoilComponent', 'CoilListComponent', {create: 'AuthAdminOrExpertGuard', update: 'AuthAdminOrExpertGuard'}),
//     getRoutesFor('user', 'UserComponent', 'UserListComponent', {create: 'AuthAdminGuard', update: 'AuthAdminGuard'}),
//     getRoutesFor('manufacturer', 'ManufacturerComponent', 'HomeComponent', {create: 'AuthAdminOrExpertGuard', update: 'AuthAdminOrExpertGuard'}),
//     getRoutesFor('manufacturer-model', 'ManufacturerModelComponent', 'HomeComponent', {create: 'AuthAdminOrExpertGuard', update: 'AuthAdminOrExpertGuard'})
// );

// console.log(routes)

// let output = '[\n';
// for (let route of routes) {
//     output += '\t{\n'
//     output += '\t\tpath: \'' + route.path + '\',\n';
//     if (route.redirectTo) {
//         output += '\t\tredirectTo: \'' + route.redirectTo + '\',\n';
//     }
//     if (route.component) {
//         output += '\t\tcomponent: ' + route.component + ',\n';
//     }
//     if (route.data) {
//         output += '\t\tdata: { mode: \'' + route.data.mode + '\' },\n';
//     }
//     if (route.canActivate) {
//        output += '\t\tcanActivate: [' + route.canActivate[0] + '],\n';
//     }
//     output += '\t},\n'
// }

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
