import { Component, OnInit, Input, Output, EventEmitter, ViewChild, ElementRef } from '@angular/core';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';
import { Router, ActivatedRoute, Params } from '@angular/router';
import { Location } from '@angular/common';

import { ExtraData }    from '../shared/extradata.model';
import { ExaminationExtraDataService } from '../shared/extradata.service';

import * as PreclinicalUtils from '../../../utils/preclinical.utils';
import { Mode } from "../../../shared/mode/mode.model";
import { Modes } from "../../../shared/mode/mode.enum";
import { ModesAware } from "../../../shared/mode/mode.decorator";

@Component({
    selector: 'extra-data-upload-form',
    templateUrl: 'extradata-form.component.html',
    providers: [ExaminationExtraDataService]
})
@ModesAware
export class ExtraDataFormComponent implements OnInit {

    @Input() extradata: ExtraData;
    @Input() examination_id: number;
    @Input() isStandalone: boolean = false;
    @Output() closing = new EventEmitter();
    @Input() mode: Mode = new Mode();
    @Input() canModify: Boolean = false;
    newExtradataForm: FormGroup;
    @Output() extradataReady = new EventEmitter();

    constructor(
        private extradataService: ExaminationExtraDataService,
        private fb: FormBuilder,
        private route: ActivatedRoute,
        private location: Location) {

    }


    ngOnInit(): void {
        if (!this.extradata) this.extradata = new ExtraData();
        this.buildForm();
    }

    buildForm(): void {
        this.newExtradataForm = this.fb.group({

        });

        this.newExtradataForm.valueChanges
            .subscribe(data => this.onValueChanged(data));
        this.onValueChanged();
    }

    onValueChanged(data?: any) {
        if (!this.newExtradataForm) { return; }
        const form = this.newExtradataForm;
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

    };

    getOut(extradata: ExtraData = null): void {
        if (this.closing.observers.length > 0) {
            this.closing.emit(extradata);
            this.location.back();
        } else {
            this.location.back();
        }
    }

    createExtraData() {
        //if (!this.extradata && !this.extradata.fileUploadReady && !this.examination_id) { return; }
        if (!this.extradata && !this.examination_id) { return; }
        this.extradata.examination_id = this.examination_id;
        this.extradataService.create(PreclinicalUtils.PRECLINICAL_EXTRA_DATA, this.extradata)
            .subscribe(extradata => {
                //Then upload the file
                //extradata.fileUploadReady = this.extradata.fileUploadReady;
                //let uploadUrl: string = PreclinicalUtils.PRECLINICAL_API_EXAMINATION_URL + "/" + PreclinicalUtils.PRECLINICAL_EXTRA_DATA + PreclinicalUtils.PRECLINICAL_UPLOAD_URL + "/";
                //extradata.fileUploadReady.launchRequest(uploadUrl.concat(extradata.id)).subscribe();
                this.getOut(extradata);
            });
    }

    onFileSelected(event) {
        //this.extradata.fileUploadReady = event;
        this.extradata.filename = event.filename;
        if (!this.isStandalone) this.extradataReady.emit(this.extradata);
        this.extradata = new ExtraData();
    }
    
    downloadExtraData = (extradata:ExtraData) => {
        window.open(this.extradataService.getDownloadUrl(extradata));
    }

}
