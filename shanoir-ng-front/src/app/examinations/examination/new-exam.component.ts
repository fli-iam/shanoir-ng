import { Component, OnInit, Input, Output, EventEmitter, SimpleChanges } from "@angular/core";
import { Location } from '@angular/common';
import { FormGroup, FormBuilder, Validators } from "@angular/forms";
import { Examination } from "../shared/examination.model";
import { ExaminationService } from "../shared/examination.service";
import { MsgBoxService } from "../../shared/msg-box/msg-box.service";

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

    constructor (private fb: FormBuilder, private examinationService: ExaminationService,
        private location: Location, private msgService: MsgBoxService) {
    }

    ngOnInit(): void {
        this.examination = new Examination();
        this.initPrefillData();
        this.buildForm();
    }

    initPrefillData(): void {
        if (this.preFillData && this.examination) {
            this.examination.studyId = this.preFillData.studyId;
            this.examination.centerId = this.preFillData.centerId;
            this.examination.subjectId = this.preFillData.subjectId;
            this.examination.examinationDate = this.preFillData.examinationDate;
            this.examination.comment = this.preFillData.comment;
        }
    } 

    ngOnChanges(changes: SimpleChanges) {
        if (changes['preFillData']) this.initPrefillData();
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
            'examinationDate': [this.examination.examinationDate, Validators.required]
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
        'examinationDate': ''
    }
}