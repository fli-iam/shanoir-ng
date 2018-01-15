import { Component, OnInit, Input } from '@angular/core';
import { Location } from '@angular/common';
import { ActivatedRoute, Router, Params } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';
import { IMyDate, IMyDateModel, IMyInputFieldChanged, IMyOptions } from 'mydatepicker';

import { Center } from '../../centers/shared/center.model';
import { CenterService } from '../../centers/shared/center.service';
import { Enum } from "../../shared/utils/enum";
import { KeycloakService } from "../../shared/keycloak/keycloak.service";
import { ImagesUrlUtil } from '../../shared/utils/images-url.util';
import { Study } from '../shared/study.model';
import { StudyCenter } from '../shared/study-center.model';
import { StudyService } from '../shared/study.service';
import { StudyStatus } from "../shared/study-status.enum";
import { Timepoint } from '../shared/timepoint.model';

@Component({
    selector: 'study-detail',
    templateUrl: 'study.component.html',
    styleUrls: ['study.component.css']
})

export class StudyComponent implements OnInit {

    private addIconPath: string = ImagesUrlUtil.ADD_ICON_PATH;
    private addDisabledIconPath: string = ImagesUrlUtil.ADD_DISABLED_ICON_PATH;
    public canModify: Boolean = false;
    private centers: Center[];
    private deleteIconPath: string = ImagesUrlUtil.DELETE_ICON_PATH;
    private editIconPath: string = ImagesUrlUtil.EDIT_ICON_PATH;
    private isEndDateValid: boolean = true;
    private isNameUnique: Boolean = true;
    private isStartDateValid: boolean = true;
    private loaderImagePath: string = ImagesUrlUtil.LOADER_IMAGE_PATH;
    private loading: boolean = true;
    public mode: "view" | "edit" | "create";
    private selectedCenter: Center;
    private selectedEndDateNormal: IMyDate;
    private selectedStartDateNormal: IMyDate;
    public study: Study = new Study();
    public studyForm: FormGroup;
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
        private centerService: CenterService, private studyService: StudyService, private fb: FormBuilder,
        private location: Location, private keycloakService: KeycloakService) {

    }

    ngOnInit(): void {
        this.getEnum();
        this.getCenters();
        this.getStudy();
        this.buildForm();
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.canModify = true;
        }
    }

    addCenterToStudy(): void {
        let studyCenter: StudyCenter = new StudyCenter();
        studyCenter.center = this.selectedCenter;
        this.study.studyCenterList.push(studyCenter);
    }

    back(): void {
        this.location.back();
    }

    buildForm(): void {
        this.studyForm = this.fb.group({
            'name': [this.study.name, [Validators.required, Validators.minLength(2), Validators.maxLength(200)]],
            'startDate': [this.study.startDate],
            'endDate': [this.study.endDate],
            'studyStatus': [this.study.studyStatus, [Validators.required]],
            'withExamination': [this.study.withExamination],
            'clinical': [this.study.clinical, [Validators.required]],
            'visibleByDefault': [this.study.visibleByDefault],
            'downloadableByDefault': [this.study.downloadableByDefault],
            'monoCenter': [this.study.monoCenter, [Validators.required]],
            'studyCenterList': [this.study.studyCenterList],
            'nbSujects': [this.study.nbSujects]
        });
        this.studyForm.valueChanges
            .subscribe(data => this.onValueChanged(data));
        this.onValueChanged();
    }

    create(): void {
        this.submit();
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
        this.router.navigate(['/study'], { queryParams: { id: this.studyId, mode: "edit" } });
    }

    editTimepoint(timepoint: Timepoint): void {
        // TODO
    }

    enableAddIcon(): boolean {
        if ((this.study.monoCenter && !this.studyCenterListEmpty())
            || (this.selectedCenter && this.isCenterAlreadyLinked(this.selectedCenter.id))) {
            return false;
        }
        return true;
    }

    getCenters(): void {
        this.centerService
            .getCentersNames()
            .then(centers => {
                this.centers = centers;
                if (centers) {
                    this.selectedCenter = centers[0];
                }
            })
            .catch((error) => {
                // TODO: display error
                console.log("error getting center list!");
            });
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
                    this.loading = true;
                    return this.studyService.getStudy(studyId, false);
                } else {
                    // create mode
                    this.initializeStudyData();
                    return Observable.of<Study>();
                }
            })
            .subscribe((study: Study) => {
                this.study = study;
                this.getDateToDatePicker(this.study);
                this.studyStatusEnumValue = StudyStatus[this.study.studyStatus];
                if (this.mode == 'view') {
                    this.getStudyWithData(this.study.id);
                }
            });
    }

    getStudyWithData(studyId: number): void {
        this.studyService.getStudy(studyId, true)
            .then((study: Study) => {
                this.study = study;
                this.getDateToDatePicker(this.study);
                this.studyStatusEnumValue = StudyStatus[this.study.studyStatus];
                this.loading = false;
            })
            .catch((error) => {
                // TODO: display error
                console.log("error getting study with data!");
            });
    }

    initializeStudyData(): void {
        this.study.clinical = false;
        this.study.monoCenter = true;
        this.study.studyCenterList = [];
        this.study.timepoints = [];
        this.study.withExamination = true;
    }

    isCenterAlreadyLinked(centerId: number): boolean {
        for (let studyCenter of this.study.studyCenterList) {
            if (centerId == studyCenter.center.id) {
                return true;
            }
        }
        return false;
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
            setTimeout(():void => this.selectedEndDateNormal = null);
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
            setTimeout(():void => this.selectedStartDateNormal = null);
        }
    }

    onValueChanged(data?: any) {
        if (!this.studyForm) { return; }
        const form = this.studyForm;
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

    removeCenterFromStudy(studyCenterId: number): void {
        this.study.studyCenterList = this.study.studyCenterList.filter(item => item.id !== studyCenterId);
    }

    removeTimepoint(timepoint: Timepoint): void {
        const index: number = this.study.timepoints.indexOf(timepoint);
        if (index !== -1) {
            this.study.timepoints.splice(index, 1);
        }
    }

    setDateFromDatePicker(): void {
        if (this.selectedStartDateNormal) {
            this.study.startDate = new Date(this.selectedStartDateNormal.year, this.selectedStartDateNormal.month - 1,
                this.selectedStartDateNormal.day);
        } else {
            this.study.startDate = null;
        }
        if (this.selectedEndDateNormal) {
            this.study.endDate = new Date(this.selectedEndDateNormal.year, this.selectedEndDateNormal.month - 1,
                this.selectedEndDateNormal.day);
        } else {
            this.study.endDate = null;
        }
    }

    studyCenterListEmpty(): boolean {
        if (this.study.studyCenterList && this.study.studyCenterList.length > 0) {
            return false;
        }
        return true;
    }

    submit(): void {
        let studyCenterListBackup: StudyCenter[] = this.study.studyCenterList;
        this.study = this.studyForm.value;
        this.study.studyCenterList = studyCenterListBackup;
        this.setDateFromDatePicker();
    }

    update(): void {
        this.submit();
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