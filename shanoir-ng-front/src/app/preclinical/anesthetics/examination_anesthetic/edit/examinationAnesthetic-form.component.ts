import { Component, OnInit, Input, Output, EventEmitter, OnChanges, ChangeDetectorRef } from '@angular/core';
import { FormGroup, FormBuilder, Validators, FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute, Params } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { Location } from '@angular/common';
import { IMyOptions, IMyDateModel, IMyInputFieldChanged } from 'mydatepicker';

import { ExaminationAnesthetic }    from '../shared/examinationAnesthetic.model';
import { ExaminationAnestheticService } from '../shared/examinationAnesthetic.service';
import { Reference }   from '../../../reference/shared/reference.model';
import { ReferenceService } from '../../../reference/shared/reference.service';
import { Anesthetic }   from '../../anesthetic/shared/anesthetic.model';
import { AnestheticService } from '../../anesthetic/shared/anesthetic.service';

import * as PreclinicalUtils from '../../../utils/preclinical.utils';
import { Enum } from "../../../../shared/utils/enum";
import { InjectionType } from "../../../shared/enum/injectionType";
import { InjectionInterval } from "../../../shared/enum/injectionInterval";
import { InjectionSite } from "../../../shared/enum/injectionSite";
import { EnumUtils } from "../../../shared/enum/enumUtils";
import { Mode } from "../../../shared/mode/mode.model";
import { Modes } from "../../../shared/mode/mode.enum";
import { ModesAware } from "../../../shared/mode/mode.decorator";

import { KeycloakService } from "../../../../shared/keycloak/keycloak.service";
import { ImagesUrlUtil } from '../../../../shared/utils/images-url.util';


@Component({
    selector: 'examination-anesthetic-form',
    templateUrl: 'examinationAnesthetic-form.component.html',
    providers: [ExaminationAnestheticService, ReferenceService, AnestheticService]
})
@ModesAware
export class ExaminationAnestheticFormComponent implements OnInit {

    @Input() examAnesthetic: ExaminationAnesthetic = new ExaminationAnesthetic();
    @Input() examination_id: number;
    @Output() closing = new EventEmitter();
    @Output() examAnestheticChange = new EventEmitter();
    @Input() newExamAnestheticForm: FormGroup;
    @Input() isStandalone: boolean = false;
    @Input() mode: Mode = new Mode();
    @Input() examAnestheticId: number;
    @Input() canModify: Boolean = false;
    anesthetics: Anesthetic[] = [];
    sites: Enum[] = [];
    intervals: Enum[] = [];
    injtypes: Enum[] = [];
    units: Reference[] = [];
    references: Reference[] = [];
    isDateValid: boolean = true;
    selectedStartDate: string = null;
    selectedEndDate: string = null;
    private addIconPath: string = ImagesUrlUtil.ADD_ICON_PATH;

    constructor(
        private examAnestheticService: ExaminationAnestheticService,
        private referenceService: ReferenceService,
        private anestheticService: AnestheticService,
        private keycloakService: KeycloakService,
        private enumUtils: EnumUtils,
        private fb: FormBuilder,
        private route: ActivatedRoute,
        private router: Router,
        private location: Location) {

    }

    loadData() {
        this.loadReferences();
    }

    loadReferences() {
        this.referenceService.getReferencesByCategoryAndType(PreclinicalUtils.PRECLINICAL_CAT_UNIT, PreclinicalUtils.PRECLINICAL_UNIT_VOLUME).then(units => {
            this.units = units;

            this.loadAnesthetics();
        });

    }

    loadAnesthetics() {
        this.anestheticService.getAnesthetics().then(anesthetics => {
            this.anesthetics = anesthetics;
            this.references = this.units;
            if(!this.mode.isCreateMode()) this.getExaminationAnesthetic();            
        });
    }

    getExaminationAnesthetic(): void {
        if (this.examination_id) {
            this.examAnestheticService.getExaminationAnesthetics(this.examination_id)
                .then(examAnesthetics => {
                    if (examAnesthetics && examAnesthetics.length > 0) {
                        //Should be only one
                        let examAnesthetic: ExaminationAnesthetic = examAnesthetics[0];
                                                
                        this.examAnesthetic = examAnesthetic;
                        this.examAnesthetic.dose_unit = this.getReferenceById(examAnesthetic.dose_unit);
                        this.examAnesthetic.anesthetic = this.getAnestheticById(examAnesthetic.anesthetic);
                        this.getDateToDatePicker(this.examAnesthetic);                    
                    }
                });
        } else {
            this.mode.createMode();
        }
    }

