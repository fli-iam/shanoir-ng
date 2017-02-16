import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Http, HttpModule } from '@angular/http';
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
import { LoginComponent }   from './users/login/login.component';
import { LoginService } from './shared/login/login.service';
import { MyDatePickerModule } from 'mydatepicker';
import { NavbarComponent }   from './shared/navbar/navbar.component';
import { SecureHttp } from './shared/http/secure.http';
import { DropdownMenuComponent }   from './shared/dropdown-menu/dropdown-menu.component';
import { MenuItemComponent }   from './shared/dropdown-menu/menu-item/menu-item.component';
import { UserListComponent }   from './users/list/user.list.component';
import { UserService } from './users/shared/user.service';
import { RoleService } from './roles/role.service';
import { TableComponent} from "./shared/table/table.component";
import { AccountRequestInfoComponent} from "./accountRequestInfo/account.request.info.component";

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
        UserListComponent,
        EditUserComponent,
        TableComponent,
        DropdownMenuComponent,
        MenuItemComponent,
        AccountRequestInfoComponent
    ],
    entryComponents: [
        ConfirmDialogComponent
    ],
    providers: [
        AccountEventsService,
        AuthAdminGuard,
        ConfirmDialogService,
        LoginService,
        RoleService,
        UserService,
        {provide: Http, useClass: SecureHttp}
    ],
    bootstrap:    [ AppComponent ]
})
export class AppModule { }