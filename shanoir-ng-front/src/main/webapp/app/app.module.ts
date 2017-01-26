import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Http, HttpModule } from '@angular/http';

import { routing } from './app.routing';

import { AccountEventsService } from './users/account/account.events.service';
import { AppComponent }   from './app.component';
import { AuthAdminGuard }   from './shared/roles/auth.admin.guard';
import { EditUserComponent }   from './users/edit/edit.user.component';
import { HeaderComponent }   from './shared/header/header.component';
import { HomeComponent }   from './home/home.component';
import { LoginComponent }   from './users/login/login.component';
import { LoginService } from './shared/login/login.service';
import { NavbarComponent }   from './shared/navbar/navbar.component';
import { SecureHttp } from 'app/shared/http/secure.http';
import { UserListComponent }   from './users/list/user.list.component';
import { UserService } from './users/shared/user.service';
import { RoleService } from './roles/role.service';
import { TableComponent} from "./shared/table/table.component";
import { MyDatePickerModule } from 'mydatepicker/dist/my-date-picker.module';

@NgModule({
    imports: [
        BrowserModule,
        FormsModule,
        HttpModule,
        ReactiveFormsModule,
        routing,
        MyDatePickerModule
    ],
    declarations: [
        AppComponent,
        EditUserComponent,
        HeaderComponent,
        HomeComponent,
        LoginComponent,
        NavbarComponent,
        UserListComponent,
        EditUserComponent,
        TableComponent
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