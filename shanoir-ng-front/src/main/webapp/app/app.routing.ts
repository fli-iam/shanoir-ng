import { ModuleWithProviders }  from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { LoginComponent }       from './users/login/login.component';
import { HomeComponent }       from './home/home.component';
import { UserListComponent }   from './users/list/user.list.component';
import { EditUserComponent }   from './users/edit/edit.user.component';
import { AccountRequestComponent }   from './users/accountRequest/account.request.component';
import { StudyTreeComponent }   from './studies/tree/study.tree.component';
import { AuthAdminGuard }   from './shared/roles/auth.admin.guard';

const appRoutes: Routes = [
  {
    path: '',
    redirectTo: '/home',
    pathMatch: 'full'
  }, {
    path: 'login',
    component: LoginComponent
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
    path: 'requestAccount',
    component: AccountRequestComponent,
  }, {
    path: 'treeTest',
    component: StudyTreeComponent,
  }
];

export const routing: ModuleWithProviders = RouterModule.forRoot(appRoutes);