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
import { AuthAdminOrExpertGuard } from '../shared/roles/auth-admin-or-expert-guard';

import { BrukerUploadComponent } from './importBruker/bruker-upload/bruker-upload.component';
import { BrukerSelectSeriesComponent } from './importBruker/select-series/bruker-select-series.component';
import { SubjectTherapyFormComponent } from './therapies/subjectTherapy/edit/subjectTherapy-form.component';
import { SubjectTherapiesListComponent } from './therapies/subjectTherapy/list/subjectTherapy-list.component';
import { AnestheticIngredientFormComponent } from './anesthetics/ingredients/edit/anestheticIngredient-form.component';
import { AnestheticIngredientsListComponent } from './anesthetics/ingredients/list/anestheticIngredient-list.component';
import { NgModule } from '@angular/core';

let routes : Routes = [
    {
        path: 'preclinical-contrastagents', 
        component: ContrastAgentsListComponent 
    },{ 
        path: 'preclinical-contrastagent', 
        component: ContrastAgentFormComponent 
    },
  	{
        path: 'importsBruker',
        // component: ImportBrukerComponent,
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
            }
        ]
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
        canActivate: [AuthAdminOrExpertGuard],
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
        data: { mode: 'create' },
        canActivate: [AuthAdminOrExpertGuard],
    }
  ];

//   routes = routes.concat(
//       getRoutesFor('preclinical-reference', ReferenceFormComponent, ReferencesListComponent, {create: AuthAdminOrExpertGuard, update: AuthAdminOrExpertGuard}), 
//       getRoutesFor('preclinical-examination', AnimalExaminationFormComponent, AnimalExaminationListComponent, {create: AuthAdminOrExpertGuard, update: AuthAdminOrExpertGuard}),
//       getRoutesFor('preclinical-therapy', TherapyFormComponent, TherapiesListComponent, {create: AuthAdminOrExpertGuard, update: AuthAdminOrExpertGuard}),
//       getRoutesFor('preclinical-pathology', PathologyFormComponent,PathologiesListComponent, {create: AuthAdminOrExpertGuard, update: AuthAdminOrExpertGuard}), 
//       getRoutesFor('preclinical-pathology-model', PathologyModelFormComponent,PathologyModelsListComponent, {create: AuthAdminOrExpertGuard, update: AuthAdminOrExpertGuard}),
//       getRoutesFor('preclinical-anesthetic-ingredient', AnestheticIngredientFormComponent,AnestheticIngredientsListComponent, {create: AuthAdminOrExpertGuard, update: AuthAdminOrExpertGuard}),
//       getRoutesFor('preclinical-anesthetic', AnestheticFormComponent,AnestheticsListComponent, {create: AuthAdminOrExpertGuard, update: AuthAdminOrExpertGuard}),
//       getRoutesFor('preclinical-subject', AnimalSubjectFormComponent,AnimalSubjectsListComponent, {create: AuthAdminOrExpertGuard, update: AuthAdminOrExpertGuard})
//   );

//   export const preclinicalRouting: ModuleWithProviders = RouterModule.forRoot(routes); 

@NgModule({
    imports: [RouterModule.forRoot(routes)],
    exports: [RouterModule]
  })
export class PreclinicalRoutingModule { }