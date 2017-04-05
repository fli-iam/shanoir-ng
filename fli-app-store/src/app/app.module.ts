import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { RouterModule, Routes } from '@angular/router';

import { AppComponent } from './app.component';
import { AppStoreComponent } from './appStore/appStore.component';
import { SmallAppComponent } from './appStore/smallApp/smallApp.component';
import { DetailsComponent } from './appStore/details/details.component';


const appRoutes: Routes = [
    { path: 'appstore', component: AppStoreComponent },
    { path: 'appstore/details/:id', component: DetailsComponent },
    { path: '', redirectTo: '/appstore', pathMatch: 'full' }
];

@NgModule({
  imports: [ 
    BrowserModule,
    RouterModule.forRoot(appRoutes)
  ],
  declarations: [ 
    AppComponent,
    AppStoreComponent,
    SmallAppComponent,
    DetailsComponent
  ],
  bootstrap: [ 
    AppComponent
  ]
})
export class AppModule { }
