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
import { Validators, UntypedFormGroup } from '@angular/forms';
import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';

import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';

import { PathologyModel }    from '../shared/pathologyModel.model';
import { PathologyModelService } from '../shared/pathologyModel.service';
import { Pathology }   from '../../pathology/shared/pathology.model';
import { PathologyService } from '../../pathology/shared/pathology.service';
import { slideDown } from '../../../../shared/animations/animations';
import { EntityComponent } from '../../../../shared/components/entity/entity.component.abstract';
import { Step } from '../../../../breadcrumbs/breadcrumbs.service';


@Component({
    selector: 'pathologyModel-form',
    templateUrl: 'pathologyModel-form.component.html',
    animations: [slideDown],
    standalone: false
})
export class PathologyModelFormComponent extends EntityComponent<PathologyModel>{

    pathologies: Pathology[];
    uploadUrl: string;
    fileToUpload: File = null;

    public isModelUnique = true;

    constructor(
        private route: ActivatedRoute,
        private modelService: PathologyModelService,
        private pathologyService: PathologyService,
        private pathologyModelService: PathologyModelService
    ) {
        super(route);
    }

    protected getRoutingName(): string {
        return 'preclinical-pathology-model';
        this.manageSaveEntity();
    }

    get pathologyModel(): PathologyModel { return this.entity; }
    set pathologyModel(model: PathologyModel) { this.entity = model; }


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
                if(patho.id == this.pathologyModel.pathology.id)
                    this.pathologyModel.pathology = patho;
            }
        }
        //Generate url for upload
        this.uploadUrl = this.modelService.getUploadUrl(this.pathologyModel.id);
        return Promise.resolve();
    }

    initCreate(): Promise<void> {
        this.entity = new PathologyModel();
        this.loadData();
        return Promise.resolve();
    }

    buildForm(): UntypedFormGroup {
        return this.formBuilder.group({
            'name': [this.pathologyModel.name, [Validators.required, this.registerOnSubmitValidator('unique', 'name')]],
            'pathology': [this.pathologyModel.pathology, Validators.required],
            'comment': [this.pathologyModel.comment],
            'filename': [this.pathologyModel.filename]
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
    	this.pathologyModel.filename = this.fileToUpload.name;
    }

    public save(afterSave?: () => Promise<void>): Promise<PathologyModel> {
        if (this.pathologyModel.id) {
            return this.updatePathologyModel().then(pathologyModel => {
                if (this.fileToUpload)
                    this.pathologyModelService.postFile(this.fileToUpload, pathologyModel.id)
                        .subscribe({
                            next: res => this.consoleService.log('info', 'File uploaded successfully'),
                            error: err => this.consoleService.log('error', 'File upload failed')
                        });

                this.onSave.next(pathologyModel);
                this.chooseRouteAfterSave(pathologyModel);
                this.consoleService.log('info', 'Preclinical-pathology-model ' + pathologyModel.name + ' has been successfully saved under the id ' + pathologyModel.id);
                return pathologyModel;
            }).catch(reason => {
                this.footerState.loading = false;
                this.catchSavingErrors(reason);
                return null;
            });
        } else {
            return this.addPathologyModel().then(async pathologyModel => {
                if (pathologyModel == null) return;

                if (this.fileToUpload)
                    this.pathologyModelService.postFile(this.fileToUpload, pathologyModel.id)
                        .subscribe({
                            next: res => this.consoleService.log('info', 'File uploaded successfully'),
                            error: err => this.consoleService.log('error', 'File upload failed')
                        });

                this.onSave.next(pathologyModel);
                this.chooseRouteAfterSave(pathologyModel);
                this.consoleService.log('info', 'Preclinical-pathology-model ' + pathologyModel.name + ' has been successfully saved under the id ' + pathologyModel.id);
                return pathologyModel;
            }).catch(reason => {
                this.footerState.loading = false;
                this.catchSavingErrors(reason);
                return null;
            });
        }
    }

    addPathologyModel(): Promise<PathologyModel> {
        if (!this.pathologyModel) return Promise.resolve(null);
        return this.pathologyModelService.create(this.pathologyModel).then(pathologyModel => {
            this.pathologyModel = pathologyModel;
            return this.pathologyModel;
        }).catch(this.catchSavingErrors);
    }

    updatePathologyModel(): Promise<PathologyModel> {
        if (!this.pathologyModel) return Promise.resolve(null);
        return this.pathologyModelService.update(this.pathologyModel.id, this.pathologyModel).then(() => {
            return this.pathologyModel;
        }).catch(this.catchSavingErrors);
    }
}
