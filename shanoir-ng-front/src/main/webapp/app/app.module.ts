import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Http, HttpModule } from '@angular/http';

import { AgGridModule } from 'ag-grid-ng2/main';

import { routing } from './app.routing';

import { AccountEventsService } from './users/account/account.events.service';
import { AppComponent }   from './app.component';
import { AuthAdminGuard }   from './shared/roles/auth.admin.guard';
import { ClickableComponent } from "./users/shared/clickable.component";
import { ClickableParentComponent } from "./users/shared/clickable.parent.component";
import { EditUserComponent }   from './users/edit/edit.user.component';
import { HeaderComponent }   from './shared/header/header.component';
import { HomeComponent }   from './home/home.component';
import { LoginComponent }   from './users/login/login.component';
import { LoginService } from './shared/login/login.service';
import { NavbarComponent }   from './shared/navbar/navbar.component';
import { RoleService } from './roles/role.service';
import { SecureHttp } from 'app/shared/http/secure.http';
import { UserListComponent }   from './users/list/user.list.component';
import { UserService } from './users/shared/user.service';

@NgModule({
    imports: [ 
        AgGridModule.withComponents([ClickableComponent, ClickableParentComponent]),
        BrowserModule,
        FormsModule,
        HttpModule,
        ReactiveFormsModule,
        routing
    ],
    declarations: [ 
        AppComponent,
        EditUserComponent,
        ClickableComponent,
        ClickableParentComponent,
        HeaderComponent,
        HomeComponent,
        LoginComponent,
        NavbarComponent,
        UserListComponent
    ],
    providers: [
        AccountEventsService,
        AuthAdminGuard,
        LoginService,
        RoleService,
        UserService,
        {provide: Http, useClass: SecureHttp}
    ],
    bootstrap:    [ AppComponent ]
})
export class AppModule { }