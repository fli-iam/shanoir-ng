import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { NgModule, ErrorHandler } from '@angular/core';

import { Autosize } from 'angular2-autosize/angular2-autosize';
import { MyDatePickerModule } from 'mydatepicker';

import { routing } from './app.routing';

import { AccountEventsService } from './users/account/account-events.service';
import { AccountRequestComponent } from "./users/account-request/account-request.component";
import { AccountRequestInfoComponent } from "./users/account-request-info/account-request-info.component";
import { AcquisitionEquipmentComponent } from "./acquisition-equipments/acquisition-equipment/acquisition-equipment.component";
import { AcquisitionEquipmentListComponent } from "./acquisition-equipments/acquisition-equipment-list/acquisition-equipment-list.component";
import { AcquisitionEquipmentPipe } from "./acquisition-equipments/shared/acquisition-equipment.pipe";
import { AcquisitionEquipmentService } from "./acquisition-equipments/shared/acquisition-equipment.service";
import { AppComponent } from './app.component';
import { AuthAdminGuard } from './shared/roles/auth-admin-guard';
import { AuthNotGuestGuard } from './shared/roles/auth-not-guest-guard';
import { CenterComponent } from './centers/center/center.component';
import { CenterListComponent } from './centers/center-list/center-list.component';
import { CenterService } from './centers/shared/center.service';
import { ConfirmDialogComponent } from "./shared/components/confirm-dialog/confirm-dialog.component";
import { ConfirmDialogService } from "./shared/components/confirm-dialog/confirm-dialog.service";
import { ConsoleComponent } from './shared/console/console.line.component';
import { ModalComponent } from "./shared/components/modal/modal.component";
import { DropdownMenuComponent } from './shared/components/dropdown-menu/dropdown-menu.component';
import { ExaminationComponent } from './examinations/examination/examination.component';
import { ExaminationListComponent } from './examinations/examination-list/examination-list.component';
import { ExaminationPipe } from './examinations/shared/examination.pipe';
import { ExaminationService } from './examinations/shared/examination.service';
import { ExaminationTreeComponent } from './examinations/tree/examination-tree.component';
import { ExtensionRequestComponent } from './users/extension-request/extension-request.component';
import { HandleErrorService } from './shared/utils/handle-error.service';
import { HeaderComponent } from './shared/header/header.component';
import { HomeComponent } from './home/home.component';
import { KeycloakHttpInterceptor } from "./shared/keycloak/keycloak.http.interceptor";
import { KeycloakService } from "./shared/keycloak/keycloak.service";
import { ImportComponent } from './import/import.component';
import { ListComponent } from "./shared/components/list/list.component";
import { ImportService } from './import/import.service';
import { LoadingBarComponent } from './shared/components/loading-bar/loading-bar.component';
import { ManufacturerComponent } from './acquisition-equipments/manufacturer/manufacturer.component';
import { ManufacturerModelComponent } from './acquisition-equipments/manufacturer-model/manufacturer-model.component';
import { ManufacturerModelPipe } from './acquisition-equipments/shared/manufacturer-model.pipe';
import { ManufacturerModelService } from './acquisition-equipments/shared/manufacturer-model.service';
import { ManufacturerService } from './acquisition-equipments/shared/manufacturer.service';
import { MenuItemComponent } from './shared/components/dropdown-menu/menu-item/menu-item.component';
import { ModalService} from './shared/components/modal/modal.service';
import { NavbarComponent } from './shared/navbar/navbar.component';
import { RoleService } from './roles/role.service';
import { StudyComponent } from './studies/study/study.component';
import { StudyListComponent } from './studies/study-list/study-list.component';
import { StudyService } from './studies/shared/study.service';
import { SubjectExaminationPipe } from './examinations/shared/subject-examination.pipe';
import { SubjectStudyPipe } from './subjects/shared/subject-study.pipe';
import { StudyTreeComponent } from './studies/tree/study-tree.component';
import { TableComponent } from "./shared/components/table/table.component";
import { TooltipComponent } from './shared/components/tooltip/tooltip.component';
import { TreeNodeComponent } from './shared/components/tree/tree-node.component';
import { UserComponent } from './users/user/user.component';
import { UserListComponent } from './users/user-list/user-list.component';
import { UserService } from './users/shared/user.service';
import { DicomArchiveService } from './import/dicom-archive.service';
import { MsgBoxComponent } from './shared/msg-box/msg-box.component'
import { MsgBoxService } from './shared/msg-box/msg-box.service'

