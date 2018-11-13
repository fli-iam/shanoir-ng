import { Component, Output, EventEmitter } from '@angular/core';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';
import { Router, ActivatedRoute, Params } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { Location } from '@angular/common';

import { PathologyModel }    from '../shared/pathologyModel.model';
import { PathologyModelService } from '../shared/pathologyModel.service';
import { Pathology }   from '../../pathology/shared/pathology.model';
import { PathologyService } from '../../pathology/shared/pathology.service';

import * as PreclinicalUtils from '../../../utils/preclinical.utils';
import { KeycloakService } from "../../../../shared/keycloak/keycloak.service";
import { Mode } from "../../../shared/mode/mode.model";
import { Modes } from "../../../shared/mode/mode.enum";
import { ModesAware } from "../../../shared/mode/mode.decorator";

import { ImagesUrlUtil } from '../../../../shared/utils/images-url.util';

@Component({
    selector: 'pathologyModel-form',
    templateUrl: 'pathologyModel-form.component.html',
    providers: [PathologyModelService, PathologyService]
})
@ModesAware
export class PathologyModelFormComponent {

    private model = new PathologyModel();
    @Output() closing = new EventEmitter();
    newModelForm: FormGroup;
    private mode:Mode = new Mode();
    private modelId: number;
    private canModify: Boolean = false;
    pathologies: Pathology[];
    private isModelUnique: Boolean = true;
    uploadUrl: string;
    fileToUpload: File = null;
    private addIconPath: string = ImagesUrlUtil.ADD_ICON_PATH;

    constructor(
        private modelService: PathologyModelService,
        private pathologyService: PathologyService,
        private keycloakService: KeycloakService,
        private fb: FormBuilder,
        private route: ActivatedRoute,
        private router: Router,
        private location: Location) { }


    getOut(model: PathologyModel = null): void {
        if (this.closing.observers.length > 0) {
            this.closing.emit(model);
        } else {
            this.location.back();
        }
    }
    
    goToAddPathology(){
        this.router.navigate(['/preclinical-pathology'], { queryParams: { mode: "create" } });
    }


    getPathologyModel() {
        this.route.queryParams
            .switchMap((queryParams: Params) => {
                let modelId = queryParams['id'];
                let mode = queryParams['mode'];
                if (mode) {
                    this.mode.setModeFromParameter(mode);
                }
                if (modelId) {
                    // view or edit mode
                    this.modelId = modelId;
                    return this.modelService.getPathologyModel(modelId);
                } else {
                    // create mode
                    return Observable.of<PathologyModel>();
                }
            })
            .subscribe(model => {
                if (!this.mode.isCreateMode()) {
                    this.model = model;
                    if(this.pathologies){
                        for(let patho of this.pathologies){
                            if(patho.id == model.pathology.id) this.model.pathology = patho;
                        }
                    }
                    //Generate url for upload
                    this.uploadUrl = this.modelService.getUploadUrl(this.model);
                }
            });
    }

    loadData() {
        this.pathologyService.getAll().then(pathologies => {
            this.pathologies = pathologies;
            this.getPathologyModel();
        });
        //this.pathologies = [];
    }

    goToEditPage(): void {
        this.router.navigate(['/preclinical-pathologies-model'], { queryParams: { id: this.modelId, mode: "edit" } });
    }

    ngOnInit(): void {
        this.loadData();
        this.buildForm();
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.canModify = true;
        }
    }


    buildForm(): void {
        this.newModelForm = this.fb.group({
            'name': [this.model.name, Validators.required],
            'pathology': [this.model.pathology, Validators.required],
            'comment': [this.model.comment],
            'specifications': [this.model.filename]
        });

        this.newModelForm.valueChanges
            .subscribe(data => this.onValueChanged(data));
        this.onValueChanged();
    }

    onValueChanged(data?: any) {
        if (!this.newModelForm) { return; }
        const form = this.newModelForm;
        for (const field in this.formErrors) {
            // clear previous error message (if any)
            this.formErrors[field] = '';
            const control = form.get(field);
            if (control && control.dirty && !control.valid) {
                for (const key in control.errors) {
                    this.formErrors[field] += key;
                }
            }
        }
    }

    formErrors = {
        'name': '',
        'pathology': ''
    };

    addPathologyModel() {
        if (!this.model) { return; }
        this.modelService.create(this.model)
            .subscribe(model => {
            	this.model = model;
                if (this.fileToUpload){
                    //Then upload specifications file
                    //this.uploadUrl = this.modelService.getUploadUrl(model);
                    //model.fileUploadReady = this.model.fileUploadReady;
                    //model.fileUploadReady.launchRequest(this.uploadUrl).subscribe();
                    this.modelService.postFile(this.fileToUpload, this.model)
                    	.subscribe(res => {console.log(res)}, 
                    				(err: String) => {console.log('error in posting File ' + err);});
                }

                this.getOut(model);
            }, (err: String) => {
                console.log('error in update ' + err);
                if (err.indexOf("should be unique") != -1) {
                    this.isModelUnique = false;
                }
            });

    }

    updatePathologyModel(): void {
        this.modelService.update(this.model)
            .subscribe(model => {
            	
                if (this.fileToUpload) {
                    //Then upload specifications file
                    this.modelService.postFile(this.fileToUpload, this.model)
                    	.subscribe(res => {console.log(res)}, 
                    				(err: String) => {console.log('error in posting File ' + err);});
                    
                }
                this.getOut(model);
            }, (err: String) => {
                console.log('error in update ' + err);
                if (err.indexOf("should be unique") != -1) {
                    this.isModelUnique = false;
                }
            });
    }
    
    
    fileChangeEvent(files: FileList){
    	this.fileToUpload = files.item(0);
    	this.model.filename = this.fileToUpload.name;
    }
    
    
    
    


}