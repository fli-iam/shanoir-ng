import { ModuleWithProviders } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { AccountRequestComponent } from './users/account-request/account-request.component';
import { AcquisitionEquipmentComponent } from './acquisition-equipments/acquisition-equipment/acquisition-equipment.component';
import { AcquisitionEquipmentListComponent } from './acquisition-equipments/acquisition-equipment-list/acquisition-equipment-list.component';
import { AuthAdminGuard } from './shared/roles/auth-admin-guard';
import { AuthNotGuestGuard } from './shared/roles/auth-not-guest-guard';
import { CenterComponent } from './centers/center/center.component';
import { CenterListComponent } from './centers/center-list/center-list.component';
import { ExaminationComponent } from './examinations/examination/examination.component';
import { ExaminationListComponent } from './examinations/examination-list/examination-list.component';
import { ExtensionRequestComponent } from './users/extension-request/extension-request.component';
import { HomeComponent } from './home/home.component';
import { ImportComponent } from './import/import.component';
import { ManufacturerComponent } from './acquisition-equipments/manufacturer/manufacturer.component';
import { ManufacturerModelComponent } from './acquisition-equipments/manufacturer-model/manufacturer-model.component';
import { StudyComponent } from './studies/study/study.component';
import { StudyListComponent } from './studies/study-list/study-list.component';
import { StudyTreeComponent } from './studies/tree/study-tree.component';
import { UserComponent } from './users/user/user.component';
import { UserListComponent } from './users/user-list/user-list.component';
import { NewInstrumentComponent } from './examinations/instrument-assessment/new-instrument.component';
import { CoilListComponent } from './coils/coil-list/coil-list.component';
import { CoilComponent } from './coils/coil/coil.component';
import { SubjectListComponent } from './subjects/subject-list/subject-list.component';
import { SubjectComponent } from './subjects/subject/subject.component';
import { DatasetComponent } from './datasets/dataset/dataset.component';
import { DatasetListComponent } from './datasets/dataset-list/dataset-list.component';

let appRoutes: Routes = [
    {
        path: '',
        redirectTo: '/home',
        pathMatch: 'full'
    }, {
        path: 'account-request',
        component: AccountRequestComponent,
    }, {
        path: 'user',
        component: UserComponent
    }, {
        path: 'user-list',
        component: UserListComponent,
        canActivate: [AuthAdminGuard]
    }, {
        path: 'extension-request',
        component: ExtensionRequestComponent,
    }, {
        path: 'home',
        component: HomeComponent
    }, {
        path: 'imports',
        component: ImportComponent
    }, {
        path: 'manufacturer/create',
        component: ManufacturerComponent,
        canActivate: [AuthNotGuestGuard],
        data: { mode: 'create' }
    }, {
        path: 'manufacturer-model/create',
        component: ManufacturerModelComponent,
        canActivate: [AuthNotGuestGuard],
        data: { mode: 'create' }
    }, {
        path: 'new-instrument',
        component: NewInstrumentComponent,
        canActivate: [AuthNotGuestGuard]
    }
];

appRoutes = appRoutes.concat(
    getRoutesFor('study', StudyComponent, StudyListComponent),
    getRoutesFor('subject', SubjectComponent, SubjectListComponent),
    getRoutesFor('examination', ExaminationComponent, ExaminationListComponent),
    getRoutesFor('dataset', DatasetComponent, DatasetListComponent),
    getRoutesFor('center', CenterComponent, CenterListComponent),
    getRoutesFor('acquisition-equipment', AcquisitionEquipmentComponent, AcquisitionEquipmentListComponent),
    getRoutesFor('coil', CoilComponent, CoilListComponent),
);

export const routing: ModuleWithProviders = RouterModule.forRoot(appRoutes);

function getRoutesFor(entityName: string, entityComponent, listComponent): Routes {
    return [
        {
            path: entityName,
            redirectTo: entityName + '/list',
            pathMatch: 'full'
        }, {
            path: entityName + '/list',
            component: listComponent,
            canActivate: [AuthNotGuestGuard],
        }, {
            path: entityName+'/details/:id',
            component: entityComponent,
            data: { mode: 'view' }
        }, {
            path: entityName+'/edit/:id',
            component: entityComponent,
            data: { mode: 'edit' }
        }, {
            path: entityName+'/create',
            component: entityComponent,
            data: { mode: 'create' }
        }
    ];
};