import '../assets/css/common.css';
import '../assets/css/papaya.css';
import { InstrumentAssessmentComponent } from './examinations/instrument-assessment/instrument-assessment.component';
import { NewInstrumentComponent } from './examinations/instrument-assessment/new-instrument.component';
import { UploadExtraDataComponent } from './examinations/attached-files/upload-extra-data.component';
import { CoilComponent } from './coils/coil/coil.component';
import { CoilListComponent } from './coils/coil-list/coil-list.component';
import { CoilService } from './coils/shared/coil.service';
import { SubjectListComponent } from './subjects/subject-list/subject-list.component';
import { SubjectService } from './subjects/shared/subject.service';
import { SubjectComponent } from './subjects/subject/subject.component';
import { SubjectTreeComponent } from './subjects/tree/subject-tree.component';
import { StudyNamePipe } from './subjects/shared/study-name.pipe';
import { DatasetComponent } from './datasets/dataset/dataset.component';
import { MrDatasetComponent } from './datasets/dataset/mr/dataset.mr.component';
import { CommonDatasetComponent } from './datasets/dataset/common/dataset.common.component';
import { DatasetListComponent } from './datasets/dataset-list/dataset-list.component';
import { DatasetService } from './datasets/shared/dataset.service';
import { DatepickerComponent } from './shared/date/date.component';
import { PapayaComponent } from './shared/components/papaya/papaya.component';
import { DatasetTypeComponent } from './datasets/shared/dataset-type/dataset-type.component';
import { ExploredEntityComponent } from './datasets/shared/explored-entity/explored-entity.component';
import { ProcessedDatasetTypeComponent } from './datasets/shared/processed-dataset-type/processed-dataset-type.component';
import { SelectSeriesComponent } from './import/select-series/select-series.component';
import { DicomUploadComponent } from './import/dicom-upload/dicom-upload.component';
import { ClinicalContextComponent } from './import/clinical-context/clinical-context.component';
import { NewExamComponent } from './examinations/examination/new-exam.component';
import { SubjectStudyListComponent } from "./shared/components/subject-study-list/subject-study-list.component";
import { DateValidator } from './shared/date/date.validator';


@NgModule({
    imports: [
        BrowserAnimationsModule,
        CommonModule,
        FormsModule,
        HttpClientModule,
        MatDialogModule,
        MyDatePickerModule,
        ReactiveFormsModule,
        routing
    ],
    declarations: [
        AccountRequestComponent,
        AccountRequestInfoComponent,
        AcquisitionEquipmentComponent,
        AcquisitionEquipmentListComponent,
        AcquisitionEquipmentPipe,
        AppComponent,
        Autosize,
        CenterComponent,
        CenterListComponent,
        ConfirmDialogComponent,
        ConsoleComponent,
        DropdownMenuComponent,
        UserComponent,
        ExaminationListComponent,
        ExaminationComponent,
        ExaminationPipe,
        ExaminationTreeComponent,
        NewExamComponent,
        NewInstrumentComponent,
        UploadExtraDataComponent,
        ExtensionRequestComponent,
        HeaderComponent,
        HomeComponent,
        ImportComponent,
        ListComponent,
        LoadingBarComponent,
        ManufacturerComponent,
        ManufacturerModelComponent,
        ManufacturerModelPipe,
        ModalComponent,
        MenuItemComponent,
        NavbarComponent,
        StudyComponent,
        StudyListComponent,
        StudyTreeComponent,
        SubjectExaminationPipe,
        SubjectStudyPipe,
        TableComponent,
        TreeNodeComponent,
        TooltipComponent,
        UserListComponent,
        InstrumentAssessmentComponent,
        CoilComponent,
        CoilListComponent,
        SubjectListComponent,
        SubjectComponent,
        SubjectTreeComponent,
        StudyNamePipe,
        DatasetComponent,
        DatasetListComponent,
        DatepickerComponent,
        MrDatasetComponent,
        CommonDatasetComponent,
        MsgBoxComponent,
        PapayaComponent,
        DatasetTypeComponent,
        ExploredEntityComponent,
        ProcessedDatasetTypeComponent,
        SelectSeriesComponent,
        DicomUploadComponent,
        ClinicalContextComponent,
        SubjectStudyListComponent,
        DateValidator
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
        ExaminationService,
        { 
            provide: ErrorHandler,
            useClass: HandleErrorService
        },
        ImportService,
        KeycloakService,
        ManufacturerModelService,
        ManufacturerService,
        MatDialog,
        ModalService,
        RoleService,
        StudyService,
        CoilService,
        SubjectService,
        UserService,
        DicomArchiveService,
        DatasetService,
        MsgBoxService,
        {
            provide: HTTP_INTERCEPTORS,
            useClass: KeycloakHttpInterceptor,
            multi: true
        }
    ],
    bootstrap: [AppComponent],
})
export class AppModule { }