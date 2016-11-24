import { ModuleWithProviders }  from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { LoginComponent }       from './users/login/login.component';
import { HomeComponent }       from './home/home.component';
import { UserListComponent }   from './users/list/user.list.component';

const appRoutes: Routes = [
  {
    path: '',
    redirectTo: '/home',
    pathMatch: 'full'
  },
  {
    path: 'login',
    component: LoginComponent
  },
  {
    path: 'home',
    component: HomeComponent
  },
  {
    path: 'userlist',
    component: UserListComponent
  }
];

export const routing: ModuleWithProviders = RouterModule.forRoot(appRoutes);