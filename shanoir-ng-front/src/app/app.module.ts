import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Http, HttpModule, RequestOptions, XHRBackend } from '@angular/http';
import { MaterialModule } from '@angular/material';
import { NgModule } from '@angular/core';

import { Autosize } from 'angular2-autosize/angular2-autosize';
import { MyDatePickerModule } from 'mydatepicker';

import { routing } from './app.routing';

import { AccountEventsService } from './users/account/account.events.service';
import { AccountRequestComponent} from "./users/accountRequest/account.request.component";
import { AccountRequestInfoComponent} from "./users/accountRequestInfo/account.request.info.component";
import { AcquisitionEquipmentDetailComponent } from "./acqEquip/detail/acqEquip.detail.component";
import { AcquisitionEquipmentListComponent } from "./acqEquip/list/acqEquip.list.component";
import { AcquisitionEquipmentService } from "./acqEquip/shared/acqEquip.service";
import { AppComponent }   from './app.component';
import { AuthAdminGuard }   from './shared/roles/auth.admin.guard';
import { AuthNotGuestGuard }   from './shared/roles/auth.not.guest.guard';
import { CenterDetailComponent } from './centers/detail/center.detail.component';
import { CenterListComponent }   from './centers/list/center.list.component';
import { CenterService } from './centers/shared/center.service';
import { ClickTipComponent }   from './shared/clickTip/clickTip.component';
import { ConfirmDialogComponent } from "./shared/utils/confirm.dialog.component";
import { ConfirmDialogService } from "./shared/utils/confirm.dialog.service";
import { ConsoleComponent }   from './shared/console/console.line.component';
import { ModalComponent } from "./shared/utils/modal.component";
import { DropdownMenuComponent }   from './shared/dropdown-menu/dropdown-menu.component';
import { EditUserComponent }   from './users/edit/edit.user.component';
import { ExtensionRequestComponent } from './users/extensionRequest/extension.request.component';
import { HandleErrorService } from './shared/utils/handle.error.service';
import { HeaderComponent }   from './shared/header/header.component';
import { HomeComponent }   from './home/home.component';
import { KeycloakHttp } from "./shared/keycloak/keycloak.http";
import { KeycloakService } from "./shared/keycloak/keycloak.service";
import { ImportComponent }   from './import/import.component';
import { LoadingBarComponent }   from './shared/loadingBar/loadingBar.component';
import { ManufacturerDetailComponent } from './acqEquip/manuf/detail/manuf.detail.component';
import { ManufacturerModelDetailComponent } from './acqEquip/manufModel/detail/manufModel.detail.component';
import { ManufacturerModelPipe } from './acqEquip/shared/manufModel.pipe';
import { ManufacturerModelService } from './acqEquip/shared/manufModel.service';
import { ManufacturerService } from './acqEquip/shared/manuf.service';
import { MenuItemComponent }   from './shared/dropdown-menu/menu-item/menu-item.component';
import { NavbarComponent }   from './shared/navbar/navbar.component';
import { RoleService } from './roles/role.service';
import { StudyTreeComponent }   from './studies/tree/study.tree.component';
import { TableComponent} from "./shared/table/table.component";
import { TreeNodeComponent }   from './shared/tree/tree.node.component';
import { UserListComponent }   from './users/list/user.list.component';
import { UserService } from './users/shared/user.service';

import '../assets/css/modal.css';
import '../assets/css/common.css';

export function httpFactory(backend: XHRBackend, defaultOptions: RequestOptions, keycloakService: KeycloakService) {
  return new KeycloakHttp(backend, defaultOptions, keycloakService);
}

@NgModule({
    imports: [
        BrowserAnimationsModule,
        CommonModule,
        FormsModule,
        HttpModule,
        MaterialModule,
        MyDatePickerModule,
        ReactiveFormsModule,
        routing
    ],
    declarations: [
        AccountRequestComponent,
        AccountRequestInfoComponent,
        AcquisitionEquipmentDetailComponent,
        AcquisitionEquipmentListComponent,
        AppComponent,
        Autosize,
        CenterDetailComponent,
        CenterListComponent,
        ClickTipComponent,
        ConfirmDialogComponent,
        ConsoleComponent,
        DropdownMenuComponent,
        EditUserComponent,
        ExtensionRequestComponent,
        HeaderComponent,
        HomeComponent,
        ImportComponent,
        LoadingBarComponent,
        ManufacturerDetailComponent,
        ManufacturerModelDetailComponent,
        ManufacturerModelPipe,
        ModalComponent,
        MenuItemComponent,
        NavbarComponent,
        StudyTreeComponent,
        TableComponent,
        TreeNodeComponent,
        UserListComponent
    ],
    entryComponents: [
        ConfirmDialogComponent
    ],
    providers: [
        AccountEventsService,
        AcquisitionEquipmentService,
        AuthAdminGuard,
        AuthNotGuestGuard,
        CenterService,
        ConfirmDialogService,
        HandleErrorService,
        KeycloakService,
        ManufacturerModelService,
        ManufacturerService,
        RoleService,
        UserService,
        {
            provide: Http,
            useFactory: httpFactory,
            deps: [XHRBackend, RequestOptions, KeycloakService]
        }
    ],
    bootstrap:    [ AppComponent ],
})
export class AppModule { }