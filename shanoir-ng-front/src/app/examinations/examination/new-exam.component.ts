import { Component, OnInit, Input, Output, EventEmitter, SimpleChanges } from "@angular/core";
import { Location } from '@angular/common';
import { FormGroup, FormBuilder, Validators } from "@angular/forms";
import { Examination } from "../shared/examination.model";
import { ExaminationService } from "../shared/examination.service";
import { MsgBoxService } from "../../shared/msg-box/msg-box.service";
import { IdNameObject } from "../../shared/models/id-name-object.model";
import { StudyService } from "../../studies/shared/study.service";
import { CenterService } from "../../centers/shared/center.service";

@Component({
    selector: 'new-exam',
    templateUrl: 'new-exam.component.html'
})

export class NewExamComponent implements OnInit {

    @Input() preFillData: Examination;
    @Output() closing: EventEmitter<any> = new EventEmitter();

    private examination: Examination = new Examination();
    private examinationForm: FormGroup;
    private isExaminationDateValid: boolean = true;
    private studies: IdNameObject[];
    private centers: IdNameObject[];

    constructor (private fb: FormBuilder, private examinationService: ExaminationService,
        private location: Location, private msgService: MsgBoxService,
        private studyService: StudyService, private centerService: CenterService) {
    }

    ngOnInit(): void {
        this.examination = new Examination();
        // this.getStudies();
        // this.getCenters();
        this.initPrefillData();
        this.buildForm();
    }

    initPrefillData(): void {
        if (this.preFillData && this.examination) {
            // for (let study of this.studies) {
            //     if (this.preFillData.studyId == study.id) {
            //         this.studies = [];
            //         this.studies.push(study);
            //     }
            // }
            this.examination.studyId = this.preFillData.studyId;
            this.examination.studyName = this.preFillData.studyName;

            // for (let center of this.centers) {
            //     if (this.preFillData.centerId == center.id) {
            //         this.centers = [];
            //         this.centers.push(center);
            //     }
            // }
            this.examination.centerId = this.preFillData.centerId;
            this.examination.centerName = this.preFillData.centerName;
            this.examination.subjectId = this.preFillData.subjectId;
            this.examination.subjectName = this.preFillData.subjectName;
            this.examination.examinationDate = new Date(this.preFillData.examinationDate);
            this.examination.comment = this.preFillData.comment;
        }
    } 

    ngOnChanges(changes: SimpleChanges) {
        if (changes['preFillData']) this.initPrefillData();
    }

    getStudies(): void {
        this.studyService
            .getStudiesNames()
            .then(studies => {
                this.studies = studies;
            })
            .catch((error) => {
                console.error(error);
            });
    }

    getCenters(): void {
        this.centerService
            .getCentersNamesForExamination()
            .then(centers => {
                this.centers = centers;
            })
            .catch((error) => {
                console.error(error);
            });
    }

    create(): void {
        // this.examination = this.examinationForm.value;
        this.examinationService.create(this.examination)
            .subscribe((examination) => {
                this.msgService.log('info', 'Examination successfully created');
                this.back(examination);
            }, (err: any) => {
                console.error(err);
        });
    }

    back(examination?: Examination): void {
        if (this.closing.observers.length > 0) {
            this.closing.emit(examination);
            this.examination = new Examination();
        } else {
            this.location.back();
        }
    }

    buildForm(): void {
        this.examinationForm = this.fb.group({
            'examinationDate': [this.examination.examinationDate, Validators.required],
            // 'studyId': [this.examination.studyId, Validators.required],
            // 'centerId': [this.examination.centerId, Validators.required],
            // 'subjectId': [this.examination.subjectId, Validators.required]
        });

        this.examinationForm.valueChanges.subscribe(data => this.onValueChanged(data));
        this.onValueChanged(); // (re)set validation messages now
    }

    onValueChanged(data?: any) {
        if (!this.examinationForm) { return; }
        for (const field in this.formErrors) {
            // clear previous error message (if any)
            this.formErrors[field] = '';
            const control = this.examinationForm.get(field);
            if (control && control.dirty && !control.valid) {
                for (const key in control.errors) {
                    this.formErrors[field] += key;
                }
            }
        }
    }

    formErrors = {
        'examinationDate': '',
        // 'studyId': '',
        // 'centerId': '',
        // 'subjectId': ''
    }
}