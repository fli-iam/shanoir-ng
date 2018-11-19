import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { CommonModule } from '@angular/common';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { NgModule, ErrorHandler, Injector } from '@angular/core';

import { Autosize } from 'angular2-autosize';
import { MyDatePickerModule } from 'mydatepicker';

import { routing } from './app.routing';
import { preclinicalRouting } from './preclinical/preclinical-routing.module'

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
import { ModalsComponent } from "./shared/components/modals/modals.component";
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
import { ImportService } from './import/import.service';
import { LoadingBarComponent } from './shared/components/loading-bar/loading-bar.component';
import { ManufacturerComponent } from './acquisition-equipments/manufacturer/manufacturer.component';
import { ManufacturerModelComponent } from './acquisition-equipments/manufacturer-model/manufacturer-model.component';
import { ManufacturerModelPipe } from './acquisition-equipments/shared/manufacturer-model.pipe';
import { ManufacturerModelService } from './acquisition-equipments/shared/manufacturer-model.service';
import { ManufacturerService } from './acquisition-equipments/shared/manufacturer.service';
import { MenuItemComponent } from './shared/components/dropdown-menu/menu-item/menu-item.component';
//import { ModalService} from './shared/components/modal/modal.service';
import { ModalService} from './shared/components/modals/modal.service';
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
import { PagerComponent } from './shared/components/table/pager/pager.component';
import { TableSearchComponent } from './shared/components/table/search/search.component';
import { TimesPipe } from './utils/app.utils';
import { FormFooterComponent } from './shared/components/form-footer/form-footer.component'
import { ServiceLocator } from './utils/locator.service';
import { BreadcrumbsComponent } from './breadcrumbs/breadcrumbs.component';
import { BreadcrumbsService } from './breadcrumbs/breadcrumbs.service';
import { Router } from './breadcrumbs/router';
import { FinishImportComponent } from './import/finish/finish.component';
import { UploaderComponent } from './shared/components/uploader/uploader.component';
import { ImportDataService } from './import/import.data-service';

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
import { ExaminationExtraDataService }      from './preclinical/extraData/extraData/shared/extradata.service';
import { PhysiologicalDataFormComponent }      from './preclinical/extraData/physiologicalData/add/physiologicalData-form.component';
import { BloodGasDataFormComponent }      from './preclinical/extraData/bloodGasData/add/bloodGasData-form.component';
import { FileUploadComponent }      from './preclinical/fileupload/fileupload.component';
import { EnumUtils }      from './preclinical/shared/enum/enumUtils';
import { ImportBrukerComponent }   from './preclinical/importBruker/importBruker.component';
import { BrukerUploadComponent }   from './preclinical/importBruker/bruker-upload/bruker-upload.component';
import { AnimalClinicalContextComponent } from './preclinical/importBruker/clinical-context/animal-clinical-context.component';
import { BrukerSelectSeriesComponent } from './preclinical/importBruker/select-series/bruker-select-series.component';
import { BrukerFinishImportComponent } from './preclinical/importBruker/finish/bruker-finish.component';
import { ImportBrukerService } from './preclinical/importBruker/importBruker.service';

@NgModule({
    imports: [
        BrowserAnimationsModule,
        CommonModule,
        FormsModule,
        HttpClientModule,
        MatDialogModule,
        MyDatePickerModule,
        ReactiveFormsModule,
        routing,
    	preclinicalRouting
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
        DatasetTypeComponent,
        ExploredEntityComponent,
        ProcessedDatasetTypeComponent,
        SelectSeriesComponent,
        DicomUploadComponent,
        ClinicalContextComponent,
        SubjectStudyListComponent,
        TableSearchComponent,
        TimesPipe,
        FormFooterComponent,
        ModalsComponent,
        BreadcrumbsComponent,
        FinishImportComponent,
        UploaderComponent,
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
    	ImportBrukerComponent, 
    	BrukerUploadComponent,
        AnimalClinicalContextComponent, 
        BrukerSelectSeriesComponent, 
        BrukerFinishImportComponent
    ],
    entryComponents: [
        ConfirmDialogComponent,
        ModalsComponent
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
    	ExaminationExtraDataService,
        AnimalExaminationService,
        AnestheticService,
    	ImportBrukerService,
    	EnumUtils,
        {
            provide: HTTP_INTERCEPTORS,
            useClass: KeycloakHttpInterceptor,
            multi: true
        },
        BreadcrumbsService,
        Router,
        ImportDataService
    ],
    bootstrap: [AppComponent],
})
export class AppModule {

    constructor(private injector: Injector) {
        ServiceLocator.injector = injector;
    }
 }