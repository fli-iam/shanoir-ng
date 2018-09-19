import { Location } from '@angular/common';
import { Component, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { IMyDate, IMyDateModel, IMyInputFieldChanged, IMyOptions } from 'mydatepicker';

import { CenterService } from '../../centers/shared/center.service';
import { FooterState } from '../../shared/components/form-footer/footer-state.model';
import { ModalComponent } from '../../shared/components/modal/modal.component';
import { KeycloakService } from '../../shared/keycloak/keycloak.service';
import { IdNameObject } from '../../shared/models/id-name-object.model';
import { ImagesUrlUtil } from '../../shared/utils/images-url.util';
import { StudyService } from '../../studies/shared/study.service';
import { Examination } from '../shared/examination.model';
import { ExaminationService } from '../shared/examination.service';


@Component({
    selector: 'examination',
    templateUrl: 'examination.component.html',
    styleUrls: ['examination.component.css'],
})

export class ExaminationComponent implements OnInit {

    @ViewChild('instAssessmentModal') instAssessmentModal: ModalComponent;
    @ViewChild('attachNewFilesModal') attachNewFilesModal: ModalComponent;
    public examinationForm: FormGroup
    private _examination: Examination;
    private id: number;
    public mode: "view" | "edit" | "create";
    private isNameUnique: Boolean = true;
    private centers: IdNameObject[];
    public studies: IdNameObject[];
    private subjects: IdNameObject[];
    private examinationExecutives: Object[];
    private addIconPath: string = ImagesUrlUtil.ADD_ICON_PATH;
    isDateValid: boolean = true;
    selectedDateNormal: IMyDate;
    private footerState: FooterState;

    constructor(private route: ActivatedRoute, private router: Router,
            private examinationService: ExaminationService, private fb: FormBuilder,
            private centerService: CenterService,
            private studyService: StudyService,
            private location: Location, private keycloakService: KeycloakService) {

        this.mode = this.route.snapshot.data['mode'];
        this.id = +this.route.snapshot.params['id'];
    }

    ngOnInit(): void {
        this.getCenters();
        this.getStudies();
        this.fetchExamination();
        this.footerState = new FooterState(this.mode, this.keycloakService.isUserAdminOrExpert());
    }

    set examination(examination: Examination) {
        this._examination = examination;
        this.buildForm();
    }

    get examination(): Examination {
        return this._examination;
    }

    fetchExamination(): void {
        if (this.mode == 'create') {
            this.examination = new Examination();
        } else {
            this.examinationService.getExamination(this.id)
            .then((examination: Examination) => {
                this.examination = examination;
                this.getDateToDatePicker(this.examination);
            });
        }
    }

    getCenters(): void {
        this.centerService
            .getCentersNamesForExamination()
            .then(centers => {
                this.centers = centers;
            });
    }

    getStudies(): void {
        this.studyService
            .getStudiesNames()
            .then(studies => {
                this.studies = studies;
            });
    }

    buildForm(): void {
        this.examinationForm = this.fb.group({
            'id': [this.examination.id],
            'studyId': [this.examination.studyId, Validators.required],
            // 'Examination executive': [this.examination.examinationExecutive],
            'centerId': [this.examination.centerId, Validators.required],
            // 'Subject': [this.examination.subject],
            'examinationDate': [this.examination.examinationDate],
            'comment': [this.examination.comment],
            'note': [this.examination.note],
            'subjectWeight': [this.examination.subjectWeight]
        });
        this.examinationForm.valueChanges
            .subscribe(data => this.onValueChanged(data));
        this.onValueChanged(); // (re)set validation messages now
        this.examinationForm.statusChanges.subscribe(status => this.footerState.valid = status == 'VALID');
    }

    onValueChanged(data?: any) {
        if (!this.examinationForm) { return; }
        const form = this.examinationForm;
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

    onInputFieldChanged(event: IMyInputFieldChanged) {
        if (event.value !== '') {
            if (!event.valid) {
                this.isDateValid = false;
            } else {
                this.isDateValid = true;
            }
        } else {
            this.isDateValid = true;
            setTimeout(():void => this.selectedDateNormal = null);
        }
    }

    private myDatePickerOptions: IMyOptions = {
        dateFormat: 'dd/mm/yyyy',
        height: '20px',
        width: '160px'
    };

    onDateChanged(event: IMyDateModel) {
        if (event.formatted !== '') {
            this.selectedDateNormal = event.date;
        }
    }

    setDateFromDatePicker(): void {
        if (this.selectedDateNormal) {
            this.examination.examinationDate = new Date(this.selectedDateNormal.year, this.selectedDateNormal.month - 1,
                this.selectedDateNormal.day);
        } else {
            this.examination.examinationDate = null;
        }
    }

    getDateToDatePicker(examination: Examination): void {
        if (examination && examination.examinationDate && !isNaN(new Date(examination.examinationDate).getTime())) {
            let expirationDate: Date = new Date(examination.examinationDate);
            this.selectedDateNormal = {
                year: expirationDate.getFullYear(), month: expirationDate.getMonth() + 1,
                day: expirationDate.getDate()
            };;
        }
    }

    formErrors = {
        'centerId': '',
        'studyId': ''
    };

    back(): void {
        this.location.back();
    }

    edit(): void {
        this.router.navigate(['/examination/edit/'+this.examination.id]);
    }

    submit(): void {
        this.examination = this.examinationForm.value;
        this.setDateFromDatePicker();        
    }

    create(): void {
        this.submit();
        this.examinationService.create(this.examination)
            .subscribe((examination) => {
                this.back();
            }, (err: String) => {

            });
    }

    update(): void {
        this.submit();
        this.examinationService.update(this.id, this.examination)
            .subscribe((examination) => {
                this.back();
            }, (err: String) => {
               /* if (err.indexOf("name should be unique") != -1) {
                    this.isNameUnique = false;
                }*/
            });
    }


    closePopin(instAssessmentId?: number) {
        this.instAssessmentModal.hide();
    }

    closeAttachedFilePopin(id?: number) {
        this.attachNewFilesModal.hide();
    }

}