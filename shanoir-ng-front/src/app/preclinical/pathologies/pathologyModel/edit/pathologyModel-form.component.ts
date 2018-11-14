import { Component } from '@angular/core';
import {  Validators, FormGroup } from '@angular/forms';
import {  ActivatedRoute } from '@angular/router';

import { PathologyModel }    from '../shared/pathologyModel.model';
import { PathologyModelService } from '../shared/pathologyModel.service';
import { Pathology }   from '../../pathology/shared/pathology.model';
import { PathologyService } from '../../pathology/shared/pathology.service';
import { slideDown } from '../../../../shared/animations/animations';
import { ModesAware } from "../../../shared/mode/mode.decorator";
import { EntityComponent } from '../../../../shared/components/entity/entity.component.abstract';
import { Step } from '../../../../breadcrumbs/breadcrumbs.service';


@Component({
    selector: 'pathologyModel-form',
    templateUrl: 'pathologyModel-form.component.html',
    providers: [PathologyModelService, PathologyService],
    animations: [slideDown]
})
@ModesAware
export class PathologyModelFormComponent extends EntityComponent<PathologyModel>{

    pathologies: Pathology[];
    uploadUrl: string;
    fileToUpload: File = null;

    constructor(
        private route: ActivatedRoute,
        private modelService: PathologyModelService, 
        private pathologyService: PathologyService) 
        {

            super(route, 'preclinical-pathology-model');
            this.manageSaveEntity();
        }

    get model(): PathologyModel { return this.entity; }
    set model(model: PathologyModel) { this.entity = model; }

    initView(): Promise<void> {
        return this.modelService.get(this.id).then(model => {
            this.model = model;
        });
    }

    initEdit(): Promise<void> {
        this.loadData();
        return this.modelService.get(this.id).then(model => {
            this.model = model;
            if(this.pathologies){
                for(let patho of this.pathologies){
                    if(patho.id == model.pathology.id) 
                        this.model.pathology = patho;
                }
            }
            //Generate url for upload
            this.uploadUrl = this.modelService.getUploadUrl(this.model.id);
        });
    }

    initCreate(): Promise<void> {
        this.entity = new PathologyModel();
        this.loadData();
        return Promise.resolve();
    }

    buildForm(): FormGroup {
        return this.formBuilder.group({
            'name': [this.model.name, Validators.required],
            'pathology': [this.model.pathology, Validators.required],
            'comment': [this.model.comment],
            'specifications': [this.model.filename]
        });
    }

    manageSaveEntity(): void {
        this.subscribtions.push(
            this.onSave.subscribe(response => {
                if (this.fileToUpload){
                    //Then upload specifications file
                    this.modelService.postFile(this.fileToUpload, response.id)
                    	.subscribe(res => {console.log(res)}, 
                    				(err: String) => {console.log('error in posting File ' + err);});
                }
            })
        );
       
    }

    goToAddPathology(){
        let currentStep: Step = this.breadcrumbsService.currentStep;
        this.router.navigate(['/preclinical-pathology/create']).then(success => {
            currentStep.waitFor(this.breadcrumbsService.currentStep).subscribe(entity => {
                this.pathologies.push(entity as Pathology);
                (currentStep.entity as PathologyModel).pathology = entity as Pathology;
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