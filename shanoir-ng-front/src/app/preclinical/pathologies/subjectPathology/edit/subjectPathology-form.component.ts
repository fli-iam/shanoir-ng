import { Component, Input, Output, EventEmitter, OnInit, OnChanges, ChangeDetectionStrategy } from '@angular/core';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';
import { Router, ActivatedRoute, Params } from '@angular/router';
import { Location } from '@angular/common';
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
import { KeycloakService } from "../../../../shared/keycloak/keycloak.service";
import { Mode } from "../../../shared/mode/mode.model";
import { Modes } from "../../../shared/mode/mode.enum";
import { ModesAware } from "../../../shared/mode/mode.decorator";
import { ImagesUrlUtil } from "../../../../shared/utils/images-url.util";


@Component({
    selector: 'subject-pathology-form',
    templateUrl: 'subjectPathology-form.component.html',
    providers: [SubjectPathologyService, PathologyModelService, PathologyService, ReferenceService]
})
@ModesAware
export class SubjectPathologyFormComponent implements OnChanges {

    subjectPathology = new SubjectPathology();
    @Input() preclinicalSubject: PreclinicalSubject;
    @Input() mode:Mode = new Mode();
    @Input() canModify: Boolean = false;
    @Input('toggleForm') toggleForm: boolean;
    @Input() subjectpathoSelected: SubjectPathology;
    @Output() onCreated = new EventEmitter();
    @Output() onCancel = new EventEmitter();
    @Input() createSPMode: boolean;
    newSubjectPathologyForm: FormGroup;
    pathologies: Pathology[] = [];
    models: PathologyModel[] = [];
    modelsDisplay: PathologyModel[] = [];
    locations: Reference[] = [];
    isDateValid: boolean = true;
    selectedStartDate: string = null;
    selectedEndDate: string = null;
    private addIconPath: string = ImagesUrlUtil.ADD_ICON_PATH;

    constructor(
        private subjectPathologyService: SubjectPathologyService,
        private modelService: PathologyModelService,
        private pathologyService: PathologyService,
        private referenceService: ReferenceService,
        private keycloakService: KeycloakService,
        private fb: FormBuilder,
        private route: ActivatedRoute,
        private router: Router,
        private location: Location) { }



    loadPathologies() {
        this.pathologyService.getPathologies().then(pathologies => this.pathologies = pathologies);
    }

    loadModels() {
        this.modelService.getPathologyModels().then(models => this.models = models);
    }

    loadReferences() {
        this.referenceService.getReferencesByCategoryAndType(PreclinicalUtils.PRECLINICAL_CAT_ANATOMY, PreclinicalUtils.PRECLINICAL_ANATOMY_LOCATION).then(locations => this.locations = locations);
    }
 
