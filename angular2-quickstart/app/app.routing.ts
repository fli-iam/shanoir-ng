import { Routes, RouterModule } from '@angular/router';

import { HeroesComponent }      from './heroes.component';
import { DashboardComponent }      from './dashboard.component';
import { HeroDetailComponent }      from './hero-detail.component';

const appRoutes: Routes = [
{
  path: 'detail/:id',
  component: HeroDetailComponent
},
{
  path: '',
  redirectTo: '/dashboard',
  pathMatch: 'full'
},
{
    path: 'heroes',
    component: HeroesComponent
},
{
    path: 'dashboard',
    component: DashboardComponent
}
];

export const routing = RouterModule.forRoot(appRoutes);