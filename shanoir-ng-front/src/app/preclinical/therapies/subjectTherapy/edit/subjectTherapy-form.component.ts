import { Component, Input, Output, EventEmitter} from '@angular/core';
import { FormGroup,  Validators } from '@angular/forms';
import {  ActivatedRoute } from '@angular/router';
import { IMyOptions, IMyDateModel, IMyInputFieldChanged } from 'mydatepicker';

import { SubjectTherapy }    from '../shared/subjectTherapy.model';
import { SubjectTherapyService } from '../shared/subjectTherapy.service';

import { TherapyService } from '../../therapy/shared/therapy.service';
import { Therapy }   from '../../therapy/shared/therapy.model';

import { ReferenceService } from '../../../reference/shared/reference.service';
import { Reference }    from '../../../reference/shared/reference.model';

import { PreclinicalSubject } from '../../../animalSubject/shared/preclinicalSubject.model';

import * as PreclinicalUtils from '../../../utils/preclinical.utils';

import { Enum } from "../../../../shared/utils/enum";
import { TherapyType } from "../../../shared/enum/therapyType";
import { EnumUtils } from "../../../shared/enum/enumUtils";
import { ModesAware } from "../../../shared/mode/mode.decorator";
import { EntityComponent } from '../../../../shared/components/entity/entity.component.abstract';
import { slideDown } from '../../../../shared/animations/animations';

@Component({
    selector: 'subject-therapy-form',
    templateUrl: 'subjectTherapy-form.component.html',
    providers: [SubjectTherapyService, TherapyService, ReferenceService],
    animations: [slideDown]
})
@ModesAware
export class SubjectTherapyFormComponent extends EntityComponent<SubjectTherapy>{

    @Input() preclinicalSubject: PreclinicalSubject;
    @Input() canModify: Boolean = false;
    @Input('toggleForm') toggleForm: boolean;
    @Input() subTherapySelected: SubjectTherapy;
    @Output() onCreated = new EventEmitter();
    @Output() onCancel = new EventEmitter();
    @Input() createSTMode: boolean;
    therapies: Therapy[];
    units: Reference[];
    frequencies: Enum[] = [];
    isDateValid: boolean = true;
    selectedStartDate: string = null;
    selectedEndDate: string = null;

    constructor(
        private route: ActivatedRoute,
        private  subjectTherapyService: SubjectTherapyService, 
        private therapyService: TherapyService,
        private referenceService: ReferenceService, 
        private enumUtils: EnumUtils
        ) {

            super(route, 'preclinical-subject-therapy');
        }

    get subjectTherapy(): SubjectTherapy { return this.entity; }
    set subjectTherapy(subjectTherapy: SubjectTherapy) { this.entity = subjectTherapy; }

    initView(): Promise<void> {
        this.getEnums();
        this.loadTherapies();
        this.loadUnits();
        if (this.subTherapySelected) {
            this.subjectTherapy = this.subTherapySelected;
        }
        this.subjectTherapyService.get(this.id).then(subjectTherapy => {
            this.subjectTherapy = subjectTherapy;
        });
        return Promise.resolve();
    }

    initEdit(): Promise<void> {
        this.getEnums();
        this.loadTherapies();
        this.loadUnits();
        if (this.subTherapySelected) {
            this.subjectTherapy = this.subTherapySelected;
        }
        this.subjectTherapyService.get(this.id).then(subjectTherapy => {
            this.subjectTherapy = subjectTherapy;
        });
        return Promise.resolve();
    }

    initCreate(): Promise<void> {
        this.entity = new SubjectTherapy();
        this.getEnums();
        this.loadTherapies();
        this.loadUnits();
        return Promise.resolve();
    }
    
    buildForm(): FormGroup {
        return this.formBuilder.group({
            'id': [this.subjectTherapy.id],
            'therapy': [this.subjectTherapy.therapy, Validators.required],
            'dose': [this.subjectTherapy.dose],
            'dose_unit': [this.subjectTherapy.dose_unit],
            'frequency': [this.subjectTherapy.frequency],
            'startDate': [this.subjectTherapy.startDate],
            'endDate': [this.subjectTherapy.endDate]
        });
    }
    
    
    
    loadTherapies() {
        this.therapyService.getTherapies().then(therapies => this.therapies = therapies);
    }