    ngOnInit(): void {
        this.loadPathologies();
        this.loadModels();
        this.loadReferences();
        this.subjectPathology = new SubjectPathology();
        if (this.subjectpathoSelected) {
            this.subjectPathology = this.subjectpathoSelected;
        }
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.canModify = true;
        }
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
        this.router.navigate(['/preclinical/pathology'], { queryParams: { mode: "create" } });
    }
    goToAddPathologyModel(){
        this.router.navigate(['/preclinical/pathologies/model'], { queryParams: { mode: "create" } });
    }
    goToAddLocation(){
        this.router.navigate(['/preclinical/reference'], { queryParams: { mode: "create", category:"anatomy", reftype:"location" } });
    }

    ngOnChanges() {
       /* if (!this.mode.isViewMode()) {
            this.loadPathologies();
            this.loadModels();
            this.loadReferences();
        }*/
        if (this.subjectpathoSelected) {
            this.loadSubjectPathologyAttributesForSelect(this.subjectpathoSelected);
        }
        if (this.toggleForm) {
            this.buildForm();
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
        
        
        this.getDateToDatePicker(this.subjectPathology);
        if(this.subjectPathology && this.subjectPathology.pathology) this.loadModelsDisplay();
    }


    buildForm(): void {
        this.newSubjectPathologyForm = this.fb.group({
            'id': [this.subjectPathology.id],
            'pathology': [this.subjectPathology.pathology, Validators.required],
            'pathologyModel': [this.subjectPathology.pathologyModel, Validators.required],
            'location': [this.subjectPathology.location, Validators.required],
            'startDate': [this.subjectPathology.startDate],
            'endDate': [this.subjectPathology.endDate]
        });

        this.newSubjectPathologyForm.valueChanges
            .subscribe(data => this.onValueChanged(data));
        this.onValueChanged();
    }

    onValueChanged(data?: any) {
        if (!this.newSubjectPathologyForm) { return; }
        const form = this.newSubjectPathologyForm;
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
        'pathology': '',
        'pathologyModel': '',
        'location': ''
    };

    addSubjectPathology() {
        if (!this.subjectPathology) {
            return;
        }
        this.setDateFromDatePicker();
        if (this.mode.isCreateMode()) {
            if (this.preclinicalSubject.pathologies === undefined) {
                this.preclinicalSubject.pathologies = [];
            }
            this.preclinicalSubject.pathologies.push(this.subjectPathology);
            if (this.onCreated.observers.length > 0) {
                this.onCreated.emit(this.subjectPathology);
            }
        } else {
            this.subjectPathologyService.create(this.preclinicalSubject, this.subjectPathology)
                .subscribe(subjectPathology => {
                    if (this.onCreated.observers.length > 0) {
                        this.onCreated.emit(subjectPathology);
                    }
                });
        }
        this.toggleFormSPAndReset(true);
    }

    updateSubjectPathology(): void {
        this.setDateFromDatePicker();
        this.subjectPathologyService.update(this.preclinicalSubject, this.subjectPathology)
            .subscribe(subjectPathology => {

            });
        this.toggleFormSPAndReset(false);
    }


    loadModelsDisplay(){
        if (this.subjectPathology && this.subjectPathology.pathology) {
            this.modelsDisplay = [];
            for (let model of this.models) {
                if (this.subjectPathology.pathology.id == model.pathology.id) {
                    this.modelsDisplay.push(model);
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


    private myDatePickerOptions: IMyOptions = {
        dateFormat: 'yyyy-mm-dd',
        height: '20px',
        width: '160px'
    };

    onDateChanged(event: IMyDateModel, date: string) {
        if (event.formatted !== '') {
            if (date == 'start') {
                this.selectedStartDate = event.formatted;
            } else if (date == 'end') {
                this.selectedEndDate = event.formatted;
            }
        }
    }

    onInputFieldChanged(event: IMyInputFieldChanged, date: string) {
        if (event.value !== '') {
            if (!event.valid) {
                this.isDateValid = false;
            } else {
                this.isDateValid = true;
            }
        } else {
            this.isDateValid = true;
            if (date == 'start') {
                this.selectedStartDate = null;
            } else if (date == 'end') {
                this.selectedEndDate = null;
            }
        }
    }

    setDateFromDatePicker(): void {
        if (this.selectedStartDate && !isNaN(new Date(this.selectedStartDate).getTime())) {
                this.subjectPathology.startDate = new Date(this.selectedStartDate);
        } else {
            this.subjectPathology.startDate = null;
        }
        if (this.selectedEndDate && !isNaN(new Date(this.selectedEndDate).getTime())) {
                this.subjectPathology.endDate = new Date(this.selectedEndDate);
        } else {
            this.subjectPathology.endDate = null;
        }
    }

    getDateToDatePicker(subjectPathology: SubjectPathology): void {
        if (subjectPathology && subjectPathology.startDate && !isNaN(new Date(subjectPathology.startDate).getTime())) {
            let date: string = new Date(subjectPathology.startDate).toISOString().split('T')[0];
            this.selectedStartDate = date;
        }
        if (subjectPathology && subjectPathology.endDate && !isNaN(new Date(subjectPathology.endDate).getTime())) {
            let date: string = new Date(subjectPathology.endDate).toISOString().split('T')[0];
            this.selectedEndDate = date;
        }
    }

}