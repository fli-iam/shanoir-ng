import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';

import { AgGridModule } from 'ag-grid-ng2/main';

import { routing } from './app.routing';

import { AppComponent }   from './app.component';
import { HeaderComponent }   from './shared/header/header.component';
import { HomeComponent }   from './home/home.component';
import { LoginComponent }   from './users/login/login.component';
import { NavbarComponent }   from './shared/navbar/navbar.component';
import { UserListComponent }   from './users/list/user.list.component';
import { AccountEventsService } from './users/account/account.events.service';
import { LoginService } from './shared/login/login.service';
import { UserService } from './users/shared/user.service';
import { EditUserComponent }   from './users/edit/edit.user.component';
import { RoleService } from './roles/role.service';

@NgModule({
    imports:      [ 
        AgGridModule.withNg2ComponentSupport(),
        BrowserModule,
        FormsModule,
        HttpModule,
        ReactiveFormsModule,
        routing
    ],
    declarations: [ 
        AppComponent,
        HeaderComponent,
        HomeComponent,
        LoginComponent,
        NavbarComponent,
        UserListComponent,
        EditUserComponent
    ],
    providers: [
        AccountEventsService,
        LoginService,
        UserService,
        RoleService
    ],
    bootstrap:    [ AppComponent ]
})
export class AppModule { }