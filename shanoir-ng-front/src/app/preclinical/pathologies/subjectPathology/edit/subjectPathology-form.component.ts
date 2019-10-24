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

import { Component, Input, Output, EventEmitter } from '@angular/core';
import { FormGroup, Validators } from '@angular/forms';
import {  ActivatedRoute } from '@angular/router';
import { IMyOptions, IMyDateModel, IMyInputFieldChanged } from 'mydatepicker';

import { SubjectPathology }    from '../shared/subjectPathology.model';
import { SubjectPathologyService } from '../shared/subjectPathology.service';
import { PathologyModelService } from '../../pathologyModel/shared/pathologyModel.service';
import { PathologyService } from '../../pathology/shared/pathology.service';
import { PathologyModel }    from '../../pathologyModel/shared/pathologyModel.model';
import { Pathology }   from '../../pathology/shared/pathology.model';
import { ReferenceService } from '../../../reference/shared/reference.service';
import { Reference }    from '../../../reference/shared/reference.model';
import { PreclinicalSubject } from '../../../animalSubject/shared/preclinicalSubject.model';
import * as PreclinicalUtils from '../../../utils/preclinical.utils';
import { ModesAware } from "../../../shared/mode/mode.decorator";
import { EntityComponent } from '../../../../shared/components/entity/entity.component.abstract';
import { slideDown } from '../../../../shared/animations/animations';

@Component({
    selector: 'subject-pathology-form',
    templateUrl: 'subjectPathology-form.component.html',
    providers: [SubjectPathologyService, PathologyModelService, PathologyService, ReferenceService],
    animations: [slideDown]
})
@ModesAware
export class SubjectPathologyFormComponent extends EntityComponent<SubjectPathology> {

    @Input() preclinicalSubject: PreclinicalSubject;
    @Input() canModify: Boolean = false;
    @Input('toggleForm') toggleForm: boolean;
    @Input() subjectpathoSelected: SubjectPathology;
    @Output() onEvent = new EventEmitter();
    @Output() onCreated = new EventEmitter();
    @Output() onCancel = new EventEmitter();
    @Input() createSPMode: boolean;
    pathologies: Pathology[] = [];
    models: PathologyModel[] = [];
    modelsDisplay: PathologyModel[] = [];
    locations: Reference[] = [];


    constructor(
        private route: ActivatedRoute,
        private  subjectPathologyService: SubjectPathologyService, 
        private modelService: PathologyModelService,
        private pathologyService: PathologyService,
        private referenceService: ReferenceService,
    )
    {

        super(route, 'preclinical-subject-pathology');
    }
   
    get subjectPathology(): SubjectPathology { return this.entity; }
    set subjectPathology(subjectPathology: SubjectPathology) { this.entity = subjectPathology; }


    initView(): Promise<void> {
        return new  Promise<void>(resolve => {
            this.subjectPathology = new SubjectPathology();
            this.loadPathologies();
            this.loadModels();
            this.loadReferences();
            if (this.subjectpathoSelected) {
                this.subjectPathology = this.subjectpathoSelected;
            }
            this.subjectPathologyService.get(this.id).then(subjectPathology => {
                this.subjectPathology = subjectPathology;
                resolve();
            });
        });        
    }

    initEdit(): Promise<void> {
        return new  Promise<void>(resolve => {
            this.subjectPathology = new SubjectPathology();
            this.loadPathologies();
            this.loadModels();
            this.loadReferences();
            if (this.subjectpathoSelected) {
                this.subjectPathology = this.subjectpathoSelected;
            }
            this.subjectPathologyService.get(this.id).then(subjectPathology => {
                this.subjectPathology = subjectPathology;
                resolve();
            });
        });
    }

    initCreate(): Promise<void> {
        this.entity = new SubjectPathology();
        this.loadPathologies();
        this.loadModels();
        this.loadReferences();
        return Promise.resolve();
    }

    buildForm(): FormGroup {
        return this.formBuilder.group({
            'id': [this.subjectPathology.id],
            'pathology': [this.subjectPathology.pathology, Validators.required],
            'pathologyModel': [this.subjectPathology.pathologyModel, Validators.required],
            'location': [this.subjectPathology.location, Validators.required],
            'startDate': [this.subjectPathology.startDate],
            'endDate': [this.subjectPathology.endDate]
        });
    }
    
    loadPathologies() {
        this.pathologyService.getAll().then(pathologies => this.pathologies = pathologies);
    }

    loadModels() {
        this.modelService.getAll().then(models => this.models = models);
    }

    loadReferences() {
        this.referenceService.getReferencesByCategoryAndType(PreclinicalUtils.PRECLINICAL_CAT_ANATOMY, PreclinicalUtils.PRECLINICAL_ANATOMY_LOCATION).then(locations => this.locations = locations);
    }