    ngOnInit(): void {
        this.getEnums();
        this.loadData();
        if (this.isStandalone) this.buildForm();
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.canModify = true;
        }
    }

    ngOnChange(): void {
        if (this.examination_id && !this.mode.isCreateMode()) this.getExaminationAnesthetic();
    }
    
    eaChange(){
        if(!this.isStandalone) {
            this.setDateFromDatePicker();
            this.examAnestheticChange.emit(this.examAnesthetic);
        }        
    }

    getEnums(): void {
        this.intervals = this.enumUtils.getEnumArrayFor('InjectionInterval');
        this.sites = this.enumUtils.getEnumArrayFor('InjectionSite');
        this.injtypes = this.enumUtils.getEnumArrayFor('InjectionType');
    }
    

    buildForm(): void {
        this.newExamAnestheticForm = this.fb.group({
            'anesthetic': [this.examAnesthetic.anesthetic, Validators.required],
            'injectionInterval': [this.examAnesthetic.injection_interval],
            'injectionSite': [this.examAnesthetic.injection_site],
            'injectionType': [this.examAnesthetic.injection_type],
            'dose': [this.examAnesthetic.dose],
            'dose_unit': [this.examAnesthetic.dose_unit],
            'startDate': [this.examAnesthetic.startDate],
            'endDate': [this.examAnesthetic.endDate]
        });

        this.newExamAnestheticForm.valueChanges
            .subscribe(data => this.onValueChanged(data));
        this.onValueChanged();
    }

    onValueChanged(data?: any) {
        if (!this.newExamAnestheticForm) { return; }
        const form = this.newExamAnestheticForm;
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
        'anesthetic': ''/*,
        'injectionInterval': '',
        'injectionSite': '',
        'injectionType': '',
        'dose': '',
        'dose_unit': '',
        'startDate': '',
        'endDate': '',*/
    };

    getOut(examAnesthetic: ExaminationAnesthetic = null): void {
        if (this.closing.observers.length > 0) {
            this.setDateFromDatePicker();
            this.closing.emit(examAnesthetic);
            this.location.back();
        } else {
            this.location.back();
        }
    }

    goToEditPage(): void {
        this.router.navigate(['/preclinical/examination/anesthetic'], { queryParams: { id: this.examAnestheticId, mode: "edit" } });
    }
    
    goToAddAnesthetic(){
        this.router.navigate(['/preclinical/anesthetic'], { queryParams: { mode: "create"} });
    }


    addExaminationAnesthetic() {
        if (!this.examAnesthetic) { return; }
        this.setDateFromDatePicker();
        this.examAnestheticService.create(this.examAnesthetic.examination_id, this.examAnesthetic)
            .subscribe(subject => {
                this.getOut(subject);
            });
    }

    updateExaminationAnesthetic(): void {
        this.setDateFromDatePicker();
        this.examAnestheticService.update(this.examAnesthetic.examination_id, this.examAnesthetic)
            .subscribe(subject => {
                this.getOut(subject);
            });
    }

    getReferenceById(reference: any): Reference {
        if (reference) {
            for (let ref of this.references) {
                if (reference.id == ref.id) {
                    return ref;
                }
            }
        }
        return null;
    }

    getAnestheticById(anesthetic: any): Anesthetic {
        if (anesthetic) {
            for (let anest of this.anesthetics) {
                if (anesthetic.id == anest.id) {
                    return anest;
                }
            }
        }
        return null;
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
                this.examAnesthetic.startDate = new Date(this.selectedStartDate);
        } else {
            this.examAnesthetic.startDate = null;
        }
        if (this.selectedEndDate && !isNaN(new Date(this.selectedEndDate).getTime())) {
                this.examAnesthetic.endDate = new Date(this.selectedEndDate);
        } else {
            this.examAnesthetic.endDate = null;
        }
    }

    getDateToDatePicker(examAnesthetic: ExaminationAnesthetic): void {
        if (examAnesthetic && examAnesthetic.startDate && !isNaN(new Date(examAnesthetic.startDate).getTime())) {
            let date: string = new Date(examAnesthetic.startDate).toISOString().split('T')[0];
            this.selectedStartDate = date;
        }
        if (examAnesthetic && examAnesthetic.endDate && !isNaN(new Date(examAnesthetic.endDate).getTime())) {
            let date: string = new Date(examAnesthetic.endDate).toISOString().split('T')[0];
            this.selectedEndDate = date;
        }
    }

}