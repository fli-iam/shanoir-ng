import { CommonModule, registerLocaleData } from '@angular/common';
import localeDe from '@angular/common/locales/de';
import localeEs from '@angular/common/locales/es';
import localeFr from '@angular/common/locales/fr';
import { enableProdMode, ErrorHandler, importProvidersFrom } from '@angular/core';
import { ClipboardModule } from '@angular/cdk/clipboard';
import { HTTP_INTERCEPTORS, provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { bootstrapApplication, BrowserModule } from '@angular/platform-browser';
import { RouterModule } from '@angular/router';
import { NgxJsonViewerModule } from 'ngx-json-viewer';

import { AcquisitionEquipmentPipe } from './app/acquisition-equipments/shared/acquisition-equipment.pipe';
import { AcquisitionEquipmentService } from './app/acquisition-equipments/shared/acquisition-equipment.service';
import { ManufacturerModelPipe } from './app/acquisition-equipments/shared/manufacturer-model.pipe';
import { ManufacturerModelService } from './app/acquisition-equipments/shared/manufacturer-model.service';
import { ManufacturerService } from './app/acquisition-equipments/shared/manufacturer.service';
import { AppRoutingModule } from './app/app-routing.module';
import { AppComponent } from './app/app.component';
import { TaskService } from './app/async-tasks/task.service';
import { BreadcrumbsService } from './app/breadcrumbs/breadcrumbs.service';
import { CenterDTOService } from './app/centers/shared/center.dto';
import { CenterService } from './app/centers/shared/center.service';
import { CoilService } from './app/coils/shared/coil.service';
import { DatasetAcquisitionDTOService } from './app/dataset-acquisitions/shared/dataset-acquisition.dto';
import { DatasetAcquisitionService } from './app/dataset-acquisitions/shared/dataset-acquisition.service';
import { DatasetProcessingPipe } from './app/datasets/dataset-processing/dataset-processing.pipe';
import { DatasetProcessingDTOService } from './app/datasets/shared/dataset-processing.dto';
import { DatasetProcessingService } from './app/datasets/shared/dataset-processing.service';
import { DatasetDTOService } from './app/datasets/shared/dataset.dto';
import { DatasetService } from './app/datasets/shared/dataset.service';
import { DuaService } from './app/dua/shared/dua.service';
import { ExaminationDTOService } from './app/examinations/shared/examination.dto';
import { ExaminationPipe } from './app/examinations/shared/examination.pipe';
import { ExaminationService } from './app/examinations/shared/examination.service';
import { SubjectExaminationPipe } from './app/examinations/shared/subject-examination.pipe';
import { ShanoirHttpInterceptor } from './app/http-interceptor/http-interceptor';
import { DicomArchiveService } from './app/import/shared/dicom-archive.service';
import { ImportDataService } from './app/import/shared/import.data-service';
import { ImportService } from './app/import/shared/import.service';
import { AnestheticService } from './app/preclinical/anesthetics/anesthetic/shared/anesthetic.service';
import { ExaminationAnestheticService } from './app/preclinical/anesthetics/examination_anesthetic/shared/examinationAnesthetic.service';
import { AnestheticIngredientService } from './app/preclinical/anesthetics/ingredients/shared/anestheticIngredient.service';
import { AnimalSubjectService } from './app/preclinical/animalSubject/shared/animalSubject.service';
import { ContrastAgentService } from './app/preclinical/contrastAgent/shared/contrastAgent.service';
import { AnimalExaminationService } from './app/preclinical/examination/shared/animal-examination.service';
import { ExtraDataService } from './app/preclinical/extraData/extraData/shared/extradata.service';
import { ImportBrukerService } from './app/preclinical/importBruker/importBruker.service';
import { PathologyService } from './app/preclinical/pathologies/pathology/shared/pathology.service';
import { PathologyModelService } from './app/preclinical/pathologies/pathologyModel/shared/pathologyModel.service';
import { SubjectPathologyService } from './app/preclinical/pathologies/subjectPathology/shared/subjectPathology.service';
import { PreclinicalRoutingModule } from './app/preclinical/preclinical-routing.module';
import { ReferenceService } from './app/preclinical/reference/shared/reference.service';
import { SubjectTherapyService } from './app/preclinical/therapies/subjectTherapy/shared/subjectTherapy.service';
import { TherapyService } from './app/preclinical/therapies/therapy/shared/therapy.service';
import { RoleService } from './app/roles/role.service';
import { ConfirmDialogService } from './app/shared/components/confirm-dialog/confirm-dialog.service';
import { ConsoleService } from './app/shared/console/console.service';
import { KeycloakHttpInterceptor } from './app/shared/keycloak/keycloak.http.interceptor';
import { KeycloakService } from './app/shared/keycloak/keycloak.service';
import { LoaderService } from './app/shared/loader/loader.service';
import { MassDownloadService } from './app/shared/mass-download/mass-download.service';
import { SingleDownloadService } from './app/shared/mass-download/single-download.service';
import { MsgBoxService } from './app/shared/msg-box/msg-box.service';
import { NotificationsService } from './app/shared/notifications/notifications.service';
import { AuthAdminGuard } from './app/shared/roles/auth-admin-guard';
import { AuthAdminOrExpertGuard } from './app/shared/roles/auth-admin-or-expert-guard';
import { CanImportFromPACSGuard } from './app/shared/roles/auth-can-import-from-PACS-guard';
import { LoginGuard } from './app/shared/roles/login-guard';
import { GlobalService } from './app/shared/services/global.service';
import { SessionService } from './app/shared/services/session.service';
import { WindowService } from './app/shared/services/window.service';
import { KeycloakSessionService } from './app/shared/session/keycloak-session.service';
import { HandleErrorService } from './app/shared/utils/handle-error.service';
import { SolrService } from './app/solr/solr.service';
import { StudyRightsService } from './app/studies/shared/study-rights.service';
import { StudyDTOService } from './app/studies/shared/study.dto';
import { StudyService } from './app/studies/shared/study.service';
import { TreeService } from './app/studies/study/tree.service';
import { DicomService } from './app/study-cards/shared/dicom.service';
import { QualityCardDTOService } from './app/study-cards/shared/quality-card.dto';
import { QualityCardService } from './app/study-cards/shared/quality-card.service';
import { StudyCardDTOService } from './app/study-cards/shared/study-card.dto';
import { StudyCardService } from './app/study-cards/shared/study-card.service';
import { SubjectStudyPipe } from './app/subjects/shared/subject-study.pipe';
import { SubjectDTOService } from './app/subjects/shared/subject.dto';
import { SubjectService } from './app/subjects/shared/subject.service';
import { AccessRequestService } from './app/users/access-request/access-request.service';
import { ShanoirEventService } from './app/users/shanoir-event/shanoir-event.service';
import { UserService } from './app/users/shared/user.service';
import { ExecutionMonitoringService } from './app/vip/execution-monitorings/execution-monitoring.service';
import { ExecutionService } from './app/vip/execution/execution.service';
import { PipelineService } from './app/vip/pipelines/pipeline/pipeline.service';
import { environment } from './environments/environment';

registerLocaleData(localeFr);
registerLocaleData(localeDe);
registerLocaleData(localeEs);

if (environment.production) {
    enableProdMode();
}

const options = {
    providers: [
        importProvidersFrom(BrowserModule, CommonModule, FormsModule, ReactiveFormsModule, NgxJsonViewerModule, AppRoutingModule, PreclinicalRoutingModule, RouterModule, ClipboardModule),
        AcquisitionEquipmentService,
        AuthAdminGuard,
        AuthAdminOrExpertGuard,
        CanImportFromPACSGuard,
        LoginGuard,
        CenterService,
        ConfirmDialogService,
        ExaminationService,
        ExecutionService,
        PipelineService,
        ExecutionMonitoringService,
        {
            provide: ErrorHandler,
            useClass: HandleErrorService
        },
        ImportService,
        KeycloakService,
        ManufacturerModelService,
        ManufacturerService,
        RoleService,
        StudyService,
        CoilService,
        AccessRequestService,
        // ToolService,
        SubjectService,
        UserService,
        DicomArchiveService,
        DatasetService,
        DatasetProcessingService,
        DatasetProcessingPipe,
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
        AnimalExaminationService,
        AnestheticService,
        ImportBrukerService,
        { provide: HTTP_INTERCEPTORS, useClass: KeycloakHttpInterceptor, multi: true },
        BreadcrumbsService,
        GlobalService,
        ImportDataService,
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
        DatasetProcessingDTOService,
        SolrService,
        NotificationsService,
        CenterDTOService,
        LoaderService,
        SubjectStudyPipe,
        KeycloakSessionService,
        ConsoleService,
        ExtraDataService,
        StudyDTOService,
        SubjectDTOService,
        QualityCardService,
        QualityCardDTOService,
        MassDownloadService,
        SingleDownloadService,
        SessionService,
        ShanoirEventService,
        TreeService,
        DuaService,
        { provide: HTTP_INTERCEPTORS, useClass: ShanoirHttpInterceptor, multi: true },
        provideHttpClient(withInterceptorsFromDi()),
    ]
}

if (window.location.href == window.origin + '/shanoir-ng/'
    || window.location.href.endsWith('/welcome')
    || window.location.href.includes('/account-request')
    || window.location.href.endsWith('/extension-request')
    || window.location.href.endsWith('/challenge-request')) {
    // Public URL
    bootstrapApplication(AppComponent, options);
} else { // private 
    const optionalAuth: boolean = window.location.href.includes('/dua/edit')
            || window.location.href.includes('/dua/view');
    KeycloakService.init(optionalAuth)
        .then(() => {
            bootstrapApplication(AppComponent, options);
        });
}
