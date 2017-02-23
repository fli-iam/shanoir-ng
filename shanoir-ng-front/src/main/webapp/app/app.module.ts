import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Http, HttpModule, RequestOptions, XHRBackend } from '@angular/http';
import { MaterialModule } from '@angular/material';

import { routing } from './app.routing';

import { AccountEventsService } from './users/account/account.events.service';
import { AppComponent }   from './app.component';
import { AuthAdminGuard }   from './shared/roles/auth.admin.guard';
import { ConfirmDialogComponent } from "./shared/utils/confirm.dialog.component";
import { ConfirmDialogService } from "./shared/utils/confirm.dialog.service";
import { EditUserComponent }   from './users/edit/edit.user.component';
import { HeaderComponent }   from './shared/header/header.component';
import { HomeComponent }   from './home/home.component';
import { KeycloakHttp } from "./shared/keycloak/keycloak.http";
import { KeycloakService } from "./shared/keycloak/keycloak.service";
import { LoginComponent }   from './users/login/login.component';
import { LoginService } from './shared/login/login.service';
import { MyDatePickerModule } from 'mydatepicker';
import { NavbarComponent }   from './shared/navbar/navbar.component';
import { UserListComponent }   from './users/list/user.list.component';
import { UserService } from './users/shared/user.service';
import { RoleService } from './roles/role.service';
import { TableComponent} from "./shared/table/table.component";

@NgModule({
    imports: [
        BrowserModule,
        FormsModule,
        HttpModule,
        MaterialModule.forRoot(),
        MyDatePickerModule,
        ReactiveFormsModule,
        routing
    ],
    declarations: [
        AppComponent,
        ConfirmDialogComponent,
        EditUserComponent,
        HeaderComponent,
        HomeComponent,
        LoginComponent,
        NavbarComponent,
        TableComponent,
        UserListComponent
    ],
    entryComponents: [
        ConfirmDialogComponent
    ],
    providers: [
        AccountEventsService,
        AuthAdminGuard,
        ConfirmDialogService,
        KeycloakService,
        LoginService,
        RoleService,
        UserService,
        {
            provide: Http,
            useFactory:
            (
                backend: XHRBackend,
                defaultOptions: RequestOptions,
                keycloakService: KeycloakService
            ) => new KeycloakHttp(backend, defaultOptions, keycloakService),
            deps: [XHRBackend, RequestOptions, KeycloakService]
        }
    ],
    bootstrap:    [ AppComponent ]
})
export class AppModule { }