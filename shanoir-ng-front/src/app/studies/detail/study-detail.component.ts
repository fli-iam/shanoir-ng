import { Component, OnInit, Input } from '@angular/core';
import { Location } from '@angular/common';
import { ActivatedRoute, Router, Params } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';
import { IMyDate, IMyDateModel, IMyInputFieldChanged, IMyOptions } from 'mydatepicker';

import { Enum } from "../../shared/utils/enum";
import { KeycloakService } from "../../shared/keycloak/keycloak.service";
import { Study } from '../shared/study.model';
import { StudyService } from '../shared/study.service';
import { StudyStatus } from "../shared/study-status.enum";

@Component({
    selector: 'studyDetail',
    templateUrl: 'study-detail.component.html',
    styleUrls: ['study-detail.component.css']
})

export class StudyDetailComponent implements OnInit {

    public canModify: Boolean = false;
    private isEndDateValid: boolean = true;
    private isNameUnique: Boolean = true;
    private isStartDateValid: boolean = true;
    public mode: "view" | "edit" | "create";
    private selectedStartDateNormal: IMyDate;
    private selectedEndDateNormal: IMyDate;
    private study: Study = new Study();
    public studyDetailForm: FormGroup;
    private studyId: number;
    private studyStatusEnumValue: string;
    private studyStatuses: Enum[] = [];

    formErrors = {
        'name': '',
        'studyStatus': ''
    };

    private myDatePickerOptions: IMyOptions = {
        dateFormat: 'dd/mm/yyyy',
        height: '20px',
        width: '160px'
    };

    constructor(private route: ActivatedRoute, private router: Router,
        private studyService: StudyService, private fb: FormBuilder,
        private location: Location, private keycloakService: KeycloakService) {

    }

    ngOnInit(): void {
        this.getEnum();
        this.getStudy();
        this.buildForm();
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.canModify = true;
        }
    }

    back(): void {
        this.location.back();
    }

    buildForm(): void {
        this.studyDetailForm = this.fb.group({
            'name': [this.study.name, [Validators.required, Validators.minLength(2), Validators.maxLength(200)]],
            'startDate': [this.study.startDate],
            'endDate': [this.study.endDate],
            'studyStatus': [this.study.studyStatus],
            'withExamination': [this.study.withExamination],
            'clinical': [this.study.clinical, [Validators.required]],
            'visibleByDefault': [this.study.visibleByDefault],
            'downloadableByDefault': [this.study.downloadableByDefault],
            'monoCenter': [this.study.monoCenter, [Validators.required]],
            'subjectNames': [this.study.subjectNames]
        });
        this.studyDetailForm.valueChanges
            .subscribe(data => this.onValueChanged(data));
        this.onValueChanged();
    }

    create(): void {
        this.study = this.studyDetailForm.value;
        this.studyService.create(this.study)
            .subscribe((study) => {
                this.back();
            }, (err: String) => {
                if (err.indexOf("name should be unique") != -1) {
                    this.isNameUnique = false;
                }
            });
    }

    edit(): void {
        this.router.navigate(['/studyDetail'], { queryParams: { id: this.studyId, mode: "edit" } });
    }

    getDateToDatePicker(study: Study): void {
        if (study) {
            if (study.startDate && !isNaN(new Date(study.startDate).getTime())) {
                let startDate: Date = new Date(study.startDate);
                this.selectedStartDateNormal = {
                    year: startDate.getFullYear(), month: startDate.getMonth() + 1,
                    day: startDate.getDate()
                };;
            }
            if (study.endDate && !isNaN(new Date(study.endDate).getTime())) {
                let endDate: Date = new Date(study.endDate);
                this.selectedEndDateNormal = {
                    year: endDate.getFullYear(), month: endDate.getMonth() + 1,
                    day: endDate.getDate()
                };;
            }
        }
    }

    getEnum(): void {
        var types = Object.keys(StudyStatus);
        for (var i = 0; i < types.length; i = i + 2) {
            var newEnum: Enum = new Enum();
            newEnum.key = types[i];
            newEnum.value = StudyStatus[types[i]];
            this.studyStatuses.push(newEnum);
        }
    }

    getStudy(): void {
        this.route.queryParams
            .switchMap((queryParams: Params) => {
                let studyId = queryParams['id'];
                let mode = queryParams['mode'];
                if (mode) {
                    this.mode = mode;
                }
                if (studyId) {
                    // view or edit mode
                    this.studyId = studyId;
                    return this.studyService.getStudy(studyId);
                } else {
                    // create mode
                    return Observable.of<Study>();
                }
            })
            .subscribe((study: Study) => {
                this.study = study;
                this.getDateToDatePicker(this.study);
                this.studyStatusEnumValue = StudyStatus[this.study.studyStatus];
            });
    }

    onEndDateChanged(event: IMyDateModel) {
        if (event.formatted !== '') {
            this.selectedEndDateNormal = event.date;
        }
    }

    onEndDateFieldChanged(event: IMyInputFieldChanged) {
        if (event.value !== '') {
            if (!event.valid) {
                this.isEndDateValid = false;
            } else {
                this.isEndDateValid = true;
            }
        } else {
            this.isEndDateValid = true;
            setTimeout(() => this.selectedEndDateNormal = null);
        }
    }

    onStartDateChanged(event: IMyDateModel) {
        if (event.formatted !== '') {
            this.selectedStartDateNormal = event.date;
        }
    }

    onStartDateFieldChanged(event: IMyInputFieldChanged) {
        if (event.value !== '') {
            if (!event.valid) {
                this.isStartDateValid = false;
            } else {
                this.isStartDateValid = true;
            }
        } else {
            this.isStartDateValid = true;
            setTimeout(() => this.selectedStartDateNormal = null);
        }
    }

    onValueChanged(data?: any) {
        if (!this.studyDetailForm) { return; }
        const form = this.studyDetailForm;
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

    update(): void {
        this.study = this.studyDetailForm.value;
        this.studyService.update(this.studyId, this.study)
            .subscribe((study) => {
                this.back();
            }, (err: String) => {
                if (err.indexOf("name should be unique") != -1) {
                    this.isNameUnique = false;
                }
            });
    }

}