import { NgModule }            from '@angular/core';
import { RouterModule }        from '@angular/router';

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
import { AnestheticIngredientsListComponent } from './anesthetics/ingredients/list/anestheticIngredient-list.component';
import { AnestheticIngredientFormComponent }      from './anesthetics/ingredients/edit/anestheticIngredient-form.component';
import { ExaminationAnestheticFormComponent }      from './anesthetics/examination_anesthetic/edit/examinationAnesthetic-form.component';
import { ExaminationAnestheticsListComponent } from './anesthetics/examination_anesthetic/list/examinationAnesthetic-list.component';
import { ContrastAgentsListComponent } from './contrastAgent/list/contrastAgent-list.component';
import { ContrastAgentFormComponent }      from './contrastAgent/edit/contrastAgent-form.component';
import { AnimalExaminationFormComponent }      from './examination/edit/animal-examination-form.component';
import { AnimalExaminationListComponent }      from './examination/list/animal-examination-list.component';
import { ExtraDataFormComponent }      from './extraData/extraData/edit/extradata-form.component';
import { PhysiologicalDataFormComponent }      from './extraData/physiologicalData/add/physiologicalData-form.component';
import { ExtraDataListComponent }      from './extraData/extraData/list/extradata-list.component';
import { BloodGasDataFormComponent }      from './extraData/bloodGasData/add/bloodGasData-form.component';
//import { ReferenceCategoryFormComponent } from './reference/category/category-form.component';
//import { ReferenceTypeFormComponent } from './reference/type/type-form.component';

@NgModule({
  imports: [RouterModule.forChild([
    { 
        path: 'preclinical/subjects', 
        component: AnimalSubjectsListComponent 
    },{ 
        path: 'preclinical/subject', 
        component: AnimalSubjectFormComponent 
    },{ 
        path: 'preclinical/references', 
        component: ReferencesListComponent 
    },{ 
        path: 'preclinical/reference', 
        component: ReferenceFormComponent 
    },{ 
        path: 'preclinical/pathologies', 
        component: PathologiesListComponent 
    },{ 
        path: 'preclinical/pathology', 
        component: PathologyFormComponent 
    },{ 
        path: 'preclinical/pathologies/models',
        component: PathologyModelsListComponent 
    },{ 
        path: 'preclinical/pathologies/model',
        component: PathologyModelFormComponent 
    },{ 
        path: 'preclinical/therapies', 
        component: TherapiesListComponent 
    },{ 
        path: 'preclinical/therapy', 
        component: TherapyFormComponent 
    },{ 
        path: 'preclinical/anesthetics', 
        component: AnestheticsListComponent 
    },{ 
        path: 'preclinical/anesthetic', 
        component: AnestheticFormComponent 
    },{ 
        path: 'preclinical/examinations', 
        component: AnimalExaminationListComponent 
    },{ 
        path: 'preclinical/examination',
        component: AnimalExaminationFormComponent 
    },{ 
        path: 'preclinical/contrastagents', 
        component: ContrastAgentsListComponent 
    },{ 
        path: 'preclinical/contrastagent', 
        component: ContrastAgentFormComponent 
    }
    
  ])],
  exports: [RouterModule]
})
export class PreclinicalRoutingModule {}