import { ModuleWithProviders } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { AccountRequestComponent } from './users/accountRequest/account.request.component';
import { AcquisitionEquipmentDetailComponent } from './acqEquip/detail/acqEquip.detail.component';
import { AcquisitionEquipmentListComponent } from './acqEquip/list/acqEquip.list.component';
import { AuthAdminGuard } from './shared/roles/auth.admin.guard';
import { AuthNotGuestGuard } from './shared/roles/auth.not.guest.guard';
import { CenterDetailComponent } from './centers/detail/center.detail.component';
import { CenterListComponent } from './centers/list/center.list.component';
import { EditUserComponent } from './users/edit/edit.user.component';
import { ExtensionRequestComponent } from './users/extensionRequest/extension.request.component';
import { HomeComponent } from './home/home.component';
import { ImportComponent } from './import/import.component';
import { ManufacturerDetailComponent } from './acqEquip/manuf/detail/manuf.detail.component';
import { ManufacturerModelDetailComponent } from './acqEquip/manufModel/detail/manufModel.detail.component';
import { StudyDetailComponent } from './studies/detail/study-detail.component';
import { StudyListComponent } from './studies/list/study-list.component';
import { StudyTreeComponent } from './studies/tree/study.tree.component';
import { UserListComponent } from './users/list/user.list.component';

const appRoutes: Routes = [
  {
    path: '',
    redirectTo: '/home',
    pathMatch: 'full'
  }, {
    path: 'accountRequest',
    component: AccountRequestComponent,
  }, {
    path: 'acqEquipDetail',
    component: AcquisitionEquipmentDetailComponent,
    canActivate: [AuthNotGuestGuard]
  }, {
    path: 'acqEquipList',
    component: AcquisitionEquipmentListComponent,
    canActivate: [AuthNotGuestGuard]
  }, {
    path: 'centerDetail',
    component: CenterDetailComponent,
    canActivate: [AuthNotGuestGuard]
  }, {
    path: 'centerlist',
    component: CenterListComponent,
    canActivate: [AuthNotGuestGuard]
  }, {
    path: 'editUser',
    component: EditUserComponent
  }, {
    path: 'extensionrequest',
    component: ExtensionRequestComponent,
  }, {
    path: 'home',
    component: HomeComponent
  }, {
    path: 'import',
    component: ImportComponent
  }, {
    path: 'manufDetail',
    component: ManufacturerDetailComponent,
    canActivate: [AuthNotGuestGuard]
  }, {
    path: 'manufModelDetail',
    component: ManufacturerModelDetailComponent,
    canActivate: [AuthNotGuestGuard]
  }, {
    path: 'studyDetail',
    component: StudyDetailComponent,
    canActivate: [AuthNotGuestGuard]
  }, {
    path: 'studylist',
    component: StudyListComponent,
    canActivate: [AuthNotGuestGuard]
  }, {
    path: 'treeTest',
    component: StudyTreeComponent,
  }, {
    path: 'userlist',
    component: UserListComponent,
    canActivate: [AuthAdminGuard]
  }
];

export const routing: ModuleWithProviders = RouterModule.forRoot(appRoutes);