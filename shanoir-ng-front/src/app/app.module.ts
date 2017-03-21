import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Http, HttpModule, RequestOptions, XHRBackend } from '@angular/http';
import { MaterialModule } from '@angular/material';

import { MyDatePickerModule } from 'mydatepicker';

import { routing } from './app.routing';

import { AccountEventsService } from './users/account/account.events.service';
import { AccountRequestComponent} from "./users/accountRequest/account.request.component";
import { AccountRequestInfoComponent} from "./users/accountRequestInfo/account.request.info.component";
import { AppComponent }   from './app.component';
import { AuthAdminGuard }   from './shared/roles/auth.admin.guard';
import { CenterDetailComponent } from './centers/detail/center.detail.component';
import { CenterListComponent }   from './centers/list/center.list.component';
import { CenterService } from './centers/shared/center.service';
import { ConfirmDialogComponent } from "./shared/utils/confirm.dialog.component";
import { ConfirmDialogService } from "./shared/utils/confirm.dialog.service";
import { DropdownMenuComponent }   from './shared/dropdown-menu/dropdown-menu.component';
import { EditUserComponent }   from './users/edit/edit.user.component';
import { HandleErrorService } from './shared/utils/handle.error.service';
import { HeaderComponent }   from './shared/header/header.component';
import { HomeComponent }   from './home/home.component';
import { KeycloakHttp } from "./shared/keycloak/keycloak.http";
import { KeycloakService } from "./shared/keycloak/keycloak.service";
import { MenuItemComponent }   from './shared/dropdown-menu/menu-item/menu-item.component';
import { NavbarComponent }   from './shared/navbar/navbar.component';
import { RoleService } from './roles/role.service';
import { StudyTreeComponent }   from './studies/tree/study.tree.component';
import { TableComponent} from "./shared/table/table.component";
import { TreeNodeComponent }   from './shared/tree/tree.node.component';
import { UserListComponent }   from './users/list/user.list.component';
import { UserService } from './users/shared/user.service';
import { ImportComponent }   from './import/import.component';


export function httpFactory(backend: XHRBackend, defaultOptions: RequestOptions, keycloakService: KeycloakService) {
  return new KeycloakHttp(backend, defaultOptions, keycloakService);
}

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
        AccountRequestComponent,
        AccountRequestInfoComponent,
        AppComponent,
        CenterDetailComponent,
        CenterListComponent,
        ConfirmDialogComponent,
        DropdownMenuComponent,
        EditUserComponent,
        HeaderComponent,
        HomeComponent,
        MenuItemComponent,
        NavbarComponent,
        StudyTreeComponent,
        TableComponent,
        TreeNodeComponent,
        UserListComponent,
        ImportComponent
    ],
    entryComponents: [
        ConfirmDialogComponent
    ],
    providers: [
        AccountEventsService,
        AuthAdminGuard,
        CenterService,
        ConfirmDialogService,
        KeycloakService,
        RoleService,
        UserService,
        HandleErrorService,
        {
            provide: Http,
            useFactory: httpFactory,
            deps: [XHRBackend, RequestOptions, KeycloakService]
        }
    ],
    bootstrap:    [ AppComponent ]
})
export class AppModule { }