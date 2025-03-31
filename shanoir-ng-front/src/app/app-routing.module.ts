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


import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AcquisitionEquipmentListComponent } from './acquisition-equipments/acquisition-equipment-list/acquisition-equipment-list.component';
import { AcquisitionEquipmentComponent } from './acquisition-equipments/acquisition-equipment/acquisition-equipment.component';
import { ManufacturerModelComponent } from './acquisition-equipments/manufacturer-model/manufacturer-model.component';
import { ManufacturerComponent } from './acquisition-equipments/manufacturer/manufacturer.component';
import { CenterListComponent } from './centers/center-list/center-list.component';
import { CenterComponent } from './centers/center/center.component';
import { CoilListComponent } from './coils/coil-list/coil-list.component';
import { CoilComponent } from './coils/coil/coil.component';
import { DatasetListComponent } from './datasets/dataset-list/dataset-list.component';
import { DatasetComponent } from './datasets/dataset/dataset.component';
import { DatasetProcessingComponent } from './datasets/dataset-processing/dataset-processing.component';
import { DatasetProcessingListComponent } from './datasets/dataset-processing-list/dataset-processing-list.component';
import { ExaminationListComponent } from './examinations/examination-list/examination-list.component';
import { ExaminationComponent } from './examinations/examination/examination.component';
import { HomeComponent } from './home/home.component';
import { BasicClinicalContextComponent } from './import/basic-clinical-context/basic-clinical-context.component';
import { DicomUploadComponent } from './import/dicom-upload/dicom-upload.component';
import { ImportComponent } from './import/import.component';
import { QueryPacsComponent } from './import/query-pacs/query-pacs.component';
import { ImportProcessedDatasetComponent } from './import/processed-dataset/processed-dataset.component';
import { SelectSeriesComponent } from './import/select-series/select-series.component';
import { AuthAdminGuard } from './shared/roles/auth-admin-guard';
import { CanImportFromPACSGuard } from './shared/roles/auth-can-import-from-PACS-guard';
import { AuthAdminOrExpertGuard } from './shared/roles/auth-admin-or-expert-guard';
import { StudyListComponent } from './studies/study-list/study-list.component';
import { StudyComponent } from './studies/study/study.component';
import { SubjectListComponent } from './subjects/subject-list/subject-list.component';
import { SubjectComponent } from './subjects/subject/subject.component';
import { AccountRequestComponent } from './users/account-request/account-request.component';
import { ExtensionRequestComponent } from './users/extension-request/extension-request.component';
import { UserListComponent } from './users/user-list/user-list.component';
import { UserComponent } from './users/user/user.component';
import { AsyncTasksComponent } from './async-tasks/async-tasks.component';
import { EegUploadComponent } from './import/eeg-upload/eeg-upload.component';
import { BidsUploadComponent } from './import/bids/bids-upload.component';
import { EegSelectSeriesComponent } from './import/eeg-select-series/eeg-select-series.component';
import { EegClinicalContextComponent } from './import/eeg-clinical-context/eeg-clinical-context.component';
import { InstrumentAssessmentComponent } from './examinations/instrument-assessment/instrument-assessment.component';
import { DownloadStatisticsComponent } from './datasets/download-statistics/download-statistics.component';
import { BrukerUploadComponent } from './preclinical/importBruker/bruker-upload/bruker-upload.component';
import { BrukerSelectSeriesComponent } from './preclinical/importBruker/select-series/bruker-select-series.component';
import { ContrastAgentsListComponent } from './preclinical/contrastAgent/list/contrastAgent-list.component';
import { ContrastAgentFormComponent } from './preclinical/contrastAgent/edit/contrastAgent-form.component';
import { ReferenceFormComponent } from './preclinical/reference/edit/reference-form.component';
import { ReferencesListComponent } from './preclinical/reference/list/reference-list.component';
import { AnimalExaminationFormComponent } from './preclinical/examination/edit/animal-examination-form.component';
import { AnimalExaminationListComponent } from './preclinical/examination/list/animal-examination-list.component';
import { TherapiesListComponent } from './preclinical/therapies/therapy/list/therapy-list.component';
import { TherapyFormComponent } from './preclinical/therapies/therapy/edit/therapy-form.component';
import { PathologyFormComponent } from './preclinical/pathologies/pathology/edit/pathology-form.component';
import { PathologiesListComponent } from './preclinical/pathologies/pathology/list/pathology-list.component';
import { PathologyModelFormComponent } from './preclinical/pathologies/pathologyModel/edit/pathologyModel-form.component';
import { AnestheticIngredientFormComponent } from './preclinical/anesthetics/ingredients/edit/anestheticIngredient-form.component';
import { PathologyModelsListComponent } from './preclinical/pathologies/pathologyModel/list/pathologyModel-list.component';
import { AnestheticIngredientsListComponent } from './preclinical/anesthetics/ingredients/list/anestheticIngredient-list.component';
import { AnestheticsListComponent } from './preclinical/anesthetics/anesthetic/list/anesthetic-list.component';
import { AnimalSubjectsListComponent } from './preclinical/animalSubject/list/animalSubject-list.component';
import { AnestheticFormComponent } from './preclinical/anesthetics/anesthetic/edit/anesthetic-form.component';
import { AnimalSubjectFormComponent } from './preclinical/animalSubject/edit/animalSubject-form.component';
import { StudyCardListComponent } from './study-cards/study-card-list/study-card-list.component';
import { StudyCardComponent } from './study-cards/study-card/study-card.component';
import { DatasetAcquisitionListComponent } from './dataset-acquisitions/dataset-acquisition-list/dataset-acquisition-list.component';
import { DatasetAcquisitionComponent } from './dataset-acquisitions/dataset-acquisition/dataset-acquisition.component';
import { SolrSearchComponent } from './solr/solr.search.component';
import { StudyCardForRulesListComponent } from './study-cards/study-card-list/study-card-list-for-rules.component';
import { ProcessedDatasetClinicalContextComponent } from './import/processed-dataset-clinical-context/processed-dataset-clinical-context.component';
import { DUAComponent } from './dua/dua.component';
import { AccessRequestComponent } from './users/access-request/access-request.component';
import { PipelinesComponent } from './vip/pipelines/pipelines.component';
import { ExecutionComponent } from './vip/execution/execution.component';
import { ExecutionMonitoringsComponent } from './vip/execution-monitorings/execution-monitorings.component';
import { MetadataComponent } from './datasets/dataset/metadata/metadata.component';
import { ApplyStudyCardOnComponent } from './study-cards/apply-study-card-on/apply-study-card-on.component';
import { PreClinicalContextComponent } from './import/pre-clinical-context/pre-clinical-context.component';
import { PacsClinicalContextComponent } from './import/pacs-clinical-context/pacs-clinical-context.component';
import { WelcomeComponent } from "./welcome/welcome.component";
import { LoginGuard } from "./shared/roles/login-guard";
import { AccessRequestListComponent } from './users/access-request/access-request-list.component';
import { QualityCardListComponent } from './study-cards/quality-card-list/quality-card-list.component';
import { QualityCardComponent } from './study-cards/quality-card/quality-card.component';
import { TaskStatusComponent } from './async-tasks/status/task-status.component';
import { DUAAssistantComponent } from './dua/dua-assistant.component';
import { TestComponent } from './utils/dev/test.component';
import { ExecutionTemplateListComponent } from "./vip/execution-template/execution-template-list.component";
import { ExecutionTemplateComponent } from "./vip/execution-template/execution-template.component";

