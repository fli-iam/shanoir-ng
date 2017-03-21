import { ModuleWithProviders }  from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { HomeComponent }       from './home/home.component';
import { UserListComponent }   from './users/list/user.list.component';
import { CenterDetailComponent } from './centers/detail/center.detail.component';
import { CenterListComponent }   from './centers/list/center.list.component';
import { EditUserComponent }   from './users/edit/edit.user.component';
import { AccountRequestComponent }   from './users/accountRequest/account.request.component';
import { StudyTreeComponent }   from './studies/tree/study.tree.component';
import { AuthAdminGuard }   from './shared/roles/auth.admin.guard';
import { ImportComponent }   from './import/import.component';

const appRoutes: Routes = [
  {
    path: '',
    redirectTo: '/home',
    pathMatch: 'full'
  }, {
    path: 'home',
    component: HomeComponent
  }, {
    path: 'userlist',
    component: UserListComponent,
    canActivate: [AuthAdminGuard]
  }, {
    path: 'editUser',
    component: EditUserComponent
  }, {
    path: 'accountRequest',
    component: AccountRequestComponent,
  }, {
    path: 'treeTest',
    component: StudyTreeComponent,
  }, {
    path: 'centerlist',
    component: CenterListComponent,
    canActivate: [AuthAdminGuard]
  }, {
    path: 'import',
    component: ImportComponent
  }, {
    path: 'detailCenter',
    component: CenterDetailComponent
  }
];

export const routing: ModuleWithProviders = RouterModule.forRoot(appRoutes);