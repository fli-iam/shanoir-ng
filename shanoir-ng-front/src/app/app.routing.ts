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

const appRoutes: Routes = [
  {
    path: '',
    redirectTo: '/home',
    pathMatch: 'full'
  }, {
    path: 'account-request',
    component: AccountRequestComponent,
  }, {
    path: 'acquisition-equipment',
    component: AcquisitionEquipmentComponent,
    canActivate: [AuthNotGuestGuard]
  }, {
    path: 'acquisition-equipment-list',
    component: AcquisitionEquipmentListComponent,
    canActivate: [AuthNotGuestGuard]
  }, {
    path: 'center',
    component: CenterComponent,
    canActivate: [AuthNotGuestGuard]
  }, {
    path: 'center-list',
    component: CenterListComponent,
    canActivate: [AuthNotGuestGuard]
  }, {
    path: 'user',
    component: UserComponent
  }, {
    path: 'extension-request',
    component: ExtensionRequestComponent,
  }, {
    path: 'home',
    component: HomeComponent
  }, {
    path: 'import',
    component: ImportComponent
  }, {
    path: 'manufacturer',
    component: ManufacturerComponent,
    canActivate: [AuthNotGuestGuard]
  }, {
    path: 'manufacturer-model',
    component: ManufacturerModelComponent,
    canActivate: [AuthNotGuestGuard]
  }, {
    path: 'study',
    component: StudyComponent,
    canActivate: [AuthNotGuestGuard]
  }, {
    path: 'study-list',
    component: StudyListComponent,
    canActivate: [AuthNotGuestGuard]
  }, {
    path: 'examination-list',
    component: ExaminationListComponent,
    canActivate: [AuthNotGuestGuard]
  },{
    path: 'examination',
    component: ExaminationComponent,
    canActivate: [AuthNotGuestGuard]
  },{
    path: 'tree-test',
    component: StudyTreeComponent,
  }, {
    path: 'user-list',
    component: UserListComponent,
    canActivate: [AuthAdminGuard]
  }
];

export const routing: ModuleWithProviders = RouterModule.forRoot(appRoutes);