let routes: Routes = [
    {
        path: '',
        redirectTo: '/welcome',
        pathMatch: 'full'
    }, {
        path: 'dua',
        component: DUAComponent,
    }, {
        path: 'account-request',
        component: AccountRequestComponent,
    }, {
        path: 'welcome',
        component: WelcomeComponent,
        canActivate: [LoginGuard]
    }, {
        path: 'account/study/:id/account-request',
        component: AccountRequestComponent,
    }, {
        path: 'extension-request',
        component: ExtensionRequestComponent,
    }, {
        path: 'home',
        component: HomeComponent
    }, {
        path: 'solr-search',
        component: SolrSearchComponent
    }, {
        path: 'execution-monitoring',
        component: ExecutionMonitoringsComponent
    }, {
        path: 'execution-template-list',
        component: ExecutionTemplateListComponent
    }, {
        path: 'pipelines',
        component: PipelinesComponent
    }, {
        path: 'execution',
        component: ExecutionComponent
    }, {
        path: 'imports',
        component: ImportComponent,
        children: [
            {
                path: '',
                pathMatch: 'full',
                redirectTo: '/home'
            }, {
                path: 'upload',
                component: DicomUploadComponent,
                data: {importMode: 'DICOM'}
            }, {
                path: 'bruker',
                component: BrukerUploadComponent,
                data: {importMode: 'BRUKER'}
            }, {
                path: 'eeg',
                component: EegUploadComponent,
                data: {importMode: 'EEG'}
            }, {
                path: 'bids',
                component: BidsUploadComponent,
                data: {importMode: 'BIDS'}
            }, {
                path: 'pacs',
                component: QueryPacsComponent,
                canActivate: [CanImportFromPACSGuard]
            }, {
                path: 'processed-dataset',
                component: ImportProcessedDatasetComponent,
                data: {importMode: 'Processed Dataset'}
            }, {
                path: 'series',
                component: SelectSeriesComponent
            }, {
                path: 'eegseries',
                component: EegSelectSeriesComponent
            }, {
                path: 'context',
                component: BasicClinicalContextComponent
            }, {
                path: 'pacs-context',
                component: PacsClinicalContextComponent
            }, {
                path: 'preclinical-context',
                component: PreClinicalContextComponent
            }, {
                path: 'eegcontext',
                component: EegClinicalContextComponent
            }, {
                path: 'processed-dataset-context',
                component: ProcessedDatasetClinicalContextComponent
            }, {
                path: 'bruker',
                component: BrukerUploadComponent
            }, {
                path: 'brukerseries',
                component: BrukerSelectSeriesComponent
            }
        ]
    }, {
        path: 'task/status/:ts',
        component: TaskStatusComponent
    }, {
        path: 'task',
        component: AsyncTasksComponent
    }, {
        path: 'study-card/select-rule',
        children: [
            {
                path: 'list/:id',
                component: StudyCardForRulesListComponent,
            }, {
                path: 'select/:id',
                component: StudyCardComponent,
                data: { mode: 'view', select: true }
            }
        ]
    },
        {
        path: 'preclinical-contrastagents',
        component: ContrastAgentsListComponent
    },{
        path: 'preclinical-contrastagent',
        component: ContrastAgentFormComponent
    },{
        path: 'download-statistics',
        component: DownloadStatisticsComponent
    },{
        path: 'study',
        redirectTo: 'study/list',
    },
    {
        path: 'study/list',
        component: StudyListComponent,
    },
    {
        path: 'study/details/:id',
        component: StudyComponent,
        data: { mode: 'view' },
    },
    {
        path: 'study/edit/:id',
        component: StudyComponent,
        data: { mode: 'edit' },
        canActivate: [AuthAdminOrExpertGuard],
    },
    {
        path: 'study/create',
        component: StudyComponent,
        data: { mode: 'create' },
        canActivate: [AuthAdminOrExpertGuard],
    },
    {
        path: 'subject',
        redirectTo: 'subject/list',
    },
    {
        path: 'subject/list',
        component: SubjectListComponent,
    },
    {
        path: 'subject/details/:id',
        component: SubjectComponent,
        data: { mode: 'view' },
    },
    {
        path: 'subject/edit/:id',
        component: SubjectComponent,
        data: { mode: 'edit' },
        canActivate: [AuthAdminOrExpertGuard],
    },
    {
        path: 'subject/create',
        component: SubjectComponent,
        data: { mode: 'create' },
    },
    {
        path: 'examination',
        redirectTo: 'examination/list',
    },
    {
        path: 'examination/list',
        component: ExaminationListComponent,
    },
    {
        path: 'examination/details/:id',
        component: ExaminationComponent,
        data: { mode: 'view' },
    },
    {
        path: 'examination/edit/:id',
        component: ExaminationComponent,
        data: { mode: 'edit' },
    },
    {
        path: 'examination/create',
        component: ExaminationComponent,
        data: { mode: 'create' },
    },
    {
        path: 'dataset',
        redirectTo: 'dataset/list',
    },
    {
        path: 'dataset/list',
        component: DatasetListComponent,
    },
    {
        path: 'dataset/details/:id',
        component: DatasetComponent,
        data: { mode: 'view' },
    },
    {
        path: 'dataset/details/dicom/:id',
        component: MetadataComponent
    },
    {
        path: 'dataset/edit/:id',
        component: DatasetComponent,
        data: { mode: 'edit' },
        canActivate: [AuthAdminOrExpertGuard],
    },
    {
        path: 'dataset/create',
        component: DatasetComponent,
        data: { mode: 'create' },
    },
    {
        path: 'dataset-processing',
        redirectTo: 'dataset-processing/list',
    },
    {
        path: 'dataset-processing/list',
        component: DatasetProcessingListComponent,
    },
    {
        path: 'dataset-processing/details/:id',
        component: DatasetProcessingComponent,
        data: { mode: 'view' },
    },
    {
        path: 'dataset-processing/edit/:id',
        component: DatasetProcessingComponent,
        data: { mode: 'edit' },
        canActivate: [AuthAdminGuard],
    },
    {
        path: 'dataset-processing/create',
        component: DatasetProcessingComponent,
        data: { mode: 'create' },
    },
    {
        path: 'center',
        redirectTo: 'center/list',
    },
    {
        path: 'center/list',
        component: CenterListComponent,
    },
    {
        path: 'center/details/:id',
        component: CenterComponent,
        data: { mode: 'view' },
    },
    {
        path: 'center/edit/:id',
        component: CenterComponent,
        data: { mode: 'edit' },
        canActivate: [AuthAdminOrExpertGuard],
    },
    {
        path: 'center/create',
        component: CenterComponent,
        data: { mode: 'create' },
        canActivate: [AuthAdminOrExpertGuard],
    },
    {
        path: 'acquisition-equipment',
        redirectTo: 'acquisition-equipment/list',
    },
    {
        path: 'acquisition-equipment/list',
        component: AcquisitionEquipmentListComponent,
    },
    {
        path: 'acquisition-equipment/details/:id',
        component: AcquisitionEquipmentComponent,
        data: { mode: 'view' },
    },
    {
        path: 'acquisition-equipment/edit/:id',
        component: AcquisitionEquipmentComponent,
        data: { mode: 'edit' },
        canActivate: [AuthAdminOrExpertGuard],
    },
    {
        path: 'acquisition-equipment/create',
        component: AcquisitionEquipmentComponent,
        data: { mode: 'create' },
        canActivate: [AuthAdminOrExpertGuard],
    },
    {
        path: 'coil',
        redirectTo: 'coil/list',
    },
    {
        path: 'coil/list',
        component: CoilListComponent,
    },
    {
        path: 'coil/details/:id',
        component: CoilComponent,
        data: { mode: 'view' },
    },
    {
        path: 'coil/edit/:id',
        component: CoilComponent,
        data: { mode: 'edit' },
        canActivate: [AuthAdminOrExpertGuard],
    },
    {
        path: 'coil/create',
        component: CoilComponent,
        data: { mode: 'create' },
        canActivate: [AuthAdminOrExpertGuard],
    },
    {
        path: 'user',
        redirectTo: 'user/list',
    },
    {
        path: 'user/list',
        component: UserListComponent,
    },
    {
        path: 'user/details/:id',
        component: UserComponent,
        data: { mode: 'view' },
    },
    {
        path: 'user/edit/:id',
        component: UserComponent,
        data: { mode: 'edit' }
    },
    {
        path: 'user/create',
        component: UserComponent,
        data: { mode: 'create' },
        canActivate: [AuthAdminGuard],
    },
    {
        path: 'manufacturer',
        redirectTo: 'manufacturer/list',
    },
    {
        path: 'manufacturer/list',
        component: HomeComponent,
    },
    {
        path: 'manufacturer/details/:id',
        component: ManufacturerComponent,
        data: { mode: 'view' },
    },
    {
        path: 'manufacturer/edit/:id',
        component: ManufacturerComponent,
        data: { mode: 'edit' },
        canActivate: [AuthAdminOrExpertGuard],
    },
    {
        path: 'manufacturer/create',
        component: ManufacturerComponent,
        data: { mode: 'create' },
        canActivate: [AuthAdminOrExpertGuard],
    },
    {
        path: 'manufacturer-model',
        redirectTo: 'manufacturer-model/list',
    },
    {
        path: 'manufacturer-model/list',
        component: HomeComponent,
    },
    {
        path: 'manufacturer-model/details/:id',
        component: ManufacturerModelComponent,
        data: { mode: 'view' },
    },
    {
        path: 'manufacturer-model/edit/:id',
        component: ManufacturerModelComponent,
        data: { mode: 'edit' },
        canActivate: [AuthAdminOrExpertGuard],
    },
    {
        path: 'manufacturer-model/create',
        component: ManufacturerModelComponent,
        data: { mode: 'create' },
        canActivate: [AuthAdminOrExpertGuard],
    },
    {
		path: 'instrument',
		redirectTo: 'instrument/list',
	},
	{
		path: 'instrument/list',
		component: HomeComponent,
	},
	{
		path: 'instrument/details/:id',
		component: InstrumentAssessmentComponent,
		data: { mode: 'view' },
	},
	{
		path: 'instrument/edit/:id',
		component: InstrumentAssessmentComponent,
		data: { mode: 'edit' },
		canActivate: [AuthAdminOrExpertGuard],
	},
	{
		path: 'instrument/create',
		component: InstrumentAssessmentComponent,
		data: { mode: 'create' },
		canActivate: [AuthAdminOrExpertGuard],
	},{
        path: 'download-statistics',
        component: DownloadStatisticsComponent
    },{
		path: 'study-card',
		redirectTo: 'study-card/list',
	},
	{
		path: 'study-card/list',
		component: StudyCardListComponent,
	},
	{
		path: 'study-card/details/:id',
		component: StudyCardComponent,
		data: { mode: 'view' },
	},
	{
		path: 'study-card/edit/:id',
		component: StudyCardComponent,
		data: { mode: 'edit' },
		canActivate: [AuthAdminOrExpertGuard],
	},
    {
		path: 'study-card/create',
		component: StudyCardComponent,
		data: { mode: 'create' },
		canActivate: [AuthAdminOrExpertGuard],
	},
    {
		path: 'study-card/apply/:id',
		component: ApplyStudyCardOnComponent,
        canActivate: [AuthAdminOrExpertGuard],
	},{
		path: 'study-card/apply-on-datasets',
		component: ApplyStudyCardOnComponent,
        canActivate: [AuthAdminOrExpertGuard],
	},
    {
		path: 'quality-card',
		redirectTo: 'quality-card/list',
	},
	{
		path: 'quality-card/list',
		component: QualityCardListComponent,
	},
	{
		path: 'quality-card/details/:id',
		component: QualityCardComponent,
		data: { mode: 'view' },
	},
	{
		path: 'quality-card/edit/:id',
		component: QualityCardComponent,
		data: { mode: 'edit' },
		canActivate: [AuthAdminOrExpertGuard],
	},
	{
		path: 'quality-card/create',
		component: QualityCardComponent,
		data: { mode: 'create' },
		canActivate: [AuthAdminOrExpertGuard],
	},
    {
		path: 'quality-card/create/for-study/:studyId',
		component: QualityCardComponent,
		data: { mode: 'create' },
		canActivate: [AuthAdminOrExpertGuard],
	},
	{
		path: 'dataset-acquisition',
		redirectTo: 'dataset-acquisition/list',
	},
	{
		path: 'dataset-acquisition/list',
		component: DatasetAcquisitionListComponent,
	},
	{
		path: 'dataset-acquisition/details/:id',
		component: DatasetAcquisitionComponent,
		data: { mode: 'view' },
	},
	{
		path: 'dataset-acquisition/edit/:id',
		component: DatasetAcquisitionComponent,
		data: { mode: 'edit' },
		canActivate: [AuthAdminOrExpertGuard],
	},
	{
		path: 'dataset-acquisition/create',
		component: DatasetAcquisitionComponent,
		data: { mode: 'create' },
	},
	{
		path: 'preclinical-reference',
		redirectTo: 'preclinical-reference/list',
	},
	{
		path: 'preclinical-reference/list',
		component: ReferencesListComponent,
	},
	{
		path: 'preclinical-reference/details/:id',
		component: ReferenceFormComponent,
		data: { mode: 'view' },
	},
	{
		path: 'preclinical-reference/edit/:id',
		component: ReferenceFormComponent,
		data: { mode: 'edit' },
		canActivate: [AuthAdminOrExpertGuard],
	},
	{
		path: 'preclinical-reference/create',
		component: ReferenceFormComponent,
		data: { mode: 'create' },
		canActivate: [AuthAdminOrExpertGuard],
	},
	{
		path: 'preclinical-examination',
		redirectTo: 'preclinical-examination/list',
	},
	{
		path: 'preclinical-examination/list',
		component: AnimalExaminationListComponent,
	},
	{
		path: 'preclinical-examination/details/:id',
		component: AnimalExaminationFormComponent,
		data: { mode: 'view' },
	},
	{
		path: 'preclinical-examination/edit/:id',
		component: AnimalExaminationFormComponent,
		data: { mode: 'edit' },
		canActivate: [AuthAdminOrExpertGuard],
	},
	{
		path: 'preclinical-examination/create',
		component: AnimalExaminationFormComponent,
		data: { mode: 'create' },
	},
	{
		path: 'preclinical-therapy',
		redirectTo: 'preclinical-therapy/list',
	},
	{
		path: 'preclinical-therapy/list',
		component: TherapiesListComponent,
	},
	{
		path: 'preclinical-therapy/details/:id',
		component: TherapyFormComponent,
		data: { mode: 'view' },
	},
	{
		path: 'preclinical-therapy/edit/:id',
		component: TherapyFormComponent,
		data: { mode: 'edit' },
		canActivate: [AuthAdminOrExpertGuard],
	},
	{
		path: 'preclinical-therapy/create',
		component: TherapyFormComponent,
		data: { mode: 'create' },
		canActivate: [AuthAdminOrExpertGuard],
	},
	{
		path: 'preclinical-pathology',
		redirectTo: 'preclinical-pathology/list',
	},
	{
		path: 'preclinical-pathology/list',
		component: PathologiesListComponent,
	},
	{
		path: 'preclinical-pathology/details/:id',
		component: PathologyFormComponent,
		data: { mode: 'view' },
	},
	{
		path: 'preclinical-pathology/edit/:id',
		component: PathologyFormComponent,
		data: { mode: 'edit' },
		canActivate: [AuthAdminOrExpertGuard],
	},
	{
		path: 'preclinical-pathology/create',
		component: PathologyFormComponent,
		data: { mode: 'create' },
		canActivate: [AuthAdminOrExpertGuard],
	},
	{
		path: 'preclinical-pathology-model',
		redirectTo: 'preclinical-pathology-model/list',
	},
	{
		path: 'preclinical-pathology-model/list',
		component: PathologyModelsListComponent,
	},
	{
		path: 'preclinical-pathology-model/details/:id',
		component: PathologyModelFormComponent,
		data: { mode: 'view' },
	},
	{
		path: 'preclinical-pathology-model/edit/:id',
		component: PathologyModelFormComponent,
		data: { mode: 'edit' },
		canActivate: [AuthAdminOrExpertGuard],
	},
	{
		path: 'preclinical-pathology-model/create',
		component: PathologyModelFormComponent,
		data: { mode: 'create' },
		canActivate: [AuthAdminOrExpertGuard],
	},
	{
		path: 'preclinical-anesthetic-ingredient',
		redirectTo: 'preclinical-anesthetic-ingredient/list',
	},
	{
		path: 'preclinical-anesthetic-ingredient/list',
		component: AnestheticIngredientsListComponent,
	},
	{
		path: 'preclinical-anesthetic-ingredient/details/:id',
		component: AnestheticIngredientFormComponent,
		data: { mode: 'view' },
	},
	{
		path: 'preclinical-anesthetic-ingredient/edit/:id',
		component: AnestheticIngredientFormComponent,
		data: { mode: 'edit' },
		canActivate: [AuthAdminOrExpertGuard],
	},
	{
		path: 'preclinical-anesthetic-ingredient/create',
		component: AnestheticIngredientFormComponent,
		data: { mode: 'create' },
		canActivate: [AuthAdminOrExpertGuard],
	},
	{
		path: 'preclinical-anesthetic',
		redirectTo: 'preclinical-anesthetic/list',
	},
	{
		path: 'preclinical-anesthetic/list',
		component: AnestheticsListComponent,
	},
	{
		path: 'preclinical-anesthetic/details/:id',
		component: AnestheticFormComponent,
		data: { mode: 'view' },
	},
	{
		path: 'preclinical-anesthetic/edit/:id',
		component: AnestheticFormComponent,
		data: { mode: 'edit' },
		canActivate: [AuthAdminOrExpertGuard],
	},
	{
		path: 'preclinical-anesthetic/create',
		component: AnestheticFormComponent,
		data: { mode: 'create' },
		canActivate: [AuthAdminOrExpertGuard],
	},
	{
		path: 'preclinical-subject',
		redirectTo: 'preclinical-subject/list',
	},
	{
		path: 'preclinical-subject/list',
		component: AnimalSubjectsListComponent,
	},
	{
		path: 'preclinical-subject/details/:id',
		component: AnimalSubjectFormComponent,
		data: { mode: 'view' },
	},
	{
		path: 'preclinical-subject/edit/:id',
		component: AnimalSubjectFormComponent,
		data: { mode: 'edit' },
		canActivate: [AuthAdminOrExpertGuard],
	},
	{
		path: 'preclinical-subject/create',
		component: AnimalSubjectFormComponent,
		data: { mode: 'create' }
	},
    {
        path: 'access-request',
        component: AccessRequestComponent,
        data: { mode: 'create' },
    },
    {
        path: 'access-request/details/:id',
        component: AccessRequestComponent,
        data: { mode: 'view' },
    },
    {
        path: 'access-request/study/:id',
        component: AccessRequestComponent,
        data: { mode: 'create' },
    },
    {
        path: 'access-request/list',
        component: AccessRequestListComponent
    },
    {
        path: 'dua/create/:studyId',
        component: DUAAssistantComponent,
        data: { mode: 'create' }
    },
    {
        path: 'dua/edit/:id',
        component: DUAAssistantComponent,
        data: { mode: 'edit' }
    },
    {
        path: 'dua/view/:id',
        component: DUAAssistantComponent,
        data: { mode: 'view' }
    },{
        path: 'dev',
        component: TestComponent
    },
    {
        path: 'execution-template/create',
        component: ExecutionTemplateComponent,
        data: { mode: 'create' },
    },
    {
        path: 'execution-template/edit/:id',
        component: ExecutionTemplateComponent,
        data: { mode: 'edit' },
    },
    {
        path: 'execution-template/details/:id',
        component: ExecutionTemplateComponent,
        data: { mode: 'view' },
    }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
