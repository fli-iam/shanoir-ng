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

import { RouterModule, Routes }        from '@angular/router';

import { AnimalSubjectsListComponent } from './animalSubject/list/animalSubject-list.component'; 
import { AnimalSubjectFormComponent }      from './animalSubject/edit/animalSubject-form.component';
import { ReferencesListComponent } from './reference/list/reference-list.component';
import { ReferenceFormComponent }      from './reference/edit/reference-form.component';
import { PathologiesListComponent } from './pathologies/pathology/list/pathology-list.component';
import { PathologyFormComponent }      from './pathologies/pathology/edit/pathology-form.component';
import { PathologyModelsListComponent } from './pathologies/pathologyModel/list/pathologyModel-list.component';
import { PathologyModelFormComponent }      from './pathologies/pathologyModel/edit/pathologyModel-form.component';
import { TherapiesListComponent } from './therapies/therapy/list/therapy-list.component';
import { TherapyFormComponent }      from './therapies/therapy/edit/therapy-form.component';
import { AnestheticsListComponent } from './anesthetics/anesthetic/list/anesthetic-list.component';
import { AnestheticFormComponent }      from './anesthetics/anesthetic/edit/anesthetic-form.component';
import { ContrastAgentsListComponent } from './contrastAgent/list/contrastAgent-list.component';
import { ContrastAgentFormComponent }      from './contrastAgent/edit/contrastAgent-form.component';
import { AnimalExaminationFormComponent }      from './examination/edit/animal-examination-form.component';
import { AnimalExaminationListComponent }      from './examination/list/animal-examination-list.component';
import { ExaminationAnestheticsListComponent } from './anesthetics/examination_anesthetic/list/examinationAnesthetic-list.component';
import { ExaminationAnestheticFormComponent } from './anesthetics/examination_anesthetic/edit/examinationAnesthetic-form.component';
import { ImportBrukerComponent } from './importBruker/importBruker.component';
import { AuthAdminOrExpertGuard } from '../shared/roles/auth-admin-or-expert-guard';

import { getRoutesFor } from '../app.routing'
import { BrukerUploadComponent } from './importBruker/bruker-upload/bruker-upload.component';
import { BrukerSelectSeriesComponent } from './importBruker/select-series/bruker-select-series.component';
import { AnimalClinicalContextComponent } from './importBruker/clinical-context/animal-clinical-context.component';
import { BrukerFinishImportComponent } from './importBruker/finish/bruker-finish.component';
import { ModuleWithProviders } from '@angular/compiler/src/core';
import { SubjectTherapyFormComponent } from './therapies/subjectTherapy/edit/subjectTherapy-form.component';
import { SubjectTherapiesListComponent } from './therapies/subjectTherapy/list/subjectTherapy-list.component';
import { AnestheticIngredientFormComponent } from './anesthetics/ingredients/edit/anestheticIngredient-form.component';
import { AnestheticIngredientsListComponent } from './anesthetics/ingredients/list/anestheticIngredient-list.component';

let routes : Routes= [
    { 
        path: 'preclinical-contrastagents', 
        component: ContrastAgentsListComponent 
    },{ 
        path: 'preclinical-contrastagent', 
        component: ContrastAgentFormComponent 
    },
  	{
        path: 'importsBruker',
        component: ImportBrukerComponent,
        children: [
            {
                path: '',
                pathMatch: 'full',
                redirectTo: 'upload'
            }, {
                path: 'upload',
                component: BrukerUploadComponent
            }, {
                path: 'series',
                component: BrukerSelectSeriesComponent
            }, {
                path: 'context',
                component: AnimalClinicalContextComponent
            }, {
                path: 'finish',
                component: BrukerFinishImportComponent
            }
        ]
    }
    
  ];

  routes = routes.concat(
      getRoutesFor('preclinical-reference', ReferenceFormComponent, ReferencesListComponent, {create: AuthAdminOrExpertGuard, update: AuthAdminOrExpertGuard}), 
      getRoutesFor('preclinical-examination', AnimalExaminationFormComponent, AnimalExaminationListComponent, {create: AuthAdminOrExpertGuard, update: AuthAdminOrExpertGuard}),
      getRoutesFor('preclinical-therapy', TherapyFormComponent, TherapiesListComponent, {create: AuthAdminOrExpertGuard, update: AuthAdminOrExpertGuard}),
      getRoutesFor('preclinical-pathology', PathologyFormComponent,PathologiesListComponent, {create: AuthAdminOrExpertGuard, update: AuthAdminOrExpertGuard}), 
      getRoutesFor('preclinical-pathology-model', PathologyModelFormComponent,PathologyModelsListComponent, {create: AuthAdminOrExpertGuard, update: AuthAdminOrExpertGuard}),
      getRoutesFor('preclinical-anesthetic-ingredient', AnestheticIngredientFormComponent,AnestheticIngredientsListComponent, {create: AuthAdminOrExpertGuard, update: AuthAdminOrExpertGuard}),
      getRoutesFor('preclinical-anesthetic', AnestheticFormComponent,AnestheticsListComponent, {create: AuthAdminOrExpertGuard, update: AuthAdminOrExpertGuard}),
      getRoutesFor('preclinical-subject', AnimalSubjectFormComponent,AnimalSubjectsListComponent, {create: AuthAdminOrExpertGuard, update: AuthAdminOrExpertGuard})
  );

  export const preclinicalRouting: ModuleWithProviders = RouterModule.forRoot(routes); 
