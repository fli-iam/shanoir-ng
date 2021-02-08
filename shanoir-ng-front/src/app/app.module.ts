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

import { AppRoutingModule } from './app-routing.module';
import { PreclinicalRoutingModule } from './preclinical/preclinical-routing.module'
import { APP_BASE_HREF, CommonModule } from '@angular/common';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { ErrorHandler, Injector, NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
// import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { Autosize } from 'ng-autosize';
import { MyDatePickerModule } from 'mydatepicker';
import { NgxJsonViewerModule } from 'ngx-json-viewer';
import { AcquisitionEquipmentListComponent } from './acquisition-equipments/acquisition-equipment-list/acquisition-equipment-list.component';
import { AcquisitionEquipmentComponent } from './acquisition-equipments/acquisition-equipment/acquisition-equipment.component';
import { ManufacturerModelComponent } from './acquisition-equipments/manufacturer-model/manufacturer-model.component';
import { ManufacturerComponent } from './acquisition-equipments/manufacturer/manufacturer.component';
import { AcquisitionEquipmentPipe } from './acquisition-equipments/shared/acquisition-equipment.pipe';
import { AcquisitionEquipmentService } from './acquisition-equipments/shared/acquisition-equipment.service';
import { ManufacturerModelPipe } from './acquisition-equipments/shared/manufacturer-model.pipe';
import { ManufacturerModelService } from './acquisition-equipments/shared/manufacturer-model.service';
import { ManufacturerService } from './acquisition-equipments/shared/manufacturer.service';
import { AppComponent } from './app.component';
// import { routing } from './app.routing';
import { AsyncTasksComponent } from './async-tasks/async-tasks.component';
import { TaskService } from './async-tasks/task.service';
import { BidsTreeComponent } from './bids/tree/bids-tree.component';
import { BreadcrumbsComponent } from './breadcrumbs/breadcrumbs.component';
import { BreadcrumbsService } from './breadcrumbs/breadcrumbs.service';
import { Router } from './breadcrumbs/router';
import { CenterListComponent } from './centers/center-list/center-list.component';
import { CenterComponent } from './centers/center/center.component';
import { CenterService } from './centers/shared/center.service';
import { CenterDTOService } from './centers/shared/center.dto';
import { CoilListComponent } from './coils/coil-list/coil-list.component';
import { CoilComponent } from './coils/coil/coil.component';
import { CoilService } from './coils/shared/coil.service';
import { DatasetAcquisitionListComponent } from './dataset-acquisitions/dataset-acquisition-list/dataset-acquisition-list.component';
import { DatasetAcquisitionComponent } from './dataset-acquisitions/dataset-acquisition/dataset-acquisition.component';
import { MrProtocolComponent } from './dataset-acquisitions/modality/mr/mr-protocol.component';
import { PetProtocolComponent } from './dataset-acquisitions/modality/pet/pet-protocol.component';
import { DatasetAcquisitionDTOService } from './dataset-acquisitions/shared/dataset-acquisition.dto';
import { DatasetAcquisitionService } from './dataset-acquisitions/shared/dataset-acquisition.service';
import { DatasetListComponent } from './datasets/dataset-list/dataset-list.component';
import { CommonDatasetComponent } from './datasets/dataset/common/dataset.common.component';
import { DatasetComponent } from './datasets/dataset/dataset.component';
import { EegDatasetComponent } from './datasets/dataset/eeg/dataset.eeg.component';
import { MrDatasetComponent } from './datasets/dataset/mr/dataset.mr.component';
import { DatasetDownloadComponent } from './datasets/download/dataset-download.component';
import { DownloadStatisticsComponent } from './datasets/download-statistics/download-statistics.component';
import { DatasetDTOService } from './datasets/shared/dataset.dto';
import { DatasetService } from './datasets/shared/dataset.service';
import { UploadExtraDataComponent } from './examinations/attached-files/upload-extra-data.component';
import { ExaminationListComponent } from './examinations/examination-list/examination-list.component';
import { ExaminationComponent } from './examinations/examination/examination.component';
import { InstrumentAssessmentComponent } from './examinations/instrument-assessment/instrument-assessment.component';
import { ExaminationDTOService } from './examinations/shared/examination.dto';
import { ExaminationPipe } from './examinations/shared/examination.pipe';
import { ExaminationService } from './examinations/shared/examination.service';
import { SubjectExaminationPipe } from './examinations/shared/subject-examination.pipe';
import { HomeComponent } from './home/home.component';
import { BidsUploadComponent } from './import/bids/bids-upload.component';
import { ClinicalContextComponent } from './import/clinical-context/clinical-context.component';
import { DicomUploadComponent } from './import/dicom-upload/dicom-upload.component';
import { EegClinicalContextComponent } from './import/eeg-clinical-context/eeg-clinical-context.component';
import { FinishEegImportComponent } from './import/eeg-finish/eeg-finish.component';
import { EegSelectSeriesComponent } from './import/eeg-select-series/eeg-select-series.component';
import { EegUploadComponent } from './import/eeg-upload/eeg-upload.component';
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
import { CheckboxListComponent } from './shared/checkbox-list/checkbox-list.component';
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
import { NotificationsService } from './shared/notifications/notifications.service';
import { AuthAdminGuard } from './shared/roles/auth-admin-guard';
import { AuthAdminOrExpertGuard } from './shared/roles/auth-admin-or-expert-guard';
import { CanImportFromPACSGuard } from './shared/roles/auth-can-import-from-PACS-guard';
import { SelectBoxComponent } from './shared/select/select.component';
import { GlobalService } from './shared/services/global.service';
import { WindowService } from './shared/services/window.service';
import { SideMenuComponent } from './shared/side-menu/side-menu.component';
import { ToggleSwitchComponent } from './shared/switch/switch.component';
import { HandleErrorService } from './shared/utils/handle-error.service';
import { SolrSearchComponent } from './solr/solr.search.component';
import { SolrService } from './solr/solr.service';
import { StudyService } from './studies/shared/study.service';
import { StudyListComponent } from './studies/study-list/study-list.component';
import { StudyComponent } from './studies/study/study.component';
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
import { AccountRequestInfoComponent } from './users/account-request-info/account-request-info.component';
import { AccountRequestComponent } from './users/account-request/account-request.component';
// import { AccountEventsService } from './users/account/account-events.service';
import { ExtensionRequestComponent } from './users/extension-request/extension-request.component';
import { UserService } from './users/shared/user.service';
import { UserListComponent } from './users/user-list/user-list.component';
import { UserComponent } from './users/user/user.component';
import { GetValuesPipe, TimesPipe } from './utils/app.utils';
import { ServiceLocator } from './utils/locator.service';
// import { NotificationsComponent } from './shared/notifications/notifications.component';
import { StudyRightsService } from './studies/shared/study-rights.service';
import { RouterModule } from '@angular/router';
import { LoaderComponent } from './shared/loader/loader.component';
import { LoaderService } from './shared/loader/loader.service';
import { ShanoirHttpInterceptor } from './http-interceptor/http-interceptor';
import { SubjectNodeComponent } from './subjects/tree/subject-node.component';
import { ReverseSubjectNodeComponent } from './subjects/tree/reverse-subject-node.component';
import { ExaminationNodeComponent } from './examinations/tree/examination-node.component';
import { DatasetAcquisitionNodeComponent } from './dataset-acquisitions/tree/dataset-acquisition-node.component';
import { DatasetNodeComponent } from './datasets/tree/dataset-node.component';
import { ProcessingNodeComponent } from './datasets/tree/processing-node.component';
import { StudyNodeComponent } from './studies/tree/study-node.component';
import { ReverseStudyNodeComponent } from './studies/tree/reverse-study-node.component';
import { CenterNodeComponent } from './centers/tree/center-node.component';
import { EquipmentNodeComponent } from './acquisition-equipments/tree/equipment-node.component';
import { MemberNodeComponent } from './users/tree/member-node.component';
import { StudyCardNodeComponent } from './study-cards/tree/study-card-node.component';

import { ReplaceSpacePipe } from './utils/pipes';

//import { ModalService} from './shared/components/modal/modal.service';
import { AnimalSubjectsListComponent }   from './preclinical/animalSubject/list/animalSubject-list.component';
import { AnimalSubjectService }   from './preclinical/animalSubject/shared/animalSubject.service';
import { AnimalSubjectFormComponent }   from './preclinical/animalSubject/edit/animalSubject-form.component';
import { ReferencesListComponent }   from './preclinical/reference/list/reference-list.component';
import { ReferenceService }   from './preclinical/reference/shared/reference.service';
import { ReferenceFormComponent }   from './preclinical/reference/edit/reference-form.component';
import { PathologiesListComponent }   from './preclinical/pathologies/pathology/list/pathology-list.component';
import { PathologyService }   from './preclinical/pathologies/pathology/shared/pathology.service';
import { PathologyFormComponent }   from './preclinical/pathologies/pathology/edit/pathology-form.component';
import { PathologyModelsListComponent }   from './preclinical/pathologies/pathologyModel/list/pathologyModel-list.component';
import { PathologyModelService }   from './preclinical/pathologies/pathologyModel/shared/pathologyModel.service';
import { PathologyModelFormComponent }   from './preclinical/pathologies/pathologyModel/edit/pathologyModel-form.component';
import { SubjectPathologiesListComponent }   from './preclinical/pathologies/subjectPathology/list/subjectPathology-list.component';
import { SubjectPathologyService }   from './preclinical/pathologies/subjectPathology/shared/subjectPathology.service';
import { SubjectPathologyFormComponent }   from './preclinical/pathologies/subjectPathology/edit/subjectPathology-form.component';
import { TherapiesListComponent }   from './preclinical/therapies/therapy/list/therapy-list.component';
import { TherapyService }   from './preclinical/therapies/therapy/shared/therapy.service';
import { TherapyFormComponent }   from './preclinical/therapies/therapy/edit/therapy-form.component';
import { SubjectTherapiesListComponent }   from './preclinical/therapies/subjectTherapy/list/subjectTherapy-list.component';
import { SubjectTherapyService }   from './preclinical/therapies/subjectTherapy/shared/subjectTherapy.service';
import { SubjectTherapyFormComponent }   from './preclinical/therapies/subjectTherapy/edit/subjectTherapy-form.component';
import { AnestheticsListComponent } from './preclinical/anesthetics/anesthetic/list/anesthetic-list.component';
import { AnestheticFormComponent }      from './preclinical/anesthetics/anesthetic/edit/anesthetic-form.component';
import { AnestheticService }      from './preclinical/anesthetics/anesthetic/shared/anesthetic.service';
import { AnestheticIngredientsListComponent } from './preclinical/anesthetics/ingredients/list/anestheticIngredient-list.component';
import { AnestheticIngredientFormComponent }      from './preclinical/anesthetics/ingredients/edit/anestheticIngredient-form.component';
import { AnestheticIngredientService }      from './preclinical/anesthetics/ingredients/shared/anestheticIngredient.service';
import { ExaminationAnestheticFormComponent }      from './preclinical/anesthetics/examination_anesthetic/edit/examinationAnesthetic-form.component';
import { ExaminationAnestheticsListComponent } from './preclinical/anesthetics/examination_anesthetic/list/examinationAnesthetic-list.component';
import { ExaminationAnestheticService }      from './preclinical/anesthetics/examination_anesthetic/shared/examinationAnesthetic.service';
import { ContrastAgentsListComponent } from './preclinical/contrastAgent/list/contrastAgent-list.component';
import { ContrastAgentFormComponent }      from './preclinical/contrastAgent/edit/contrastAgent-form.component';
import { ContrastAgentService }      from './preclinical/contrastAgent/shared/contrastAgent.service';
import { AnimalExaminationFormComponent }      from './preclinical/examination/edit/animal-examination-form.component';
import { AnimalExaminationListComponent }      from './preclinical/examination/list/animal-examination-list.component';
import { AnimalExaminationService }   from './preclinical/examination/shared/animal-examination.service';
import { ExtraDataListComponent }      from './preclinical/extraData/extraData/list/extradata-list.component';
import { ExtraDataFormComponent }      from './preclinical/extraData/extraData/edit/extradata-form.component';
import { ExtraDataService }      from './preclinical/extraData/extraData/shared/extradata.service';
import { PhysiologicalDataFormComponent }      from './preclinical/extraData/physiologicalData/add/physiologicalData-form.component';
import { BloodGasDataFormComponent }      from './preclinical/extraData/bloodGasData/add/bloodGasData-form.component';
import { FileUploadComponent }      from './preclinical/fileupload/fileupload.component';
import { EnumUtils }      from './preclinical/shared/enum/enumUtils';
// import { ImportBrukerComponent }   from './preclinical/importBruker/importBruker.component';
import { BrukerUploadComponent }   from './preclinical/importBruker/bruker-upload/bruker-upload.component';
// import { AnimalClinicalContextComponent } from './preclinical/importBruker/clinical-context/animal-clinical-context.component';
import { BrukerSelectSeriesComponent } from './preclinical/importBruker/select-series/bruker-select-series.component';
import { BrukerFinishImportComponent } from './preclinical/importBruker/finish/bruker-finish.component';
import { ImportBrukerService } from './preclinical/importBruker/importBruker.service';
import { KeycloakSessionService } from './shared/session/keycloak-session.service';
import { DUAComponent } from './dua/dua.component';
import { DUASigningComponent } from './dua/dua-signing/dua-signing.component';

@NgModule({
    imports: [
        BrowserModule,
        BrowserAnimationsModule,
        CommonModule,
        FormsModule,
        HttpClientModule,
        MyDatePickerModule,
        ReactiveFormsModule,
        NgxJsonViewerModule,
        AppRoutingModule,
        PreclinicalRoutingModule,
        RouterModule
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
        StudyComponent,
        StudyListComponent,
        SubjectExaminationPipe,
        SubjectStudyPipe,
        TableComponent,
        PagerComponent,
        TreeNodeComponent,
        BidsTreeComponent,
        TooltipComponent,
        UserListComponent,
        InstrumentAssessmentComponent,
        CoilComponent,
        CoilListComponent,
        SubjectListComponent,
        SubjectComponent,
        StudyNamePipe,
        DatasetComponent,
        EegDatasetComponent,
        DatasetListComponent,
        DatasetDownloadComponent,
        DownloadStatisticsComponent,
        DatepickerComponent,
        MrDatasetComponent,
        CommonDatasetComponent,
        MsgBoxComponent,
        PapayaComponent,
        SelectSeriesComponent,
        EegSelectSeriesComponent,
        DicomUploadComponent,
        EegUploadComponent,
        BidsUploadComponent,
        QueryPacsComponent,
        ClinicalContextComponent,
        EegClinicalContextComponent,
        SubjectStudyListComponent,
        TableSearchComponent,
        TimesPipe,
        FormFooterComponent,
        ModalsComponent,
        BreadcrumbsComponent,
        SelectBoxComponent,
        FinishImportComponent,
        FinishEegImportComponent,
        UploaderComponent,
        HelpMessageComponent,
        AsyncTasksComponent,
        ToggleSwitchComponent,
        CheckboxComponent,
        HelpMessageComponent,
        ReplaceSpacePipe,
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
        AutoAdjustInputComponent,
        SolrSearchComponent,
        CheckboxListComponent,
    	AnimalSubjectsListComponent,   
    	AnimalSubjectFormComponent,
    	ReferencesListComponent,
    	ReferenceFormComponent,
    	PathologiesListComponent,
    	PathologyFormComponent,
    	PathologyModelsListComponent,
    	PathologyModelFormComponent,
    	SubjectPathologiesListComponent,
    	SubjectPathologyFormComponent,
    	TherapiesListComponent,
    	TherapyFormComponent,
    	SubjectTherapiesListComponent,
    	SubjectTherapyFormComponent,
    	AnestheticsListComponent,
    	AnestheticFormComponent,
    	AnestheticIngredientsListComponent,
    	AnestheticIngredientFormComponent,
    	ExaminationAnestheticFormComponent,
    	ExaminationAnestheticsListComponent,
    	ContrastAgentsListComponent,
    	ContrastAgentFormComponent,
    	AnimalExaminationFormComponent,
    	AnimalExaminationListComponent,
    	FileUploadComponent,
    	ExtraDataFormComponent,
    	PhysiologicalDataFormComponent,
    	ExtraDataListComponent,
    	BloodGasDataFormComponent, 
    	BrukerUploadComponent,
        BrukerSelectSeriesComponent, 
        BrukerFinishImportComponent,
        LoaderComponent,
        SubjectNodeComponent,
        ExaminationNodeComponent,
        DatasetAcquisitionNodeComponent,
        DatasetNodeComponent,
        ProcessingNodeComponent,
        StudyNodeComponent,
        CenterNodeComponent,
        EquipmentNodeComponent,
        MemberNodeComponent,
        StudyCardNodeComponent,
        ReverseSubjectNodeComponent,
        ReverseStudyNodeComponent,
        DUAComponent,
        DUASigningComponent
    ],
    entryComponents: [
        ConfirmDialogComponent,
        ModalsComponent
    ],
    // Not required anymore with Angular > 9.0
    // entryComponents: [
    //     ConfirmDialogComponent,
    //     ModalsComponent
    // ],
    providers: [
        // {
        //     provide: APP_BASE_HREF, 
        //     useValue: environment.production  ? '/shanoir-ng/' : '/dev/'
        // },
        // AccountEventsService,
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
        // ToolService,
        SubjectService,
        UserService,
        DicomArchiveService,
        DatasetService,
        MsgBoxService,
    	PathologyService,
        AnimalSubjectService,
        ReferenceService,
    	PathologyModelService,
    	SubjectPathologyService,
    	TherapyService,
    	SubjectTherapyService,
    	AnestheticIngredientService,
    	ExaminationAnestheticService,
    	ContrastAgentService,
        ExtraDataService,
        AnimalExaminationService,
        AnestheticService,
    	ImportBrukerService,
    	EnumUtils,
        { provide: HTTP_INTERCEPTORS, useClass: KeycloakHttpInterceptor, multi: true },
        BreadcrumbsService,
        Router,
        GlobalService,
        ImportDataService,
        NiftiConverterService,
        TaskService,
        StudyRightsService,
        // {
        //   provide: RxStompService,
        //   useFactory: rxStompServiceFactory,
        //   deps: [InjectableRxStompConfig]
        // }
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
        ExaminationPipe,
        DatasetDTOService,
        SolrService,
        NotificationsService,
        CenterDTOService,
        LoaderService,
        SubjectStudyPipe,
        KeycloakSessionService,
        { provide: HTTP_INTERCEPTORS, useClass: ShanoirHttpInterceptor, multi: true }
    ],
    bootstrap: [AppComponent]
})
export class AppModule {

    constructor(private injector: Injector) {
        ServiceLocator.injector = injector;
    }
 }