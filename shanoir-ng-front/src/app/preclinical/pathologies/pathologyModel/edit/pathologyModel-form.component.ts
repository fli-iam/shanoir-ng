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

import { Component } from '@angular/core';
import { Validators, UntypedFormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import {  ActivatedRoute } from '@angular/router';

import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';

import { PathologyModel }    from '../shared/pathologyModel.model';
import { PathologyModelService } from '../shared/pathologyModel.service';
import { Pathology }   from '../../pathology/shared/pathology.model';
import { PathologyService } from '../../pathology/shared/pathology.service';
import { slideDown } from '../../../../shared/animations/animations';
import { EntityComponent } from '../../../../shared/components/entity/entity.component.abstract';
import { Step } from '../../../../breadcrumbs/breadcrumbs.service';
import { FormFooterComponent } from '../../../../shared/components/form-footer/form-footer.component';
import { SelectBoxComponent } from '../../../../shared/select/select.component';


@Component({
    selector: 'pathologyModel-form',
    templateUrl: 'pathologyModel-form.component.html',
    animations: [slideDown],
    imports: [FormsModule, ReactiveFormsModule, FormFooterComponent, SelectBoxComponent]
})
export class PathologyModelFormComponent extends EntityComponent<PathologyModel>{

    pathologies: Pathology[];
    uploadUrl: string;
    fileToUpload: File = null;

    public isModelUnique = true;

    constructor(
        private route: ActivatedRoute,
        private modelService: PathologyModelService,
        private pathologyService: PathologyService)
        {

            super(route, 'preclinical-pathology-model');
            this.manageSaveEntity();
        }

    get model(): PathologyModel { return this.entity; }
    set model(model: PathologyModel) { this.entity = model; }

    getService(): EntityService<PathologyModel> {
        return this.modelService;
    }

    initView(): Promise<void> {
        return Promise.resolve();
    }

    initEdit(): Promise<void> {
        this.loadData();
        if(this.pathologies){
            for(const patho of this.pathologies){
                if(patho.id == this.model.pathology.id)
                    this.model.pathology = patho;
            }
        }
        //Generate url for upload
        this.uploadUrl = this.modelService.getUploadUrl(this.model.id);
        return Promise.resolve();
    }

    initCreate(): Promise<void> {
        this.entity = new PathologyModel();
        this.loadData();
        return Promise.resolve();
    }

    buildForm(): UntypedFormGroup {
        return this.formBuilder.group({
            'name': [this.model.name, [Validators.required, this.registerOnSubmitValidator('unique', 'name')]],
            'pathology': [this.model.pathology, Validators.required],
            'comment': [this.model.comment],
            'filename': [this.model.filename]
        });
    }

    manageSaveEntity(): void {
        this.subscriptions.push(
            this.onSave.subscribe(response => {
                if (this.fileToUpload){
                    //Then upload specifications file
                    this.modelService.postFile(this.fileToUpload, response.id);
                }
            })
        );

    }

    goToAddPathology(){
        const currentStep: Step = this.breadcrumbsService.currentStep;
        this.router.navigate(['/preclinical-pathology/create']).then(() => {
            currentStep.waitFor(this.breadcrumbsService.currentStep).subscribe(entity => {
                this.pathologies.push(entity as Pathology);
                this.entity.pathology = entity as Pathology;
            });
        });
    }

    loadData() {
        this.pathologyService.getAll().then(pathologies => {
            this.pathologies = pathologies;
        });
    }

    fileChangeEvent(files: FileList){
    	this.fileToUpload = files.item(0);
    	this.model.filename = this.fileToUpload.name;
    }






}