    toggleFormSP(creation: boolean): void {
        if (this.toggleForm == false) {
            this.toggleForm = true;
        } else if (this.toggleForm == true) {
            this.toggleForm = false;
            this.onCancel.emit(false);
        } else {
            this.toggleForm = false;
            this.onCancel.emit(false);
        }
        this.createSPMode = creation;
    }
    
    toggleFormSPAndReset(creation: boolean): void {
        this.toggleFormSP(creation);
        this.subjectPathology = new SubjectPathology();
    }

    
    goToAddPathology(){
        this.router.navigate(['/preclinical-pathology/create']);
    }
    goToAddPathologyModel(){
        this.router.navigate(['/preclinical-pathology-model/create']);
    }
    goToAddLocation(){
        this.router.navigate(['/preclinical-reference/create'], { queryParams: { category:"anatomy", reftype:"location" } });
    }

    ngOnChanges() {
        if (this.subjectpathoSelected) {
            this.loadSubjectPathologyAttributesForSelect(this.subjectpathoSelected);
        }
        if (this.toggleForm) {
            this.buildForm();
            this.form.markAsUntouched();
        }
   }

    //AS we need to have the same object reference in select and model, w have to set them from loaded lists 
    loadSubjectPathologyAttributesForSelect(selectedPatho: SubjectPathology) {
        this.subjectPathology = selectedPatho;
        if (this.pathologies) {
            for (let patho of this.pathologies) {
                if (selectedPatho.pathology && selectedPatho.pathology.id == patho.id) {
                    this.subjectPathology.pathology = patho;
                }
            }
        }
        if (this.models) {
            for (let model of this.models) {
                if (selectedPatho.pathologyModel && selectedPatho.pathologyModel.id == model.id) {
                    this.subjectPathology.pathologyModel = model;
                }
            }
        }
        if (this.locations) {
            for (let location of this.locations) {
                if (selectedPatho.location && selectedPatho.location.id == location.id) {
                    this.subjectPathology.location = location;
                }
            }
        }
        
        
        if(this.subjectPathology && this.subjectPathology.pathology) this.loadModelsDisplay();
    }



    addSubjectPathology() {
        if (!this.subjectPathology) {
            return;
        }
        if (this.mode == 'create' ) {
            if (this.preclinicalSubject.pathologies === undefined) {
                this.preclinicalSubject.pathologies = [];
            }
            if (this.onCreated.observers.length > 0) {
                this.onCreated.emit(this.subjectPathology);
            }
        } else {
            this.subjectPathologyService.createSubjectPathology(this.preclinicalSubject, this.subjectPathology)
                .then(subjectPathology => {
                    if (this.onCreated.observers.length > 0) {
                        this.onCreated.emit(subjectPathology);
                    }
                });
        }
        this.toggleFormSPAndReset(true);
    }

    updateSubjectPathology(): void {
        this.subjectPathologyService.updateSubjectPathology(this.preclinicalSubject, this.subjectPathology);
        this.toggleFormSPAndReset(false);
    }


    loadModelsDisplay(){
        if (this.subjectPathology && this.subjectPathology.pathology) {
            this.modelsDisplay = [];
            if (this.models){
            	for (let model of this.models) {
                	if (this.subjectPathology.pathology.id == model.pathology.id) {
                    	this.modelsDisplay.push(model);
                	}
            	} 
            }           
        }else{
            this.modelsDisplay = [];   
        }
    }
    
    
    refreshModelsByPathology() {
        this.loadModelsDisplay();
        //must empty model value
        if (this.subjectPathology) this.subjectPathology.pathologyModel = undefined;
    }




    canUpdatePathology(): boolean{
        return !this.createSPMode && this.keycloakService.isUserAdminOrExpert && this.mode != 'view';
    }

    canAddPathology(): boolean{
        return this.createSPMode && this.mode != 'view';
    }

    cancelPathology(){
        this.toggleFormSP(false);
    }

    addPathology(): Promise<void> {
        if (!this.subjectPathology) { 
            console.log('nothing to create');
            return; 
        }
        if(this.preclinicalSubject.pathologies === undefined){
            this.preclinicalSubject.pathologies = [];
        }
        if (this.onEvent.observers.length > 0) {
            this.onEvent.emit([this.subjectPathology, true]);
        }
        this.toggleForm = false;
        this.subjectPathology = new SubjectPathology();
    }
    
    updatePathology(): void {
        if (!this.subjectPathology) { 
            console.log('nothing to update');
            return; 
        }
        if (this.onEvent.observers.length > 0) {
            this.onEvent.emit([this.subjectPathology, false]);
        }
        this.toggleForm = false;
        this.subjectPathology = new SubjectPathology();
    }

}