    loadUnits() {
        this.referenceService.getReferencesByCategoryAndType(PreclinicalUtils.PRECLINICAL_CAT_UNIT, PreclinicalUtils.PRECLINICAL_UNIT_VOLUME).then(units => this.units = units);
    }

   

    getEnums(): void {
        this.frequencies = this.enumUtils.getEnumArrayFor('Frequency');
    }

    toggleFormST(creation: boolean): void {
        if (this.toggleForm == false) {
            this.toggleForm = true;
        } else if (this.toggleForm == true) {
            this.toggleForm = false;
            this.onCancel.emit(false);
        } else {
            this.toggleForm = false;
            this.onCancel.emit(false);
        }
        this.createSTMode = creation;
    }
    
    toggleFormSTAndReset(creation: boolean): void {
        this.toggleFormST(creation);
        this.subjectTherapy = new SubjectTherapy();
    }

    displayDoseFrequency(): boolean {
        if (this.subjectTherapy) {
            if (this.subjectTherapy.therapy && this.subjectTherapy.therapy.therapyType
                && TherapyType[this.subjectTherapy.therapy.therapyType] != 'SURGERY') {
                return true;
            }
        } else {
            return false;
        }
    }

    ngOnChanges() {
        if (this.subTherapySelected) {
            this.loadSubjectTherapyAttributesForSelect(this.subTherapySelected);
        }
        if (this.toggleForm) {
            this.buildForm();
        }
    }

    loadSubjectTherapyAttributesForSelect(selectedTherapy: SubjectTherapy) {
        this.subjectTherapy = selectedTherapy;
        if (this.therapies) {
            for (let therapy of this.therapies) {
                if (selectedTherapy.therapy && selectedTherapy.therapy.id == therapy.id) {
                    this.subjectTherapy.therapy = therapy;
                }
            }
        }
        if (this.units) {
            for (let unit of this.units) {
                if (selectedTherapy.dose_unit && selectedTherapy.dose_unit.id == unit.id) {
                    this.subjectTherapy.dose_unit = unit;
                }
            }
        }
        this.getDateToDatePicker(this.subjectTherapy);
    }
    
    goToAddTherapy(){
        this.router.navigate(['/preclinical-therapy'], { queryParams: { mode: "create" } });
    }



    addSubjectTherapy() {
        if (!this.subjectTherapy) {
            console.log('nothing to create');
            return;
        }
        this.setDateFromDatePicker();
        if (this.mode == 'create' ) {
            if (this.preclinicalSubject.therapies === undefined) {
                this.preclinicalSubject.therapies = [];
            }
            this.preclinicalSubject.therapies.push(this.subjectTherapy);
            if (this.onCreated.observers.length > 0) {
                this.onCreated.emit(this.subjectTherapy);
            }
        } else {
            this.subjectTherapyService.createSubjectTherapy(this.preclinicalSubject, this.subjectTherapy)
                .subscribe(subjectTherapy => {
                    if (this.onCreated.observers.length > 0) {
                        this.onCreated.emit(subjectTherapy);
                    }
                });
        }
        this.toggleFormSTAndReset(true);
    }

    updateSubjectTherapy(): void {
        this.setDateFromDatePicker();
        this.subjectTherapyService.updateSubjectTherapy(this.preclinicalSubject, this.subjectTherapy)
            .subscribe();
        this.toggleFormSTAndReset(false);
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
                this.subjectTherapy.startDate = new Date(this.selectedStartDate);
        } else {
            this.subjectTherapy.startDate = null;
        }
        if (this.selectedEndDate && !isNaN(new Date(this.selectedEndDate).getTime())) {
                this.subjectTherapy.endDate = new Date(this.selectedEndDate);
        } else {
            this.subjectTherapy.endDate = null;
        }
    }

    getDateToDatePicker(subjectTherapy: SubjectTherapy): void {
        if (subjectTherapy && subjectTherapy.startDate && !isNaN(new Date(subjectTherapy.startDate).getTime())) {
            let date: string = new Date(subjectTherapy.startDate).toISOString().split('T')[0];
            this.selectedStartDate = date;
        }
        if (subjectTherapy && subjectTherapy.endDate && !isNaN(new Date(subjectTherapy.endDate).getTime())) {
            let date: string = new Date(subjectTherapy.endDate).toISOString().split('T')[0];
            this.selectedEndDate = date;
        }
    }

}