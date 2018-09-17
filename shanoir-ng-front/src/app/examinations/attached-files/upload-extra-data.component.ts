import { Location } from '@angular/common';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { KeycloakService } from '../../shared/keycloak/keycloak.service';
import { IdNameObject } from '../../shared/models/id-name-object.model';
import { Examination } from '../shared/examination.model';
import { ExaminationService } from '../shared/examination.service';
import { MsgBoxComponent } from '../../shared/msg-box/msg-box.component';
import { MsgBoxService } from '../../shared/msg-box/msg-box.service';


@Component({
    selector: 'upload-extra-data',
    templateUrl: 'upload-extra-data.component.html'
})

export class UploadExtraDataComponent implements OnInit {

    public uploadExtraDataForm: FormGroup;
    public mode: "view" | "edit" | "create";
    fileToUpload: File = null;
    @Input() examination: Examination;
    @Input() studies:  IdNameObject[];
    @Output() closing: EventEmitter<any> = new EventEmitter();
    public canModify: Boolean = false;

    constructor(
            private fb: FormBuilder, 
            private location: Location,
            private keycloakService: KeycloakService,
            private examinationService: ExaminationService,
            private msgService: MsgBoxService) {

    }

    ngOnInit(): void {
        this.buildForm();
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.canModify = true;
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
        this.examinationService.postFile(this.fileToUpload).subscribe(data => {
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



    add(): void {
      
    }


}