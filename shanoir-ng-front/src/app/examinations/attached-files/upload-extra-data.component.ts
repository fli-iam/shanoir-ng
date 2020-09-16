/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

import { Location } from '@angular/common';
import { Component, EventEmitter, Input, OnInit, Output, OnChanges, SimpleChanges } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { KeycloakService } from '../../shared/keycloak/keycloak.service';
import { IdName } from '../../shared/models/id-name.model';
import { MsgBoxService } from '../../shared/msg-box/msg-box.service';
import { Examination } from '../shared/examination.model';
import { ExaminationService } from '../shared/examination.service';
import { Option } from '../../shared/select/select.component';

@Component({
    selector: 'upload-extra-data',
    templateUrl: 'upload-extra-data.component.html'
})

export class UploadExtraDataComponent implements OnInit, OnChanges {

    public uploadExtraDataForm: FormGroup;
    public mode: "view" | "edit" | "create";
    fileToUpload: File = null;
    @Input() examination: Examination;
    @Input() studies:  IdName[];
    public studyOptions: Option<number>[];
    @Output() closing: EventEmitter<any> = new EventEmitter();
    public canModify: Boolean = false;
    examinationStudyId = null;

    constructor(
            private fb: FormBuilder, 
            private location: Location,
            private keycloakService: KeycloakService,
            private examinationService: ExaminationService,
            private msgService: MsgBoxService) {

    }

    ngOnInit(): void {
        this.buildForm();
        if (this.keycloakService.isUserAdminOrExpert) {
            this.canModify = true;
        }
    }

    ngOnChanges(changes: SimpleChanges) {
        if (changes.studies) {
            this.studyOptions = [];
            if (this.studies) {
                this.studies.forEach(study => {
                    let option: Option<number> = new Option<number>(study.id, study.name);
                    this.studyOptions.push(option);
                })
            }
        }
    }

    buildForm(): void {
        this.uploadExtraDataForm = this.fb.group({
            add: 'add',
            studyId: 'studyId',
            examination: 'examination'
        });

    }

    handleFileInput(files: FileList) {
        this.fileToUpload = files.item(0);
    }

    uploadFileToActivity() {
        this.examinationService.postFile(this.fileToUpload, this.examination.id).subscribe(data => {
            this.msgService.log('info', 'The file has been sucessfully uploaded');
        });
      }

    formErrors = {
        'add': ''
    };

   back(id?: number): void {
        if (this.closing.observers.length > 0) {
           this.closing.emit(id);
        } else {
            this.location.back();
        }
    }

    create(): void {
        
    }

    add(): void {
      
    }

    edit(): void {
        
    }

    update(): void {

    }

}