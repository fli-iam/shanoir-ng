/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */
import '../assets/css/common.css';
import '../assets/css/papaya.css';

import { CommonModule } from '@angular/common';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { ErrorHandler, Injector, NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { Autosize } from 'angular2-autosize';
import { MyDatePickerModule } from 'mydatepicker';

import {
    AcquisitionEquipmentListComponent,
} from './acquisition-equipments/acquisition-equipment-list/acquisition-equipment-list.component';
import {
    AcquisitionEquipmentComponent,
} from './acquisition-equipments/acquisition-equipment/acquisition-equipment.component';
import { ManufacturerModelComponent } from './acquisition-equipments/manufacturer-model/manufacturer-model.component';
import { ManufacturerComponent } from './acquisition-equipments/manufacturer/manufacturer.component';
import { AcquisitionEquipmentPipe } from './acquisition-equipments/shared/acquisition-equipment.pipe';
import { AcquisitionEquipmentService } from './acquisition-equipments/shared/acquisition-equipment.service';
import { ManufacturerModelPipe } from './acquisition-equipments/shared/manufacturer-model.pipe';
import { ManufacturerModelService } from './acquisition-equipments/shared/manufacturer-model.service';
import { ManufacturerService } from './acquisition-equipments/shared/manufacturer.service';
import { AppComponent } from './app.component';
import { routing } from './app.routing';
import { AsyncTasksComponent } from './async-tasks/async-tasks.component';
import { TaskService } from './async-tasks/task.service';
import { BreadcrumbsComponent } from './breadcrumbs/breadcrumbs.component';
import { BreadcrumbsService } from './breadcrumbs/breadcrumbs.service';
import { Router } from './breadcrumbs/router';
import { CenterListComponent } from './centers/center-list/center-list.component';
import { CenterComponent } from './centers/center/center.component';
import { CenterService } from './centers/shared/center.service';
import { CoilListComponent } from './coils/coil-list/coil-list.component';
import { CoilComponent } from './coils/coil/coil.component';
import { CoilService } from './coils/shared/coil.service';
import {
    DatasetAcquisitionListComponent,
} from './dataset-acquisitions/dataset-acquisition-list/dataset-acquisition-list.component';
import { DatasetAcquisitionComponent } from './dataset-acquisitions/dataset-acquisition/dataset-acquisition.component';
import { MrProtocolComponent } from './dataset-acquisitions/modality/mr/mr-protocol.component';
import { PetProtocolComponent } from './dataset-acquisitions/modality/pet/pet-protocol.component';
import { DatasetAcquisitionDTOService } from './dataset-acquisitions/shared/dataset-acquisition.dto';
import { DatasetAcquisitionService } from './dataset-acquisitions/shared/dataset-acquisition.service';
import { DatasetListComponent } from './datasets/dataset-list/dataset-list.component';
import { CommonDatasetComponent } from './datasets/dataset/common/dataset.common.component';
import { DatasetComponent } from './datasets/dataset/dataset.component';
import { MrDatasetComponent } from './datasets/dataset/mr/dataset.mr.component';
import { DatasetDTOService } from './datasets/shared/dataset.dto';
import { DatasetService } from './datasets/shared/dataset.service';
import { UploadExtraDataComponent } from './examinations/attached-files/upload-extra-data.component';
import { ExaminationListComponent } from './examinations/examination-list/examination-list.component';
import { ExaminationComponent } from './examinations/examination/examination.component';
import { InstrumentAssessmentComponent } from './examinations/instrument-assessment/instrument-assessment.component';
import { NewInstrumentComponent } from './examinations/instrument-assessment/new-instrument.component';
import { ExaminationDTOService } from './examinations/shared/examination.dto';
import { ExaminationPipe } from './examinations/shared/examination.pipe';
import { ExaminationService } from './examinations/shared/examination.service';
import { SubjectExaminationPipe } from './examinations/shared/subject-examination.pipe';
import { ExaminationTreeComponent } from './examinations/tree/examination-tree.component';
import { HomeComponent } from './home/home.component';
import { ClinicalContextComponent } from './import/clinical-context/clinical-context.component';
import { DicomUploadComponent } from './import/dicom-upload/dicom-upload.component';
import { FinishImportComponent } from './import/finish/finish.component';
import { ImportComponent } from './import/import.component';
import { QueryPacsComponent } from './import/query-pacs/query-pacs.component';
import { SelectSeriesComponent } from './import/select-series/select-series.component';
import { DicomArchiveService } from './import/shared/dicom-archive.service';
import { ImportDataService } from './import/shared/import.data-service';
import { ImportService } from './import/shared/import.service';
import { NiftiConverterService } from './niftiConverters/nifti.converter.service';
import { RoleService } from './roles/role.service';
import { AutoAdjustInputComponent } from './shared/auto-ajust-input/auto-ajust-input.component';
import { CheckboxComponent } from './shared/checkbox/checkbox.component';
import { ConfirmDialogComponent } from './shared/components/confirm-dialog/confirm-dialog.component';
import { ConfirmDialogService } from './shared/components/confirm-dialog/confirm-dialog.service';
import { DropdownMenuComponent } from './shared/components/dropdown-menu/dropdown-menu.component';
import { MenuItemComponent } from './shared/components/dropdown-menu/menu-item/menu-item.component';
import { FormFooterComponent } from './shared/components/form-footer/form-footer.component';
import { LoadingBarComponent } from './shared/components/loading-bar/loading-bar.component';
import { ModalComponent } from './shared/components/modal/modal.component';
import { ModalService } from './shared/components/modals/modal.service';
import { ModalsComponent } from './shared/components/modals/modals.component';
import { PapayaComponent } from './shared/components/papaya/papaya.component';
import { SubjectStudyListComponent } from './shared/components/subject-study-list/subject-study-list.component';
import { PagerComponent } from './shared/components/table/pager/pager.component';
import { TableSearchComponent } from './shared/components/table/search/search.component';
import { TableComponent } from './shared/components/table/table.component';
import { TooltipComponent } from './shared/components/tooltip/tooltip.component';
import { TreeNodeComponent } from './shared/components/tree/tree-node.component';
import { UploaderComponent } from './shared/components/uploader/uploader.component';
import { ConsoleComponent } from './shared/console/console.line.component';
import { DatepickerComponent } from './shared/date-picker/date-picker.component';
import { HeaderComponent } from './shared/header/header.component';
import { HelpMessageComponent } from './shared/help-message/help-message.component';
import { KeycloakHttpInterceptor } from './shared/keycloak/keycloak.http.interceptor';
import { KeycloakService } from './shared/keycloak/keycloak.service';
import { MsgBoxComponent } from './shared/msg-box/msg-box.component';
import { MsgBoxService } from './shared/msg-box/msg-box.service';
import { NavbarComponent } from './shared/navbar/navbar.component';
import { NotificationsComponent } from './shared/notifications/notifications.component';
import { AuthAdminGuard } from './shared/roles/auth-admin-guard';
import { AuthAdminOrExpertGuard } from './shared/roles/auth-admin-or-expert-guard';
import { CanImportFromPACSGuard } from './shared/roles/auth-can-import-from-PACS-guard';
import { SelectBoxComponent } from './shared/select/select.component';
import { GlobalService } from './shared/services/global.service';
import { WindowService } from './shared/services/window.service';
import { SideMenuComponent } from './shared/side-menu/side-menu.component';
import { ToggleSwitchComponent } from './shared/switch/switch.component';
import { HandleErrorService } from './shared/utils/handle-error.service';
import { StudyRightsService } from './studies/shared/study-rights.service';
import { StudyService } from './studies/shared/study.service';
import { StudyListComponent } from './studies/study-list/study-list.component';
import { StudyComponent } from './studies/study/study.component';
import { StudyTreeComponent } from './studies/tree/study-tree.component';
import { DicomService } from './study-cards/shared/dicom.service';
import { StudyCardDTOService } from './study-cards/shared/study-card.dto';
import { StudyCardService } from './study-cards/shared/study-card.service';
import { StudyCardForRulesListComponent } from './study-cards/study-card-list/study-card-list-for-rules.component';
import { StudyCardListComponent } from './study-cards/study-card-list/study-card-list.component';
import { StudyCardActionComponent } from './study-cards/study-card-rules/action/action.component';
import { StudyCardConditionComponent } from './study-cards/study-card-rules/condition/condition.component';
import { DicomTagPipe } from './study-cards/study-card-rules/condition/dicom-tag.pipe';
import { StudyCardRuleComponent } from './study-cards/study-card-rules/study-card-rule.component';
import { StudyCardRulesComponent } from './study-cards/study-card-rules/study-card-rules.component';
import { StudyCardComponent } from './study-cards/study-card/study-card.component';
import { StudyNamePipe } from './subjects/shared/study-name.pipe';
import { SubjectStudyPipe } from './subjects/shared/subject-study.pipe';
import { SubjectService } from './subjects/shared/subject.service';
import { SubjectListComponent } from './subjects/subject-list/subject-list.component';
import { SubjectComponent } from './subjects/subject/subject.component';
import { SubjectTreeComponent } from './subjects/tree/subject-tree.component';
import { AccountRequestInfoComponent } from './users/account-request-info/account-request-info.component';
import { AccountRequestComponent } from './users/account-request/account-request.component';
import { AccountEventsService } from './users/account/account-events.service';
import { ExtensionRequestComponent } from './users/extension-request/extension-request.component';
import { UserService } from './users/shared/user.service';
import { UserListComponent } from './users/user-list/user-list.component';
import { UserComponent } from './users/user/user.component';
import { GetValuesPipe, TimesPipe } from './utils/app.utils';
import { ServiceLocator } from './utils/locator.service';


@NgModule({
    imports: [
        BrowserAnimationsModule,
        CommonModule,
        FormsModule,
        HttpClientModule,
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
        NewInstrumentComponent,
        UploadExtraDataComponent,
        ExtensionRequestComponent,
        HeaderComponent,
        HomeComponent,
        ImportComponent,
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
        PagerComponent,
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
        SelectSeriesComponent,
        DicomUploadComponent,
        QueryPacsComponent,
        ClinicalContextComponent,
        SubjectStudyListComponent,
        TableSearchComponent,
        TimesPipe,
        FormFooterComponent,
        ModalsComponent,
        BreadcrumbsComponent,
        SelectBoxComponent,
        FinishImportComponent,
        UploaderComponent,
        HelpMessageComponent,
        NotificationsComponent,
        AsyncTasksComponent,
        ToggleSwitchComponent,
        CheckboxComponent,
        HelpMessageComponent,
        SideMenuComponent,
        StudyCardComponent,
        StudyCardListComponent,
        StudyCardForRulesListComponent,
        StudyCardRuleComponent,
        StudyCardRulesComponent,
        StudyCardConditionComponent,
        StudyCardActionComponent,
        GetValuesPipe,
        DatasetAcquisitionListComponent,
        DatasetAcquisitionComponent,
        MrProtocolComponent,
        PetProtocolComponent,
        DicomTagPipe,
        AutoAdjustInputComponent
    ],
    entryComponents: [
        ConfirmDialogComponent,
        ModalsComponent
    ],
    providers: [
        AccountEventsService,
        AcquisitionEquipmentService,
        AuthAdminGuard,
        AuthAdminOrExpertGuard,
        CanImportFromPACSGuard,
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
        },
        BreadcrumbsService,
        Router,
        GlobalService,
        ImportDataService,
        NiftiConverterService,
        TaskService,
        StudyRightsService,
        StudyCardService,
        AcquisitionEquipmentPipe,
        DatasetAcquisitionService,
        DatasetAcquisitionDTOService,
        ExaminationDTOService,
        StudyCardDTOService,
        WindowService,
        DicomService,
        ManufacturerModelPipe,
        SubjectExaminationPipe,
        DatasetDTOService
    ],
    bootstrap: [AppComponent],
})
export class AppModule {

    constructor(private injector: Injector) {
        ServiceLocator.injector = injector;
    }
 }